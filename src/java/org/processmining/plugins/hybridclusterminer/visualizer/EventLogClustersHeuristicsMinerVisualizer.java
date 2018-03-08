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
package org.processmining.plugins.hybridclusterminer.visualizer;

import org.deckfour.xes.model.XLog;
import org.processmining.clustering.evaluator.model.EventLogClusters;
import org.processmining.clustering.evaluator.model.HeuristicsProcessModel;
import org.processmining.clustering.evaluator.model.IProcessModel;
import org.processmining.clustering.model.VariantCluster;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import javax.swing.*;

@Plugin(name = "Cluster Visualizer (Heuristics Miner)", returnLabels = {"Example Return UI"}, returnTypes = {
        JComponent.class}, userAccessible = true, parameterLabels = {"Event Log Clusters"})
@Visualizer
public class EventLogClustersHeuristicsMinerVisualizer {

    @PluginVariant(requiredParameterLabels = {0})
    public JComponent visualise(PluginContext context, EventLogClusters item) {
        context.getProgress().setMaximum(item.getClusters().size());

        JTabbedPane tabbed = new JTabbedPane();

        double fitness = 0;
        int shownTraces = 0;

        int i = 1;
        for (VariantCluster cluster : item.getClusters()) {
            context.getProgress().inc();
            XLog log = cluster.getLog();
            shownTraces += log.size();

            // mine heuristic net
            IProcessModel model = HeuristicsProcessModel.createInstance(context, log);
            JComponent heuristicVisualizer = model.getUI();

            fitness += model.getFitness() * log.size();

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.add(new JLabel("Traces: " + log.size()));
            contentPanel.add(heuristicVisualizer);

            tabbed.add("Cluster " + i, contentPanel);
            i++;
        }

        fitness /= shownTraces;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel totalSizeLabel = new JLabel(shownTraces + " / " + item.getLogSize() + " Traces  --  Fitness = " + fitness + "  --  Silhouette = " + item.getSilhouetteCoefficient());
        panel.add(totalSizeLabel);
        panel.add(tabbed);

        return panel;
    }
}
