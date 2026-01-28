package madMax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MadMax extends JPanel implements ActionListener, KeyListener {
    private static final int ANCHO = 800;
    private static final int ALTO = 600;

    private Coche jugador; 
    private List<Coche> vehiculos;
    private List<Peaton> peatones;
    private List<ManchaSangre> manchasSangre;

    private Timer timer;
    private Random random;
    private int puntuacion;

    // Sistema de Oleadas
    private int oleada;
    private float tiempoRestante;
    private int puntuacionObjetivo;
    private boolean juegoTerminado;

    // Cámara
    private double camaraX, camaraY;

    // Imágenes
    private BufferedImage fondo;
    private BufferedImage[] imagenesPeaton;
    private BufferedImage imagenCocheJugador;
    private BufferedImage[] imagenesCocheNPC;
    private BufferedImage[] imagenesCocheNPCDestruido;
    private BufferedImage imagenSangre;

    // Dimensiones del mapa
    private int anchoFondo = 1600;
    private int altoFondo = 1200;

    // Controles
    private boolean arribaPresionado, abajoPresionado, izquierdaPresionado, derechaPresionado;

    public MadMax() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(60, 60, 60));
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        iniciarJuego();

        timer = new Timer(16, this);
        timer.start();
    }

    private void iniciarJuego() {
        jugador = new Coche(anchoFondo / 2, altoFondo / 2, Color.RED, false, random);
        vehiculos = new ArrayList<>();
        peatones = new ArrayList<>();
        manchasSangre = new ArrayList<>();

        oleada = 1;
        tiempoRestante = 60;
        puntuacionObjetivo = 1000;
        juegoTerminado = false;

        puntuacion = 0;
        camaraX = jugador.x;
        camaraY = jugador.y;

        // Cargar recursos
        cargarFondo();
        cargarImagenPeaton();
        cargarImagenCocheJugador();
        cargarImagenesCocheNPC();
        cargarImagenesCocheNPCDestruido();
        cargarImagenSangre();

        // Generar tráfico
        for (int i = 0; i < 12; i++) {
            generarVehiculo();
        }

        // Generar peatones
        for (int i = 0; i < 20; i++) {
            generarPeaton();
        }
    }

    private void cargarFondo() {
        try {
            File archivoImagen = new File("fondo.png");
            if (!archivoImagen.exists())
                archivoImagen = new File("fondo.jpg");
            if (archivoImagen.exists()) {
                fondo = ImageIO.read(archivoImagen);
                anchoFondo = fondo.getWidth();
                altoFondo = fondo.getHeight();
            }
        } catch (Exception e) {
            System.out.println("Error cargando fondo: " + e.getMessage());
        }
    }

    private void cargarImagenPeaton() {
        imagenesPeaton = new BufferedImage[4];
        try {
            for (int i = 1; i <= 4; i++) {
                File f = new File("peaton" + i + ".png");
                if (f.exists())
                    imagenesPeaton[i - 1] = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.out.println("Error cargando peatones: " + e.getMessage());
        }
    }

    private void cargarImagenCocheJugador() {
        try {
            File f = new File("coche_jugador.png");
            if (f.exists())
                imagenCocheJugador = ImageIO.read(f);
        } catch (Exception e) {
            System.out.println("Error cargando coche jugador: " + e.getMessage());
        }
    }

    private void cargarImagenesCocheNPC() {
        imagenesCocheNPC = new BufferedImage[4];
        try {
            for (int i = 1; i <= 4; i++) {
                File f = new File("npc_car" + i + ".png");
                if (f.exists())
                    imagenesCocheNPC[i - 1] = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.out.println("Error cargando coches NPC: " + e.getMessage());
        }
    }

    private void cargarImagenesCocheNPCDestruido() {
        imagenesCocheNPCDestruido = new BufferedImage[4];
        int contCargados = 0;
        try {
            for (int i = 1; i <= 4; i++) {
                File f = new File("npc_car" + i + "_destruido.png");
                if (f.exists()) {
                    imagenesCocheNPCDestruido[i - 1] = ImageIO.read(f);
                    contCargados++;
                }
            }
            if (contCargados > 0) {
                System.out.println(contCargados + " imágenes de coches destruidos cargadas.");
            } else {
                System.out.println("AVISO: No se encontraron imágenes '_destruido.png'.");
            }
        } catch (Exception e) {
            System.out.println("Error cargando coches destruidos: " + e.getMessage());
        }
    }

    private void cargarImagenSangre() {
        try {
            File f = new File("sangre.png");
            if (f.exists()) {
                imagenSangre = ImageIO.read(f);
                System.out.println("Imagen de sangre cargada.");
            } else {
                System.out.println("AVISO: No se encontró 'sangre.png'.");
            }
        } catch (Exception e) {
            System.out.println("Error cargando sangre: " + e.getMessage());
        }
    }

    private void generarVehiculo() {
        double x = random.nextDouble() * anchoFondo;
        double y = random.nextDouble() * altoFondo;
        Color color = new Color(random.nextInt(200) + 55, random.nextInt(200) + 55, random.nextInt(200) + 55);
        vehiculos.add(new Coche(x, y, color, true, random));
    }

    private void generarPeaton() {
        double x = random.nextDouble() * anchoFondo;
        double y = random.nextDouble() * altoFondo;
        peatones.add(new Peaton(x, y, random));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actualizarJuego();
        repaint();
    }

    private void actualizarJuego() {
        if (juegoTerminado)
            return;

        tiempoRestante -= 0.016;
        if (tiempoRestante <= 0) {
            juegoTerminado = true;
            tiempoRestante = 0;
        }

        if (puntuacion >= puntuacionObjetivo) {
            oleada++;
            puntuacionObjetivo += 1500; // Incremento fijo o dinámico
            tiempoRestante = 60; // Reiniciar tiempo a 60s (o sumar)
            jugador.setVelocidadMaxima(7.0 + (oleada * 1.0)); // Aumentar velocidad

            for (int i = 0; i < 15; i++) {
                generarVehiculo();
                generarPeaton();
            }
        }
        if (arribaPresionado)
            jugador.acelerar();
        if (abajoPresionado)
            jugador.frenar();
        if (izquierdaPresionado)
            jugador.girarIzquierda();
        if (derechaPresionado)
            jugador.girarDerecha();

        jugador.actualizar();

        jugador.x = Math.max(20, Math.min(anchoFondo - 20, jugador.x));
        jugador.y = Math.max(20, Math.min(altoFondo - 20, jugador.y));

        camaraX += (jugador.x - camaraX) * 0.1;
        camaraY += (jugador.y - camaraY) * 0.1;

        for (Coche npc : vehiculos) {
            npc.actualizarIA();
            npc.actualizar();

            if (npc.x < 0 || npc.x > anchoFondo || npc.y < 0 || npc.y > altoFondo) {
                npc.x = random.nextDouble() * anchoFondo;
                npc.y = random.nextDouble() * altoFondo;
            }

            if (npc.esIA && !npc.estaDestruido) {
                Rectangle2D npcHitbox = new Rectangle2D.Double(npc.x - 10, npc.y - 10, 20, 20);
                if (jugador.obtenerLimites().intersects(npcHitbox)) {
                    npc.estaDestruido = true;
                    puntuacion += 50;
                    jugador.velocidad *= 0.5;
                    npc.velocidad = 0;
                }
            }
        }

        for (int i = peatones.size() - 1; i >= 0; i--) {
            Peaton p = peatones.get(i);
            p.actualizar(anchoFondo, altoFondo);

            if (jugador.obtenerLimites().intersects(p.obtenerLimites()) && Math.abs(jugador.velocidad) > 2) {
                manchasSangre.add(new ManchaSangre(p.x, p.y));
                peatones.remove(i);
                puntuacion += 15;
                generarPeaton();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int offsetX = ANCHO / 2 - (int) camaraX;
        int offsetY = ALTO / 2 - (int) camaraY;

        if (fondo != null)
            g2d.drawImage(fondo, offsetX, offsetY, null);

        if (imagenSangre != null) {
            int tamanoSangre = 64;
            for (ManchaSangre mancha : manchasSangre) {
                g2d.drawImage(imagenSangre,
                        (int) mancha.x + offsetX - tamanoSangre / 2,
                        (int) mancha.y + offsetY - tamanoSangre / 2,
                        tamanoSangre, tamanoSangre, null);
            }
        }

        for (Peaton p : peatones)
            p.dibujar(g2d, offsetX, offsetY, imagenesPeaton);

        for (Coche coche : vehiculos)
            coche.dibujar(g2d, offsetX, offsetY, imagenCocheJugador, imagenesCocheNPC, imagenesCocheNPCDestruido);

        jugador.dibujar(g2d, offsetX, offsetY, imagenCocheJugador, imagenesCocheNPC, imagenesCocheNPCDestruido);

        dibujarHUD(g2d);
    }

    private void dibujarHUD(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(5, 5, 250, 100);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Puntos: " + puntuacion + " / " + puntuacionObjetivo, 15, 25);
        g2d.drawString("Velocidad: " + (int) Math.abs(jugador.velocidad * 10), 15, 45);
        g2d.drawString("Oleada: " + oleada, 15, 65);
        g2d.drawString("Tiempo: " + (int) tiempoRestante + "s", 15, 85);

        if (juegoTerminado) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, ALTO / 2 - 50, ANCHO, 100);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String msg = "GAME OVER";
            int w = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, ANCHO / 2 - w / 2, ALTO / 2 + 10);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String subMsg = "Puntuación Final: " + puntuacion;
            int w2 = g2d.getFontMetrics().stringWidth(subMsg);
            g2d.drawString(subMsg, ANCHO / 2 - w2 / 2, ALTO / 2 + 40);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                arribaPresionado = true;
                break;
            case KeyEvent.VK_DOWN:
                abajoPresionado = true;
                break;
            case KeyEvent.VK_LEFT:
                izquierdaPresionado = true;
                break;
            case KeyEvent.VK_RIGHT:
                derechaPresionado = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                arribaPresionado = false;
                break;
            case KeyEvent.VK_DOWN:
                abajoPresionado = false;
                break;
            case KeyEvent.VK_LEFT:
                izquierdaPresionado = false;
                break;
            case KeyEvent.VK_RIGHT:
                derechaPresionado = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mad Max: Wasteland Driver");
            MadMax game = new MadMax();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(false);
        });
    }
}
