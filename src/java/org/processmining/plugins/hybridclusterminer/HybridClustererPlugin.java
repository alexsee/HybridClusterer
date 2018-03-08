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
package org.processmining.plugins.hybridclusterminer;

import net.metaopt.swarm.FitnessFunction;
import net.metaopt.swarm.pso.Particle;
import net.metaopt.swarm.pso.Swarm;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.clustering.Clusterer;
import org.processmining.clustering.Configuration;
import org.processmining.clustering.evaluator.EvaluationUtils;
import org.processmining.clustering.evaluator.model.EventLogClusters;
import org.processmining.clustering.evaluator.model.HeuristicsProcessModel;
import org.processmining.clustering.evaluator.model.IProcessModel;
import org.processmining.clustering.model.*;
import org.processmining.clustering.similarity.ItemsetSimilarity;
import org.processmining.clustering.similarity.SequenceSimilarity;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.LogUtils;
import org.processmining.plugins.hybridclusterminer.pattern.FrequentItemset;
import org.processmining.plugins.hybridclusterminer.pattern.FrequentPatternMiner;
import org.processmining.plugins.log.ReSortLog;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.WardLinkage;

import java.util.*;
import java.util.Map.Entry;

@Plugin(name = "Clustering: Hybrid Feature Set", parameterLabels = {"Log"}, returnLabels = {
        "Cluster of eventlogs"}, returnTypes = {EventLogClusters.class}, categories = {
        PluginCategory.Discovery, PluginCategory.Analytics})
public class HybridClustererPlugin extends Clusterer {

    /**
     * Stores the log
     */
    private XLog sublog;

    /**
     * Stores the categorical fields used for frequent itemset mining
     */
    private List<String> categoricalFields;

    /**
     * Stores the traces for a "trace variant" (distinct sequence of activities)
     */
    private Map<Variant, List<XTrace>> variantTraceMap;

    @PluginVariant(variantLabel = "Cluster Eventlog using Hybrid Clusterer", requiredParameterLabels = {0})
    @UITopiaVariant(affiliation = "Technische Universität Darmstadt", author = "A. Seeliger", email = "seeliger@tk.tu-darmstadt.de", pack = "HybridClusterer")
    public EventLogClusters cluster(UIPluginContext context, XLog log) {
        setContext(context);
        log("Read log and calculate trace variants...");
        setProgressInterminate(true);

        // sort the log and calculate categorical fields
        this.sublog = Configuration.SORT_EVENT_LOG ? ReSortLog.removeEdgePoints(context, log) : log;
        this.categoricalFields = LogUtils.getCategoricalFields(sublog);

        // generate a variant trace map
        this.variantTraceMap = LogUtils.getLogVariants(sublog);

        // use the optimizer for calculating the best support value
        Swarm swarm = new Swarm(5, new Particle(3), new FitnessFunction(true) {

            @Override
            public double evaluate(double[] position) {
                return performPSOStep(position[0], position[1], position[2]);
            }

        });

        // set default pso values
        swarm.setInertia(0.729844);
        swarm.setMaxPosition(new double[]{0.6, Math.min(Math.max(2, variantTraceMap.size()), 100), 1});
        swarm.setMinPosition(new double[]{0.05, 2, 0});
        swarm.setMaxMinVelocity(0.2);
        swarm.setGlobalIncrement(1.49618);
        swarm.setParticleIncrement(1.49618);

        // perform 10 iterations
        int numberOfIterations = 10;

        for (int i = 0; i < numberOfIterations; i++) {
            swarm.evolve();

            System.out.println("Iteration: " + i);
            System.out.println(swarm.toStringStats());
        }

        // if we found a solution, return the best
        if (swarm.getBestPosition() != null) {
            EventLogClusters clusters = mine(swarm.getBestPosition()[0], (int) swarm.getBestPosition()[1],
                    swarm.getBestPosition()[2]);
            clusters.setLog(sublog);
            clusters.setWeighting(swarm.getBestPosition()[2]);

            return clusters;
        }

        return null;
    }

