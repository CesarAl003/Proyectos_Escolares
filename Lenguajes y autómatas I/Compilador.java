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

public class Compilador {
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
    
    public static void AbrirArchivo (String ruta) {
        List<String> palReserv = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        List<String> tDatos = Arrays.asList("ent", "dec", "cad");
        List<String> etiquetas = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "sino:", "finsi;" , "finpara;", "finmientras;");
        List<String> errLex = new ArrayList<>();
        List<String> errSintac = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int contLin = 0, inicio = 0, fin = -1;
        String linea;
        boolean declaracion = false, bloque = false, haycomillas = false;
        StringBuilder contenido = new StringBuilder(); //Para editar el archivo
        List<token> misTokens = new ArrayList<>(); //Mis tokens
        List<simbolo> tablaSimbolos = new ArrayList<>();
        Queue<String> cola = new LinkedList<>();

        try { // Abre el archivo para lectura77
            FileReader archivoFuente = new FileReader(ruta);
            BufferedReader lector = new BufferedReader(archivoFuente); // Crea un BufferedReader para leer líneas

            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea
                Matcher comillas = Pattern.compile("\"").matcher(linea);
                
                if (comillas.find()) { // Si hay comillas en la linea
                    inicio = comillas.end(); // Guarda el inicio y el fin
                    if (comillas.find()) fin = comillas.start();
                    else errLex.add("Error sintáctico en la línea " + contLin  + ", faltan comillas\n");
                }
                
                String[] palabras = linea.split(" ");
                for (String palabra : palabras) { //System.out.println(palabra);
                    if (fin != -1)
                    if (palabra.equals("\"")) {
                        haycomillas = !haycomillas;
                        if (!haycomillas) misTokens.add(new token("const_str", linea.substring(inicio, fin)));
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
                            else if (!Pattern.matches(".*\".*", palabra)) { // No coincidió con ningún patron
                                errLex.add(palabra + ". Error léxico en fila: " + contLin + "\n");
                            }
                        }
                    }
                }
                haycomillas = false;
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
            if (!errLex.isEmpty()) {
                contenido.append("\n");
                for (String err : errLex) { // Agregar errores al final del archivo de salida
                    contenido.append(err);
                } 
            }//Creamos un nuevo archivo de salida y le agregamos el contenido
            File nuevoF = new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
            FileWriter archivoSalida = new FileWriter(nuevoF);
            BufferedWriter escritor = new BufferedWriter(archivoSalida);
            escritor.write(contenido.toString());
            escritor.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        //Analisis sintáctico
        palReserv = Arrays.asList("ent", "dec", "cad", "inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        String digito = "0|1|2|3|4|5|6|7|8|9";
        String letra = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z";
        String tipo = "ent|dec|cad";
        String op_arit = "\\*|/|\\+|-|mod|div";
        String op_rel = ">=|<=|==|<>|<|>";
        String op_log = "(&&)|(\\|\\|)";
        String cte_ent = "(" + digito + ")+";
        String cte_dec = cte_ent + "\\." + cte_ent;
        String cte_cad = "\"(.*?)\"";
        String cte = "(" + cte_ent + ")" + "|" + "(" + cte_dec + ")" + "|" + "(" + cte_cad + ")";
        String id = "(?!" + String.join("|", palReserv) + ")(" + letra + ")(_*(" + letra + "|" + digito + ")+)*"; 
        String num = cte_ent + "|" + id;
        String operando = id + "|" + cte;
        String condicional = "((" + operando + ") (" + op_rel + ") (" + operando + "))( (" + op_log + ") (" + operando + ") (" + op_rel + ") (" + operando + "))*" ;
        String operacion = "((" + operando + ") (" + op_arit + ") (" + operando + "))( (" + op_arit + ") (" + operando + "))*";
        String expresion = "(" + operando + ")( \\+ (" + operando + "))*";
        String asign = "(" + id + ") = (" + operando + ") ;|(" + id + ") = (" + operacion + ") ;";
        String imp = "imp \\( (" + expresion + ") \\) ;";
        String esc = id + " = " + "esc \\( (" + cte_cad + ") \\) ;";
        String si = "si \\( (" + condicional + ") \\) :";
        String para = "para (" + id + ") = (" + num + ") hasta (" + num + ")( contando (" + num + "))? :";
        String mientras = "mientras \\( (" + condicional + ") \\) hacer:";
        String declara = "(" + tipo + ") ((" + id + ") ;|(" + asign +"))";
      
        if (errLex.isEmpty()) { System.out.println(declara);
            contLin = 0;
            try {
                FileReader archivoSalida = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
                BufferedReader lector = new BufferedReader(archivoSalida); // Crea un BufferedReader para leer líneas

                while ((linea = lector.readLine()) != null) {
                    contLin++; // Cuenta cada línea
                    if (linea.equals("inicio:")) bloque = true;
                    if (linea.equals("fin;")) bloque = false;
                    else if (bloque) {
                        if (!Pattern.matches(imp, linea))
                        if (!Pattern.matches(esc, linea))
                        if (!Pattern.matches(asign, linea))
                        if (!Pattern.matches(si, linea))
                        if (!Pattern.matches(para, linea))
                        if (!Pattern.matches(mientras, linea))
                        if (!Pattern.matches(operacion, linea))
                        if (!etiquetas.contains(linea))
                        if (!Pattern.matches(declara, linea))
                        if (!Pattern.matches(mientras, linea))
                            errSintac.add("Error sintáctico en la linea " + indices.get(contLin - 1));
                    }
                    else {
                        errSintac.add("Error: Codigo fuera de bloque en la linea " + contLin);
                    }
                }
                for (String e: errSintac) {
                    System.out.println(e);
                }
                lector.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else {
            for (String e: errLex) {
                System.out.println(e);
            }
        }/*
        System.out.println("\n---------TOKENS---------");
        for (token t : misTokens) {
            System.out.println("Nombre: " + t.nombre + ", Valor: " + t.valor + "\n");
        }
        
        System.out.println("\n---------SIMBOLOS---------");
        for (simbolo s: tablaSimbolos) {
            System.out.println("Tipo: " + s.tipo + ", Nombre: " + s.nombre);
        } */
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        AbrirArchivo(ruta);
        

    }
}