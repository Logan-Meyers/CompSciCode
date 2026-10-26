#!/bin/sh
# Figures out which executable to debug and writes an LLDB command file
# (.vscode/.debug-target.lldb) that launch.json uses to create the target.
#
# Usage: debug-target.sh [program] [make-target]
#   program      optional path (relative to the workspace) to force a specific
#                executable. If empty, the most recently modified Mach-O/ELF
#                executable in the workspace is used.
#   make-target  optional Makefile target (e.g. test01) whose command runs the
#                program. Its executable and arguments are used instead, so
#                debugging matches what `make test01` would run.

cd "$(dirname "$0")/.." || exit 1
out=".vscode/.debug-target.lldb"

is_executable() {
    [ -f "$1" ] && [ -x "$1" ] && file -b "$1" | grep -qE '^(Mach-O|ELF).*executable'
}

# Wraps an argument in double quotes for an LLDB command line
lldb_quote() {
    printf '"%s"' "$(printf '%s' "$1" | sed 's/[\\"]/\\&/g')"
}

prog="$1"
target="$2"
run_args=""

if [ -n "$target" ]; then
    # Find the last line of the target's recipe that starts with a compiled program
    cmd_line=""
    while IFS= read -r line; do
        eval "set -- $line" 2>/dev/null || continue
        if [ $# -gt 0 ] && is_executable "$1"; then
            cmd_line="$line"
        fi
    done <<EOF
$(make -n "$target" 2>/dev/null)
EOF

    if [ -z "$cmd_line" ]; then
        echo "debug-target: 'make -n $target' doesn't run a compiled program." >&2
        exit 1
    fi

    eval "set -- $cmd_line"
    prog="$1"
    shift
    for arg in "$@"; do
        run_args="$run_args $(lldb_quote "$arg")"
    done
fi

if [ -z "$prog" ]; then
    # Newest compiled executable, skipping .git, .vscode, and .dSYM bundles
    prog=$(find . -maxdepth 3 \
                \( -name .git -o -name .vscode -o -name '*.dSYM' -o -name node_modules \) -prune -o \
                -type f -perm -u+x -print0 2>/dev/null |
           xargs -0 file -N -F '|' 2>/dev/null |
           grep -E '\| *(Mach-O|ELF).*executable' |
           cut -d'|' -f1 |
           tr '\n' '\0' |
           xargs -0 ls -t 2>/dev/null |
           head -n 1)
fi

if [ -z "$prog" ] || [ ! -f "$prog" ]; then
    echo "debug-target: could not find an executable to debug." >&2
    echo "  Set \"project.program\" in .vscode/settings.json to its path." >&2
    exit 1
fi

prog="$(cd "$(dirname "$prog")" && pwd)/$(basename "$prog")"
printf 'target create %s\n' "$(lldb_quote "$prog")" > "$out"
if [ -n "$run_args" ]; then
    printf 'settings set target.run-args%s\n' "$run_args" >> "$out"
fi
echo "debug-target: debugging $prog$run_args"