    /**
     * Generates the clusters for the given minSupport, numClusters and weighting value.
     *
     * @param minSupport
     * @param numClusters
     * @param w1
     * @return
     */
    public EventLogClusters mine(double minSupport, int numClusters, double w1) {
        Map<Itemset, Variants> frequentItemsetListMap = new HashMap<>();
        Map<Itemset, Double> frequentItemsetSupport = new HashMap<>();

        // now we need to extract frequent patterns for each variant
        for (Entry<Variant, List<XTrace>> variant : variantTraceMap.entrySet()) {
            HashMap<FieldValue, Integer> itemsetValues = new HashMap<>();
            HashMap<Integer, FieldValue> valueMap = new HashMap<>();

            // write database file
            FrequentPatternMiner miner = new FrequentPatternMiner();
            List<List<Integer>> transactions = miner.getDatabase(variant.getValue(), itemsetValues, valueMap,
                    categoricalFields);

            // extract frequent itemsets
            Set<FrequentItemset> frequentItemsets = miner.extractFrequentItemsets(transactions, minSupport);

            for (FrequentItemset itemset : frequentItemsets) {
                Itemset values = new Itemset();
                itemset.forEach(x -> values.add(valueMap.get(x)));

                Variants vars = frequentItemsetListMap.getOrDefault(values, new Variants());
                vars.add(variant.getKey());

                frequentItemsetListMap.put(values, vars);

                // update support
                Double support = frequentItemsetSupport.getOrDefault(values, 0.0D);
                support += itemset.getSupport();

                frequentItemsetSupport.put(values, support);
            }
        }

        // generate separate logs for each cluster
        List<Itemset> itemsets = new ArrayList<>(frequentItemsetListMap.keySet());
        itemsets.sort(new ItemsetComparator(frequentItemsetSupport));

        if (itemsets.size() < numClusters) {
            return null;
        }
        frequentItemsetSupport.clear();


        // distance matrix
        log("Calculating distance matrix for " + itemsets.size() + " itemsets...");
        double[][] distanceMatrix = getDistanceMatrix(w1, itemsets, frequentItemsetListMap);


        // perform clustering
        log("Clustering...");

        HierarchicalClustering algorithm = new HierarchicalClustering(new WardLinkage(distanceMatrix));
        int[] clusterMap = algorithm.partition((int) Math.min(numClusters, distanceMatrix.length));
        Map<Integer, VariantCluster> clusters = buildClustersFromHACResult(clusterMap, itemsets, frequentItemsetListMap);


        // resolve overlaps
        log("Resolve overlapping clusters...");

        List<VariantCluster> cls = new ArrayList<>(clusters.values());
        while (compactClusterByMoveStrategy(cls)) {
            // do nothing
        }


        // assign traces to clusters
        log("Assign traces to clusters...");
        EventLogClusters eventLogClusters = assignTracesToClusters(clusters, minSupport);

        double silhouette = EvaluationUtils.silhouetteCoefficient(distanceMatrix, clusterMap);
        eventLogClusters.setSilhouetteCoefficient(silhouette);

        return eventLogClusters;
    }

