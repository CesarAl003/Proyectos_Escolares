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
    public static class simbolo {
        public String tipo;
        public String nombre;

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

    public static boolean buscarSmb (List<simbolo> tablaSimbolos, String nombre) { // Utilizar los metodos de arraylist en lugar de este metodo
        for (simbolo smb : tablaSimbolos) { //Comprobar si el símbolo esta guardado
            if (smb.nombre.equals(nombre)) return true;
        }
        return false;
    }
    
    public static void AbrirArchivo (String ruta) {
        List<String> palReserv = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        List<String> tDatos = Arrays.asList("ent", "dec", "cad");
        List<String> etiquetas = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "sino:", "finsi;" , "finpara;", "finmientras;");
        List<error> tabErr = new ArrayList<>();
        String miAlf = "[*]|[/]|[+]|[-]|[=]|[<>]|[==]|[<=]|]>=]|[<|>]|[(]|[)]|[;]|[:]|(^[&][&]|[\\|][\\|]$)|(^[\\d]+$|^[\\d]+\\.[\\d]+$)|(.*\\\".*)";
        List<Integer> indices = new ArrayList<>(); //Para guardar las lineas que si contienen información
        int contLin = 0;
        String linea;
        boolean declaracion = false, bloque = false, haycomillas = false;
        List<simbolo> tabSim = new ArrayList<>();
        Queue<String> cola = new LinkedList<>();

        try { // Abre el archivo para lectura
            BufferedReader lector = new BufferedReader(new FileReader(ruta)); // Crea un BufferedReader para leer líneas
            // Para generar el archivo de salida
            File salida = new File ("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
            BufferedWriter escritor = new BufferedWriter(new FileWriter(salida));

            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea
                // Si la linea esta vacia, el string palabras es un array de cero posiciones (vacio), sino, es un array de palabras separadas por uno o más espacios en blanco
                String[] palabras = linea.trim().isEmpty() ? new String[0] : linea.trim().split("\\s+");

                for (String palabra : palabras) { //System.out.println(palabra);
                    if (palabra.equals("\"")) haycomillas = !haycomillas;

                    if (!haycomillas) { // Evitar que considere lo que está entre comillas
                        if (tDatos.contains(palabra)) { // Es un tipo de dato
                            if (declaracion) { // Nos encontramos en el bloque de declaración?
                                cola.offer(palabra); // Para guardar cada dato en la tabla de simbolos
                            }    
                            else tabErr.add(new error("syn03", contLin, palabra));
                        }
                        else if (palabra.equals ("mod") || palabra.equals("div")) {
                            
                        }
                        else if (palReserv.contains(palabra)) { // Si es una palabra reservada
                            
                            if (palabra.equals("varinicio:")) declaracion = true; // Bloque de declaraciones
                            if (palabra.equals("varfin;")) declaracion = false;
                        }
                        else if (Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$", palabra)) { //Es un id
                            if (declaracion) { // Aun estamos en el bloque de declaración?
                                
                                if (!cola.isEmpty()) { // Hay un tipo de dato guardado?
                                    tabSim.add(new simbolo(cola.poll(), palabra));// Guardamos tipo de dato y nombre
                                }
                                else if (!buscarSmb(tabSim, palabra)) { // Ya lo hemos guardado en la tabla de simbolos? (declara y asigna operaciones aritmeticas)
                                    tabErr.add(new error("syn04", contLin, palabra));
                                }
                            } //Si ya no estamos en el bloque de declaración
                            else if (!buscarSmb(tabSim, palabra)) { // Ya lo hemos guardado en la tabla de simbolos?
                                tabErr.add(new error("lex01", contLin, palabra)); //Palabra es el simb no encontrado
                            }
                        }
                        else if (!Pattern.matches(miAlf, palabra)) { // No coincidió con ningún patron
                            tabErr.add(new error("lex01", contLin, palabra));
                        }
                    }
                }
                haycomillas = false;
                
                if (!linea.trim().isEmpty()) { // Si la linea no esta vacia
                    indices.add(contLin); // Guarda los indices de las lineas que sí tienen info
                    escritor.write(linea.trim().replaceAll("\\s+", " ") + "\n"); // Elimina todos los espacios en blanco y escribe la linea
                }
            }
            
            lector.close(); // Cierra el archivo    
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
        String declara = "((" + tipo + ") ((" + id + ") ;|(" + asign +")))?";
        String sentencias = "((" + asign + ")|(" + imp + ")|(" + esc + ")|(" + si + ")|(sino:)|(" + para + ")|(" + mientras + ")|(finsi;)|(finpara;)|(finmientras;)|(" + declara +")|(varinicio:)|(varfin;))*";
      
        if (tabErr.isEmpty()) {
            contLin = 0;
            try {
                FileReader archivoSalida = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
                BufferedReader lector = new BufferedReader(archivoSalida); // Crea un BufferedReader para leer líneas

                while ((linea = lector.readLine()) != null) {
                    contLin++; // Cuenta cada línea
                    if (linea.equals("inicio:")) bloque = true;
                    else if (linea.equals("fin;")) bloque = false;
                    else if (bloque) {
                        if (!Pattern.matches(sentencias, linea)) {
                            tabErr.add(new error("syn02", indices.get(contLin - 1), linea));
                        }
                    }
                    else {
                        tabErr.add(new error("syn01", contLin, linea));
                    }
                }

                lector.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println("\n---------Errores---------");
        for (error e: tabErr) {
            System.out.println("Error: " + e.codigo + ", linea: " + e.linea + ", linea: " + e.token + "\n");
        }
        /*
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