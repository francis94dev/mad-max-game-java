package madMax;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Coche {
    double x, y, angulo, velocidad;
    Color color;
    boolean esIA;
    boolean estaDestruido;
    double anguloObjetivo;
    int indiceImagen;
    private Random random;

    public Coche(double x, double y, Color color, boolean esIA, Random random) {
        this.x = x;
        this.y = y;
        this.random = random;
        this.angulo = random.nextDouble() * Math.PI * 2;
        this.velocidad = 0;
        this.color = color;
        this.esIA = esIA;
        this.anguloObjetivo = angulo;
        this.indiceImagen = random.nextInt(4);
        this.estaDestruido = false;
    }

    private double velocidadMaxima = 7.0;

    public void setVelocidadMaxima(double max) {
        this.velocidadMaxima = max;
    }

    public void acelerar() {
        velocidad = Math.min(velocidadMaxima, velocidad + 0.25);
    }

    public void frenar() {
        velocidad = Math.max(-3, velocidad - 0.3);
    }

    public void girarIzquierda() {
        if (Math.abs(velocidad) > 0.5)
            angulo -= 0.06;
    }

    public void girarDerecha() {
        if (Math.abs(velocidad) > 0.5)
            angulo += 0.06;
    }

    public void actualizarIA() {
        if (!esIA)
            return;

        if (estaDestruido) {
            velocidad *= 0.90;
            return;
        }

        velocidad = 2 + random.nextDouble();
        if (random.nextInt(100) == 0)
            anguloObjetivo = random.nextDouble() * Math.PI * 2;
        double diff = anguloObjetivo - angulo;
        while (diff > Math.PI)
            diff -= Math.PI * 2;
        while (diff < -Math.PI)
            diff += Math.PI * 2;
        angulo += diff * 0.03;
    }

    public void actualizar() {
        x += Math.cos(angulo) * velocidad;
        y += Math.sin(angulo) * velocidad;
        velocidad *= 0.97;
    }

    public Rectangle2D obtenerLimites() {
        return new Rectangle2D.Double(x - 12, y - 12, 24, 24);
    }

    public void dibujar(Graphics2D g2d, int offsetX, int offsetY, BufferedImage imagenCocheJugador,
            BufferedImage[] imagenesCocheNPC, BufferedImage[] imagenesCocheNPCDestruido) {
        AffineTransform old = g2d.getTransform();
        g2d.translate((int) x + offsetX, (int) y + offsetY);
        g2d.rotate(angulo);

        if (!esIA && imagenCocheJugador != null) {
            // Dibujo del jugador
            g2d.rotate(Math.PI / 2);
            int size = 84;
            g2d.drawImage(imagenCocheJugador, -size / 2, -size / 2, size, size, null);

        } else if (esIA && imagenesCocheNPC != null && imagenesCocheNPC[indiceImagen] != null) {
            // Lógica de dibujo para IA con imágenes
            g2d.rotate(Math.PI / 2);
            int size = 75;

            if (estaDestruido && imagenesCocheNPCDestruido != null && imagenesCocheNPCDestruido[indiceImagen] != null) {
                g2d.drawImage(imagenesCocheNPCDestruido[indiceImagen], -size / 2, -size / 2, size, size, null);
            } else {
                g2d.drawImage(imagenesCocheNPC[indiceImagen], -size / 2, -size / 2, size, size, null);
            }

        }
        g2d.setTransform(old);
    }
}
