/*
Gutiérrez García Cesar Alejandro
Determinar las raices de una ecuacion cuadrática, sean reales o complejas.
*/

import java.util.Scanner;

public class FormulaGral {
    public static void main(String[] args) {
        Scanner sc = new Scanner (System.in);
        double a, b, c, d;
        double x1, x2, i, R;
        //Solicitar datos al usuario
        System.out.println("Ingrese los coeficientes de cada termino de la ecuación:");
        System.out.print("a = "); a = sc.nextDouble();
        System.out.print("b = "); b = sc.nextDouble();
        System.out.print("c = "); c = sc.nextDouble();
  
        if (a == 0) { //Evitar indeterminación
            System.out.println("Indeterminacion. Coeficiente -a- debe ser diferente de cero.");
        }
        else {
            //Calcular la discriminante
            d = b*b - 4*a*c;
    
            if (d >= 0) { //Aplicar formula general
                x1 = (-b+Math.sqrt(d))/(2*a);
                x2 = (-b-Math.sqrt(d))/(2*a);
                //Mostrar resultados
                System.out.printf("\nx1 = %5.4f\nx2 = %5.4f\n",x1,x2);         
            }
            else { //Utilizar numeros complejos
                d = Math.abs(d); //Calcular el valor absoluto del discriminante
                R = -b/(2*a); //Calcular la parte real R
                i = (Math.sqrt(d))/(2*a); //Calular la parte imaginaria
                //Mostrar resultados con números complejos
                System.out.printf("\nx1 = %5.4f + %5.4fi\nx2 = %5.4f - %5.4fi\n",R,i,R,i);
            }
        }
    }   
}
