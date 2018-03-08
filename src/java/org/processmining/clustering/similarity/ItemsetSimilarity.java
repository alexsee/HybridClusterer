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

import org.processmining.clustering.model.Itemset;

public class ItemsetSimilarity {

	public static double calculateItemsetDistance(Itemset i1, Itemset i2) {
		Itemset tmp = new Itemset();
		tmp.addAll(i1);
		tmp.retainAll(i2);

		double inBoth = tmp.size();
		return 1 - (2 * inBoth / (i1.size() + i2.size()));
	}

}
