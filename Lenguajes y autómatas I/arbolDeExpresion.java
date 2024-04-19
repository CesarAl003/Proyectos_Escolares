import java.util.Stack;
import java.util.regex.Pattern;

public class LeerFichero {
    public static class Nodo {
        String pal;
        Nodo izq, der;

        public Nodo (String pal) { // Nodo sin hijos
            this.pal = pal;
            this.der = this.izq = null;
        }
        
        public Nodo (String pal, Nodo izq, Nodo der) {
            this.pal = pal;
            this.izq = izq;
            this.der = der;
        }
    }
    
    static String op_arit = "\\*|/|\\+|-|mod|div";
    
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
       // String in_fijo = "2 * 2 + 5 * 3 + 1 - 10 / 2"; // 15
     //   String pos_fijo = infijo_posfijo (in_fijo);
       // System.out.println("Expresión infija: ." + in_fijo + ".");
      //  System.out.println("Expresión posfija: ." + pos_fijo + ".");
      //  Nodo n = crearArbol(pos_fijo);
        //recorrerInorden(n);
       // recorrerPosorden(n);

    }
}
