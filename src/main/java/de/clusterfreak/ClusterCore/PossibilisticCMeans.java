package de.clusterfreak.ClusterCore;

import java.util.Vector;

/**
 * Possivilistic-C-Means (PCM)
 * <p>
 * Cluster analysis with Possivilistic-C-Means clustering algorithm
 *
 * <PRE>
 * Step 1: Initialization
 * Step 2: Determination of the cluster centers
 * Step 3: Calculate the new partition matrix and ni
 * Step 4: Termination or repetition
 * Step 5: optional - Repeat calculation (steps 2 to 4)
 * </PRE>
 *
 * @author Thomas Heym
 * @version 1.2.3 (2020-11-01)
 * @see FuzzyCMeans
 */
public class PossibilisticCMeans {
    /**
     * Quantity/number of clusters
     */
    private final int cluster;
    /**
     * Euclidean distance norm, exponent, initial value 2
     */
    private final static int m = 2;
    /**
     * Termination threshold, initial value 1.0e-7
     */
    private double e = 1.0e-7;
    /**
     * Each Object represents 1 cluster vi
     */
    private final double[][] object;
    /**
     * Cluster centers vi
     */
    private double[][] vi;
    /**
     * Complete search path
     */
    private double[][] viPath;
    /**
     * npcm
     */
    private final double[] ni;
    /**
     * Number of PCM passes
     */
    private int repeat;
    /**
     * Partition matrix (Membership values of the k-th object to the i-th
     * cluster)
     */
    private static double[][] getMik;

    /**
     * Generates PCM-Object from a set of Points
     *
     * @param object       Objects
     * @param clusterCount Number of clusters
     * @param repeat       Number of PCM passes for determination of the cluster centers
     * @see FuzzyCMeans
     */
    public PossibilisticCMeans(double[][] object, int clusterCount, int repeat) {
        this.object = object;
        this.cluster = clusterCount;
        this.vi = new double[cluster][2];
        this.ni = new double[cluster];
        this.repeat = repeat;
    }

    /**
     * Generates PCM-Object from a set of Points
     *
     * @param object       Objects
     * @param clusterCount Number of clusters
     * @param repeat       Number of PCM passes for determination of the cluster centers
     * @param e            Termination threshold, initial value 1.0e-7
     * @see FuzzyCMeans
     */
    public PossibilisticCMeans(double[][] object, int clusterCount, int repeat, double e) {
        this.object = object;
        this.cluster = clusterCount;
        this.vi = new double[cluster][2];
        this.ni = new double[cluster];
        this.repeat = repeat;
        this.e = e;
    }

