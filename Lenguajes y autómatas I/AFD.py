import tkinter as tk
from tkinter import ttk

'''---------Inicializamos los componentes---------'''
alf = [] #Alfabeto
num_s = 0 #Número de estados
abc = [] #A cada estado le asigno una letra del abecedario
fa = [] #Estados finales o de aceptación 
entries = [] #Creamos una lista vacía para los entries
valido = False #Para validar la cadena
afd = {} #Mi AFD
vtn_ent = None
check_fa = []
#Definimos mis fuentes
font1 = ("Courier New", 12, "bold")
font2 = ("Courier New", 12)

#Para movernos al estado siguiente, a partir de un estado actual con un símbolo dado
def mover (estadoAct, simbolo):
    s = -1 #Hacia que estado me logré mover?
    print("ea: ", estadoAct, simbolo)
    for i in range(len(alf)): # Para i que va de 0 hasta la longitud de alfabeto
        if alf[i] == simbolo:
            #Ingresar al diccionario en el estado actual y en mi lista
            s = afd.get(estadoAct)[i]
    return s

#Método principal para validar la cadena de entrada
def validar ():
    global vtn_t
    global valido
    valido = False
    global lbl_msj
    global entry_cad
    global fa
    global check_fa

    s = 0 #Comenzamos en el estado 

    cad = entry_cad.get()
    print("cad: ", cad)
    for caracter in cad: #Para cada caracter de la cadena de entrada
        s = mover(s, caracter) #s es el estado actual 
        print(s)
        if s == -1:
            break
    
    for EstFin in fa:
        if s == EstFin:
            print("Estado final o de aceptación")
            valido = True

    if valido:    
        mensaje = "Cadena muy válida"
    else:
        mensaje = "Cadena no válida"

    print(mensaje)
    #Aquí quiero ctualizar la etiqueta lbl_msj con el mensaje
    lbl_msj.config(text=mensaje)

def AFD ():
    global lbl_msj
    global vtn_t
    global entry_cad

    vtn_t = tk.Tk() #Nueva ventana para mi tabla
    vtn_t.title("Mi tabla")
    vtn_t.geometry(f"{(len(alf) * (70 + 3) + 160)}x{( num_s * (22) + 40)}")
    vtn_t.configure(bg = "#FC215F")
    #vtn_t.resizable(False,False)

    #Creamos una tabla para mostrar los datos ingresados
    tabla = ttk.Treeview(vtn_t, columns=["Estado"] + alf, show="headings")
    #Esta es la columna S
    tabla.column("Estado", width=25)
    tabla.heading("Estado", text="S", anchor=tk.CENTER)
    tabla.column("Estado", anchor=tk.CENTER)

    #Creamos las columnas cuyo encabezado es mi alfabeto
    for letra in alf:
        tabla.column(letra, width=25)
        tabla.heading(letra, text=letra, anchor=tk.CENTER)
        tabla.column(letra, anchor=tk.CENTER)
        
    #Insertar las filas correspondientes
    for i in range(len(afd)):
        lista = afd[i]
        nueva_fila = []
        for j in range(len(lista)):
            nodo = lista[j]
            if nodo == -1:
                nueva_fila.append("")
            else: 
                if nodo < len(abc):
                    nueva_fila.append(abc[nodo])
                else:
                    nueva_fila.append("")
        fila = [abc[i]] + nueva_fila
        tabla.insert("", "end", values=fila)
        
    tabla.place(x = 10, y = 10, height=(num_s*20 + 26))
    vtn_t.update()
    

    coordenada_x = tabla.winfo_width()

    lbl_cad = tk.Label(vtn_t, text = "Cadena a validar: ", fg = "black", bg = "#FC215F", font = font1)
    lbl_cad.place(x = (coordenada_x + 33), y = 15)
    entry_cad = tk.Entry(vtn_t, width=15, font=font2)
    entry_cad.place( x = (coordenada_x + 33), y = 55)

    lbl_msj = tk.Label(vtn_t, text = "", fg = "black", bg = "#FC215F", font = font1)
    lbl_msj.place(x = (coordenada_x + 33), y = 145)

    btn_validar = tk.Button(vtn_t, text="Validar cadena", command=validar) #command es la función del boton
    btn_validar.place(x = (coordenada_x + 33), y = 105)

#Mostrar la matriz de transiciones del AFD
def mostrar_t ():
    global vtn_ent
    global txt_fa
    #Dimensionar la ventana segun el numero de simbolos y estados
    vtn_ent = tk.Tk() #Nueva ventana para mi tabla
    vtn_ent.title("Mi tabla")
    vtn_ent.geometry(f"{len(alf) * (50 + 3) + 82}x{num_s * (28 + 3) + 70 + 80}")
    vtn_ent.configure(bg = "#FC215F")
    #vtn_ent.resizable(False,False)

    #Este frame contendrá a todos los entries
    frm_entries = tk.Frame(vtn_ent, bg="#FC215F")
    frm_entries.place(x = 50, y = 30)

    #Colocamos los simbolos del alfabeto
    for i in range(len(alf)):
        lbl_alf = tk.Label(vtn_ent, text = alf[i], fg = "black", bg = "#FC215F", font = font1)
        lbl_alf.place(x = (65 + 52*i), y = 5)

    for i in range(num_s):
        #Coocamos los estados a la izquierda jsjs
        lbl_alf = tk.Label(vtn_ent, text = abc[i], fg = "black", bg = "#FC215F", font = font1)
        lbl_alf.place(x = 25, y = 31 + 29*i)

    #Colocamos los entries
    for i in range(len(abc)):
        row_entries = []
        for j in range(len(alf)): #Cada uno de los elementos de la lista
            entry = tk.Entry(frm_entries, width=4, justify='center', font=font2)
            entry.grid(row=i, column=j, padx=3, pady=3)  # Colocar el Entry en la cuadrícula de la ventana
            #entry.insert(0, transiciones[i][j])
            row_entries.append(entry)
        entries.append(row_entries)
    
    lbl_s = tk.Label(vtn_ent, text = "Estados de aceptación: ", fg = "black", bg = "#FC215F", font = font1)
    lbl_s.place(x = 10, y = (num_s*30 + 35))
    txt_fa = tk.Entry(vtn_ent, width = 20, font = font2)
    txt_fa.place(x = 10, y = (num_s*30 + 75))

    btn_save = tk.Button(vtn_ent, text="Guardar", command=guardar) #command es la función del boton
    btn_save.place(x =(len(alf) * 20 + 30), y = (num_s*30 + 115))


