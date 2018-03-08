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
package org.processmining.clustering;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.clustering.evaluator.EvaluationUtils;
import org.processmining.clustering.evaluator.model.EventLogClusters;
import org.processmining.clustering.model.FieldValue;
import org.processmining.clustering.model.Itemset;
import org.processmining.clustering.model.Variant;
import org.processmining.clustering.model.VariantCluster;
import org.processmining.extensions.AbstractPlugin;
import org.processmining.plugins.hybridclusterminer.pattern.FrequentItemset;
import org.processmining.plugins.hybridclusterminer.pattern.FrequentPatternMiner;
import org.xeslite.lite.factory.XFactoryLiteImpl;
import smile.clustering.HierarchicalClustering;

import java.util.*;

public class Clusterer extends AbstractPlugin {

    /**
     * Assignes the traces in the event log to the clusters.
     *
     * @param clusters
     * @param variantTraceMap
     * @param log
     */
    public void assignTracesToVariants(Collection<VariantCluster> clusters, Map<Variant, List<XTrace>> variantTraceMap,
                                       XLog log) {
        // assign traces to clusters
        for (VariantCluster cluster : clusters) {
            XLog newlog = new XFactoryLiteImpl().createLog(log.getAttributes());
            newlog.getClassifiers().addAll(log.getClassifiers());

            for (Variant variant : cluster.getVariants()) {
                newlog.addAll(variantTraceMap.get(variant));
            }

            cluster.setLog(newlog);
        }
    }

}
