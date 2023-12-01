import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mi_casita extends JPanel implements KeyListener {
    private int x = 250;
    private int y = 250;
    private JButton btnTras;
    private JButton btnEsc;
    private JButton btnRot;
    private JButton btnSesg;
    private int Op = 0;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double ang = 0.0;
    private double shearX = 0;
    private double shearY = 0;

    //Constructor
    public Mi_casita() {
        addKeyListener(this); //Agregamos eventos por teclado a 'Mi_casita'
        setFocusable(true); //Activamos el foco para que pueda detectar los eventos
        setFocusTraversalKeysEnabled(false);
        //requestFocus();
        
        //Inicializar cada botón y añadirle un linstener.
        btnTras = new JButton("Traslación"); 
        btnTras.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Op = 1;
                Mi_casita.this.requestFocus();
            }
        });
        
        //Inicializar cada botón y añadirle un linstener.
        btnEsc = new JButton("Escalamiento"); 
        btnEsc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Op = 2;
                Mi_casita.this.requestFocus();
            }
        });

        //Inicializar cada botón y añadirle un linstener.
        btnRot = new JButton("Rotación"); 
        btnRot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Op = 3;
                Mi_casita.this.requestFocus();
            }
        });

        //Inicializar cada botón y añadirle un linstener.
        btnSesg = new JButton("Sesgado"); 
        btnSesg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Op = 4;
                Mi_casita.this.requestFocus();
            }
        });

        //Declaro e inicializo un nuevo panel para mis botoncitos
        JPanel pnBotones = new JPanel();
        pnBotones.add(btnTras); //Agrego mis botoncitos al panel
        pnBotones.add(btnEsc);
        pnBotones.add(btnRot);
        pnBotones.add(btnSesg);
        //Agregar un esquema de divisiones a mi ventana
        setLayout(new BorderLayout());
        //Agrego mi panel de botones a la ventana en la lozalización sur
        add(pnBotones, BorderLayout.SOUTH);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Guardar la transformación actual
        AffineTransform initialTransform = g2d.getTransform();
        
        if (Op == 2) g2d.scale(scaleX, scaleY);  //Escalamiento
        if (Op == 4) g2d.shear(shearX, shearY); //Sesgado

        // Aplicar la rotación
        AffineTransform rotation = AffineTransform.getRotateInstance(ang, x, y);
        g2d.transform(rotation);

        //Dibuja el cuerpo de la casita
        g.setColor(new Color(238, 131, 77));
        g.fillRect(x - 70, y - 70,100,100);

        // Dibuja el techo de mi casita
        int[] xTecho = {x - 70, x + 30, (x - 20)};  // Coordenadas x de los vértices
        int[] yTecho = {y - 70, y - 70, y - 150};  // Coordenadas y de los vértices
        g.fillPolygon(xTecho, yTecho, 3);  // Rellena el triángulo
        
        //Dibuja la puerta de mi casita
        g.setColor(new Color(169, 50, 38));
        g.fillRect(x - 30, y - 5,20,35);

        //Dibuja las ventanas
        g.setColor(new Color(89, 255, 252));
        g.fillRect(x - 50, y - 50,20,20); //Ventana 1
        g.fillRect(x - 10, y - 50,20,20); //Ventana 2

        //Establecer el grosor del contorno
        BasicStroke grosor = new BasicStroke(2.0f);
        g2d.setStroke(grosor);
        g.setColor(Color.BLACK); //Color del contorno
        g.drawRect(x - 70, y - 70,100,100);//Cuerpo
        g.drawPolygon(xTecho, yTecho, 3);//Techo
        g.drawRect(x - 30, y - 5,20,35); //Puerta
        g.drawRect(x - 50, y - 50,20,20); //Ventana 1
        g.drawRect(x - 10, y - 50,20,20); //Ventana 2

        this.requestFocus();
        g2d.setTransform(initialTransform);
    }

    //Este metodo pertenece a la clases Key listener
    public void keyPressed(KeyEvent e) {
        //Mi_casita.this.requestFocus(); Traslación
        if (Op == 1) {
            //Obtener el código de la tecla que activo el envento
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_LEFT) {
                x -= 10;
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                x += 10;
            } else if (keyCode == KeyEvent.VK_UP) {
                y -= 10;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                y += 10;
            }
        }
        else if (Op == 2) { //Escalamiento
            //Obtener el código de la tecla que activo el envento
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_UP) {
                scaleX *= 1.01;
                scaleY *= 1.01;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                scaleX *= 0.99;
                scaleY *= 0.99;
            }
        }
        else if (Op == 3) { //Rotación
            //Obtener el código de la tecla que activo el envento
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_LEFT) {
                ang -= Math.toRadians(15);
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                ang += Math.toRadians(15);
            }
        }
        else if (Op == 4) { //Sesgado
            //Obtener el código de la tecla que activo el envento
            int keyCode = e.getKeyCode();
            System.out.println(keyCode);
            if (keyCode == KeyEvent.VK_LEFT) {
                // Realiza el sesgado horizontal hacia la izquierda
                shearX = 0.2;
                shearY = 0;
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                // Realiza el sesgado horizontal hacia la derecha
                shearX = 0;
                shearY = 0.2;
            }
        }

        repaint();
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame myFrame = new JFrame("Tarea de Orozco");
        Mi_casita micasita = new Mi_casita();
        myFrame.add(micasita);
        myFrame.setSize(500, 500);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setVisible(true);
    }
}