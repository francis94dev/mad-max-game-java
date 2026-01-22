package madMax;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Peaton {
    double x, y, vx, vy;
    Color color;
    int indiceImagen;
    private Random random;

    public Peaton(double x, double y, Random random) {
        this.x = x;
        this.y = y;
        this.random = random;
        this.vx = (random.nextDouble() - 0.5) * 0.6;
        this.vy = (random.nextDouble() - 0.5) * 0.6;
        this.color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        this.indiceImagen = random.nextInt(4);
    }

    public void actualizar(int anchoFondo, int altoFondo) {
        x += vx;
        y += vy;
        if (random.nextInt(120) == 0) {
            vx = (random.nextDouble() - 0.5) * 0.6;
            vy = (random.nextDouble() - 0.5) * 0.6;
        }
        x = Math.max(10, Math.min(anchoFondo - 10, x));
        y = Math.max(10, Math.min(altoFondo - 10, y));
    }

    public Rectangle2D obtenerLimites() {
        return new Rectangle2D.Double(x - 15, y - 15, 30, 30);
    }

    public void dibujar(Graphics2D g2d, int offsetX, int offsetY, BufferedImage[] imagenesPeaton) {
        int drawX = (int) x + offsetX;
        int drawY = (int) y + offsetY;

        if (imagenesPeaton != null && imagenesPeaton[indiceImagen] != null) {
            int size = 64;
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(drawX - 10, drawY - 4, 20, 8);
            g2d.drawImage(imagenesPeaton[indiceImagen], drawX - size / 2, drawY - size / 2, size, size, null);
        }
    }
}
