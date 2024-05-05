import java.io.BufferedReader;
import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compilador {
    public static class simbolo {
        public String tipo;
        public String nombre;
        public String valor;

        public simbolo () {
            this.tipo = "";
            this.nombre = "";
            this.valor = "";
        }

        public simbolo (String tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre;
        }
        
    }

    public static class error {
        public String codigo;
        public int linea;
        public String token;

        public error (String codigo, int linea, String token) {
            this.codigo = codigo;
            this.linea = linea;
            this.token = token;
        }
    }

    public static class etiqueta {
        public String nombre;
        public int linea;

        public etiqueta (String nombre, int linea) {
            this.nombre = nombre;
            this.linea = linea;
        }
    }

    static List<simbolo> tabSim = new ArrayList<>();
    
    public static simbolo buscarSmb (String nombre) { // Utilizar los metodos de arraylist en lugar de este metodo
        for (simbolo smb : tabSim) { //Comprobar si el símbolo esta guardado
            if (smb.nombre.equals(nombre)) return smb;
        }
        return null;
    }

    static String linea, nuevaLinea = "", cte_ent = "(\\d)+";
    static String cte_dec = cte_ent + "\\." + cte_ent;
    static String cte_cad = "(\" [^\\\"]* \")|(\" \")";
    static String id = "^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$";
    static String op = "\\*|/|\\+|-|mod|div|(&&)|(\\|\\|)|>=|<=|==|<>|<|>";
    static List<error> tabErr = new ArrayList<>();
    static boolean haycomillas = false;
    static List<Integer> indices = new ArrayList<>(); //Para guardar las lineas que si contienen información
    static Stack<etiqueta> pilaSem = new Stack<>();
    
    public static void comprobarTipos (String operacion, int contLin, String tipo) {
        String regex = ""; // Revisa declaraciones inicializadas con un valor y asignaciones
        String[] operandos = operacion.split(op); // Separa cada uno de los operandos
        switch (tipo) { // Determinamos el tipo para poder hacer la comprobación
            case "ent": regex = cte_ent; break;
            case "dec": regex = cte_dec; break;
            case "cad": regex = cte_cad; break;
        }

        for (String operando : operandos) { // Verificar cada operando
            operando = operando.trim();
            if (!Pattern.matches(regex, operando)) { // Evalua que las constantes sean del mismo tipo
                if (Pattern.matches(id, operando)) {  // Si es un id, lo busca en la tabla de simbolos
                    simbolo simb = buscarSmb(operando);
                    if (simb != null) // si lo encuentra, evalúa que sea del tipo de dato correcto
                    if (!simb.tipo.equals(tipo)) 
                        tabErr.add(new error("sem04", contLin, operando));
                }
                else tabErr.add(new error("sem04", contLin, operando)); // No coincide ni con una cte ni con un id

            }
        }
    }

    public static void comprobarTipos (String operacion, int contLin) { // Revisa comparaciones condicionales
        String[] operandos = operacion.split(op); // Se compara cada par de operandos
        String tipo = "";
        for (String operando : operandos) {
            if (tipo.equals("")) { 
                operando = operando.trim(); // Determinamos el tipo para poder hacer la comprobación
                if (Pattern.matches(cte_ent, operando)) tipo = "ent";
                else if (Pattern.matches(cte_dec, operando)) tipo = "dec";
                else if (Pattern.matches(cte_cad, operando)) tipo = "cad";
                else {
                    if (Pattern.matches(id, operando)) {  // Si es un id, lo busca en la tabla de simbolos
                        simbolo simb = buscarSmb(operando);
                        if (simb != null) tipo = simb.tipo;
                    }
                }
            }
            else {
               comprobarTipos(operacion, contLin, tipo);
               tipo = "";
            }
        }
    }

    public static String AnalisisLexico (String ruta) {
        List<String> palReserv = Arrays.asList("ent", "dec", "cad", "inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        LinkedList<String> constCad = new LinkedList<>();
        
        simbolo smb = new simbolo();
        String miAlf = "[*]|[/]|[+]|[-]|[=]|(<>)|(==)|(<=)|(>=)|[<|>]|[(]|[)]|[;]|[:]|(^[&][&]|[\\|][\\|]$)|(^[\\d]+$|^[\\d]+\\.[\\d]+$)";
        boolean declaracion = false;
        int contLin = 0;
        Pattern patCad = Pattern.compile(cte_cad);

        
        try { // Abre el archivo para lectura
            BufferedReader lector = new BufferedReader(new FileReader(ruta)); // Crea un BufferedReader para leer líneas
            // Para generar el archivo de salida
            ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt";
            File salida = new File (ruta);
            BufferedWriter escritor = new BufferedWriter(new FileWriter(salida));

            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea

                // Si la linea esta vacia, el string palabras es un array de cero posiciones (vacio), sino, es un array de palabras separadas por uno o más espacios en blanco
                String[] palabras = linea.trim().isEmpty() ? new String[0] : linea.trim().split("\\s+");
                
                for (String palabra : palabras) { // System.out.println(palabra);
                    if (palabra.equals("\"")) haycomillas = !haycomillas;
                    else if (!haycomillas) { // Evitar que considere lo que está entre comillas
                        if (Pattern.matches("ent|dec|cad", palabra)) { // Es un tipo de dato
                            if (declaracion) smb.tipo = palabra; // Nos encontramos en el bloque de declaración?
                            else tabErr.add(new error("syn03", contLin, palabra));
                        }
                        else if (palReserv.contains(palabra)) { // Si es una palabra reservada
                            if (palabra.equals("varinicio:")) declaracion = true; // Bloque de declaraciones
                            if (palabra.equals("varfin;")) declaracion = false;
                        }
                        else if (Pattern.matches(id, palabra)) { //Es un id
                            if (buscarSmb(palabra) == null) {
                                if (declaracion) { // Aun estamos en el bloque de declaración?
                                    if (!smb.tipo.equals("")) { // El ID tiene un tipo?
                                        smb.nombre = palabra; // Si tiene un valor asignado, marcamos que no es nulo
                                        tabSim.add(smb); // Una vez completo, guardamos el simbolo y lo limpiamos
                                        smb = new simbolo();
                                    }
                                    else tabErr.add(new error("lex00", contLin, palabra)); // Falta el tipo de dato
                                }
                                else tabErr.add(new error("lex00", contLin, palabra)); // Simb no encontrado
                            } // Nos encontramos un ID que exisite y estamos en el bloque de declaración
                            else if (declaracion) {
                                if (!smb.tipo.equals("")) {
                                    tabErr.add(new error("sem03", contLin, palabra)); // El simb no se está utilizando en una asignación, por lo tanto esta repetido
                                    smb = new simbolo();
                                }
                            }
                        }
                        else if (!Pattern.matches(miAlf, palabra)) { // No coincidió con ningún patron
                            tabErr.add(new error("lex00", contLin, palabra));
                        }
                    }
                }
                if (haycomillas) { // Significa que no se cerraron las comillas en la misma linea
                    tabErr.add(new error("syn06", contLin, ""));
                    haycomillas = false;
                }

                if (!linea.trim().isEmpty()) { // Si la linea no esta vacia
                    indices.add(contLin); // Guarda los indices de las lineas que sí tienen info

                    // Guardar las constantes de tipo cad
                    Matcher matCad = patCad.matcher(linea);
                    while (matCad.find()) constCad.offer(matCad.group(0));
                    
                    if (!constCad.isEmpty()) {
                        String[] laOtraParte = linea.split(cte_cad);
                        
                        for (String parte : laOtraParte) {
                            nuevaLinea += parte.replaceAll("\\s+", " ");
                            if (!constCad.isEmpty()) nuevaLinea += constCad.poll();
                        }
                        nuevaLinea = nuevaLinea.trim();                        
                    }
                    else nuevaLinea = linea.trim().replaceAll("\\s+", " ");
                    escritor.write(nuevaLinea + "\n"); // Elimina todos los espacios en blanco y escribe la linea
                    nuevaLinea = "" ;
                }
            }
            
            lector.close(); // Cierra el archivo    
            escritor.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        return ruta;
    }

    public static void analisisSintactico (String ruta) {
        int contLin = 0; 
        try { // syn
            BufferedReader lector = new BufferedReader(new FileReader(ruta));
            BufferedReader regex = new BufferedReader (new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\regex.txt"));
            String sentencias = regex.readLine(); // Mi gramatica
            regex.close();
            boolean hayInicio = false, hayVarInicio = false;
            
            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea
                if (linea.equals("inicio:")) {
                        if (hayInicio) tabErr.add(new error("syn00", indices.get(contLin - 1), linea));
                        else hayInicio = true;
                }
                else if (linea.equals("fin;")) hayInicio = false;
                else if (hayInicio) {
                    if (linea.equals("varinicio:")) {
                        if (contLin != 2) tabErr.add(new error("syn05", indices.get(contLin - 1), ""));
                        if (hayVarInicio) tabErr.add(new error("syn00", indices.get(contLin - 1), linea));
                        else hayVarInicio = true;
                    }
                    else if (!Pattern.matches(sentencias, linea)) tabErr.add(new error("syn02", indices.get(contLin - 1), linea));   
                }
                else {
                    tabErr.add(new error("syn01", indices.get(contLin - 1), ""));
                    break; // Ya no tiene caso seguir si no hay inicio:
                }
                
            }
            lector.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void analisisSemantico (String ruta) {
        try { // Sem
            int contLin = 0; 
            BufferedReader lector = new BufferedReader(new FileReader(ruta)); // Crea un BufferedReader para leer líneas
            etiqueta et = null;
            while ((linea = lector.readLine()) != null) {
                contLin++;
                String operacion = "";
                String[] palabras = linea.split("\\s");
                
                switch (palabras[0]) {
                    case "inicio:": 
                        pilaSem.push(new etiqueta("inicio:", indices.get(contLin - 1)));
                        break;
                    case "varinicio:": 
                        pilaSem.push(new etiqueta("varinicio:", indices.get(contLin - 1)));
                        break;
                    case "ent":
                        
                        if (palabras[2].equals("=")) {
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) operacion += palabras[i];
                            comprobarTipos(operacion, indices.get(contLin - 1), "ent");
                            //smb.valor = operacion; // Completar la tabla de simbolos
                        }
                        else {
                            simbolo smb = buscarSmb(palabras[1]);
                            if ( smb != null ) smb.valor = "0";
                        }
                        break;
                    case "dec":
                        //smb = buscarSmb(palabras[1]);
                        if (palabras.length > 3 && palabras[2].equals("=")) {
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) operacion += palabras[i];
                            comprobarTipos(operacion, indices.get(contLin - 1), "dec");
                            //smb.valor = operacion; // Completar la tabla de simbolos
                        }
                        //else smb.valor = "0.0";
                        break;
                    case "cad": 
                        //smb = buscarSmb(palabras[1]);
                        if (palabras.length > 3 && palabras[2].equals("=")) {
                            Matcher matOp_arit = Pattern.compile(op).matcher(linea);
                            while (matOp_arit.find()) { // Revisa todos los operadores aritmeticos de la linea
                                if (!matOp_arit.group(0).equals("+"))  // si no es un +, marca error
                                    tabErr.add(new error("sem05", indices.get(contLin - 1), ""));
                            }
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) operacion += palabras[i] + " ";
                            comprobarTipos(operacion, indices.get(contLin - 1), "cad");  
                            //smb.valor = operacion.trim(); // Completar la tabla de simbolos
                        }
                        //else smb.valor = "\"\"";
                        break;
                    case "varfin;" : 
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "varfin;"));
                        else {
                            et = pilaSem.pop();
                            if (!et.nombre.equals("varinicio:")) tabErr.add(new error("sem00", et.linea, "varinicio:"));
                        }
                        break;

                    case "imp":  break;
                    case "si": 
                        pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
                        String[] comparaciones = linea.substring(5, linea.length() - 4 ).split("(&&)|(\\|\\|)");
                        for (String comparacion : comparaciones) { //Enviamos cada comparación individual
                            comprobarTipos(comparacion.trim(), indices.get(contLin - 1));
                        }
                        break;
                    
                    case "sino:": // EL sino no afecta la pila.
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "sino:"));
                        else {
                            if (!pilaSem.peek().nombre.equals("si")) tabErr.add(new error("sem01", indices.get(contLin - 1), "sino:"));
                        }
                        break;

                    case "finsi;": 
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "finsi;"));
                            else {
                                et = pilaSem.pop();
                                if (!et.nombre.equals("si")) tabErr.add(new error("sem00", et.linea, et.nombre));
                        }
                        break;
                    
                    case "para": 
                        pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
                        operacion = "";
                        for (int i = 1; i < palabras.length; i += 2) {
                            
                            operacion += palabras[i] + " ";
                        }
                        comprobarTipos(operacion, contLin, "ent");
                        break;

                    case "finpara;": 
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "finpara;"));
                        else {
                            et = pilaSem.pop();
                            if (!et.nombre.equals("para")) tabErr.add(new error("sem00", et.linea,  et.nombre));
                        }
                        break;

                    case "mientras": 
                        pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
                        String[] comparacion = linea.substring(5, linea.length() - 4 ).split("(&&)|(\\|\\|)");
                        for (String compara : comparacion) { //Enviamos cada comparación individual
                            comprobarTipos(compara.trim(), indices.get(contLin - 1));
                        }
                        break;
                    case "finmientras;":
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "finmientras;"));
                        else {
                            et = pilaSem.pop();
                            if (!et.nombre.equals("mientras")) tabErr.add(new error("sem00", et.linea,  et.nombre));
                        }
                        break;

                    case "fin;":  
                        if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "varfin;"));
                        else {
                            et = pilaSem.pop();
                            if (!et.nombre.equals("inicio:")) tabErr.add(new error("sem00", et.linea,  et.nombre));
                        }
                        break;
                }

                if (palabras.length > 1) 
                switch (palabras[1]) {
                    case "=": {
                        if (palabras.length > 2) 
                        if (palabras[2].equals("esc")) {}//System.out.print("esc");
                        else { // Asign
                            String tipo = "";
                            for (int i = 2; i < palabras.length-1; i++) operacion += palabras[i] + " ";

                            simbolo simb = buscarSmb(palabras[0]);
                            if (simb != null) tipo = simb.tipo; // Si no es nulo, guarda el tipo
                                comprobarTipos(operacion, indices.get(contLin - 1), tipo);

                            if (tipo.equals("cad")) {
                                Matcher matOp_arit = Pattern.compile(op).matcher(linea);
                                while (matOp_arit.find()) { // Revisa todos los operadores aritmeticos de la linea
                                    if (!matOp_arit.group(0).equals("+"))  // si no es un +, marca error
                                        tabErr.add(new error("sem05", indices.get(contLin - 1), ""));
                                }
                            }
                        }
                    }
                }
                if (linea.equals("fin;")) break;
                
            }
            // Las etiquetas restantes en la pila son errores
            for (etiqueta e: pilaSem) {
                tabErr.add(new error("sem00", e.linea,  e.nombre));
            }
                
            lector.close();

        } catch (Exception e) {
            System.out.println(e);
        } 
    }

    // Concatenar se requiere en dos casos, imp y esc
    public static String concatena (String[] palabras, int inicio) {
        // Primero, extraer y concatenar constantes
        String constante = "";
        for (int i = inicio; i < palabras.length-2; i++) {
            if (palabras[i].equals("\"")) haycomillas = !haycomillas;
            if (Pattern.matches(id, palabras[i]) && !haycomillas) {
                simbolo smb = buscarSmb(palabras[i]);
                constante += smb.valor + " "; // Extraer valor de variables
            }
            else constante += palabras[i] + " ";
        }
        constante = constante.replaceAll("\"", "").replaceAll(" \\+ ","").trim(); //System.out.println("."+constante+".");
        return constante;
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        int cont = 0;
        ruta = AnalisisLexico(ruta); // La ruta ahora es el archivo de salida
        analisisSintactico(ruta);
        analisisSemantico(ruta);
        
        if (tabErr.isEmpty()) {
            try { //Generador de código
                BufferedReader lector = new BufferedReader(new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt")); // Crea un BufferedReader para leer líneas
                BufferedWriter escritor = new BufferedWriter(new FileWriter(new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Ensamb.txt")));
                LinkedList<String> data = new LinkedList<>();
                
                while ((linea = lector.readLine()) != null) {
                    String[] palabras = linea.split("\\s");
                    
                    switch (palabras[0]) {
                        case "inicio:":
                            escritor.write(".MODEL SMALL\n");
                            escritor.write(".CODE\n");
                            escritor.write("Inicio:\n");
                            escritor.write("mov Ax, @Data\n");
                            escritor.write("mov Ds, Ax \n");
                            break;
                        case "varinicio:": 
                            break;
    
                            case "ent":
                            if (palabras.length > 3 && palabras[2].equals("=")) {
                    
                            }
                            break;
                        case "dec":
                            if (palabras.length > 3 && palabras[2].equals("=")) {
                               
                            }
                            break;
                        case "cad": //
                            if (palabras.length > 3 && palabras[2].equals("=")) {
                                
                            }
                            break;
                        case "varfin;" : 
                            break;
    
                        case "imp": // Determinar si ser requiere imprimir una variable que se leyó por teclado
                            // Primero, concatenar variables y constantes
                            String constante = concatena(palabras,2);
                            // Salta
                            escritor.write("mov Ah, 9h\n");
                            escritor.write("mov Dx, Offset S\n");
                            escritor.write("int 21h\n");
                            // Imprime la constante
                            escritor.write("mov Dx, Offset dato"+ cont + "\n");
                            escritor.write("int 21h\n");
    
                            data.add("dato" + cont + " db \"" + constante.replace("\"", "") + "\", 10,13,36\n");
                            cont++;
                            break;
                        case "si": 
                            break;
                        
                        case "sino:": 
                            break;
    
                        case "finsi;": 
                            break;
                        
                        case "para": 
                            break;
    
                        case "finpara;": 
    
                            break;
    
                        case "mientras": 
    
                            break;
                        case "finmientras;":
    
                            break;
    
                        case "fin;":  
                            escritor.write("mov Ah, 4Ch\n");
                            escritor.write("int 21h\n");
    /*------------------------------------------VARIABLES------------------------------------------*/
                            escritor.write(".DATA\n");
                            escritor.write("S db 10,13,24h\n"); // Salto de linea y fin
                            for (String dato : data) {
                                escritor.write(dato);
                            }
                            /*
                            for (simbolo s: tabSim) {
                                switch (s.tipo) {
                                    case "ent":
                                        escritor.write(s.nombre + " db ? \n");
                                        break;
                                    case "dec":
                                        escritor.write(s.nombre + " db ? \n");
                                        break;
                                    case "cad":
                                        escritor.write(s.nombre + " db 255,?,20 dup ('$') \n"); // Para leer
                                        escritor.write(s.nombre + " db ? \n"); // DEclara inicializa null5
                                        break;
                                }
                                
                            } */
                            escritor.write(".STACK\n");
                            escritor.write("END Inicio\n");
                            break;
                    }
    
                    if (palabras.length > 1) 
                    switch (palabras[1]) {
                        case "=": {//esc
                            if (palabras.length > 2) 
                            if (palabras[2].equals("esc")) {
                                // Imprimir mensaje
                                if (palabras[4].equals("\"")) {
                                    String constante = concatena(palabras, 4);
                                    // Salta
                                    escritor.write("mov Ah, 9h\n");
                                    escritor.write("mov Dx, Offset S\n");
                                    escritor.write("int 21h\n");
                                    // Imprime la constante
                                    escritor.write("mov Dx, Offset dato"+ cont + "\n");
                                    escritor.write("int 21h\n");
                                    // Crear variable
                                    data.add("dato" + cont + " db \"" + constante.replace("\"", "") + "\", 10,13,36\n");
                                    cont++;
                                }
                                // Leer por teclado
                                escritor.write("mov Ah, 0Ah \n");
                                escritor.write("mov Dx, Offset "+ palabras[0] + "\n");
                                escritor.write("int 21h\n");
                                // Guardar la variable
                                data.add(palabras[0] + " db 255,?,255\n");
                                //simbolo smb = buscarSmb(palabras[0]);
                                //if (smb != null ) smb.valor = "?";
                            }
                            else { // Asign
                                
                            }
                        }
                    }
                }
    
                //File f = new File ("file.asm");
                lector.close();
                escritor.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else {
            try {// Errores
                FileReader errores = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\errDesc.txt");
                BufferedReader lector = new BufferedReader(errores); // Crea un BufferedReader para leer líneas
            
                List<String> errDesc = new ArrayList<>();
                String error;

                while ((error = lector.readLine()) != null) errDesc.add(error);
                lector.close();
                for (error e: tabErr) {
                    for (String ln : errDesc) {
                        if (e.codigo.equals(ln.substring(0,5))){
                            System.out.println("Error " + e.codigo + ", linea: " + e.linea + ": " + e.token + " \n" + ln.substring(6, ln.length()) + "\n");
                        }
                    }               
                }
                
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        for (simbolo smb: tabSim) System.out.printf("tipo: %s, nombre: %s, valor: %s \n", smb.tipo, smb.nombre, smb.valor);
    }
}