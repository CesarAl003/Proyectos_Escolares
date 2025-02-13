.MODEL SMALL
.CODE
Inicio:
mov Ax, @Data
mov Ds, Ax 
mov Dl, 0
mov num, dx
add num, 30h
mov Dl, 0
mov m, dx
add m, 30h
mov Dl, 0
mov cont, dx
add cont, 30h
mov Dl, 0
mov res, dx
add res, 30h
mov Dl, offset 2
mov dh, 36
mov repetir, Dx
add repetir, 30h
push Cx
xor Cx, Cx
salto1:
mov Si, offset repetir
mov Cl, byte ptr [si]
mov Ch, 1
add Ch, 30h
cmp Cl, Ch
jna salto2
pop Cx
mov Ah, 9h
mov Dx, Offset S
int 21h
mov Dx, Offset dato0
int 21h
mov Ah, 0Ah 
mov Dx, Offset num
int 21h
xor Bx, Bx
mov Bx, num[2]
mov Bh, 24h
mov num[0], Bx
push Cx
xor Cx, Cx
mov Si, offset num
mov Cl, byte ptr [si]
mov Ch, 1
add Ch, 30h
cmp Cl, Ch
jna salto3
pop Cx
push Ax
push Bx
push Cx
push Dx
xor Bx, Bx
mov Bx, num
sub Bx, 30h
push Bx
push 2
xor Ax, Ax
pop Dx
mov Bl, Dl
pop Dx
mov Al, Dl
div Bl
mov dato1, Ah
mov Al, Ah
xor Ah, Ah
push Ax
xor Dx, Dx
mov Dl, dato1
mov Dh, 36
mov Dh, 36
mov m, dx
add m, 30h
pop Ax
pop Bx
pop Cx
pop Dx
push Cx
xor Cx, Cx
mov Si, offset m
mov Cl, byte ptr [si]
mov Ch, 0
add Ch, 30h
cmp Cl, Ch
jne salto4
pop Cx
push Cx
push Dx
mov cont, offset 1
add cont, 30h
xor Cx, Cx
mov Cl,5
sub cont, 30h
sub Cx, cont
add Cl, 1
add cont, 30h
xor Ch, Ch
Salto5: 
pop Dx
push Ax
push Bx
push Cx
push Dx
xor Bx, Bx
mov Bx, cont
sub Bx, 30h
push Bx
xor Bx, Bx
mov Bx, num
sub Bx, 30h
push Bx
xor Ax, Ax
pop Dx
mov Ah, Dl
pop Dx
mov AL, Dl
mul Ah
mov dato2, Al
xor Ah, Ah
push Ax
xor Dx, Dx
mov Dl, dato2
mov Dh, 36
mov Dh, 36
mov res, dx
add res, 30h
pop Ax
pop Bx
pop Cx
pop Dx
mov Ah, 9h
mov Dx, Offset S
int 21h
mov Dx, Offset num
int 21h
mov Dx, Offset dato3
int 21h
mov Dx, Offset cont
int 21h
mov Dx, Offset dato4
int 21h
mov Dx, Offset res
int 21h
inc cont
loop Salto5
pop Cx
salto4:
push Cx
xor Cx, Cx
mov Si, offset m
mov Cl, byte ptr [si]
mov Ch, 1
add Ch, 30h
cmp Cl, Ch
jne salto6
pop Cx
push Cx
push Dx
mov cont, offset 1
add cont, 30h
xor Cx, Cx
mov Cl,5
sub cont, 30h
sub Cx, cont
add Cl, 1
add cont, 30h
xor Ch, Ch
Salto7: 
pop Dx
push Ax
push Bx
push Cx
push Dx
xor Bx, Bx
mov Bx, cont
sub Bx, 30h
push Bx
xor Bx, Bx
mov Bx, num
sub Bx, 30h
push Bx
xor Cx, Cx
pop Dx
mov Ch, Dl
pop Dx
mov Cl, Dl
mov dato5, Cl
add dato5, Ch
mov dl, dato5
xor Dh, Dh
push dx
xor Dx, Dx
mov Dl, dato5
mov Dh, 36
mov Dh, 36
mov res, dx
add res, 30h
pop Ax
pop Bx
pop Cx
pop Dx
mov Ah, 9h
mov Dx, Offset S
int 21h
mov Dx, Offset num
int 21h
mov Dx, Offset dato6
int 21h
mov Dx, Offset cont
int 21h
mov Dx, Offset dato7
int 21h
mov Dx, Offset res
int 21h
inc cont
loop Salto7
pop Cx
salto6:
salto3:
mov Ah, 9h
mov Dx, Offset S
int 21h
mov Dx, Offset dato8
int 21h
mov Ah, 0Ah 
mov Dx, Offset repetir
int 21h
xor Bx, Bx
mov Bx, repetir[2]
mov Bh, 24h
mov repetir[0], Bx
jmp salto1
salto2:
mov Ah, 4Ch
int 21h
.DATA
S db 10,13,24h
num dw 255 dup ('$')
m dw 255 dup ('$')
cont dw 255 dup ('$')
res dw 255 dup ('$')
repetir dw 255 dup ('$')
dato0 dw "Ingresa un numero:  ", 36
dato1 db 255 dup ('$')
dato2 db 255 dup ('$')
dato3 db " * ", 36
dato4 db " = ", 36
dato5 db 255 dup ('$')
dato6 db "+", 36
dato7 db " = ", 36
dato8 dw "Ingrese 0 o 1 para terminar ", 36
.STACK
END Inicio
