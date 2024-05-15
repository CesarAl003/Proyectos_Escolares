import java.util.Stack;
import java.util.regex.Pattern;

public class arbolDeExpresion {
    static String op_arit = "\\*|/|\\+|-|mod|div";
    
    public static class Nodo {
        String pal;
        Nodo izq, der;

        public Nodo (String pal) { // Nodo sin hijos
            this.pal = pal;
            this.izq = this.der = null;
        }
        
        public Nodo (String pal, Nodo izq, Nodo der) {
            this.pal = pal;
            this.izq = izq;
            this.der = der;
        }
    }
    
    
/*    
    public static String infijo_posfijo (String in_fijo) {
        String post_fijo = "";
        Stack<String> pila_op = new Stack<>();
        String[] elementos = in_fijo.split(" ");
        
        for (String l : elementos) {
            if (Pattern.matches(op_arit, l)) { // Es un operador?
                while (!pila_op.isEmpty() && jerarquia(l) <= jerarquia(pila_op.peek())) {
                    post_fijo += (pila_op.pop() + " ");
                }
                pila_op.push(l);
            }
            else post_fijo += (l + " "); // Es un operando
        }

        // Desapilar cualquier operador restante de la pila
        while (!pila_op.isEmpty()) {
            post_fijo += (pila_op.pop() + " ");
        }

        return post_fijo;
    } */
 
    public static String[] infijo_posfijo(String in_fijo) {
        String post_fijo = "";
        Stack<String> pila_op = new Stack<>();
        String[] elementos = in_fijo.split(" ");
    
        for (String l : elementos) {
            if (l.equals("(")) { // Guardar ()
                pila_op.push(l);
            }
            else if (l.equals(")")) { // calcular la jerarquía dentro de los parentesis
                while (!pila_op.isEmpty() && !pila_op.peek().equals("(")) {
                    post_fijo += (pila_op.pop() + " ");
                }
                if (!pila_op.isEmpty()) {
                    pila_op.pop(); // Elimina el "(" de la pila
                }
            }
            else if (Pattern.matches(op_arit, l)) {
                while (!pila_op.isEmpty() && jerarquia(l) <= jerarquia(pila_op.peek())) {
                    post_fijo += (pila_op.pop() + " ");
                }
                pila_op.push(l);
            }
            else post_fijo += (l + " "); // Es un operando, se guarda inmediatamente
        }
    
        // Desapilar cualquier operador restante de la pila
        while (!pila_op.isEmpty()) {
            post_fijo += (pila_op.pop() + " ");
        }
        post_fijo = post_fijo.trim();

        String [] arrayPostFijo = post_fijo.split(" ");
        return arrayPostFijo; // Elimina el espacio final
    }
    
    private static int jerarquia (String opArit) {
        // Definir la precedencia de los operadores
        switch (opArit) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "mod":
            case "div":
                return 3;
            default:
                return 0;
        }
    }
    static Stack<Nodo> miPila = new Stack<>();
    public static Nodo crearArbol (String pos_fijo) {
        String [] elementos = pos_fijo.split(" ");
        Nodo izq, der;
        for(String l : elementos) {
            if (Pattern.matches(op_arit, l)) {
                der = miPila.pop();
                izq = miPila.pop();
                Nodo nuevo = new Nodo(l, izq, der);
                miPila.add(nuevo);
            }
            else miPila.add(new Nodo(l));
        }
        
        return miPila.pop();
    }
    public static void recorrerInorden (Nodo raiz) {
        Nodo actual = raiz;

        while (!miPila.isEmpty() || actual != null) {
            if (actual != null) {
                miPila.push(actual);
                actual = actual.izq;
            }
            else {
                actual = miPila.pop();
                System.out.println(actual.pal);
                actual = actual.der;
            }
        }
    }
    public static void recorrerPosorden (Nodo raiz) {//IDR
        Nodo actual;
        Stack<String> out = new Stack<>();

        if (raiz == null) return;
        miPila.push(raiz);

        while (!miPila.isEmpty()) {
            
            actual = miPila.pop();
            out.push(actual.pal);

            if (actual.izq != null) {
                miPila.push(actual.izq);
            }
            if (actual.der != null) {
                miPila.push(actual.der);
            }
        }
        // imprime el recorrido posterior al pedido
        while (!out.empty()) {
            System.out.println(out.pop() + " ");
        }

    }

    public static void main(String[] args) {
        String in_fijo = "5 mod 2 - 3 * 1 * 5 + 2 mod 1 + 1";
        String[] pos_fijo = infijo_posfijo (in_fijo);
        for (String l : pos_fijo) 
            System.out.print(l+" ");
        
        //System.err.println(pos_fijo); */
    }  
}