def guardar ():
    global check_fa
    global fa
    global afd
    fa = []
    #contruir mi diccionario de forma dinámica y guardar los datos de los entries
    for i in range(num_s): #Cada clave del diccionario corresponde a un estado.
        for j in range(len(alf)): #Cada símbolo del alfabeto corresponde a un elemento en la lista
            #Extraemos el dato del entry (Recordemos que tenemos una lista de listas de puros enties)
            letra = entries[i][j].get().upper() #Admite minúsculas
            for k in range(len(abc)):
                if letra == abc[k]: #Revisa si la letra ingresada corresponde con un estado
                    afd[i][j] = k #Mi diccionario guarda solo números 

    '''fa_letras = txt_fa.get().split()
    
    for i in range(len(fa_letras)):
        letra = fa_letras[i].upper()
        for j in range(len(abc)):
            if letra == abc[j]:
                fa.append(j)
    print(fa)'''
    fa = []
    for i, var in enumerate(check_fa):
        print(var.get())
        if var.get():
            fa.append(i)
    #print(transiciones)

    print(fa)
    AFD()

def aceptar(): #Guardar los datos de la ventana principal
    #Especifica que queremos usar la variable global, y no crear una nueva variable local
    global alf
    global num_s
    global abc
    abc = []
    alfabeto = txt_alf.get()
    alf = alfabeto.split() #Separamos cada símbolo de mi alfabeto en una lista llamada alf
    num_s = int(txt_s.get()) #No. de estados
    print(alf)
    #Para nombrar cada estado con una letra
    a_unicode = ord('A') #Valor unicode de A
    for i in range(num_s):
        letra = chr(a_unicode + i) #Convertir el valor unicode de cada eltra en un String
        abc.append(letra) #Agregar la letra a la lista abc
    
    #Inicializar mi diccionario con valores nulos (-1)
    for i in range(num_s):
        afd[i] = []
        for j in range(len(alf)):
            afd[i].append(-1)
    print(afd)
    mostrar_t() #Lanzar la tabla


'''----------Crear una ventana principal----------'''
vtn_main = tk.Tk()
vtn_main.title("Autómata Finito Determinista (AFD)")
vtn_main.geometry("500x240") #tamaño
vtn_main.iconbitmap("C:/Users/Ayums/Pictures/Screenshots/logo.png") #icono de la ventana
vtn_main.configure(bg="#FC215F") #Color
vtn_main.resizable(False, False) #Ni ancho ni alto

# Crear etiquetas
lbl_alf = tk.Label(vtn_main, text = "Alfabeto: ", fg = "black", bg = "#FC215F", font = font1)
lbl_alf.place(x = 163, y = 60)  # Colocar la etiqueta en la ventana
lbl_s = tk.Label(vtn_main, text = "Número de estados: ", fg = "black", bg = "#FC215F", font = font1)
lbl_s.place(x = 65, y = 100)

#Donde va a ingresar los datos
txt_alf = tk.Entry(vtn_main, width = 20, font = font2)
txt_alf.place(x = 270, y = 60)
txt_s = tk.Entry(vtn_main, width = 20, font = font2)
txt_s.place(x = 270, y = 100)

#Creamos un boton para guardar los datos
btn_aceptar = tk.Button(vtn_main, text="Aceptar", command = aceptar) #command es la función del boton
btn_aceptar.place(x = 230, y = 180)

# Iniciar el bucle principal de la aplicación
vtn_main.mainloop()

'''
#AFD 1 (mi pripia solución)
transiciones = {
    0: [-1, 1],
    1: [2, -1],
    2: [3, 4],
    3: [5, 6],
    4: [5, 6],
    5: [7, 8],
    6: [7, 8],
    7: [5, 6],
    8: [5, 6],
}

AFD2 (Solución de Moises)
transiciones = {
    0: [-1, 1],
    1: [2, -1],
    2: [3, 4],
    3: [5, 6],
    4: [7, 8],
    5: [3, 4],
    6: [3, 4],
    7: [3, 4],
    8: [3, 4],
}

AFD3 (Primerísimo ejercicio)
transiciones = {
    0: [1, 2, -1],
    1: [3, 4, 5],
    2: [3, 4, 5],
    3: [6, -1, -1],
    4: [-1, 7, -1],
    5: [-1, -1, -1],
    6: [3, 4, 5],
    7: [3, 4, 5],
}
'''

