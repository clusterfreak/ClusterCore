package de.clusterfreak.ClusterCore;

/**
 * Point with 2D coordinates
 *
 * @version 1.3.2 (2020-05-24)
 * @author Thomas Heym
 */
public class Point2D {
    public double x = 0.0;
    public double y = 0.0;

    public Point2D(double xi, double yi) {
        x = xi;
        y = yi;
    }

    /**
     * Convert Point2D to PointPixel
     *
     * @param pixelOffset
     *            pixel Offset
     * @return pixel point
     */
    public PointPixel toPointPixel(int pixelOffset) {
        PointPixel pointPixel = new PointPixel(0, 0);
        int x = 0;
        int y = 0;
        double o;
        double p;
        for (int t = 0; t < pixelOffset; t++) {
            o = (double) t / pixelOffset;
            p = o + (double) 1 / pixelOffset;
            p = Math.round(p * 100.) / 100.;
            if ((this.x >= o) & (this.x < p))
                x = t;
            if ((this.y >= o) & (this.y < p))
                y = t;
        }
        pointPixel.x = x;
        pointPixel.y = y;
        return pointPixel;
    }
}