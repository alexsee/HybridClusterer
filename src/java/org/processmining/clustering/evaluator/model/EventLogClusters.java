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

import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.clustering.model.VariantCluster;

public class EventLogClusters {

	private int logSize = 0;

	private double minSupport = 0;

	private List<VariantCluster> clusters;

	private XLog log;

	private double silhouetteCoefficient = 0;

	private double weighting = 0;

	public EventLogClusters() {

	}

	public EventLogClusters(List<VariantCluster> clusters, int logSize, XLog log) {
		this.clusters = clusters;
		this.logSize = logSize;
		this.log = log;
	}

	public List<VariantCluster> getClusters() {
		return this.clusters;
	}

	public int getLogSize() {
		return logSize;
	}

	public void setLogSize(int logSize) {
		this.logSize = logSize;
	}

	public double getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(double minSupport) {
		this.minSupport = minSupport;
	}

	public XLog getLog() {
		return log;
	}

	public void setLog(XLog log) {
		this.log = log;
	}

	public double getSilhouetteCoefficient() {
		return silhouetteCoefficient;
	}

	public void setSilhouetteCoefficient(double silhouetteCoefficient) {
		this.silhouetteCoefficient = silhouetteCoefficient;
	}

	public double getWeighting() {
		return weighting;
	}

	public void setWeighting(double weighting) {
		this.weighting = weighting;
	}
}
