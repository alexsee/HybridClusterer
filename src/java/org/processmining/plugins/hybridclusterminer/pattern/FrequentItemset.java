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
package org.processmining.plugins.hybridclusterminer.pattern;

import java.util.HashSet;
import java.util.stream.Collectors;

public class FrequentItemset extends HashSet<Integer> {

	private static final long serialVersionUID = 1L;
	
	private double support;

    @Override
    public String toString() {
        return this.stream().map(x -> x.toString()).collect(Collectors.joining(","));
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }
}
