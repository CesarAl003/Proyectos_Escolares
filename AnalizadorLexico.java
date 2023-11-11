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
    
    static List<String> palReserv = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "finpara;", "mod", "div", "true", "false");
    static List<String> tDatos = Arrays.asList("ent", "dec", "cad", "guti");
    static List<String> lstErrores = new ArrayList<>();
    static int contLin = 0, inicio = 0, fin = -1;
    static String linea, const_string, error;
    static boolean declaracion = false, hayError = false, haycomillas = false;
    static StringBuilder contenido = new StringBuilder(); //Para editar el archivo
    static List<token> misTokens = new ArrayList<>(); //Mis tokens
    static List<simbolo> tablaSimbolos = new ArrayList<>();
    static Queue<String> cola = new LinkedList<>();

    public static void AbrirArchivo (String ruta) {
        try { // Abre el archivo para lectura
            FileReader archivoFuente = new FileReader(ruta);
            // Crea un BufferedReader para leer líneas
            BufferedReader lector = new BufferedReader(archivoFuente);

            while ((linea = lector.readLine()) != null) { //Mi alfabeto:
                contLin++; // Cuenta cada línea
                Matcher comillas = Pattern.compile("\"").matcher(linea);
                
                if (comillas.find()) { //Si hya comillas en la linea
                    inicio = comillas.end(); //Guarda el inicio y el fin
                    if (comillas.find()) {
                        fin = comillas.start();
                        const_string = linea.substring(inicio, fin);
                    }
                    else  {
                        error = "Error léxico en la línea " + contLin  + ", faltan comillas";
                        lstErrores.add(error);
                    }
                }
                
                String[] palabras = linea.split(" ");
                for (String palabra : palabras) {         
                    //System.out.println(palabra);
                    if (palabra.equals("\"")) {
                        haycomillas = !haycomillas;
                        token token = new token("const_str", const_string);
                        if (!haycomillas) misTokens.add(token);
                    }
                    if (!haycomillas) { //Para evitar que revise lo que hay dentro de las comillas
                        if (!palabra.equals("")) { //Aquí nos aseguramos de que no tome cadenas vacías
                            if (tDatos.contains(palabra)) { //Es un tipo de dato
                                if (declaracion) { //Nos encontramos en el bloque de declaración?
                                    token token = new token("tipo_dato", palabra);
                                    misTokens.add(token);
                                    cola.offer(palabra); //Para guardar cada dato en la tabla de simbolos
                                }    
                                else {
                                    error = "Error linea " + contLin + ", tipo de dato declarado fuera de lugar" + "\n";
                                    lstErrores.add(error);
                                }
                            }
                            else if (palabra.equals ("mod") || palabra.equals("div")) { //Estas palabras reservadas en realidad son operadores aritméticos
                                token token = new token("op_arit", palabra);
                                misTokens.add(token);
                            }
                            else if (palReserv.contains(palabra)) { //Si es una palabra reservada
                                token token = new token("palabra_clave", palabra); //Creamos un token de palabra reservada
                                misTokens.add(token); //Añadirmos el token a la lista
                                if (palabra.equals("varinicio:")) declaracion = true; //Bloque de declaraciones
                                if (palabra.equals("varfin;")) declaracion = false;
                            }
                            else if (Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[az0-9]$|^[a-z][a-z0-9]{11}$", palabra)) { //Es un id
                                if (declaracion) { //Aun estamos en el bloque de declaración
                                    token token = new token("id " + palabra, null); 
                                    misTokens.add(token);
                                    if (!cola.isEmpty()) { //Si la cola no esta vacía, quiere decir que hay un tipo de dato guardado
                                        simbolo smb = new simbolo(cola.poll(), palabra); //Guardamos tipo de dato, nombre
                                        tablaSimbolos.add(smb);//Meter id a la tabla de simbolos (GUARDADO)
                                    }
                                    else {
                                        error = "Error: Sin declaración de tipo en la línea " + contLin  + "\n";
                                        lstErrores.add(error);
                                    }
                                } //Si ya no estamos en el bloque de declaración
                                else if (!buscarSmb(tablaSimbolos, palabra)) { //Debe comprobar si el simbolo esta guardado en la tabla de simbolos
                                    error = palabra + ". Error: Símbolo no encontrado en la línea " + contLin  + "\n";
                                    lstErrores.add(error);
                                }
                            }
                            else if (palabra.equals("=")) {
                                token token = new token("op_asign", palabra);
                                misTokens.add(token);
                            }
                            else if (Pattern.matches("[*]|[/]|[+]|[-]", palabra)) { //Operadores aritméticos
                                token token = new token("op_arit", palabra);
                                misTokens.add(token);
                            }
                            else if (Pattern.matches("[<]|[>]|[==]|[<=]|[>=]", palabra)) {//Operadores relacionales
                            token token = new token("op_relac", palabra);
                            misTokens.add(token);
                            }
                            else if (Pattern.matches("^[&][&]|[\\|][\\|]|[!]$", palabra)) { //Operadores lógicos
                                token token = new token("op_log", palabra);
                                misTokens.add(token);
                            }
                            else if (Pattern.matches("[(]|[)]|[;]|[:]", palabra)) { //Signos de puntuación
                                token token = new token("signo", palabra);
                                misTokens.add(token);
                            }
                            else if (Pattern.matches("^[\\d]+$|^[\\d]+\\.[\\d]+$", palabra)) { //Constante numérica
                                token token = new token("num_const", palabra);
                                misTokens.add(token);
                            }
                            else if (!Pattern.matches(".*\".*", palabra)) {
                                error = palabra + ". Error léxico en fila: " + contLin + "\n";
                                lstErrores.add(error);
                            }
                        }
                        
                    }
                }
                fin = -1; //Para que se vuelva a habilitar el matAlf
                String reemplazo = linea.replaceAll("\\s+", " ");
                //Guarda cada linea si no esta vacía
                if (!linea.trim().isEmpty()) contenido.append(reemplazo).append("\n");
            }
            
            // Cierra el archivo
            lector.close();
            //Agregar errores al final del archivo de salida
            contenido.append("\n");
            for (String err : lstErrores) {
                contenido.append(err);
            }
            //Creamos un nuevo archivo de salida y le agregamos el contenido
            File nuevoF = new File ("Salida.txt");
            FileWriter archivoSalida = new FileWriter(nuevoF);
            BufferedWriter escritor = new BufferedWriter(archivoSalida);
            escritor.write(contenido.toString());
            escritor.close();
        } catch (Exception e) {
            System.out.println(e);
        } 
        //*System.out.println("\n---------TOKENS---------");
        for (token t : misTokens) {
            System.out.println("Nombre: " + t.nombre + ", Valor: " + t.valor + "\n");
        }
        System.out.println("\n---------SIMBOLOS---------");
        for (simbolo s: tablaSimbolos) {
            System.out.println("Tipo: " + s.tipo + ", Nombre: " + s.nombre);
        }
        System.out.println("\n---------ERRORES---------");
        
        for (String e: lstErrores) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        //Ruta del archivo
        String ruta = "Fuente.txt";
        AbrirArchivo(ruta);
        
    }
}