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

        public simbolo () {
            this.tipo = "";
            this.nombre = "";
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
    static String op_arit = "\\*|/|\\+|-|mod|div";
    
    public static void comprobarTipos (String[] palabras, int contLin) {
        for (int i = 3; i < palabras.length - 1; i++) {
            if (!Pattern.matches(op_arit, palabras[i]))
            if (!Pattern.matches(cte_ent, palabras[i])) {
                if (Pattern.matches(id, palabras[i])) {
                    simbolo simb = buscarSmb(palabras[i]);
                    if (simb != null)
                    if (!buscarSmb(palabras[i]).tipo.equals("ent"))
                        tabErr.add(new error("sem04", contLin, palabras[i]));
                }
                else tabErr.add(new error("sem04", contLin, palabras[i]));
            }
        }
    }

    public static void AbrirArchivo (String ruta) {
        List<String> palReserv = Arrays.asList("ent", "dec", "cad", "inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        List<error> tabErr = new ArrayList<>();
        LinkedList<String> constCad = new LinkedList<>();
        List<Integer> indices = new ArrayList<>(); //Para guardar las lineas que si contienen información
        Stack<etiqueta> pilaSem = new Stack<>();
        simbolo smb = new simbolo();
        String miAlf = "[*]|[/]|[+]|[-]|[=]|(<>)|(==)|(<=)|(>=)|[<|>]|[(]|[)]|[;]|[:]|(^[&][&]|[\\|][\\|]$)|(^[\\d]+$|^[\\d]+\\.[\\d]+$)";
        boolean declaracion = false, haycomillas = false, hayInicio = false, hayVarInicio = false;
        int contLin = 0;
        Pattern patCad = Pattern.compile(cte_cad);

        

        try { // Abre el archivo para lectura
            BufferedReader lector = new BufferedReader(new FileReader(ruta)); // Crea un BufferedReader para leer líneas
            // Para generar el archivo de salida
            File salida = new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
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
        contLin = 0; 
        try { // syn
            BufferedReader lector = new BufferedReader(new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt"));
            BufferedReader regex = new BufferedReader (new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\regex.txt"));
            String sentencias = regex.readLine(); // Mi gramatica
            regex.close();
            
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
        contLin = 0;
        try { // Sem
            FileReader archivoSalida = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
            BufferedReader lector = new BufferedReader(archivoSalida); // Crea un BufferedReader para leer líneas
            etiqueta et = null;
            
            while ((linea = lector.readLine()) != null) {
                contLin++;
                String[] palabras = linea.split("\\s");
                
                    switch (palabras[0]) {
                        case "inicio:": 
                            pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
                            break;
                        case "varinicio:": 
                            pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
                            break;
                        case "ent":
                            if (palabras.length > 3 && palabras[2].equals("=")) comprobarTipos(palabras, contLin);
                            else {
                                if (!buscarSmb(palabras[1]).tipo.equals("ent")) tabErr.add(new error("sem04", contLin, palabras[1]));
                            }
                            break;
                        case "dec":
                            if (palabras.length > 3 && palabras[2].equals("=")) {
                                for (int i = 3; i < palabras.length - 1; i++) {
                                    if (!Pattern.matches(op_arit, palabras[i]))
                                    if (!Pattern.matches(cte_dec, palabras[i])) {
                                        if (Pattern.matches(id, palabras[i])) {
                                            simbolo simb = buscarSmb(palabras[i]);
                                            if (simb != null)
                                            if (!buscarSmb(palabras[i]).tipo.equals("dec"))
                                                tabErr.add(new error("sem04", contLin, palabras[i]));
                                        }
                                        else tabErr.add(new error("sem04", contLin, palabras[i]));
                                    }
                                }
                            }
                            break;
                        case "cad":
                            if (palabras.length > 3 && palabras[2].equals("=")) {
                                // Para verificar que solo se usen '+' y no otros operadores aritmeticos
                                for (int i = 3; i < palabras.length - 1; i++) {
                                    if (Pattern.matches(op_arit, palabras[i])) {
                                        if (!palabras[i].equals("+")) tabErr.add(new error("sem05", contLin, linea));
                                    } // Para verificar que los simbolos sean de tipo cad
                                    else if (palabras[i].equals("\"")) haycomillas = !haycomillas;
                                    else if (Pattern.matches(id, palabras[i])) {
                                            simbolo simb = buscarSmb(palabras[i]);
                                            if (simb != null)
                                            if (!buscarSmb(palabras[i]).tipo.equals("cad"))
                                                tabErr.add(new error("sem04", contLin, palabras[i]));
                                    }
                                    else if (!haycomillas) {
                                        if (Pattern.matches(cte_ent, palabras[i]) || Pattern.matches(cte_dec, palabras[i])) {
                                            tabErr.add(new error("sem04", contLin, palabras[i]));
                                        }
                                    }
                                    Matcher matCad = patCad.matcher(linea);
                                    while (matCad.find()) { // Para verificar que las constantes cad sean válidas
                                        String cteString = matCad.group(0);
                                        if (!Pattern.matches(cte_cad, cteString))
                                            tabErr.add(new error("sem04", contLin, cteString));
                                    }
                                }
                            }
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
                            break;

                        case "finsi;": 
                            if (pilaSem.isEmpty()) tabErr.add(new error("sem01", indices.get(contLin - 1), "finsi;"));
                                else {et = pilaSem.pop();
                                if (!et.nombre.equals("si")) tabErr.add(new error("sem00", et.linea, et.nombre));
                            }
                            break;
                        
                        case "para": 
                            pilaSem.push(new etiqueta(palabras[0], indices.get(contLin - 1)));
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
                            if (palabras[2].equals("esc")) System.out.print("esc");
                            else {
                                String tipo = buscarSmb(palabras[0]).tipo;
                                System.out.println(tipo);
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

        try {// Errores
            FileReader errores = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\errDesc.txt");
            BufferedReader lector = new BufferedReader(errores); // Crea un BufferedReader para leer líneas
           
            List<String> errDesc = new ArrayList<>();
            String error;

            while ((error = lector.readLine()) != null) {
                errDesc.add(error);
            }
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
        /*
        for (simbolo s: tabSim) {
            System.out.println(s.nombre + " de tipo " + s.tipo);
        }   */
        
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        AbrirArchivo(ruta);
    }
}
