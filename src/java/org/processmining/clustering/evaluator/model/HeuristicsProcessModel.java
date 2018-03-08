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
package org.processmining.clustering.evaluator.model;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.processmining.plugins.heuristicsnet.visualizer.HeuristicsNetAnnotatedVisualization;

public class HeuristicsProcessModel implements IProcessModel {

	private PluginContext context;

	private HeuristicsNet net;

	private int numberOfEdges = 0;

	public HeuristicsProcessModel(PluginContext context, HeuristicsNet net) {
		this.context = context;
		this.net = net;
	}

	public static HeuristicsProcessModel createInstance(PluginContext context, XLog log) {
		// mine heuristic net
		HeuristicsMinerSettings settings = new HeuristicsMinerSettings();
		settings.setClassifier(log.getClassifiers().get(0));
		
		HeuristicsMinerNoUI miner = new HeuristicsMinerNoUI(log, settings);
		HeuristicsNet net = miner.mine();

		// return heuristics process model
		HeuristicsProcessModel processModel = new HeuristicsProcessModel(context, net);

		// calc number of edges
		processModel.numberOfEdges = net.getArcUsage().cardinality();
		return processModel;
	}

	@Override
	public double getFitness() {
		return net.getFitness();
	}

	@Override
	public JComponent getUI() {
		return HeuristicsNetAnnotatedVisualization.visualize(context, net);
	}

	@Override
	public int getNumberOfEdges() {
		return numberOfEdges;
	}

	@Override
	public int getNumberOfNodes() {
		return net.getActivitiesMappingStructures().getActivitiesMapping().length;
	}

}
