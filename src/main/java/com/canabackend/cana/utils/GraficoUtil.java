package com.canabackend.cana.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;

/**
 * Graficas del PDF, dibujadas con Graphics2D y entregadas como imagen a Jasper.
 *
 * <p>Se dibujan a mano y no con un motor de graficas para no meter una
 * dependencia (JFreeChart y su modulo de Jasper) al proyecto por tres tipos de
 * grafica. Todo lo que se necesita —barras, barras agrupadas, dona— cabe en
 * este archivo y se ve igual que la version en pantalla.
 *
 * <p>Las imagenes se generan al doble del tamano al que se colocan en el PDF
 * ({@link #ESCALA}) para que no se vean pixeladas al imprimir.
 */
public final class GraficoUtil {

    private GraficoUtil() {
    }

    /** Factor de sobremuestreo respecto al tamano final en el PDF. */
    public static final int ESCALA = 2;

    public static final Color AZUL = new Color(0x02, 0x19, 0x30);
    public static final Color AMBAR = new Color(0xF2, 0xB1, 0x34);
    public static final Color GRIS_TEXTO = new Color(0x6B, 0x72, 0x80);
    public static final Color GRIS_LINEA = new Color(0xE2, 0xE8, 0xF0);

    /** Paleta de las graficas de composicion, en orden de uso. */
    private static final Color[] PALETA = {
            new Color(0x02, 0x19, 0x30), new Color(0xF2, 0xB1, 0x34),
            new Color(0x0F, 0x4A, 0x7A), new Color(0xD9, 0x77, 0x06),
            new Color(0x15, 0x65, 0xA8), new Color(0x9A, 0x3E, 0x00),
            new Color(0x38, 0xBD, 0xF8), new Color(0x7C, 0x3A, 0xED),
            new Color(0x05, 0x96, 0x69), new Color(0x94, 0xA3, 0xB8)
    };

    public static Color colorSerie(int indice) {
        return PALETA[Math.floorMod(indice, PALETA.length)];
    }