    /**
     * Returns the cluster centers
     *
     * @param random     random initialization
     * @param returnPath Determines whether return the complete search path. Values:
     *                   <code>true</code>, <code>false</code>
     * @return Cluster centers and serarch path (optional); The cluster centers
     * are at the end.
     */
    public double[][] determineClusterCenters(boolean random, boolean returnPath) {
        double euclideanDistance;
        /*
         * When false return only the class centers
         */
        Vector<Point2D> viPathRec = new Vector<>();
        // Step 1: Initialization
        FuzzyCMeans fcm;
        if (e == 1.0e-7) {
            fcm = new FuzzyCMeans(object, cluster);
        } else {
            fcm = new FuzzyCMeans(object, cluster, e);
        }
        double[][] getViPath = fcm.determineClusterCenters(random, true);
        for (double[] doubles : getViPath) viPathRec.add(new Point2D(doubles[0], doubles[1]));
        vi = fcm.getVi();
        double[][] mik = fcm.getMik();
        do { // while (repeat>0)
            repeat--;
            /*
             * Perform calculation of ni
             */
            boolean ni_calc = true;
            do { // while (euclideanDistance>=e)
                // Step 2: Determination of the cluster centers
                // --> Step 5: optional - Repeat calculation (steps 2 to 4)
                for (int k = 0; k < vi.length; k++) {
                    double mikm, mikm0 = 0.0, mikm1 = 0.0, mikms = 0.0;
                    for (int i = 0; i < mik.length; i++) {
                        mikm = Math.pow(mik[i][k], m);
                        mikm0 += mikm * object[i][0];
                        mikm1 += mikm * object[i][1];
                        mikms += mikm;
                    }
                    vi[k][0] = mikm0 / mikms;
                    vi[k][1] = mikm1 / mikms;
                }
                // record cluster points
                if (returnPath) {
                    for (double[] doubles : vi) viPathRec.add(new Point2D(doubles[0], doubles[1]));
                }
                // Step 3: Calculate the new partition matrix and ni
                double[][] mik_before = new double[mik.length][cluster];
                double[] miks = new double[vi.length];
                if (ni_calc) {
                    // Calulate ni (Distance from the class center to the point
                    // with a membership value of 0.5 to the actual cluster)
                    // initial ni = 0
                    for (int i = 0; i < vi.length; i++) {
                        ni[i] = 0.0;
                        miks[i] = 0.0;
                    }
                    // ni = sum mik&sup2;*dik&sup2;
                    for (int i = 0; i < mik.length; i++) {
                        for (int k = 0; k < vi.length; k++) {
                            double dik = Math
                                    .sqrt(Math.pow(object[i][0] - vi[k][0], 2) + Math.pow(object[i][1] - vi[k][1], 2));
                            ni[k] += Math.pow(Math.pow(mik[i][k], 2), 2) * Math.pow(dik, 2);
                            miks[k] += Math.pow(mik[i][k], 2);
                        }
                    }
                    // ni = sum(mik&sup2;*dik&sup2;) / sum mik&sup2;
                    for (int i = 0; i < vi.length; i++) {
                        ni[i] /= miks[i];
                    }
                    ni_calc = false;
                }
                for (int k = 0; k < vi.length; k++) {
                    for (int i = 0; i < mik.length; i++) {
                        mik_before[i][k] = mik[i][k];
                        mik[i][k] = 1 / (1 + (Math.pow(
                                Math.sqrt(Math.pow(object[i][0] - vi[k][0], 2) + Math.pow(object[i][1] - vi[k][1], 2)),
                                2)) / ni[k]);
                        // NaN-Error
                        if (Double.isNaN(mik[i][k]))
                            mik[i][k] = 1.0;
                    }
                }
                // calculate euclidean distance
                euclideanDistance = 0.0;
                for (int k = 0; k < vi.length; k++) {
                    for (int i = 0; i < mik.length; i++) {
                        euclideanDistance += Math.pow((mik[i][k] - mik_before[i][k]), 2);
                    }
                }
                euclideanDistance = Math.sqrt(euclideanDistance);
            }
            // Step 4: Termination or repetition
            while (euclideanDistance >= e);
        } while (repeat > 0);
        getMik = mik;
        // Value return
        if (returnPath) {
            double[][] viPathCut = new double[viPathRec.size()][2];
            for (int k = 0; k < viPathCut.length; k++) {
                Point2D cut = viPathRec.elementAt(k);
                viPathCut[k][0] = cut.x;
                viPathCut[k][1] = cut.y;
            }
            setViPath(viPathCut);
        }
        return vi;
    }

    /**
     * Returns the partition matrix (Membership values of the k-th object to the
     * i-th cluster)
     *
     * @return Partition matrix
     */
    public double[][] getMik() {
        return getMik;
    }

    /**
     * Set partition matrix
     *
     * @param setMik partition matrix
     */
    public static void setMik(double[][] setMik) {
        getMik = setMik;
    }

    /**
     * Returns cluster centers vi
     *
     * @return vi
     */
    public double[][] getVi() {
        return vi;
    }

    /**
     * Set viPath
     *
     * @param setViPath Set viPath
     */
    private void setViPath(double[][] setViPath) {
        viPath = setViPath;
    }

    /**
     * Returns the complete search path
     *
     * @return viPath
     */
    public double[][] getViPath() {
        return viPath;
    }
}