    /**
     * Returns the distance matrix for the given itemsets.
     *
     * @param w1
     * @param itemsets
     * @param frequentItemsetListMap
     * @return
     */
    private double[][] getDistanceMatrix(double w1, List<Itemset> itemsets, Map<Itemset, Variants> frequentItemsetListMap) {
        double[][] distanceMatrix = new double[itemsets.size()][itemsets.size()];

        for (int i = 0; i < itemsets.size(); i++) {
            Itemset i1 = itemsets.get(i);
            Variants v1 = frequentItemsetListMap.get(i1);

            for (int j = 0; j < i; j++) {
                // get variants and itemsets
                Itemset i2 = itemsets.get(j);
                Variants v2 = frequentItemsetListMap.get(i2);

                // calc distance between variants
                double variantsDistance = SequenceSimilarity.calculateVariantDistance(v1, v2);
                double itemsetsDistance = ItemsetSimilarity.calculateItemsetDistance(i1, i2);

                double distance = w1 * variantsDistance + (1 - w1) * itemsetsDistance;
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }

        return distanceMatrix;
    }

    /**
     * Builds the clusters using HAC.
     *
     * @param clusterMap
     * @param itemsets
     * @param frequentItemsetListMap
     * @return
     */
    private Map<Integer, VariantCluster> buildClustersFromHACResult(int[] clusterMap, List<Itemset> itemsets, Map<Itemset, Variants> frequentItemsetListMap) {
        Map<Integer, VariantCluster> clusters = new HashMap<>();

        for (int i = 0; i < clusterMap.length; i++) {
            VariantCluster cluster = clusters.getOrDefault(clusterMap[i],
                    new VariantCluster(new HashSet<>(), new HashSet<>()));

            Itemset itemset = itemsets.get(i);
            Variants variants = frequentItemsetListMap.get(itemset);

            cluster.getVariants().addAll(variants);
            clusters.put(clusterMap[i], cluster);
        }

        return clusters;
    }

    /**
     * Assigns the trace to the clusters.
     *
     * @param clusters
     * @param minSupport
     * @return
     */
    private EventLogClusters assignTracesToClusters(Map<Integer, VariantCluster> clusters, double minSupport) {
        assignTracesToVariants(clusters.values(), variantTraceMap, sublog);

        EventLogClusters result = new EventLogClusters(new ArrayList<>(clusters.values()), sublog.size(), sublog);
        result.setMinSupport(minSupport);

        return result;
    }

    /**
     * Compacts overlapping clusters by moving variants to the most similar cluster.
     *
     * @param clusters
     */
    private boolean compactClusterByMoveStrategy(List<VariantCluster> clusters) {
        // find variants in different clusters
        for (int i = 0; i < clusters.size(); i++) {
            VariantCluster cluster1 = clusters.get(i);

            for (Variant variant : cluster1.getVariants()) {
                // get distance to current cluster
                double distance = SequenceSimilarity.calculateVariantDistanceToCluser(cluster1.getVariants(), variant);

                for (int j = 0; j < clusters.size(); j++) {
                    if (i == j)
                        continue;

                    VariantCluster cluster2 = clusters.get(j);

                    if (cluster2.getVariants().contains(variant)) {
                        double distanceOther = SequenceSimilarity
                                .calculateVariantDistanceToCluser(cluster2.getVariants(), variant);

                        if (distanceOther >= distance) {
                            cluster2.getVariants().remove(variant);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Perform a single PSO step with the given optimization parameters and return the optimization value, i.e.,
     * the weighted process model fitness.
     *
     * @param support
     * @param numClusters
     * @param weighting
     * @return
     */
    private double performPSOStep(double support, double numClusters, double weighting) {
        double minSupport = Math.round(support * 1000d) / 1000d;
        System.out.println("PSO values: [" + minSupport + ", " + (int) numClusters + ", " + weighting + "]");

        // now generate the clusters for the given minSupport
        EventLogClusters clusters = mine(minSupport, (int) numClusters, weighting);
        if (clusters == null)
            return 0;

        // now we need to evaluate the clusters to calculate the weighted fitness
        double fitness = 0.0D;
        int numTraces = 0;

        for (VariantCluster cluster : clusters.getClusters()) {
            XLog sublog = cluster.getLog();

            // mine heuristic net
            IProcessModel model = HeuristicsProcessModel.createInstance(getContext(), sublog);
            numTraces += sublog.size();

            double currentFitness = model.getFitness();
            fitness += currentFitness * sublog.size();
        }

        // calc fitness
        fitness /= numTraces;
        return (fitness +
                ((double) numTraces / sublog.size()) +
                clusters.getSilhouetteCoefficient() +
                (1 - (clusters.getClusters().size() / Math.min((double) variantTraceMap.size(), 100d))))
                / 4;
    }
}
