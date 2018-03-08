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

import org.deckfour.xes.model.XLog;
import org.processmining.clustering.evaluator.model.EventLogClusters;
import org.processmining.clustering.model.VariantCluster;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Event Log Clusters", parameterLabels = { "Log" }, returnLabels = {
"XLogs" }, returnTypes = {
		Object.class }, userAccessible = true, handlesCancel = true, categories = {
				PluginCategory.Discovery, PluginCategory.Analytics })
public class EventLogClustersPlugin {

	@PluginVariant(variantLabel = "Export as XLog", requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "Technische Universität Darmstadt", author = "A. Seeliger", email = "seeliger@tk.tu-darmstadt.de", pack = "HybridClusterer")
	public Object export(UIPluginContext context, EventLogClusters clusters) {
		int i = 1;
		for(VariantCluster cluster : clusters.getClusters()) {
			context.getProvidedObjectManager().createProvidedObject(
				    "Cluster" + i, cluster.getLog(), XLog.class, context);
			i++;
		}
		
		return null;
	}
	
}
