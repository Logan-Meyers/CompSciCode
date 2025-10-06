        .data
msg:    .asciiz "Hello, CPTS_260!\n"
var1:  .word 1
var2:  .word 2

        .text
        .globl main
main:
        lw $t0, var1
        lw $t1, var2
        add $t3, $t0, $t1

        li $v0, 4
        la $a0, msg
        syscall

        li $v0, 10
        syscall