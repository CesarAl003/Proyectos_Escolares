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
        public boolean esNulo;

        public simbolo () {
            this.tipo = "";
            this.nombre = "";
            this.esNulo = true;
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

    static List<simbolo> tabSim = new ArrayList<>();
    public static boolean buscarSmb (String nombre) { // Utilizar los metodos de arraylist en lugar de este metodo
        for (simbolo smb : tabSim) { //Comprobar si el símbolo esta guardado
            if (smb.nombre.equals(nombre)) return true;
        }
        return false;
    }
    
    public static void AbrirArchivo (String ruta) {
        List<String> palReserv = Arrays.asList("ent", "dec", "cad", "inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        List<error> tabErr = new ArrayList<>();
        String miAlf = "[*]|[/]|[+]|[-]|[=]|[<>]|[==]|[<=]|]>=]|[<|>]|[(]|[)]|[;]|[:]|(^[&][&]|[\\|][\\|]$)|(^[\\d]+$|^[\\d]+\\.[\\d]+$)";
        List<Integer> indices = new ArrayList<>(); //Para guardar las lineas que si contienen información
        int contLin = 0;
        String linea;
        boolean declaracion = false, bloque = false, haycomillas = false;
        
        simbolo smb = new simbolo();

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
                        else if (Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$", palabra)) { //Es un id
                            if (declaracion) { // Aun estamos en el bloque de declaración?
                                if (buscarSmb(palabra)) { //No se admiten ID con nombre repetido
                                    tabErr.add(new error("syn05", contLin, palabra));
                                }
                                else if (!smb.tipo.equals("")) { // El ID tiene un tipo?
                                    smb.nombre = palabra; // Si tiene un valor asignado, marcamos que no es nulo
                                    if (Pattern.matches(".*=.*", linea)) smb.esNulo = false;
                                    tabSim.add(smb); // Una vez completo, guardamos el simbolo y lo limpiamos
                                    smb = new simbolo();
                                }
                                else tabErr.add(new error("syn04", contLin, palabra)); // Falta el tipo de dato
                            } // Si ya no estamos en el bloque de declaración
                            else if (!buscarSmb(palabra)) { // Ya lo hemos guardado en la tabla de simbolos?
                                tabErr.add(new error("lex01", contLin, palabra)); // Palabra es el simb no encontrado
                            }
                        }
                        else if (!Pattern.matches(miAlf, palabra)) { // No coincidió con ningún patron
                            tabErr.add(new error("lex01", contLin, palabra));
                        }
                    }
                }
                if (haycomillas) { // Significa que no se cerraron las comillas en la misma linea
                    tabErr.add(new error("syn06", contLin, ""));
                    haycomillas = false;
                }

                
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
        
        String letra = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z";
        String digito = "0|1|2|3|4|5|6|7|8|9";
        String tipo = "ent|dec|cad";
        String op_arit = "\\*|/|\\+|-|mod|div";
        String op_rel = ">=|<=|==|<>|<|>";
        String op_log = "(&&)|(\\|\\|)";
        String id = "(?!" + String.join("|", palReserv) + ")(" + letra + ")(_*(" + letra + "|" + digito + ")+)*"; 
        String cte_ent = "(" + digito + ")+";
        String cte_dec = cte_ent + "\\." + cte_ent;
        String cte_cad = "\"([^\"]*)\"";
        String cte = "(" + cte_ent + ")" + "|" + "(" + cte_dec + ")" + "|" + "(" + cte_cad + ")";
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
        try {
            FileReader errores = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\errDesc.txt");
            BufferedReader lector = new BufferedReader(errores); // Crea un BufferedReader para leer líneas
           
            List<String> errDesc = new ArrayList<>();
            String error;

            while ((error = lector.readLine()) != null) {
                errDesc.add(error);
            }
            lector.close();
            System.out.println("\n---------Errores---------");
            for (error e: tabErr) {
                for (String ln : errDesc) {
                    if (e.codigo.equals(ln.substring(0,5))){
                        System.out.println("Error: " + e.codigo + ", linea: " + e.linea + ": " + e.token + "\n" + ln.substring(6, ln.length()) + "\n");
                    }
                }               
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
        
        System.out.println("\n---------SIMBOLOS---------");
        for (simbolo s: tabSim) {
            System.out.println(s.nombre + " de tipo " + s.tipo + ", Es nulo: " + s.esNulo);
        }  
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        AbrirArchivo(ruta);
    }
}

/*import java.io.BufferedReader; //Usa split mediante los espacios en blanco
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
        public String valor;

        public simbolo (String tipo, String nombre, String valor) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.valor = valor;
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
        List<String> palReserv = Arrays.asList("ent", "dec", "cad","inicio:", "fin;", "varinicio:", "varfin;", "imp", "esc", "si", "sino:", "finsi;" , "para", "hasta", "contando", "finpara;", "mientras", "hacer:", "finmientras;", "mod", "div");
        List<error> tabErr = new ArrayList<>();
        String miAlf = "$[*]|[/]|[+]|[-]|[=]|[<>]|[==]|[<=]|]>=]|[<|>]|[(]|[)]|[;]|[:]|(^[&][&]|[\\|][\\|]$)|(^[\\d]+$|^[\\d]+\\.[\\d]+)|(.*\\\".*)^";
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
                String[] tokens = linea.trim().isEmpty() ? new String[0] : linea.trim().split("\\s+");

                for (String token : tokens) { //System.out.println(palabra);
                    if (token.equals("\"")) haycomillas = !haycomillas;
                    
                    else if (!haycomillas) { // Evitar que considere lo que está entre comillas
                        if (!Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[a-z0-9\\_]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$", token))  //Es un id?
                        if (!palReserv.contains(token)) // Es una palabra reservada? 
                        if (!Pattern.matches(miAlf, token))  // No coincidió con ningún patron?
                            tabErr.add(new error("lex01", contLin, token));
                    } 
                
                }
                if (!linea.trim().isEmpty()) { // Si la linea no esta vacia
                    indices.add(contLin); // Guarda los indices de las lineas que sí tienen info
                    escritor.write(linea.trim().replaceAll("\\s+", " ") + "\n"); // Elimina todos los espacios en blanco y escribe la linea
                }
                haycomillas = false; // Este podría ser un detalle...
            }
            
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
        String sentencias = "((" + asign + ")|(" + imp + ")|(" + esc + ")|(" + si + ")|(sino:)|(" + para + ")|(" + mientras + ")|(finsi;)|(finpara;)|(finmientras;)|(varinicio:)|(varfin;))*";
      
        if (tabErr.isEmpty()) {
            contLin = 0;
            try {
                FileReader archivoSalida = new FileReader("C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt");
                BufferedReader lector2 = new BufferedReader(archivoSalida); // Crea un BufferedReader para leer líneas
                while ((linea = lector2.readLine()) != null) {
                    contLin++; // Cuenta cada línea
                    if (linea.equals("inicio:")) bloque = true;
                    else if (linea.equals("fin;")) bloque = false;
                    else if (linea.equals("varinicio:")) declaracion = true;
                    else if (linea.equals("varfin;")) declaracion = false;
                    else if (bloque) {
                        if (declaracion) {
                            if (Pattern.matches(declara, linea)) {
                                String[] tokens = linea.split("\\s+");
                                if (linea.contains("\"")) tabSim.add(new simbolo(tokens[0], tokens[1], linea.substring(10, linea.length()-4)));
                                else if (tokens.length > 3) tabSim.add(new simbolo(tokens[0], tokens[1], tokens[3]));
                                else tabSim.add(new simbolo(tokens[0], tokens[1], ""));
                            }
                            else {
                                //System.out.println("Solo declaraciones en el bloque de declaración");
                            }
                        }
                        else if (Pattern.matches(declara, linea)) {
                            //System.out.println("Declaración fuera del bloque correspondiente"); 
                        }
                        else if (!Pattern.matches(sentencias, linea)) {
                            tabErr.add(new error("syn02", indices.get(contLin - 1), linea));
                        }
                        
                    }
                    else {
                        tabErr.add(new error("syn01", contLin, linea));
                    }
                }
                lector.close();
                lector2.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println("\n---------Errores---------");
        for (error e: tabErr) {
            System.out.println("Error: " + e.codigo + ", linea: " + e.linea + ", linea: " + e.token + "\n");
        }

        System.out.println("\n---------SIMBOLOS---------");
        for (simbolo s: tabSim) {
            System.out.println("Tipo: " + s.tipo + ", Nombre: " + s.nombre + ", valor: " + s.valor);
        }
    }

    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Fuente.txt";
        AbrirArchivo(ruta);
    }
} */