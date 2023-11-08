import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Funciona bien, pero depende de los espacios y no valida constantes string
public class AnalizadorLexico {
    static List<String> palReserv = Arrays.asList("inicio", "fin", "varinicio", "varfin", "imp", "esc", "ent", "declarg", "dec", "cad", "guti", "si", "sino", "repetir", "hacer", "mod", "div");
    public static void main(String[] args) {
        String cadena = "ent papu = 3 * 3 -9;";
        String palabra;

        Pattern patAlf = Pattern.compile("[a-z0-9\\w\\-\\*/+-<>=&\\|!()\\;]+"); //Todo el alfabeto?
        Matcher matAlf = patAlf.matcher(cadena);
        for (int i = 0; i < cadena.length(); i++) {
            System.out.println("index: " + i);
            System.out.println(cadena.charAt(i));
                if (matAlf.find()) { //Encontramos una palabra reservada o un id
                    palabra = matAlf.group();
                    System.out.println(palabra);
                    if (palReserv.contains(palabra)) { //Si es una palabra reservada
                        System.out.println(palabra + " es una palabra reservada.");
                    }
                    else if (Pattern.matches("^[a-z]{0,12}$|^[a-z]{1}[\\w\\-]{0,10}[a-z0-9]$|^[a-z][a-z0-9]{11}$", palabra)) {
                        System.out.println(palabra + " es un id.");
                    }
                    else if (Pattern.matches("[*]|[/]|[+]|[-]", palabra)) {
                        System.out.println(palabra + " es un operador aritmético.");
                    }
                    else if (Pattern.matches("[<]|[>]|[=]|[==]|[<=]|[>=]", palabra)) {
                        System.out.println(palabra + " es un operador relacional.");
                    }
                    else if (Pattern.matches("^[&][&]|[\\|][\\|]|[!]$", palabra)) {
                        System.out.println(palabra + " es un operador lógico.");
                    }
                    else if (Pattern.matches("[(]|[)]|[;]|[:]", palabra)) {
                        System.out.println(palabra + " es un signo de puntuación.");
                    }
                    else if (Pattern.matches("^[\\d]+$|^[\\d]+.[\\d]+$", palabra)) {
                        System.out.println(palabra + " es una constante numérica.");
                    }
                    else {
                        System.out.println("Error en fila: " + 0 + ", columna: " + i);
                    }
                        //System.out.println("Error en fila: " + 0 + ", columna: " + i);
                i = matAlf.end() - 1;
                }
                //if (Pattern.matches(cadena.charAt(i), palabra))
            }
            
        
    }   
}

/* 

        String cadena;
        System.out.print("Cadena: "); cadena = leeLinea();
        
        //Palabras reservadas
        List<String> palReserv = Arrays.asList("imp", "esc", "ent", "declarg", "dec", "cad", "guti", "si", "sino", "repetir", "hacer", "mod", "div");
        String regexPalReserv = "\\b(" + String.join("|", palReserv) + ")\\b";
        Pattern patPalReserv = Pattern.compile(regexPalReserv);
        Matcher matPalreserv = patPalReserv.matcher(cadena);

        //Identificador
        Pattern patID = Pattern.compile("^([A-Z])$|^([A-Z])([\\w-]{0,10})([A-Za-z0-9])$|^([A-Z])([A-Za-z0-9])$");
        Matcher matID = patID.matcher(cadena);

        //Operadores
        //aritméticos 
        Pattern patOpArit = Pattern.compile("[*]|[/]|[+]|[-]");
        Matcher matOpArit= patOpArit.matcher(cadena);
        //relacionales. Cómo diferenciar si debe ser de asignación '=' o de comparación '=='
        Pattern patOpRel = Pattern.compile("[<]|[>]|[=]");
        Matcher matOPRel = patOpRel.matcher(cadena);
        //lógicos
        Pattern patOpLog = Pattern.compile("[&]|[\\|]|[!]");
        Matcher matOpLog = patOpLog.matcher(cadena);
        //Constantes
        Pattern patConstLit = Pattern.compile("^\"[ -~|ñÑ¿]*\"$ "); //Constante literal
        Matcher matConstLit = patConstLit.matcher(cadena);
        Pattern patConstNum = Pattern.compile("^[\\d]+$|^[\\d]+.[\\d]+$"); //Constante numero
        Matcher matConstNum = patConstNum.matcher(cadena);
        //Signos de puntuación
        Pattern patSign = Pattern.compile("[(]|[)]|[;]"); 
        Matcher matSign = patSign.matcher(cadena);

        if (matPalreserv.matches()) System.out.println("exitosa");
        else System.out.println("No encontrado");
*/