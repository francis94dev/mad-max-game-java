package madMax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MadMax extends JPanel implements ActionListener, KeyListener {
    // Definición de Estados del Juego
    public enum EstadoJuego {
        MENU,
        JUGANDO,
        GAME_OVER
    }

    private Clip musica;
    private EstadoJuego estadoActual = EstadoJuego.MENU; // Empezamos en el menú

    // Tamaño de la ventana
    private static final int ANCHO = 800;
    private static final int ALTO = 600;

    // Objetos del juego
    private Coche jugador; // El coche que controlamos
    private List<Coche> vehiculos; // Los coches enemigos
    private List<Peaton> peatones; // La gente que camina
    private List<ManchaSangre> manchasSangre; // Efectos de sangre

    private Timer timer; // Para el bucle del juego
    private Random random; // Para generar cosas aleatorias
    private int puntuacion; // Puntos del jugador

    // Sistema de Oleadas
    private int oleada;
    private float tiempoRestante;
    private int puntuacionObjetivo;

    // Cámara
    private double camaraX, camaraY;

    // Imágenes
    private BufferedImage fondo;
    private BufferedImage[] imagenesPeaton;
    private BufferedImage imagenCocheJugador;
    private BufferedImage[] imagenesCocheNPC;
    private BufferedImage[] imagenesCocheNPCDestruido;
    private BufferedImage imagenSangre;
    private BufferedImage imagenTitulo;
    private BufferedImage imagenGameOver;

    // Dimensiones del mapa
    private int anchoFondo = 1600;
    private int altoFondo = 1200;

    // Controles
    private boolean arribaPresionado, abajoPresionado, izquierdaPresionado, derechaPresionado;

    public MadMax() {
        // Configuración de la ventana (JPanel)
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(60, 60, 60)); // Color gris de fondo
        setFocusable(true); // Para que detecte las teclas
        addKeyListener(this); // Añadimos el "escucha" de teclas

        random = new Random();

        // Cargar todos los recursos (imágenes)
        cargarRecursos();

        // Inicializar objetos del juego (resetear variables)
        iniciarPartida();

        // El timer corre siempre, pero la lógica depende del estado
        // 16ms es aproximadamente 60 imágenes por segundo (FPS)
        timer = new Timer(16, this);
        timer.start(); // Arranca el juego
    }

    private void cargarRecursos() {
        cargarFondo();
        cargarImagenPeaton();
        cargarImagenCocheJugador();
        cargarImagenesCocheNPC();
        cargarImagenesCocheNPCDestruido();
        cargarImagenSangre();
        cargarImagenTitulo();
        cargarImagenGameOver();
    }

    // Prepara todo para empezar una partida nueva
    private void iniciarPartida() {
        // Creamos al jugador en el centro
        jugador = new Coche(anchoFondo / 2, altoFondo / 2, Color.RED, false, random);

        // Listas vacías para llenarlas luego
        vehiculos = new ArrayList<>();
        peatones = new ArrayList<>();
        manchasSangre = new ArrayList<>();

        // Valores iniciales
        oleada = 1;
        tiempoRestante = 60;
        puntuacionObjetivo = 1000;
        puntuacion = 0;

        // La cámara empieza donde el jugador
        camaraX = jugador.x;
        camaraY = jugador.y;

        // Reiniciar variables de control (teclas sueltas)
        arribaPresionado = false;
        abajoPresionado = false;
        izquierdaPresionado = false;
        derechaPresionado = false;

        // Generar tráfico y gente al principio
        for (int i = 0; i < 12; i++) {
            generarVehiculo();
        }

        for (int i = 0; i < 20; i++) {
            generarPeaton();
        }
    }

    private void cargarImagenTitulo() {
        try {
            File f = new File("title_screen.png");
            if (f.exists()) {
                imagenTitulo = ImageIO.read(f);
            } else {
                System.out.println("AVISO: No se encontró 'title_screen.png'.");
            }
        } catch (Exception e) {
            System.out.println("Error cargando titulo: " + e.getMessage());
        }
    }

    private void cargarImagenGameOver() {
        try {
            File f = new File("game_over.png");
            if (f.exists()) {
                imagenGameOver = ImageIO.read(f);
            } else {
                System.out.println("AVISO: No se encontró 'game_over.png'.");
            }
        } catch (Exception e) {
            System.out.println("Error cargando game over: " + e.getMessage());
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

    // Crea un coche enemigo en una posición al azar
    private void generarVehiculo() {
        double x = random.nextDouble() * anchoFondo;
        double y = random.nextDouble() * altoFondo;
        // Color aleatorio
        Color color = new Color(random.nextInt(200) + 55, random.nextInt(200) + 55, random.nextInt(200) + 55);
        vehiculos.add(new Coche(x, y, color, true, random));
    }

    // Crea un peatón en una posición al azar
    private void generarPeaton() {
        double x = random.nextDouble() * anchoFondo;
        double y = random.nextDouble() * altoFondo;
        peatones.add(new Peaton(x, y, random));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoActual == EstadoJuego.JUGANDO) {
            actualizarJuego();
        }
        repaint();
    }

    // Método donde ocurre toda la lógica del juego (movimiento, choques, etc.)
    private void actualizarJuego() {
        // Restamos tiempo (aprox 0.016 segundos por vuelta)
        tiempoRestante -= 0.016;
        if (tiempoRestante <= 0) {
            estadoActual = EstadoJuego.GAME_OVER; // Se acabó el tiempo
            gestionarMusica(false);
            tiempoRestante = 0;
        }

        // Si llegamos a los puntos necesarios, pasamos de ronda
        if (puntuacion >= puntuacionObjetivo) {
            oleada++;
            puntuacionObjetivo += 1500;
            tiempoRestante = 60;
            jugador.setVelocidadMaxima(7.0 + (oleada * 1.0));

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
                    puntuacion += 70; // updated score as per user edit
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

    // Método para dibujar todo en la pantalla
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia la pantalla
        Graphics2D g2d = (Graphics2D) g;
        // Esto hace que los bordes se vean mejor (suavizados)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Si estamos en el MENÚ, dibujamos la pantalla de título
        if (estadoActual == EstadoJuego.MENU) {
            if (imagenTitulo != null) {
                g2d.drawImage(imagenTitulo, 0, 0, ANCHO, ALTO, null);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, ANCHO, ALTO);
                g2d.setColor(Color.ORANGE);
                g2d.setFont(new Font("Arial", Font.BOLD, 60));
                String titulo = "MAD MAX";
                int w = g2d.getFontMetrics().stringWidth(titulo);
                g2d.drawString(titulo, ANCHO / 2 - w / 2, ALTO / 2 - 50);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String sub = "Presiona ENTER para comenzar";
                int w2 = g2d.getFontMetrics().stringWidth(sub);
                g2d.drawString(sub, ANCHO / 2 - w2 / 2, ALTO / 2 + 50);
            }
            return;
        }

        // Estado JUGANDO o GAME_OVER (renderiza el juego de fondo)
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
        // Variables para la posición y tamaño del cuadro
        int x = 5;
        int y = 5;
        int ancho = 250;
        int alto = 100;

        // Dibujamos el fondo del cuadro
        // Color gris oscuro semi-transparente
        g2d.setColor(new Color(20, 20, 25, 200));
        g2d.fillRect(x, y, ancho, alto);

        // Borde del cuadro
        // Color naranja para el borde
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(180, 100, 40));
        g2d.drawRect(x, y, ancho, alto);

        // Configuración para el texto
        g2d.setFont(new Font("Arial", Font.BOLD, 15));

        // Mostramos los puntos en color dorado
        g2d.setColor(new Color(255, 190, 100));
        g2d.drawString("PUNTOS: " + puntuacion + " / " + puntuacionObjetivo, x + 15, y + 25);

        // Color gris claro para el resto de datos
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));

        // Mostramos la oleada y el tiempo
        g2d.drawString("OLEADA: " + oleada, x + 15, y + 50);

        // Si queda poco tiempo lo ponemos en rojo
        if (tiempoRestante < 10)
            g2d.setColor(new Color(255, 80, 80));
        g2d.drawString("TIEMPO: " + (int) tiempoRestante + "s", x + 130, y + 50);

        // Barra de velocidad
        // Regla de 3 para calcular el ancho de la barra según la velocidad
        int velocidadReal = (int) Math.abs(jugador.velocidad * 10);
        int maxVelocidad = 100; // Ajusta esto a tu máx velocidad
        int anchoBarra = (int) ((velocidadReal / (double) maxVelocidad) * 220); // 220 es el ancho máx de la barra
        if (anchoBarra > 220)
            anchoBarra = 220;

        // Texto de velocidad
        g2d.setColor(new Color(180, 180, 180));
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("VELOCIDAD " + velocidadReal + " km/h", x + 15, y + 75);

        // Fondo de la barra
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x + 15, y + 80, 220, 8);

        // Relleno naranja según la velocidad
        g2d.setColor(new Color(255, 140, 0));
        g2d.fillRect(x + 15, y + 80, anchoBarra, 8);

        if (estadoActual == EstadoJuego.GAME_OVER) {
            // Dibujar imagen de Game Over si existe
            if (imagenGameOver != null) {
                g2d.drawImage(imagenGameOver, 0, 0, ANCHO, ALTO, null);
            } else {
                // Fallback si no hay imagen
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, ALTO / 2 - 80, ANCHO, 160);

                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                String msg = "GAME OVER";
                int w = g2d.getFontMetrics().stringWidth(msg);
                g2d.drawString(msg, ANCHO / 2 - w / 2, ALTO / 2 - 10);
            }

            // Datos de puntuación y reinicio

        }
    }

    // Cuando presionamos una tecla
    @Override
    public void keyPressed(KeyEvent e) {
        // Controles del MENÚ
        if (estadoActual == EstadoJuego.MENU) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                iniciarPartida(); // Empezar juego nuevo
                gestionarMusica(true);
                estadoActual = EstadoJuego.JUGANDO;
            }
            return; // Importante: no procesar otros controles
        }

        // Controles de GAME OVER
        if (estadoActual == EstadoJuego.GAME_OVER) {
            if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER) {
                iniciarPartida();
                gestionarMusica(true);
                estadoActual = EstadoJuego.JUGANDO;
            } else if (e.getKeyCode() == KeyEvent.VK_M) {
                estadoActual = EstadoJuego.MENU;
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                arribaPresionado = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                abajoPresionado = true;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                izquierdaPresionado = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                derechaPresionado = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                arribaPresionado = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                abajoPresionado = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                izquierdaPresionado = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                derechaPresionado = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void gestionarMusica(boolean reproducir) {
        try {
            if (reproducir) {
                if (musica != null && musica.isRunning())
                    return;

                if (musica == null) { // Solo cargamos la primera vez
                    File f = new File("Screen_Recording_20260128_205851_YouTube (online-audio-converter.com).wav");
                    if (!f.exists())
                        f = new File("musica.wav");
                    if (!f.exists())
                        return;

                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
                    musica = AudioSystem.getClip();
                    musica.open(audioIn);
                    ((FloatControl) musica.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-10.0f);
                }

                musica.setFramePosition(0); // Empezar desde el principio
                musica.setLoopPoints(0, -1); // Asegurar loop de todo el archivo
                musica.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                if (musica != null)
                    musica.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // El main arranca la ventana
    public static void main(String[] args) {
        // SwingUtilities.invokeLater es para que la interfaz no se bloquee
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mad Max: Wasteland Driver");
            MadMax game = new MadMax();
            frame.add(game);
            frame.pack(); // Ajusta el tamaño al del panel
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra todo al salir
            frame.setLocationRelativeTo(null); // Centra la ventana
            frame.setVisible(true); // Muestra la ventana
            frame.setResizable(false); // Que no se pueda cambiar el tamaño
        });
    }
}