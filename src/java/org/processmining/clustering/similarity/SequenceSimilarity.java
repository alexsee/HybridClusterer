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
package org.processmining.clustering.similarity;

import org.apache.commons.math3.stat.StatUtils;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XTrace;
import org.processmining.clustering.model.Variant;
import org.processmining.log.utils.XUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SequenceSimilarity {

    /**
     * Encode a trace into an ordered list of event indexes.
     *
     * @param t
     * @param eventNames
     * @return
     */
    public static List<Integer> encodeTrace(XTrace t, List<String> eventNames) {
        List<Integer> trace = new ArrayList<>();
        t.forEach(x -> trace.add(eventNames.indexOf(XUtils.getConceptName(x))));

        return trace;
    }

    /**
     * Calculates the distance between two traces using the Levenstein-Dammerau
     * distance.
     *
     * @param loginfo
     * @param trace1
     * @param trace2
     * @return
     */
    public static double calculateDistance(XLogInfo loginfo, XTrace trace1, XTrace trace2) {
        List<Integer> sequence1 = new ArrayList<>();
        List<Integer> sequence2 = new ArrayList<>();

        trace1.forEach(x -> sequence1.add(loginfo.getEventClasses().getClassOf(x).getIndex()));
        trace2.forEach(x -> sequence2.add(loginfo.getEventClasses().getClassOf(x).getIndex()));

        return getLevenshteinDistance(sequence1, sequence2);
    }

    /**
     * Calculates the Levenshtein-Dammerau distance for two sequences.
     *
     * @param s
     * @param t
     * @return
     */
    public static double getLevenshteinDistance(List<Integer> s, List<Integer> t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
         * The difference between this impl. and the previous is that, rather than
         * creating and retaining a matrix of size s.length()+1 by t.length()+1, we
         * maintain two single-dimensional arrays of length s.length()+1. The first, d,
         * is the 'current working' distance array that maintains the newest distance
         * cost counts as we iterate through the characters of String s. Each time we
         * increment the index of String t we are comparing, d is copied to p, the
         * second int[]. Doing so allows us to retain the previous cost counts as
         * required by the algorithm (taking the minimum of the cost count to the left,
         * up one, and diagonally up and to the left of the current cost count being
         * calculated). (Note that the arrays aren't really copied anymore, just
         * switched...this is clearly much better than cloning an array or doing a
         * System.arraycopy() each time through the outer loop.)
         *
         * Effectively, the difference between the two implementations is this one does
         * not cause an out of memory condition when calculating the LD over two very
         * large strings.
         */

        int n = s.size(); // length of s
        int m = t.size(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; // placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        Integer t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.get(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.get(i - 1).equals(t_j) ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return (double) p[n] / (double) Math.max(n, m);
    }

    public static double getLevenshteinDistance(List<Integer> x, List<Integer> y, double[] duration1, double[] duration2) {
        double[][] dp = new double[x.size() + 1][y.size() + 1];

        for (int i = 0; i <= x.size(); i++) {
            for (int j = 0; j <= y.size(); j++) {
                if (i == 0) {
                    dp[i][j] = j == 0 ? 0 : dp[i][j - 1] + duration2[j - 1];
                } else if (j == 0) {
                    dp[i][j] = i == 0 ? 0 : dp[i - 1][j] + duration1[i - 1];
                } else {
                    dp[i][j] = StatUtils.min(new double[]{
                            dp[i - 1][j - 1] + (x.get(i - 1) - y.get(j - 1)),
                            dp[i - 1][j] + duration1[i - 1],
                            dp[i][j - 1] + duration2[j - 1]});
                }
            }
        }

        double diff = dp[x.size()][y.size()];
        double perf = Math.max(StatUtils.sum(duration1), StatUtils.sum(duration2));

        return diff / perf;
    }

    /**
     * Calculates the difference of the given variants and returns a value that
     * indicates the pairwise distance divided by the number of variants * 2.
     *
     * @param variants1
     * @param variants2
     * @return
     */
    public static double calculateVariantDistance(Collection<Variant> variants1, Collection<Variant> variants2) {
        double dist = 0;
        for (Variant v1 : variants1) {
            for (Variant v2 : variants2) {
                dist += getLevenshteinDistance(v1, v2);
            }
        }

        dist /= (variants1.size() * variants2.size());
        return dist;
    }

    /**
     * Calculate the distance of a variant to all variants in the given cluster.
     *
     * @param cluster
     * @param variant
     * @return
     */
    public static double calculateVariantDistanceToCluser(Collection<Variant> cluster, Variant variant) {
        double dist = cluster.stream().mapToDouble(x -> getLevenshteinDistance(x, variant)).sum();
        dist /= cluster.size();

        return dist;
    }
}
