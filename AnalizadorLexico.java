import java.io.BufferedReader; //Usa split mediante los espacios en blanco
import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLexico {
    public static class token {
        public String nombre;
        public String valor;

        public token (String nombre, String valor) {
            this.nombre = nombre;
            this.valor = valor;
        }
    }

    public static class simbolo {
        public String tipo;
        public String nombre;

        public simbolo (String tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre;
        }
    }

    public static boolean buscarSmb (List<simbolo> tablaSimbolos, String nombre) {
        for (simbolo smb : tablaSimbolos) { //Comprobar si el símbolo esta guardado
            if (smb.nombre.equals(nombre)) return true;
        }
        return false;
    }
    
    static List<String> palReserv = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
    static List<String> tDatos = Arrays.asList("ent", "dec", "cad");
    static List<String> etiquetas = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "sino:", "finsi;" , "finpara;", "finmientras;");
    static List<String> errLex = new ArrayList<>();
    static List<String> errSintac = new ArrayList<>();
    static List<Integer> indices = new ArrayList<>();
    static int contLin = 0, inicio = 0, fin = -1;
    static String linea, const_string;
    static boolean declaracion = false, hayErrorLex = false, haycomillas = false;
    static StringBuilder contenido = new StringBuilder(); //Para editar el archivo
    static List<token> misTokens = new ArrayList<>(); //Mis tokens
    static List<simbolo> tablaSimbolos = new ArrayList<>();
    static Queue<String> cola = new LinkedList<>();

    static String digito = "0|1|2|3|4|5|6|7|8|9";
    static String letra = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z";
    static String tipo = "ent|dec|cad";
    static String op_arit = "\\*|/|\\+|-|mod|div";
    static String op_rel = ">=|<=|==|<>|<|>";
    static String op_log = "(&&)|(\\|\\|)";
    static String cte_ent = "(" + digito + ")+";
    static String cte_dec = cte_ent + "\\." + cte_ent;
    static String cte_cad = "\" (.*?) \"";
    static String cte = "(" + cte_ent + ")" + "|" + "(" + cte_dec + ")" + "|" + "(" + cte_cad + ")";
    static String id = "(" + letra + ")(_*(" + letra + "|" + digito + ")+)*"; 
    static String num = cte_ent + "|" + id;
    static String operando = id + "|" + cte;
    static String condicional = "((" + operando + ") (" + op_rel + ") (" + operando + "))( (" + op_log + ") (" + operando + ") (" + op_rel + ") (" + operando + "))*" ;
    static String operacion = "((" + operando + ") (" + op_arit + ") (" + operando + "))( (" + op_arit + ") (" + operando + "))*";
    static String expresion = "(" + operando + ")( \\+ (" + operando + "))*";
    static String asign = "(" + id + ") = (" + operando + ") ;|(" + id + ") = (" + operacion + ") ;";
    static String imp = "imp \\( (" + expresion + ") \\) ;";
    static String esc = id + " = " + "esc \\( (" + cte_cad + ") \\) ;";
    static String si = "si \\( (" + condicional + ") \\) :";
    static String para = "para (" + id + ") = (" + num + ") hasta (" + num + ")( contando (" + num + "))? :";
    static String mientras = "mientras \\( (" + condicional + ") \\) hacer:";
    static String declara = "(" + tipo + ") ((" + id + ") ;|(" + asign +"))";

    public static void AbrirArchivo (String ruta) {
        try { // Abre el archivo para lectura
            FileReader archivoFuente = new FileReader(ruta);
            BufferedReader lector = new BufferedReader(archivoFuente); // Crea un BufferedReader para leer líneas

            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea
                Matcher comillas = Pattern.compile("\"").matcher(linea);
                
                if (comillas.find()) { // Si hay comillas en la linea
                    inicio = comillas.end(); // Guarda el inicio y el fin
                    if (comillas.find()) fin = comillas.start();
                    else errLex.add("Error léxico en la línea " + contLin  + ", faltan comillas\n");
                }
                
                String[] palabras = linea.split(" ");
                for (String palabra : palabras) {         
                    //System.out.println(palabra);
                    if (palabra.equals("\"")) {
                        haycomillas = !haycomillas;
                        token token = new token("const_str", linea.substring(inicio, fin));
                        if (!haycomillas) misTokens.add(token);
                    }
                    if (!haycomillas) { // Evitar que considere lo que está entre comillas
                        if (!palabra.equals("")) { // Aquí nos aseguramos de que no tome cadenas vacías
                            if (tDatos.contains(palabra)) { // Es un tipo de dato
                                if (declaracion) { // Nos encontramos en el bloque de declaración?
                                    misTokens.add(new token("tipo_dato", palabra));
                                    cola.offer(palabra); // Para guardar cada dato en la tabla de simbolos
                                }    
                                else errLex.add("Error linea " + contLin + ", tipo de dato declarado fuera de lugar\n");
                            }
                            else if (palabra.equals ("mod") || palabra.equals("div")) {
                                misTokens.add(new token("op_arit", palabra));
                            }
                            else if (palReserv.contains(palabra)) { // Si es una palabra reservada
                                token token = new token("palabra_clave", palabra); // Creamos un token de palabra reservada
                                misTokens.add(token); // Añadir el token a la lista
                                if (palabra.equals("varinicio:")) declaracion = true; // Bloque de declaraciones
                                if (palabra.equals("varfin;")) declaracion = false;
                            }
                            else if (Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[az0-9]$|^[a-z][a-z0-9]{11}$", palabra)) { //Es un id
                                if (declaracion) { // Aun estamos en el bloque de declaración?
                                    misTokens.add(new token("id " + palabra, null));
                                    if (!cola.isEmpty()) { // Hay un tipo de dato guardado?
                                        tablaSimbolos.add(new simbolo(cola.poll(), palabra));// Guardamos tipo de dato y nombre
                                    }
                                    else if (!buscarSmb(tablaSimbolos, palabra)) { // Ya lo hemos guardado en la tabla de simbolos? (declara y asigna operaciones aritmeticas)
                                        errLex.add("Error: Sin declaración de tipo en la línea " + contLin  + "\n");
                                    }
                                } //Si ya no estamos en el bloque de declaración
                                else if (!buscarSmb(tablaSimbolos, palabra)) { // Ya lo hemos guardado en la tabla de simbolos?
                                    errLex.add(palabra + ". Error: Símbolo no encontrado en la línea " + contLin  + "\n");
                                }
                            }
                            else if (palabra.equals("=")) {
                                misTokens.add(new token("op_asign", palabra));
                            }
                            else if (Pattern.matches("[*]|[/]|[+]|[-]", palabra)) { //Operadores aritméticos
                                misTokens.add(new token("op_arit", palabra));
                            }
                            else if (Pattern.matches("<>|==|<=|>=|<|>", palabra)) {//Operadores relacionales
                                misTokens.add(new token("op_relac", palabra));
                            }
                            else if (Pattern.matches("^[&][&]|[\\|][\\|]$", palabra)) { //Operadores lógicos
                                misTokens.add(new token("op_log", palabra));
                            }
                            else if (Pattern.matches("[(]|[)]|[;]|[:]", palabra)) { //Signos de puntuación
                                misTokens.add(new token("signo", palabra));
                            }
                            else if (Pattern.matches("^[\\d]+$|^[\\d]+\\.[\\d]+$", palabra)) { //Constante numérica
                                misTokens.add(new token("num_const", palabra));
                            }
                            else if (!Pattern.matches(".*\".*", palabra)) {
                                errLex.add(palabra + ". Error léxico en fila: " + contLin + "\n");
                            }
                        }
                    }
                }
                fin = -1; // Para que se vuelva a habilitar el matAlf
                String remplazo = linea.replaceAll("\\s+", " ");  // Reduce los espacios en blanco
                if (!linea.trim().isEmpty()) { // Si la linea no esta vacia
                    indices.add(contLin); // Guarda los indices de las lineas que sí tienen info
                    if (remplazo.charAt(0) == ' ') remplazo = remplazo.replaceFirst(" ", ""); // Elimina el espacio en blanco del comienzo y del final
                    if (remplazo.charAt(remplazo.length()-1) == ' ') remplazo = remplazo.replaceAll("\\s+$", "");
                    contenido.append(remplazo).append("\n"); // Para el archivo de salida
                }
            }
            
            lector.close(); // Cierra el archivo
            contenido.append("\n");
            for (String err : errLex) { // Agregar errores al final del archivo de salida
                contenido.append(err);
            } //Creamos un nuevo archivo de salida y le agregamos el contenido
            File nuevoF = new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
            FileWriter archivoSalida = new FileWriter(nuevoF);
            BufferedWriter escritor = new BufferedWriter(archivoSalida);
            escritor.write(contenido.toString());
            escritor.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        
        if (!hayErrorLex) { //Analisis sintáctico
            try {
                FileReader archivoSalida = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
                BufferedReader lector = new BufferedReader(archivoSalida); // Crea un BufferedReader para leer líneas

                while ((linea = lector.readLine()) != null) {
                    contLin++; // Cuenta cada línea
                    String[] palabras = linea.split(" "); 
                    
                    if (!etiquetas.contains(linea)) {
                        
                        switch (palabras[0]) {
                            case "imp":
                                if (!Pattern.matches(imp, linea)) errSintac.add("Error sintáctico en la linea " + contLin); break;
                            case "si":
                                if (!Pattern.matches(si, linea)) errSintac.add("Error sintáctico en la linea " + contLin); break;
                            case "para":
                                if (!Pattern.matches(para, linea)) errSintac.add("Error sintáctico en la linea " + contLin); break;
                            case "mientras":
                                if (!Pattern.matches(mientras, linea)) errSintac.add("Error sintáctico en la linea " + contLin); break;
                            case tipo: 
                                System.out.println(linea); break;
                            default:
                                
                                if (palabras.length >= 3) 
                                if (palabras[3].equals("esc")) {
                                    if (Pattern.matches(esc, linea)) System.out.println(linea);
                                }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else {
            System.out.println("\n---------ERRORES---------");
            for (String e: errLex) {
                System.out.println(e);
            }
        }
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        AbrirArchivo(ruta);
        /*System.out.println("\n---------TOKENS---------");
        for (token t : misTokens) {
            System.out.println("Nombre: " + t.nombre + ", Valor: " + t.valor + "\n");
        }
        
        System.out.println("\n---------SIMBOLOS---------");
        for (simbolo s: tablaSimbolos) {
            System.out.println("Tipo: " + s.tipo + ", Nombre: " + s.nombre);
        }
*/ 
    }
}