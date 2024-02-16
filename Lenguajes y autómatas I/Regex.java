import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
    static String cadena;
    static String regex;
    static boolean isMatched;

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
    public static String leeLinea () {
        String str = null;
        try {
           str = br.readLine(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    //Búsqueda
    public static void Ejemplo1 () { 
        cadena = "Esto es Chaitanya " + "de Beginnersbook.com.";
        regex = ".*Begi.*";
        //Busca 'Begi' en la cadena
        isMatched = Pattern.matches(regex, cadena);
        if (isMatched) System.out.println("Busqueda exitosa");
        else System.out.println("No encontrado"); 
    }

    //Búsqueda 
    public static void Ejemplo2 () {
        cadena = "This is a tutorial Website!";
        //la expresión .* se utiliza para cero y más caracteres. 
        //Por lo que permite cero o más caracteres al principio y al final del String «tutorial»
        regex = ".*tutorial.*";
        isMatched = Pattern.matches(regex, cadena);
        if (isMatched) System.out.println("Busqueda exitosa");
        else System.out.println("No encontrado"); 
    }

    //Busqueda indiferente a mayúsculas y minúsculas
    public static void Ejemplo3 () {
        cadena = "This is a tutorial Website!";
        regex = ".*tuToRiAl.*";
        //Pueden agregarse diferentes parametros aquí. CASE_INSENSITIVE 
        //ignora las mayúsculas y minúsculas al buscar
        Pattern pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher mat = pat.matcher(cadena);
        isMatched = mat.matches();
        if (isMatched) System.out.println("Busqueda exitosa");
        else System.out.println("No encontrado"); 
    }

    //Dividir la cadena en subcadenas a partir de un delimitador
    public static void Ejemplo4 () {
        cadena = "ThisIsChaitanya.ItISMyWebsite"; 
        //El delimitador es "is"
        String del = "is"; 
        Pattern pat = Pattern.compile(del, Pattern.CASE_INSENSITIVE); 
        //Utilización del método split
        String[] myStrings = pat.split(cadena);
        //Ciclo for-each
        for (String temp: myStrings) { 
            System.out.println("-" + temp); 
        } 
        System.out.println("Número de cadenas divididas: " + myStrings.length);
        //El segundo String dividido es nulo en la salida, devido a que hay dos 'is' 
        //juntos, no hay nada entre ellos.
    }

    //Múltiples coincidencias con find
    public static void Ejemplo5 () {
        cadena = "ZZZ AA PP AA QQQ AAA ZZ"; 
        String buscar = "AA"; 
        Pattern pat = Pattern.compile(buscar);
        Matcher mat = pat.matcher(cadena);
        //Buscamos todas las coincidencias
        while(mat.find()) { 
            //start y end devuelven el rango de la coincidencia [   )
            System.out.println("Encontrado entre: "+ mat.start() + " - " + mat.end()); 
        }
    }

    //Comparaciones de dos cadenas
    public static void Ejemplo6 () {
        // Devolvería true si la cadena coincide exactamente con "tom" 
        System.out.println(Pattern.matches("tom", "Tom")); //False 
        // [Tt] = T | t, es decir, un 'or'
        System.out.println(Pattern.matches("[Tt]om", "Tom")); //True 
        System.out.println(Pattern.matches("[Tt]om", "Tom")); //True 
        System.out.println(Pattern.matches("[tT]im | [jJ]in", "Tim")); //True 
        System.out.println(Pattern.matches("[tT]im|[jJ]in", "jin")); //True
        //Retorno falso ya que cualquiera de ellos puede estar en texto no ambos.
        System.out.println(Pattern.matches("[pqr]", "pq")); //False 
    }
    
    //Más ejemplos de expresiones regulares.
    public static void Ejemplo7 () {
        //Devuelve verdadero si la cadena contiene "abc" en cualquier lugar
        System.out.println(Pattern.matches(".*abc.*", "deabcpq")); //True 
        //Devuelve verdadero si la cadena no tiene un número al principio. 
        System.out.println( Pattern.matches("^[^d].*", "123abc")); //Falso 
        System.out.println( Pattern.matches("^[^d].*", "abc123")); //Verdadero
        //Devuelve verdadero si la cadena contiene tres letras 
        System.out.println(Pattern.matches("[a-zA-Z][a-zA-Z][a-zA-Z]", "aPz")); //Verdadero 
        System.out.println(Pattern.matches("[a-zA-Z][a-zA-Z][a-zA-Z]", "aAA")); //True 
        System.out.println(Pattern.matches("[a-zA-Z][a-zA-Z][a-zA-Z]", "apZx")); //False 
        // Devuelve verdadero si la cadena contiene 0 o más no-dígitos
        System.out.println(Pattern.matches("\\D*", "abcde")); //True
        System.out.println(Pattern.matches("\\D*", "abcde123")); //False 
        // ^ denota el comienzo de la línea mientras que $ denota el final de la línea 
        System.out.println(Pattern.matches("^Este$", "Esta es la Chaitanya")); //Falso
        System.out.println(Pattern.matches("^Este$", "Este")); // Verdadero 
        System.out.println(Pattern.matches("^Esta$", "Esta es la Chaitanya")); //Falso
    }
    
    //Comprobar si el String cadena contiene exactamente el patrón (matches) “abc”
    public static void Ejemplo8 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("abc");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Comprobar si el String cadena contiene “abc”
    public static void Ejemplo9 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile(".*abc.*");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Usando el método find
    public static void Ejemplo10 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("abc");
        Matcher mat = pat.matcher(cadena);
        if (mat.find()) System.out.println("Válido");
        else System.out.println("No Válido");
    }

    //. Comprobar si el String cadena empieza por “abc”
    public static void Ejemplo11 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("^abc.*");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Comprobar si el String cadena empieza por “abc” ó “Abc”
    public static void Ejemplo12 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("^[aA]bc.*");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
     }

     // Comprobar si el String  cadena  está formado por un mínimo de 5 letras mayúsculas o minúsculas y un máximo de 10.
    public static void Ejemplo13 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("[a-zA-Z]{5,10}");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Comprobar si el String cadena no empieza por un dígito
    public static void Ejemplo14 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("^[^\\d].*");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Comprobar si el String cadena no acaba con un dígito
    public static void Ejemplo15 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile(".*[^\\d]$");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Comprobar si el String cadena solo contienen los caracteres a ó b
    public static void Ejemplo16 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile("(a|b)+");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }
    
    //Comprobar si el String contiene un 1 y ese 1 no está seguido por un 2
    public static void Ejemplo17 () {
        System.out.print("Cadena: "); cadena = leeLinea();
        Pattern pat = Pattern.compile(".*1(?!2).*");
        Matcher mat = pat.matcher(cadena);
        if (mat.matches()) System.out.println("SI");
        else System.out.println("NO");
    }

    //Expresión regular para comprobar si un email es válido
    public static void Ejemplo18 () {String email;
        System.out.print("Introduce email: ");
        email = leeLinea();
        Pattern pat = Pattern.compile("^[\\w]+(\\.[\\w]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mat = pat.matcher(email);
        if(mat.find()) System.out.println("Correo Válido");
        else System.out.println("Correo No Válido");
    }

    //Subcadenas    
    public static void Ejemplo19 () {
        String str = "blanco­rojo:amarillo.verde_azul";
        String [] cadenas = str.split("[­:._]");
        for (int i = 0; i<cadenas.length; i++){
            System.out.println(cadenas[i]);
        }
    }
    //Subcadenas
    public static void Ejemplo20 () {
        String str = "esto es un ejemplo de como funciona split";
        String [] cadenas = str.split("(e[s|m])|(pl)");
        for (int i = 0; i<cadenas.length; i++) {
            System.out.println(cadenas[i]);
        }
    }

    //Valida el identificador de una variable en mi lenguaje
     public static void Ejemplo21 () {
        regex = "^([A-Z])$|^([A-Z])([\\w-]{0,10})([A-Za-z0-9])$|^([A-Z])([A-Za-z0-9])$";
        System.out.println("Ingrese un identificador de variable: ");
        cadena = leeLinea(); //ID a validar
        Pattern pat = Pattern.compile (regex); //Expresion regular
        Matcher mat = pat.matcher(cadena); //ID 

        if (mat.matches()) System.out.println("ID CORRECTO");
        else System.out.println("ID INCORRECTO");
        
        //Comprobar si mi ID contiene un guion
        String regex2 = "[\\-_]";
        pat = Pattern.compile (regex2);
        mat = pat.matcher(cadena);

        if (mat.find()) System.out.println("ID contiene guión");
        else  System.out.println("ID no contiene guión");
    }

    public static void Ejemplo22 () {
        String palabra = "a.";
        Pattern patAlf = Pattern.compile(".*\\p{L}.*"); //Clase de caracteres unicode
        Matcher matAlf;
        //Buscamos todas las coincidencias
        matAlf = patAlf.matcher(palabra);

        if (matAlf.matches()) System.out.println("palabra");
        else System.out.println("No palabra");

    }

    public static void main(String[] args) {
        String linea = ".   .";
        String reemplazo = linea.replaceAll("\\s+", " ");
        System.out.println(reemplazo);
        /* int op = 1;
        while (op != 0) {  
            Pattern pat = Pattern.compile("[0-9]|1[0-9]|2[02]");
            Matcher mat;
            do {
                System.out.println("Digite 0 para salir");
                System.out.print("Ingrese el número del ejemplo [1-22]: ");
                cadena = leeLinea();
                mat = pat.matcher(cadena);
            } while (!mat.matches());
            op = Integer.parseInt(cadena);
            switch (op) {
                case 1: Ejemplo1(); break;
                case 2: Ejemplo2(); break;
                case 3: Ejemplo3(); break;
                case 4: Ejemplo4(); break;
                case 5: Ejemplo5(); break;
                case 6: Ejemplo6(); break;
                case 7: Ejemplo7(); break;
                case 8: Ejemplo8(); break;
                case 9: Ejemplo9(); break;
                case 10: Ejemplo10(); break;
                case 11: Ejemplo11(); break;
                case 12: Ejemplo12(); break;
                case 13: Ejemplo13(); break;
                case 14: Ejemplo14(); break;
                case 15: Ejemplo15(); break;
                case 16: Ejemplo16(); break;
                case 17: Ejemplo17(); break;
                case 18: Ejemplo18(); break;
                case 19: Ejemplo19(); break;
                case 20: Ejemplo20(); break;
                case 21: Ejemplo21(); break;
                case 22: Ejemplo22(); break;
            }  
        }*/
    }
    /*

    static Scanner sc = new Scanner (System.in);
    //Mi regex
    static String regex = "^([A-Z])$|^([A-Z])([\\w-]{0,10})([A-Za-z0-9])$|^([A-Z])([A-Za-z0-9])$";
    //Regex de Frida que no se proque a mi no me funciona
    static String regex1 = "^[A-Z]{1}([\\w\\-]{0,10}[A-Za-z0-9]{1})?$/";
    //Buscar en mi Id un guion
    static String regex2 = "[\\-_]";
    static String prueba; //ID a validar

    public static Pattern pat;
    public static Matcher mat;

    
        while (true) {
            System.out.print("Ingresa tu variable: ");
            prueba = sc.next();
            
            pat = Pattern.compile (regex); //Expresion regular
            mat = pat.matcher(prueba); //ID 

            if (mat.matches()) System.out.println("ID CORRECTO");
            else System.out.println("ID INCORRECTO");
            
            pat = Pattern.compile (regex2);
            mat = pat.matcher(prueba);

            if (mat.find()) System.out.println("Si tiene guión");
            else  System.out.println("No tiene guión");

            //HAcer que mi ID solo tenga un guión
        }    
    }*/
}
