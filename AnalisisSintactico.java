import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AnalisisSintactico {
    static List<String> errSintac = new ArrayList<>();
    static List<String> etiquetas = Arrays.asList("inicio:", "fin;", "varinicio:", "varfin;", "sino:", "finsi;" , "finpara;", "finmientras;");
    
    static String digito = "0|1|2|3|4|5|6|7|8|9";
    static String letra = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z";
    static String tipo = "ent|dec|cad";
    static String op_arit = "\\*|/|\\+|-|mod|div";
    static String op_rel = ">=|<=|==|<>|<|>";
    static String op_log = "(&&)|(\\|\\|)";
    static String cte_ent = "(" + digito + ")+";
    static String cte_dec = cte_ent + "\\." + cte_ent;
    static String cte_cad = "\" (.*?) \"";
    static String cte = "(" + cte_ent + ")" + "|" + "(" + cte_dec + ")" + "|" + "(" + cte_cad + ")";
    static String id = "(" + letra + ")(_*(" + letra + "|" + digito + ")+)*"; 
    static String num = cte_ent + "|" + id;
    static String operando = id + "|" + cte;
    static String condicional = "((" + operando + ") (" + op_rel + ") (" + operando + "))( (" + op_log + ") (" + operando + ") (" + op_rel + ") (" + operando + "))*" ;
    static String operacion = "((" + operando + ") (" + op_arit + ") (" + operando + "))( (" + op_arit + ") (" + operando + "))*";
    static String expresion = "(" + operando + ")( \\+ (" + operando + "))*";
    static String asign = "(" + id + ") = (" + operando + ") ;|(" + id + ") = (" + operacion + ") ;";
    static String imp = "imp \\( (" + expresion + ") \\) ;";
    static String esc = id + " = " + "esc \\( (" + cte_cad + ") \\) ;";
    static String si = "si \\( (" + condicional + ") \\) :";
    static String para = "para (" + id + ") = (" + num + ") hasta (" + num + ")( contando (" + num + "))? :";
    static String mientras = "mientras \\( (" + condicional + ") \\) hacer:";
    static String declara = "(" + tipo + ") ((" + id + ") ;|(" + asign +"))";

    static String sent = asign + "|" + imp + "|" + esc + "|" + si + "|" + para + "|" + mientras + "|sino:|finsi;|finpara;|finmientras;" ;
    static String sentencias = "(" + sent + ")*";
    static String programa = "inicio: varinicio: " + declara + "varfin;" + sentencias + "fin;" ;
    
    static String linea, error;
    static int contLin = 0;
    public static void AbrirArchivo (String ruta) {
        try { // Abre el archivo para lectura
            FileReader archivoFuente = new FileReader(ruta);
            // Crea un BufferedReader para leer líneas
            BufferedReader lector = new BufferedReader(archivoFuente);

            while ((linea = lector.readLine()) != null) {
                contLin++; // Cuenta cada línea
                String[] palabras = linea.split(" "); 
                //System.out.println(palabras[0]);
                if (etiquetas.contains(linea)) {
                    System.out.println("Simon, es un" + linea);
                }
                else {
                    switch (palabras[0]) {
                        case "imp":
                            if (Pattern.matches(imp, linea)) System.out.println(contLin+" simon, es un imp");
                            else errSintac.add("Error sintáctico en la linea " + contLin);
                            break;
                        case "si":
                            if (Pattern.matches(si, linea)) System.out.println(contLin+" simon, es un si");
                            else errSintac.add("Error sintáctico en la linea " + contLin);
                            break;
                        case "para":
                            if (Pattern.matches(para, linea)) System.out.println(contLin+" simon, es un para");
                            else
                                errSintac.add("Error sintáctico en la linea " + contLin);
                            break;
                        case "mientras":
                            if (Pattern.matches(mientras, linea)) System.out.println(contLin+" simon, es un mientras");
                            else errSintac.add("Error sintáctico en la linea " + contLin);
                            break;
                        default:
                            
                            break;
                    }
                }
                
            }
            
            // Cierra el archivo
            lector.close();
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
/*
        System.out.println("\n---------ERRORES---------");
        for (String e: errSintac) {
            System.out.println(e);
        }*/
    }


    public static void main(String[] args) {
        String ruta = "C:\\Users\\Ayums\\WorkSpace\\Proyectos_Escolares\\Salida.txt";
        AbrirArchivo(ruta);        
    }
}
