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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XTrace;
import org.processmining.clustering.model.FieldValue;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.hybridclusterminer.pattern.spmf.frequentpatterns.AlgoFPClose;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

public class FrequentPatternMiner {

	/**
	 * Extracts all closed frequent item sets from the given database file.
	 * 
	 * @param minSupport
	 * @return
	 */
	public Set<FrequentItemset> extractFrequentItemsets(List<List<Integer>> transactions, double minSupport) {
		Set<FrequentItemset> result = new HashSet<>();
		AlgoFPClose fpClose = new AlgoFPClose();

		Itemsets itemsets = fpClose.runAlgorithm(transactions, minSupport);

		// convert to simple format
		for (List<Itemset> level : itemsets.getLevels()) {
			for (Itemset itemset : level) {
				FrequentItemset items = new FrequentItemset();
				items.setSupport(itemset.support);

				Arrays.stream(itemset.getItems()).forEach(items::add);

				result.add(items);
			}
		}

		return result;
	}

	/**
	 * Writes traces with its attributes to the disk database.
	 * 
	 * @param traces
	 * @param itemsetValues
	 * @param valueMap
	 */
	public List<List<Integer>> getDatabase(List<XTrace> traces, HashMap<FieldValue, Integer> itemsetValues,
			HashMap<Integer, FieldValue> valueMap, List<String> categoricalAttributes) {

		List<List<Integer>> file = new ArrayList<>();

		for (XTrace trace : traces) {
			XAttributeMap attributes = trace.getAttributes();

			List<Integer> transaction = new ArrayList<>();

			for (String key : attributes.keySet()) {
				XAttribute attribute = attributes.get(key);

				// is categorical
				if (!categoricalAttributes.contains(key))
					continue;

				FieldValue value = new FieldValue(key, XUtils.getAttributeValue(attribute).toString());

				Integer itemsetValue = itemsetValues.get(value);
				if (itemsetValue == null) {
					itemsetValue = itemsetValues.size() + 1;
					itemsetValues.put(value, itemsetValue);
					valueMap.put(itemsetValue, value);
				}

				transaction.add(itemsetValue);
			}

			file.add(transaction);
		}

		return file;
	}
}
