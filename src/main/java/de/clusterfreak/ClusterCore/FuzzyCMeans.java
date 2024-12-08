package de.clusterfreak.ClusterCore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Fuzzy-C-Means (FCM)
 * <p>
 * Cluster analysis with Fuzzy-C-Means clustering algorithm
 *
 * <pre>
 * Step 1: Initialization
 * Step 2: Determination of the cluster centers
 * Step 3: Calculate the new partition matrix
 * Step 4: Termination or repetition
 * Step 5: optional - Repeat calculation (steps 2 to 4)
 * </PRE>
 *
 * @version 1.7.0 (2024-12-07)
 * @author Thomas Heym
 */
public class FuzzyCMeans {
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
    private final double[][] vi;
    /**
     * Complete search path
     */
    private double[][] viPath;
    /**
     * Partition matrix (Membership values of the k-th object to the i-th
     * cluster)
     */
    private static double[][] getMik;

    /**
     * Generates FCM-Object from a set of Points
     *
     * @param object       Objects
     * @param clusterCount Number of clusters
     */
    public FuzzyCMeans(double[][] object, int clusterCount) {
        this.object = object;
        this.cluster = clusterCount;
        this.vi = new double[cluster][2];
    }

    /**
     * Generates FCM-Object from a set of Points
     *
     * @param object       Objects
     * @param clusterCount Number of clusters
     * @param e            Termination threshold, initial value 1.0e-7
     */
    public FuzzyCMeans(double[][] object, int clusterCount, double e) {
        this.object = object;
        this.cluster = clusterCount;
        this.vi = new double[cluster][2];
        this.e = e;
    }

    /**
     * Returns the cluster centers
     *
     * @param random     random initialization
     * @param returnPath Determines whether return the complete search path. Values:
     *                   <code>true</code>, <code>false</code>
     * @return Cluster centers and search path (optional); The cluster centers
     * are at the end.
     */
    public double[][] determineClusterCenters(boolean random, boolean returnPath) {
        double euclideanDistance;
        double[][] mik = new double[object.length][cluster];
        /*
         * When false return only the class centers
         */
        List<Point2D> viPathRec = new ArrayList<>();
        // Step 1: Initialization
        if (random) {
            for (int i = 0; i < mik.length; i++) {
                for (int k = 0; k < cluster; k++) {
                    mik[i][k] = Math.random();
                }
            }
        } else {
            int s = 0;
            for (int i = 0; i < mik.length; i++) {
                for (int k = 0; k < cluster; k++) {
                    mik[i][k] = (k == s) ? 1 : 0.0;
                }
                s = (s + 1) % cluster;
            }
        }

        ForkJoinPool pool = new ForkJoinPool();

        do {
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
            // Step 3: Calculate the new partition matrix
            double[][] mik_before = new double[mik.length][cluster];
            for (int k = 0; k < vi.length; k++) {
                for (int i = 0; i < mik.length; i++) {
                    mik_before[i][k] = mik[i][k];
                }
            }
            pool.invoke(new UpdatePartitionMatrixTask(mik, vi, object, cluster));
            // calculate euclidean distance
            euclideanDistance = 0.0;
            for (int k = 0; k < vi.length; k++) {
                for (int i = 0; i < mik.length; i++) {
                    euclideanDistance += Math.pow((mik[i][k] - mik_before[i][k]), 2);
                }
            }
            euclideanDistance = Math.sqrt(euclideanDistance);
            // Step 4: Termination or repetition
        } while (euclideanDistance >= e);
        getMik = mik;
        if (returnPath) {
            double[][] viPathCut = new double[viPathRec.size()][2];
            for (int k = 0; k < viPathCut.length; k++) {
                Point2D cut = viPathRec.get(k);
                viPathCut[k][0] = cut.x;
                viPathCut[k][1] = cut.y;
            }
            setViPath(viPathCut);
        }
        return vi;
    }

    /**
     * RecursiveTask for updating the partition matrix
     */
    private static class UpdatePartitionMatrixTask extends RecursiveTask<Void> {
        private final double[][] mik;
        private final double[][] vi;
        private final double[][] object;
        private final int cluster;

        public UpdatePartitionMatrixTask(double[][] mik, double[][] vi, double[][] object, int cluster) {
            this.mik = mik;
            this.vi = vi;
            this.object = object;
            this.cluster = cluster;
        }

        @Override
        protected Void compute() {
            for (int k = 0; k < vi.length; k++) {
                for (int i = 0; i < mik.length; i++) {
                    double dik = 0.0;
                    for (double[] doubles : vi) {
                        dik += Math.pow(1 / (Math.sqrt(Math.pow(object[i][0] - doubles[0], 2) + Math.pow(object[i][1] - doubles[1], 2))), 1 / (m - 1));
                    }
                    mik[i][k] = Math.pow(1 / (Math.sqrt(Math.pow(object[i][0] - vi[k][0], 2) + Math.pow(object[i][1] - vi[k][1], 2))), 1 / (m - 1)) / dik;
                    if (Double.isNaN(mik[i][k])) mik[i][k] = 1.0;
                }
            }
            return null;
        }
    }

    /**
     * Returns the partition matrix (Membership values of the k-th object to the
     * i-th cluster) The method is also called from Pos±sibilisticCMeans.
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