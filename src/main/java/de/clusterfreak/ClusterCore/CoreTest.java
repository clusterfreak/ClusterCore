package de.clusterfreak.ClusterCore;

import de.clusterfreak.ClusterCore.FuzzyCMeans;
import de.clusterfreak.ClusterCore.PossibilisticCMeans;

/**
 *
 * Internal Core Self Tests
 *
 * @version 0.1.2 (2020-10-31)
 * @author Thomas Heym
 *
 */

public class CoreTest {

    private static long timeBegin = System.currentTimeMillis();
    private static double object[][] = { { 0.1, 0.3 }, { 0.1, 0.5 }, { 0.1, 0.7 }, { 0.7, 0.3 }, { 0.7, 0.7 },
            { 0.8, 0.5 }, { 0.9, 0.5 } };
    private static int cluster = 2;
    private static double vi[][];
    private static double fcmReference[][] = { { 0.147070835, 0.5 }, { 0.758778663, 0.5 } };
    private static double pcm1Reference[][] = { { 0.102492638, 0.5 }, { 0.83065648, 0.5 } };
    private static double pcm2Reference[][] = { { 0.10000244, 0.5 }, { 0.801756421, 0.5 } };
    private static double delta = 0.000003;

    private static boolean testReference(double vi[][], double reference[][]) {
        boolean test = true;
        if ((Math.abs(vi[0][0] - reference[0][0]) < delta) && (Math.abs(vi[0][1] - reference[0][1]) < delta)) {
            if ((Math.abs(vi[1][0] - reference[1][0]) > delta) || (Math.abs(vi[1][1] - reference[1][1]) > delta)) {
                test = false;
            }
        } else {
            if ((Math.abs(vi[0][0] - reference[1][0]) < delta) && (Math.abs(vi[0][1] - reference[1][1]) < delta)) {
                if ((Math.abs(vi[1][0] - reference[0][0]) > delta) || (Math.abs(vi[1][1] - reference[0][1]) > delta)) {
                    test = false;
                }
            } else {
                test = false;
            }
        }
        return test;
    }

    public static void main(String[] args) {

        System.out.println("\nClusterCore 1.1.2\n");

        FuzzyCMeans fcm = new FuzzyCMeans(object, cluster);
        vi = fcm.determineClusterCenters(true, false);
        System.out.print("FCM Test: ");
        if (testReference(vi, fcmReference))
            System.out.println("ok");
        else
            System.out.println("error");

        PossibilisticCMeans pcm = new PossibilisticCMeans(object, cluster, 1);
        vi = pcm.determineClusterCenters(true, false);
        System.out.print("PCM Test (1st pass): ");
        if (testReference(vi, pcm1Reference))
            System.out.println("ok");
        else
            System.out.println("error");

        pcm = new PossibilisticCMeans(object, cluster, 2);
        vi = pcm.determineClusterCenters(true, false);
        System.out.print("PCM Test (2nd pass): ");
        if (testReference(vi, pcm2Reference))
            System.out.println("ok");
        else
            System.out.println("error");

        long timeEnd = System.currentTimeMillis() - timeBegin;
        System.out.println(timeEnd+" ms");
        System.out.println(System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch"));
    }
}