    /**
     * Barras verticales de dos series (facturado vs cobrado). Si {@code serieB}
     * es null dibuja una sola serie.
     *
     * @param ancho ancho final en puntos del PDF; la imagen sale a ESCALA x eso.
     */
    public static BufferedImage barrasAgrupadas(List<String> etiquetas,
                                                List<Double> serieA, String nombreA, Color colorA,
                                                List<Double> serieB, String nombreB, Color colorB,
                                                int ancho, int alto) {
        BufferedImage imagen = lienzo(ancho, alto);
        Graphics2D g = abrir(imagen);

        int w = ancho * ESCALA;
        int h = alto * ESCALA;
        int margenIzq = 46 * ESCALA;
        int margenDer = 8 * ESCALA;
        int margenSup = (serieB != null ? 20 : 8) * ESCALA;
        int margenInf = 22 * ESCALA;
        int areaW = w - margenIzq - margenDer;
        int areaH = h - margenSup - margenInf;

        double maximo = Math.max(maximo(serieA), serieB != null ? maximo(serieB) : 0d);
        double tope = escalaSuperior(maximo);

        dibujarGrilla(g, margenIzq, margenSup, areaW, areaH, tope);

        if (serieB != null) {
            leyenda(g, margenIzq, 10 * ESCALA, nombreA, colorA, nombreB, colorB);
        }

        int n = etiquetas.size();
        if (n == 0) {
            g.dispose();
            return imagen;
        }

        double paso = (double) areaW / n;
        double anchoGrupo = paso * 0.62;
        int series = serieB != null ? 2 : 1;
        double anchoBarra = anchoGrupo / series;
        int base = margenSup + areaH;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7 * ESCALA));
        for (int i = 0; i < n; i++) {
            double centro = margenIzq + paso * i + paso / 2d;
            double x = centro - anchoGrupo / 2d;

            barra(g, colorA, x, base, anchoBarra, valor(serieA, i), tope, areaH);
            if (serieB != null) {
                barra(g, colorB, x + anchoBarra, base, anchoBarra, valor(serieB, i), tope, areaH);
            }

            // Con muchos meses las etiquetas se encimarian: se alternan.
            if (n <= 13 || i % 2 == 0) {
                textoCentrado(g, etiquetas.get(i), centro, base + 13 * ESCALA, GRIS_TEXTO);
            }
        }

        g.dispose();
        return imagen;
    }

    /** Barras horizontales, para rankings donde la etiqueta es larga. */
    public static BufferedImage barrasHorizontales(List<String> etiquetas, List<Double> valores,
                                                   Color color, boolean moneda, int ancho, int alto) {
        BufferedImage imagen = lienzo(ancho, alto);
        Graphics2D g = abrir(imagen);

        int w = ancho * ESCALA;
        int h = alto * ESCALA;
        int margenIzq = 108 * ESCALA;
        int margenDer = 46 * ESCALA;
        int margenSup = 4 * ESCALA;
        int margenInf = 4 * ESCALA;
        int areaW = w - margenIzq - margenDer;
        int areaH = h - margenSup - margenInf;

        int n = etiquetas.size();
        if (n == 0) {
            g.dispose();
            return imagen;
        }

        double maximo = Math.max(maximo(valores), 1d);
        double paso = (double) areaH / n;
        double altoBarra = Math.min(paso * 0.62, 16d * ESCALA);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7 * ESCALA));
        for (int i = 0; i < n; i++) {
            double centro = margenSup + paso * i + paso / 2d;
            double y = centro - altoBarra / 2d;
            double largo = Math.max(areaW * valor(valores, i) / maximo, 1d);

            g.setColor(color);
            g.fillRoundRect(margenIzq, (int) Math.round(y), (int) Math.round(largo),
                    (int) Math.round(altoBarra), 3 * ESCALA, 3 * ESCALA);

            g.setColor(GRIS_TEXTO);
            textoDerecha(g, recortar(g, etiquetas.get(i), margenIzq - 6 * ESCALA),
                    margenIzq - 6 * ESCALA, centro + 3 * ESCALA);
            g.setColor(AZUL);
            g.drawString(moneda ? montoCorto(valor(valores, i)) : numero(valor(valores, i)),
                    (int) Math.round(margenIzq + largo + 5 * ESCALA), (int) Math.round(centro + 3 * ESCALA));
        }

        g.dispose();
        return imagen;
    }

    /** Dona con leyenda a la derecha. Los valores se reparten sobre su suma. */
    public static BufferedImage dona(List<String> etiquetas, List<Double> valores, int ancho, int alto) {
        BufferedImage imagen = lienzo(ancho, alto);
        Graphics2D g = abrir(imagen);

        int w = ancho * ESCALA;
        int h = alto * ESCALA;

        double total = 0;
        for (Double v : valores) {
            total += v != null ? v : 0d;
        }
        if (total <= 0) {
            g.setColor(GRIS_TEXTO);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8 * ESCALA));
            textoCentrado(g, "Sin datos en el periodo", w / 2d, h / 2d, GRIS_TEXTO);
            g.dispose();
            return imagen;
        }

        int diametro = Math.min(h - 8 * ESCALA, (int) (w * 0.46));
        int cx = 4 * ESCALA + diametro / 2;
        int cy = h / 2;

        double angulo = 90;
        for (int i = 0; i < valores.size(); i++) {
            double parte = valor(valores, i) / total * 360d;
            g.setColor(colorSerie(i));
            g.fill(new Arc2D.Double(cx - diametro / 2d, cy - diametro / 2d, diametro, diametro,
                    angulo, -parte, Arc2D.PIE));
            angulo -= parte;
        }

        // Centro hueco: la dona lee mejor que el pastel lleno.
        g.setColor(Color.WHITE);
        int hueco = (int) (diametro * 0.56);
        g.fill(new Ellipse2D.Double(cx - hueco / 2d, cy - hueco / 2d, hueco, hueco));

        g.setColor(AZUL);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11 * ESCALA));
        textoCentrado(g, numero(total), cx, cy + 2 * ESCALA, AZUL);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 6 * ESCALA));
        textoCentrado(g, "TOTAL", cx, cy + 12 * ESCALA, GRIS_TEXTO);

        // Leyenda
        int xLeyenda = cx + diametro / 2 + 12 * ESCALA;
        int disponible = w - xLeyenda - 4 * ESCALA;
        int alturaFila = 13 * ESCALA;
        int yLeyenda = cy - (Math.min(valores.size(), 6) * alturaFila) / 2 + 4 * ESCALA;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7 * ESCALA));
        for (int i = 0; i < etiquetas.size() && i < 6; i++) {
            g.setColor(colorSerie(i));
            g.fillRoundRect(xLeyenda, yLeyenda - 6 * ESCALA, 7 * ESCALA, 7 * ESCALA, 2 * ESCALA, 2 * ESCALA);
            g.setColor(GRIS_TEXTO);
            String texto = etiquetas.get(i) + "  " + porcentaje(valor(valores, i) / total * 100d);
            g.drawString(recortar(g, texto, disponible - 11 * ESCALA), xLeyenda + 11 * ESCALA, yLeyenda);
            yLeyenda += alturaFila;
        }

        g.dispose();
        return imagen;
    }

    // ─── Auxiliares de dibujo ────────────────────────────────────

    private static BufferedImage lienzo(int ancho, int alto) {
        return new BufferedImage(ancho * ESCALA, alto * ESCALA, BufferedImage.TYPE_INT_RGB);
    }

    private static Graphics2D abrir(BufferedImage imagen) {
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imagen.getWidth(), imagen.getHeight());
        return g;
    }

    private static void dibujarGrilla(Graphics2D g, int x, int y, int ancho, int alto, double tope) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7 * ESCALA));
        g.setStroke(new BasicStroke(ESCALA));
        for (int i = 0; i <= 4; i++) {
            int lineaY = y + alto - (alto * i / 4);
            g.setColor(GRIS_LINEA);
            g.drawLine(x, lineaY, x + ancho, lineaY);
            g.setColor(GRIS_TEXTO);
            textoDerecha(g, montoCorto(tope * i / 4d), x - 5 * ESCALA, lineaY + 3 * ESCALA);
        }
    }

    private static void leyenda(Graphics2D g, int x, int y, String nombreA, Color colorA,
                                String nombreB, Color colorB) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7 * ESCALA));
        g.setColor(colorA);
        g.fillRoundRect(x, y - 6 * ESCALA, 7 * ESCALA, 7 * ESCALA, 2 * ESCALA, 2 * ESCALA);
        g.setColor(GRIS_TEXTO);
        g.drawString(nombreA, x + 11 * ESCALA, y);

        int ancho = g.getFontMetrics().stringWidth(nombreA);
        int x2 = x + 11 * ESCALA + ancho + 14 * ESCALA;
        g.setColor(colorB);
        g.fillRoundRect(x2, y - 6 * ESCALA, 7 * ESCALA, 7 * ESCALA, 2 * ESCALA, 2 * ESCALA);
        g.setColor(GRIS_TEXTO);
        g.drawString(nombreB, x2 + 11 * ESCALA, y);
    }

    private static void barra(Graphics2D g, Color color, double x, int base, double ancho,
                              double valor, double tope, int areaH) {
        if (valor <= 0 || tope <= 0) {
            return;
        }
        double altura = Math.max(areaH * valor / tope, 1d);
        g.setColor(color);
        g.fillRoundRect((int) Math.round(x), (int) Math.round(base - altura),
                Math.max((int) Math.round(ancho) - ESCALA, 2), (int) Math.round(altura),
                2 * ESCALA, 2 * ESCALA);
    }

    private static void textoCentrado(Graphics2D g, String texto, double cx, double y, Color color) {
        g.setColor(color);
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, (int) Math.round(cx - ancho / 2d), (int) Math.round(y));
    }

    private static void textoDerecha(Graphics2D g, String texto, double xDerecha, double y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, (int) Math.round(xDerecha - ancho), (int) Math.round(y));
    }

    private static String recortar(Graphics2D g, String texto, int anchoMaximo) {
        if (g.getFontMetrics().stringWidth(texto) <= anchoMaximo) {
            return texto;
        }
        String recortado = texto;
        while (recortado.length() > 1
                && g.getFontMetrics().stringWidth(recortado + "…") > anchoMaximo) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "…";
    }

    // ─── Auxiliares de calculo y formato ─────────────────────────

    private static double valor(List<Double> serie, int indice) {
        if (serie == null || indice >= serie.size() || serie.get(indice) == null) {
            return 0d;
        }
        return serie.get(indice);
    }

    private static double maximo(List<Double> serie) {
        double maximo = 0;
        if (serie != null) {
            for (Double v : serie) {
                if (v != null && v > maximo) {
                    maximo = v;
                }
            }
        }
        return maximo;
    }

    /** Redondea el tope del eje hacia arriba para que la grilla de numeros limpios. */
    private static double escalaSuperior(double maximo) {
        if (maximo <= 0) {
            return 4d;
        }
        double magnitud = Math.pow(10, Math.floor(Math.log10(maximo)));
        double normalizado = maximo / magnitud;
        double redondeado;
        if (normalizado <= 1) {
            redondeado = 1;
        } else if (normalizado <= 2) {
            redondeado = 2;
        } else if (normalizado <= 4) {
            redondeado = 4;
        } else if (normalizado <= 5) {
            redondeado = 5;
        } else {
            redondeado = 10;
        }
        return redondeado * magnitud;
    }

    private static String montoCorto(double valor) {
        if (valor >= 1_000_000) {
            return recortarDecimal(valor / 1_000_000d) + "M";
        }
        if (valor >= 1_000) {
            return recortarDecimal(valor / 1_000d) + "k";
        }
        return String.valueOf(Math.round(valor));
    }

    private static String recortarDecimal(double valor) {
        String texto = String.format(Locale.US, "%.1f", valor);
        return texto.endsWith(".0") ? texto.substring(0, texto.length() - 2) : texto;
    }

    private static String numero(double valor) {
        return valor == Math.rint(valor)
                ? String.valueOf((long) valor)
                : String.format(Locale.US, "%.1f", valor);
    }

    private static String porcentaje(double valor) {
        return String.format(Locale.US, "%.0f%%", valor);
    }
}
