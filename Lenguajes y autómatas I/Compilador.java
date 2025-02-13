import java.io.BufferedReader;
import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    static String cte = "(" + cte_ent + ")" + "|" + "(" + cte_dec + ")" + "|" + "(" + cte_cad + ")";
    static String id = "^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$";
    static String op = "\\*|/|\\+|-|mod|div|(&&)|(\\|\\|)|>=|<=|==|<>|<|>";
    static List<error> tabErr = new ArrayList<>();
    static boolean haycomillas = false;
    static List<Integer> indices = new ArrayList<>(); //Para guardar las lineas que si contienen información
    static Stack<etiqueta> pilaSem = new Stack<>();
    
    public static void comprobarTipos (String operacion, int contLin, String tipo) {
        String regex = ""; // Revisa declaraciones inicializadas con un valor y asignaciones
        Matcher matOp = Pattern.compile(op).matcher(operacion);
        String[] operandos;
        if (matOp.find()) operandos = operacion.split(op); // Separa cada uno de los operandos
        else operandos = operacion.split(" ");
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

    // Concatenar se requiere en imp y esc
    public static List<String> concatena (String[] palabras, int inicio, int fin) {
        List<String> ctes = new ArrayList<>();
        // Primero, extraer y concatenar constantes
        String constante = "";
        for (int i = inicio; i < palabras.length-fin; i++) {
            if (palabras[i].equals("\"")) haycomillas = !haycomillas; // Para evitar confusiones con los id's
            else if (Pattern.matches(id, palabras[i]) && !haycomillas) {
                simbolo smb = buscarSmb(palabras[i]);
                if (smb != null) { // Si el valor es ?, es un id que se leyó por teclado
                    if (smb.valor == "?" || smb.valor.equals("!")) {
                        constante = constante.replaceAll("\"", "").replaceAll(" \\+ ","").replaceAll("\\+ ","");
                        ctes.add(constante); // constante + id + constante + ...
                        ctes.add(smb.nombre); 
                        constante = "";
                    }
                    else constante += smb.valor; // Extraer valor de variable no leida
                }
            }
            else constante += palabras[i] + " "; // Concatenar la constante
        }
        
        if (!constante.equals("")) { // Guardar la última constante
            
            constante = constante.replaceAll("\"", "").replaceAll(" \\+ ","").replaceAll("\\+ ","");
            ctes.add(constante);
        }
        return ctes;
    }
    //asigna se utiliza en asign y en declara
    public static String asigna (String[] palabras, int inicio) {
        String constante = "";
        for (int i = inicio; i < palabras.length; i+=2) { // i-1 operador, i operando
            if (Pattern.matches(id, palabras[i])) {
                simbolo smb = buscarSmb(palabras[i]);
                if (smb != null) constante += palabras[i-1] + " " + smb.valor + " ";
            }
            else constante += palabras[i-1] + " " + palabras[i] + " ";
        }
        return constante.substring(2);
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
                
                for (String palabra : palabras) {
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
            simbolo smb;

            while ((linea = lector.readLine()) != null) {
                contLin++;
                String operacion = "", constante = "";;
                String[] palabras = linea.split("\\s");
                
                
                switch (palabras[0]) {
                    case "inicio:": 
                        pilaSem.push(new etiqueta("inicio:", indices.get(contLin - 1)));
                        break;
                    case "varinicio:": 
                        pilaSem.push(new etiqueta("varinicio:", indices.get(contLin - 1)));
                        break;
                    case "ent":
                        smb = buscarSmb(palabras[1]);
                        if (palabras[2].equals("=")) {
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) {
                                if (palabras[i].equals(palabras[1])) tabErr.add(new error("lex00", contLin, palabras[i]));
                                else operacion += palabras[i] + " ";
                            }
                            comprobarTipos(operacion, indices.get(contLin - 1), "ent");
                            
                        }
                        else if ( smb != null ) smb.valor = "0";
                         
                        break;
                    case "dec":
                        smb = buscarSmb(palabras[1]);
                        if (palabras.length > 3 && palabras[2].equals("=")) {
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) {
                                if (palabras[i].equals(palabras[1])) tabErr.add(new error("lex00", contLin, palabras[i]));
                                else operacion += palabras[i] + " ";
                            }
                            comprobarTipos(operacion, indices.get(contLin - 1), "dec");
                        }
                        else smb.valor = "0.0";
                        break;
                    case "cad":
                        smb = buscarSmb(palabras[1]);
                        if (palabras.length > 3 && palabras[2].equals("=")) {
                            Matcher matOp_arit = Pattern.compile(op).matcher(linea);
                            while (matOp_arit.find()) { // Revisa todos los operadores aritmeticos de la linea
                                if (!matOp_arit.group(0).equals("+"))  // si no es un +, marca error
                                    tabErr.add(new error("sem05", indices.get(contLin - 1), ""));
                            }
                            // Obtiene la operación compuesta por operadores y operandos (a + b - c)
                            for (int i = 3; i < palabras.length-1; i++) {
                                if (palabras[i].equals(palabras[1])) tabErr.add(new error("lex00", contLin, palabras[i]));
                                else operacion += palabras[i] + " ";
                            }
                            comprobarTipos(operacion, indices.get(contLin - 1), "cad");
                            operacion = operacion.replace(" \"", "").replace("\" ", "").replace(" + ","");  
                            
                            List<String> constantes = concatena(palabras, 3, 1);
                            for (String c : constantes) constante += c;
                            
                            smb.valor = constante; // Completar la tabla de simbolos
                        }
                        else smb.valor = "";
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
                        for (int i = 1; i < palabras.length; i += 2) operacion += palabras[i] + " ";
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
                        if (palabras[2].equals("esc")) {}
                        else { // Asign
                            String tipo = "";
                            for (int i = 2; i < palabras.length-1; i++) operacion += palabras[i] + " ";
                            
                            simbolo simb = buscarSmb(palabras[0]);
                            if (simb != null) {
                                tipo = simb.tipo; // Si no es nulo, guarda el tipo
                                comprobarTipos (operacion, indices.get(contLin - 1), tipo);
                            
                                if (tipo.equals("cad")) {
                                    Matcher matOp_arit = Pattern.compile(op).matcher(linea);
                                    while (matOp_arit.find()) { // Revisa todos los operadores aritmeticos de la linea
                                        if (!matOp_arit.group(0).equals("+"))  // si no es un +, marca error
                                            tabErr.add(new error("sem05", indices.get(contLin - 1), ""));
                                    }
                                    List<String> ctes = concatena(palabras, 2, 1);
                                    // Asignar el valor para completar la tabla de simbolos
                                    for (String c : ctes) constante += c;
                                    simb.valor = constante;
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

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        int cont = 0, salto = 0;
        Stack<Integer> anidacion = new Stack<>();
        ruta = AnalisisLexico(ruta); // La ruta ahora es el archivo de salida
        analisisSintactico(ruta);
        analisisSemantico(ruta);
        
        if (tabErr.isEmpty()) {
            try { //Generador de código
                BufferedReader lector = new BufferedReader(new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt")); // Crea un BufferedReader para leer líneas
                BufferedWriter escritor = new BufferedWriter(new FileWriter(new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Ensamb.asm")));
                LinkedList<String> data = new LinkedList<>();
                String tempo = "";
                while ((linea = lector.readLine()) != null) {
                    String[] palabras = linea.split("\\s");
                    simbolo smb;
                    switch (palabras[0]) {
                        case "inicio:":
                            escritor.write(".MODEL SMALL\n");
                            escritor.write(".CODE\n");
                            escritor.write("Inicio:\n");
                            escritor.write("mov Ax, @Data\n");
                            escritor.write("mov Ds, Ax \n");
                            break;
                        case "varinicio:": break;
                        case "ent": // ent a = 1 - 1 ;
                            if (palabras[2].equals("=")) {// Asignacion
                                if (Pattern.matches(op, palabras[4])) { // Operacion
                                    String operacion = "";
                                    for (int i = 3; i < palabras.length-1; i++) operacion += palabras[i] + " ";
                                    String[] posfijo = arbolDeExpresion.infijo_posfijo(operacion);
                                    
                                    for (String l : posfijo) { // Generar codigo en ensamblador de operacion
                                        l = l.trim();
                                        switch (l) {
                                            case "+": // Sacar de la pila el valor y guardarlo
                                                escritor.write("xor Cx, Cx\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ch, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Cl, Dl\n");
                                                //Realizar la suma
                                                escritor.write("mov dato" + cont + ", Cl\n");
                                                escritor.write("add dato" + cont + ", Ch\n");
                                                escritor.write("mov Dl, dato" + cont + "\n");
                                                escritor.write("xor Dh, Dh\n");
                                                escritor.write("push Dx\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
    
                                            case "-": 
                                                escritor.write("xor Cx, Cx\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ch, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Cl, Dl\n");
                                                //Realizar la resta
                                                escritor.write("mov dato" + cont + ", Cl\n");
                                                escritor.write("sub dato" + cont + ", Ch\n");
                                                escritor.write("mov Dl, dato" + cont + "\n");
                                                escritor.write("xor Dh, Dh\n");
                                                escritor.write("push Dx\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
    
                                            case "*":
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ah, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");;
                                                escritor.write("mov AL, Dl\n");
                                                //Realizar la multiplicación
                                                escritor.write("mul Ah\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
                                                
                                            case "/": 
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;

                                            case "mod": 
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Ah\n");
                                                escritor.write("mov Al, Ah\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
                                                
                                            case "div":
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;

                                            default: // OPERADOR. Guardar en la pila de ensamblador
                                                if (Pattern.matches(cte, l)) { // constantes entran directamente
                                                    escritor.write("push " + l + "\n");
                                                }
                                                else { // Variable
                                                    escritor.write("xor Bx, Bx\n");
                                                    smb = buscarSmb(l);
                                                    if (smb != null) {
                                                        if (smb.valor.equals("?")) // Si es una variable leida
                                                            escritor.write("mov Bx, offset" + l + " + 2\n");
                                                        else escritor.write("mov Bx, " + smb.nombre + "\n"); // variable normal
                                                        escritor.write("sub Bx, 30h\n"); // Realizar el ajuste antes de meter a la pila
                                                        escritor.write("push Bx\n");
                                                    }
                                                }
                                        }
                                    }
                                    // cont-- es la última variable que guardó el resultado fina
                                    escritor.write("mov Dl, dato" + (cont-1) + "\n");
                                    escritor.write("mov dh, 36\n"); 
                                } // Un solo valor a asignar
                                else if (Pattern.matches(id, palabras[3])) { // Las variables tienen un trato ligeramente diferente
                                    escritor.write("mov Dx, " + palabras[3] + "\n");
                                    escritor.write("sub Dx, 30h\n");
                                }
                                else escritor.write("mov Dl, offset " + palabras[3] + "\n"); // Es una constante
                                escritor.write("mov dh, 36\n");
                                escritor.write("mov " + palabras[1] + ", Dx\n");
                                escritor.write("add " + palabras[1] + ", 30h\n");
                                smb = buscarSmb(palabras[1]);
                                smb.valor = "!";
                                data.add(palabras[1] + " dw 255 dup ('$')\n");
                            }
                            else { // Inicializa como 0
                                escritor.write("mov Dl, 0\n"); 
                                escritor.write("mov " + palabras[1] + ", dx\n");
                                escritor.write("add " + palabras[1] + ", 30h\n");
                                smb = buscarSmb(palabras[1]);
                                smb.valor = "!";
                                data.add(palabras[1] + " dw 255 dup ('$')\n");
                            }
                            break;

                        case "dec": break;
                        case "cad": break;
                        case "varfin;": break;

                        case "imp":
                            // Primero, concatenar variables y constantes
                            List<String> ctes = concatena (palabras,2,2);

                            // Salta
                            escritor.write("mov Ah, 9h\n");
                            escritor.write("mov Dx, Offset S\n");
                            escritor.write("int 21h\n");
                            
                            for (String cte: ctes) {
                                if (!cte.equals("")) {    // Imprime la constante o variable
                                    smb = buscarSmb(cte);
                                    if (smb != null) {
                                        if (smb.valor.equals("?")) { // Variable leida por teclado
                                            escritor.write("mov Dx, Offset " + cte + " + 2\n");
                                            escritor.write("int 21h\n");
                                        }
                                        else if (smb.valor.equals("!")) { // Variable asignada
                                            escritor.write("mov Dx, Offset " + cte + "\n");// Debe imprimir solo el primer caracter
                                            escritor.write("int 21h\n");
                                            
                                        }
                                    }    
                                    else {
                                        escritor.write("mov Dx, Offset dato" + cont + "\n");
                                        escritor.write("int 21h\n");
                                        // Crear variable
                                        data.add("dato" + cont + " db \"" + cte + "\", 36\n");
                                        cont++;
                                    }
                                }
                            }
                            break;

                        case "si": { // si ( a < b ) :
                            salto++;
                            anidacion.push(salto);
                            escritor.write("push Cx\n");
                            escritor.write("xor Cx, Cx\n");
                            if (Pattern.matches(cte, palabras[2])) {
                                escritor.write("mov Cl, " + palabras[2] +"\n");
                                escritor.write("add Cl, 30h\n");
                            }
                            else {
                                smb = buscarSmb(palabras[2]);
                                if (smb.valor.equals("?")) {
                                    escritor.write("mov Si, offset " + palabras[2] + " + 2\n");
                                    escritor.write("mov Cl, byte ptr [si]\n");
                                }
                                else {
                                    escritor.write("mov Si, offset " + palabras[2] + "\n");
                                    escritor.write("mov Cl, byte ptr [si]\n");
                                }
                            }
                            if (Pattern.matches(cte, palabras[4])) {
                                escritor.write("mov Ch, " + palabras[4] +"\n");
                                escritor.write("add Ch, 30h\n");
                            }
                            else {
                                smb = buscarSmb(palabras[4]);
                                if (smb.valor.equals("?")) {
                                    escritor.write("mov Si, offset " + palabras[4] + " + 2\n");
                                    escritor.write("mov Ch, byte ptr [Si]\n");
                                }
                                else {
                                    escritor.write("mov Si, offset " + palabras[4] + "\n");
                                    escritor.write("mov Ch, byte ptr [Si]\n");
                                }
                            }
                            escritor.write("cmp Cl, Ch\n");
                            switch (palabras[3]) {
                                case ">": escritor.write("jna salto" + salto + "\n"); break;
                                case "<": escritor.write("jnb salto" + salto + "\n"); break;
                                case ">=": escritor.write("jnae salto" + salto + "\n"); break;
                                case "<=": escritor.write("jnbe salto" + salto + "\n"); break;
                                case "==": escritor.write("jne salto" + salto + "\n"); break;
                                case "<>": escritor.write("je salto" + salto + "\n"); break;
                            }
                            escritor.write("pop Cx\n");
                            break;
                        }
                        case "sino:": 
                            break;
    
                        case "finsi;":
                            escritor.write("salto" + anidacion.pop() + ":\n");
                            break;
                        
                        case "para": // para b = li hasta ls :
                            salto++;
                            anidacion.push(salto);
                            tempo = palabras[1];
                            escritor.write("push Cx\n");
                            escritor.write("push Dx\n");

                            // Limite inferior
                            if (Pattern.matches(cte, palabras[3])) {//Suponer si son constantes
                                escritor.write("mov " + tempo + ", offset " + palabras[3] + "\n"); // Constante
                                escritor.write("add " + tempo + ", 30h\n");
                            }
                            else { // Es un ID
                                smb = buscarSmb(palabras[3]); // Variable leida:
                                escritor.write("xor Dx, Dx\n");
                                escritor.write("mov Dx, offset " + palabras[3] + "\n");
                                escritor.write("mov Dh, 36\n");
                                escritor.write("mov " + tempo + ", dx\n");
                                escritor.write("add " + tempo + ", 30h\n");
                                
                            } // Limite superior en Cl:
                            escritor.write("xor Cx, Cx\n");
                            if (Pattern.matches(cte, palabras[5])) {// Ls es constante
                                escritor.write("mov Cl," + palabras[5] + "\n"); // Limite superior en Cl
                                //escritor.write("add Cl, 30h\n");
                            }
                            else { // ES un ID
                                smb = buscarSmb(palabras[5]);
                                escritor.write("mov Cx," + palabras[5] + "\n"); // Variable asignada
                                escritor.write("sub Cl, 30h\n"); // Ajuste
                            }
                            escritor.write("sub " + tempo + ", 30h\n"); // Ajuste
                            escritor.write("sub Cx, " + tempo + "\n"); // Ajustar 
                            escritor.write("add Cl, 1\n");
                            escritor.write("add " + tempo + ", 30h\n");
                            escritor.write("xor Ch, Ch\n");
                            escritor.write("Salto" + salto + ": \n"); // Limite superior
                            escritor.write("pop Dx\n");
                            smb = buscarSmb(tempo); smb.valor="!";
                            break;

                        case "finpara;": 
                            escritor.write("inc " + tempo + "\n");
                            escritor.write("loop Salto" + anidacion.pop() + "\n");
                            escritor.write("pop Cx\n");
                            break;
    
                        case "mientras": {// mientras ( a <> b ) hacer:
                        salto++;
                        anidacion.push(salto);
                        escritor.write("push Cx\n");
                        escritor.write("xor Cx, Cx\n");
                        escritor.write("salto" + salto + ":\n");
                            if (Pattern.matches(cte, palabras[2])) {
                                escritor.write("mov Cl, " + palabras[2] +"\n");
                                escritor.write("add Cl, 30h\n");
                            }
                            else {
                                smb = buscarSmb(palabras[2]);
                                if (smb.valor.equals("?")) { 
                                    escritor.write("mov Si, offset " + palabras[2] + " + 2\n");
                                    escritor.write("mov Cl, byte ptr [si]\n");
                                }
                                else { 
                                    escritor.write("mov Si, offset " + palabras[2] + "\n");
                                    escritor.write("mov Cl, byte ptr [si]\n");
                                }
                            }
                            if (Pattern.matches(cte, palabras[4])) {
                                escritor.write("mov Ch, " + palabras[4] +"\n");
                                escritor.write("add Ch, 30h\n");
                            }
                            else {
                                smb = buscarSmb(palabras[4]);
                                if (smb.valor.equals("?")) {
                                    escritor.write("mov Si, offset " + palabras[4] + " + 2\n");
                                    escritor.write("mov Ch, byte ptr [Si]\n");
                                }
                                else {
                                    escritor.write("mov Si, offset " + palabras[4] + "\n");
                                    escritor.write("mov Ch, byte ptr [Si]\n");
                                }
                            }
                            escritor.write("cmp Cl, Ch\n");
                            salto++;
                            anidacion.push(salto);
                            switch (palabras[3]) {
                                case ">": escritor.write("jna salto" + salto + "\n"); break;
                                case "<": escritor.write("jnb salto" + salto + "\n"); break;
                                case ">=": escritor.write("jnae salto" + salto + "\n"); break;
                                case "<=": escritor.write("jnbe salto" + salto + "\n"); break;
                                case "==": escritor.write("jne salto" + salto + "\n"); break;
                                case "<>": escritor.write("je salto" + salto + "\n"); break;
                            }
                            escritor.write("pop Cx\n");
                            break;
                        }
                        case "finmientras;":
                            Integer caca = anidacion.pop();    
                            escritor.write("jmp salto" + anidacion.pop() + "\n");
                            escritor.write("salto" + caca  + ":\n");
                            break;
    
                        case "fin;":  
                            escritor.write("mov Ah, 4Ch\n");
                            escritor.write("int 21h\n");
                            escritor.write(".DATA\n"); // VARIABLES
                            escritor.write("S db 10,13,24h\n"); // Salto de linea y fin
                            for (String dato : data) escritor.write(dato);
                            
                            //Vaciar tabla de simbolos
                            for (simbolo s: tabSim) { // Las variables leidas se declaran diferente al leer
                                if (!s.valor.equals("?") && !s.valor.equals("!")) escritor.write(s.nombre + " dw \"" + s.valor + "\", '$'\n"); 
                            }
                            escritor.write(".STACK\n");
                            escritor.write("END Inicio\n");
                            break;
                    }
                    
                    if (palabras.length > 1) 
                    switch (palabras[1]) {
                        case "=": { 
                            if (palabras.length > 2) 
                            if (palabras[2].equals("esc")) { // x = esc ( " Ingresa n1 " ) ;
                                List<String> ctes = concatena (palabras, 4,2);
                                // Salta
                                escritor.write("mov Ah, 9h\n"); 
                                escritor.write("mov Dx, Offset S\n");
                                escritor.write("int 21h\n");
                                
                                for (String cte: ctes) { // cte contiene todas las constantes y variables leidas
                                    if (!cte.equals("")) {
                                        smb = buscarSmb(cte);
                                        if (!cte.equals(""))
                                        if (smb != null) { // Es una variable leida o no?
                                            if (smb.valor.equals("?")) { // Variable leida por teclado
                                                escritor.write("mov Dx, Offset " + cte + " + 2\n"); // Imprimir
                                                escritor.write("int 21h\n");
                                            }
                                            else if (smb.valor.equals("!")) { // Variable asignada
                                                escritor.write("mov Dx, Offset " + smb.nombre + "\n");
                                                escritor.write("int 21h\n");
                                            }
                                        }
                                        else {
                                            escritor.write("mov Dx, Offset dato" + cont + "\n");
                                            escritor.write("int 21h\n"); // Crea una variable para imprimir la constante
                                            // Crear variable
                                            data.add("dato" + cont + " dw \"" + cte + "\", 36\n");
                                            cont++;
                                        }
                                    }
                                }
                                // Leer por teclado
                                escritor.write("mov Ah, 0Ah \n");
                                escritor.write("mov Dx, Offset " + palabras[0] + "\n");
                                escritor.write("int 21h\n");

                                escritor.write("xor Bx, Bx\n");
                                escritor.write("mov Bx, " + palabras[0] + "[2]\n");
                                escritor.write("mov Bh, 24h\n");
                                escritor.write("mov " + palabras[0] + "[0], Bx\n");
/* 
                                escritor.write("xor Bx, Bx\n");
                                escritor.write("mov Bx, " + palabras[0] + "[1]\n");
                                escritor.write("xor Bh, Bh\n");
                                escritor.write("mov " + palabras[0] + "[Bx+2], '$'\n"); // Para que se borre el caracter 0dh, que regresa el cursor al inicio de la linea
                                */
                                smb = buscarSmb(palabras[0]); smb.valor = "!";
                            }
                            else { // Asign x = n1 + n2+ n3 ...
                                if (Pattern.matches(op, palabras[3])) { // Operacion
                                    String operacion = "";
                                    for (int i = 2; i < palabras.length-1; i++) operacion += palabras[i] + " ";
                                    String[] posfijo = arbolDeExpresion.infijo_posfijo(operacion);
                                    escritor.write("push Ax\n");
                                    escritor.write("push Bx\n");
                                    escritor.write("push Cx\n");
                                    escritor.write("push Dx\n");
                                    for (String l : posfijo) { // Generar codigo en ensamblador de operacion
                                        l = l.trim();
                                        switch (l) {
                                            case "+": // Sacar de la pila el valor y guardarlo
                                                escritor.write("xor Cx, Cx\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ch, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Cl, Dl\n");
                                                //Realizar la suma
                                                escritor.write("mov dato" + cont + ", Cl\n");
                                                escritor.write("add dato" + cont + ", Ch\n");
                                                escritor.write("mov dl, dato" + cont + "\n");
                                                escritor.write("xor Dh, Dh\n");
                                                escritor.write("push dx\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;

                                            case "-": 
                                                escritor.write("xor Cx, Cx\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ch, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Cl, Dl\n");
                                                //Realizar la resta
                                                escritor.write("mov dato" + cont + ", Cl\n");
                                                escritor.write("sub dato" + cont + ", Ch\n");
                                                escritor.write("mov dl, dato" + cont + "\n");
                                                escritor.write("xor Dh, Dh\n");
                                                escritor.write("push dx\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;


                                            case "*":
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Ah, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");;
                                                escritor.write("mov AL, Dl\n");
                                                //Realizar la multiplicación
                                                escritor.write("mul Ah\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
                                                
                                            case "/": 
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
                                            case "mod": 
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Ah\n");
                                                escritor.write("mov Al, Ah\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;
                                                
                                            case "div":
                                                escritor.write("xor Ax, Ax\n");
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Bl, Dl\n");
                                                // Los registros aqui van al revés por la estructura de la pila
                                                escritor.write("pop Dx\n");
                                                escritor.write("mov Al, Dl\n");
                                                //Realizar la división
                                                escritor.write("div Bl\n");
                                                escritor.write("mov dato" + cont + ", Al\n");
                                                escritor.write("xor Ah, Ah\n");
                                                escritor.write("push Ax\n");
                                                // Crear variable temporal
                                                data.add("dato" + cont + " db 255 dup ('$')\n");
                                                cont++;
                                                break;

                                            default: {// OPERADOR. Guardar en la pila de ensamblador
                                                if (Pattern.matches(cte,l)) { // constantes entran directamente
                                                    escritor.write("push " + l + "\n");
                                                }
                                                else { // Variable
                                                    escritor.write("xor Bx, Bx\n");
                                                    smb = buscarSmb(l);
                                                    if (smb != null) {
                                                        if (smb.valor.equals("?")) // Si es una variable leida
                                                            escritor.write("mov Bx, " + l + " + 2\n");
                                                        else escritor.write("mov Bx, " + smb.nombre + "\n"); // variable normal
                                                        escritor.write("sub Bx, 30h\n"); // Realizar el ajuste antes de meter a la pila
                                                        escritor.write("push Bx\n");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // cont-- es la última variable que guardó el resultado fina
                                    escritor.write("xor Dx, Dx\n");
                                    escritor.write("mov Dl, dato" + (cont-1) + "\n");
                                    escritor.write("mov Dh, 36\n");
                                } // Solo es un solo valor a asignar
                                else if (Pattern.matches(id, palabras[2])) { // Las variables tienen un trato ligeramente diferente
                                    escritor.write("xor Dx, Dx\n");
                                    escritor.write("mov Dx, " + palabras[2] + "\n");
                                    escritor.write("sub Dx, 30h\n");
                                }
                                else { // Es una constante
                                    escritor.write("xor Dx, Dx\n");
                                    escritor.write("mov Dl, offset " + palabras[2] + "\n");
                                }
                                smb = buscarSmb(palabras[0]);
                                
                                if (smb.valor.equals("?")) { // Limpiar la variable si ya fue utilizada para leer
                                    escritor.write("xor Bx, Bx\n");
                                    escritor.write("mov " + palabras[0] + "[2], '$'\n");
                                }
                                smb.valor = "!";
                                // Asignar el valor a la variable
                                escritor.write("mov Dh, 36\n");
                                escritor.write("mov " + palabras[0] + ", dx\n");
                                escritor.write("add " + palabras[0] + ", 30h\n");
                                escritor.write("pop Ax\n");
                                escritor.write("pop Bx\n");
                                escritor.write("pop Cx\n");
                                escritor.write("pop Dx\n");
                            }
                        }
                    }
                }
                
                lector.close();
                escritor.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else { // Errores
            try {
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
//for (simbolo smb: tabSim) System.out.printf("%s %s = %s \n", smb.tipo, smb.nombre, smb.valor);
    }
}