/*Dado un alfabeto, definir los primeros 100 elementos del lenguaje, es decir,
 la llave de klein positiva (sin tomar en cuenta el elemento neutro) */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Lenguaje {
    static Scanner sc = new Scanner(System.in);
    static int n; //Número de elementos mostrados
    static int cont = 0, cont1;
    static String alfabeto;

    public static BufferedReader br = new BufferedReader( new InputStreamReader(System.in)); //Lector BufferedReader
    
    //Mètodo para leer un string usando BufferedReader
    public static String leeLinea() {
        String cad="";
        try {
            cad = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cad;
    }    
    
    public static void main(String[] args) {
        System.out.print("Ingrese un alfabeto. Cada símbolo separado por un espacio. ");
        alfabeto = leeLinea();
        System.out.print("Cuantos elemenotos desea? ");
        n = sc.nextInt();
        //Llave de klein positiva
        String[] klein = new String[n];
        
        //Separa los símbolos de mi alfabeto en un arreglo 'alf'
        Pattern pat = Pattern.compile(" ."); 
        String[] alf = pat.split(alfabeto);

        //Ciclo for-each para guardar los simbolos del alfabeto en Klein
        for (String temp: alf) { 
            klein[cont] = temp;
            cont++;
        }
        //Un segundo contador para guardar la siguiente posición disponible en klein
        cont1 = alf.length;
        //
        for(int i = 0; cont1 < n; i++) { //(2; n; i +=2) 
            cont = 0;
           // klein[i] = klein[cont] + klein[cont + 1];
            while (cont < alf.length) { //cont < 2
                klein[cont1] = klein[i] + klein[cont];
                cont++;
                cont1++;
            }
        }
        for (int i = 0; i < n; i++) {
            System.out.print(klein[i] + ", ");
        }
    }
}