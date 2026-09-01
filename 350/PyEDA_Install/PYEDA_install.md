# PYEDA Install Instructions

> Credit to **Aman Verma** for macOS instructions

PyEDA can also be installed directly on macOS without using VirtualBox or Ubuntu.

This method uses Homebrew, so make sure Homebrew is installed first.

Open Terminal and run:

```
brew update
brew install pyenv

pyenv init --install
exec "$SHELL"
```

Then install Python 3.8.20:

```pyenv install 3.8.20```

Create the course directory and set it to use Python 3.8.20:

```
mkdir -p cpts350
cd cpts350

pyenv local 3.8.20
```

Check that the correct Python version is being used:

```python3 -V```

It should display:

```Python 3.8.20```

You can also verify that Python is being managed by pyenv:

```which python3```

It should show a path containing:

```.pyenv/shims/python3```

Then install PyEDA:

```CFLAGS="-Wno-error=incompatible-function-pointer-types" python3 -m pip install pyeda```

The CFLAGS option helps prevent a compiler error that may occur on newer versions of macOS when PyEDA builds its C extensions.

To check that PyEDA installed correctly, run:

```python3 -c "from pyeda.inter import *; print('PyEDA works!')"```

You should see:

PyEDA works!

On Mac, you can create your Python file using:

```nano mycode.py```

or use VS Code.

Then run the code using:

```python3 mycode.py```
