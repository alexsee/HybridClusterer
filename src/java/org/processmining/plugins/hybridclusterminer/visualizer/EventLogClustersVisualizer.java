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

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.clustering.evaluator.model.EventLogClusters;
import org.processmining.clustering.model.VariantCluster;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.ui.logdialog.LogDialogInitializer;

@Plugin(name = "Cluster Visualizer (Log Dialog)", returnLabels = { "Example Return UI" }, returnTypes = {
		JComponent.class }, userAccessible = true, parameterLabels = { "Matching Instances" })
@Visualizer
public class EventLogClustersVisualizer {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualise(UIPluginContext context, EventLogClusters item) {
		context.getProgress().setMaximum(item.getClusters().size());
		
		JTabbedPane tabbed = new JTabbedPane();

		int i = 1;
		for (VariantCluster cluster : item.getClusters()) {
			context.getProgress().inc();
			
			XLog log = cluster.getLog();
			XLogInfo summary = XLogInfoFactory.createLogInfo(log);

			LogDialogInitializer logdialog = new LogDialogInitializer();
			tabbed.add("Cluster " + i, logdialog.initialize(context, log, summary));
			i++;
		}

		return tabbed;
	}
}
