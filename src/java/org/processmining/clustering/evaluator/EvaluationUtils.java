/*
 *  Hybrid Feature Set Clustering
 *  Copyright (C) 2018  Alexander Seeliger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.processmining.clustering.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexander Seeliger on 12.02.2018.
 */
public class EvaluationUtils {

    private static double distToCluster(double[][] distanceMatrix, int index, List<Integer> indexes) {
        double dist = 0;
        for (int other : indexes) {
            dist += distanceMatrix[index][other];
        }
        return dist / (indexes.size() + 1);
    }

    private static double distToOtherClusters(double[][] distanceMatrix, int index, int[] clusters) {
        int[] clusterIndexes = Arrays.stream(clusters).distinct().toArray();
        int clusterIndex = clusters[index];

        double dist = Double.MAX_VALUE;
        for (int cluster : clusterIndexes) {
            if (cluster == clusterIndex)
                continue;

            List<Integer> inCluster = getIndexesOfItemsInCluster(cluster, clusters);

            double newDist = distToCluster(distanceMatrix, index, inCluster);
            if (newDist < dist) {
                dist = newDist;
            }
        }

        return dist;
    }

    private static List<Integer> getIndexesOfItemsInCluster(int clusterIndex, int[] clusters) {
        List<Integer> inCluster = new ArrayList<>();
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] == clusterIndex) {
                inCluster.add(i);
            }
        }
        return inCluster;
    }

    private static double silhouette(double[][] distanceMatrix, int[] clusters, int index) {
        double distA = distToCluster(distanceMatrix, index, getIndexesOfItemsInCluster(clusters[index], clusters));
        double distB = distToOtherClusters(distanceMatrix, index, clusters);

        if (distA == distB && distA == 0) {
            return 0;
        } else {
            return (distB - distA) / Math.max(distA, distB);
        }
    }

    private static double silhouetteCoefficient(double[][] distanceMatrix, int[] clusters, int clusterIndex) {
        double coefficient = 0;
        List<Integer> items = getIndexesOfItemsInCluster(clusterIndex, clusters);

        for (int index : items) {
            coefficient += silhouette(distanceMatrix, clusters, index);
        }
        return coefficient / items.size();
    }

    /**
     * Calculates the silhouette coefficient for the given clustering result.
     *
     * @param distanceMatrix
     * @param clusters
     * @return
     */
    public static double silhouetteCoefficient(double[][] distanceMatrix, int[] clusters) {
        double coefficient = 0;

        int[] clusterIndexes = Arrays.stream(clusters).distinct().toArray();
        for (int cluster : clusterIndexes) {
            coefficient += silhouetteCoefficient(distanceMatrix, clusters, cluster) * getIndexesOfItemsInCluster(cluster, clusters).size();
        }

        return coefficient / clusters.length;
    }
}
