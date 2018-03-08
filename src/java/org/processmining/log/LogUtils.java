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
package org.processmining.log;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.*;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.processmining.clustering.model.Variant;
import org.processmining.json.JsonParser;
import org.processmining.log.utils.XUtils;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class LogUtils {

    /**
     * Returns the number of different activities present in the log.
     *
     * @param log
     * @return
     */
    public static int getNumberOfActivities(XLog log) {
        XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
        return logInfo.getEventClasses().size();
    }

    /**
     * Calculates the trace variances and filters out variants that do not occur at
     * least 2 times.
     *
     * @param log
     * @return
     */
    public static Map<Variant, List<XTrace>> getLogVariants(XLog log) {
        return getLogVariants(log, true);
    }

    /**
     * Generates a map of variants with the corresponding XTrace elements as values.
     *
     * @param log
     * @param filter Determine if variants with less than 2 occurrences should be
     *               filtered.
     * @return
     */
    public static Map<Variant, List<XTrace>> getLogVariants(XLog log, boolean filter) {
        XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
        Map<Variant, List<XTrace>> variantTraceMap = new HashMap<>();

        // for each trace in the event log
        for (XTrace trace : log) {
            Variant variant = encodeXTrace(logInfo, trace);

            // add to variant map
            List<XTrace> traces = variantTraceMap.getOrDefault(variant, new ArrayList<>());
            traces.add(trace);

            variantTraceMap.put(variant, traces);
        }

        if (filter) {
            for (Variant variant : new HashSet<>(variantTraceMap.keySet())) {
                List<XTrace> traces = variantTraceMap.get(variant);
                if (traces.size() <= 2) {
                    variantTraceMap.remove(variant);
                }
            }
        }

        return variantTraceMap;
    }

    /**
     * Encodes a given trace to a variant.
     *
     * @param logInfo
     * @param trace
     * @return
     */
    public static Variant encodeXTrace(XLogInfo logInfo, XTrace trace) {
        Variant variant = new Variant();
        for (XEvent event : trace) {
            XEventClass eventClass = logInfo.getEventClasses().getClassOf(event);
            variant.add(eventClass.getIndex());
        }
        return variant;
    }

    /**
     * Returns all filtered traces from the event log.
     *
     * @param log
     * @return
     */
    public static List<XTrace> getFilteredTraces(XLog log) {
        XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
        Map<Variant, List<XTrace>> variantTraceMap = new HashMap<>();

        // for each trace in the event log
        for (XTrace trace : log) {
            Variant variant = encodeXTrace(logInfo, trace);

            // add to variant map
            List<XTrace> traces = variantTraceMap.getOrDefault(variant, new ArrayList<>());
            traces.add(trace);

            variantTraceMap.put(variant, traces);
        }

        List<XTrace> filtered = new ArrayList<>();

        for (Variant variant : new HashSet<>(variantTraceMap.keySet())) {
            List<XTrace> traces = variantTraceMap.get(variant);
            if (traces.size() <= 2) {
                filtered.addAll(traces);
            }
        }

        return filtered;
    }

    /**
     * Reads a log from the file system. (xes and xes.gz files are supported)
     *
     * @param file
     * @return
     */
    public static XLog readLog(String file) {
        XesXmlParser parser = null;

        if (file.endsWith(".xes.gz")) {
            parser = new XesXmlGZIPParser(XFactoryRegistry.instance().currentDefault());
        } else if (file.endsWith(".json.gz")) {
            return readJsonLog(file);
        } else {
            parser = new XesXmlParser(XFactoryRegistry.instance().currentDefault());
        }

        try {
            List<XLog> logs = parser.parse(new File(file));
            return logs.get(0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static XLog readJsonLog(String file) {
        try (FileInputStream input = new FileInputStream(file)) {
            try (InputStreamReader reader = (file.toLowerCase().endsWith("gz"))
                    ? new InputStreamReader(new GZIPInputStream(input))
                    : new InputStreamReader(input)) {
                JsonParser parser = new JsonParser();
                return parser.parseJson(reader);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static void writeLog(XLog log, String file) {
        XesXmlGZIPSerializer serializer = new XesXmlGZIPSerializer();
        try (FileOutputStream fos = new FileOutputStream(new File(file))) {
            serializer.serialize(log, fos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns a list of all categorical attributes (those that do not have a high
     * number of different values).
     *
     * @param log
     * @return
     */
    public static List<String> getCategoricalFields(XLog log) {
        Map<String, Map<String, Integer>> fieldCount = new HashMap<>();

        for (XTrace trace : log) {
            XAttributeMap attributes = trace.getAttributes();

            for (String key : attributes.keySet()) {
                XAttribute attribute = attributes.get(key);
                String value = XUtils.getAttributeValue(attribute).toString();

                if (key.equals(XConceptExtension.ATTR_NAME.getKey()) || key.equals("cluster") || key.equals("label"))
                    continue;

                Map<String, Integer> valueCount = fieldCount.getOrDefault(key, new HashMap<>());
                Integer cnt = valueCount.getOrDefault(value, 0);
                cnt++;

                valueCount.put(value, cnt);
                fieldCount.put(key, valueCount);
            }
        }

        List<String> categoricalFields = new ArrayList<>();

        for (String field : fieldCount.keySet()) {
            Map<String, Integer> values = fieldCount.get(field);

            int unique = 0;
            for (Integer cnt : values.values()) {
                if (cnt == 1)
                    unique++;
            }

            if ((double) unique / (double) log.size() <= 0.01) {
                categoricalFields.add(field);
            }
        }

        return categoricalFields;
    }

    /**
     * Returns for each categorical attribute in the event log a set of values contained in the log.
     *
     * @param log
     * @return
     */
    public static Map<String, List<String>> getAttributeValues(XLog log) {
        Map<String, Set<String>> values = new HashMap<>();

        for (String attributeName : getCategoricalFields(log)) {
            values.put(attributeName, new HashSet<>());
        }

        for (XTrace trace : log) {
            for (String attributeName : values.keySet()) {
                XAttribute attribute = trace.getAttributes().get(attributeName);
                if (attribute != null) {
                    Object value = XUtils.getAttributeValue(attribute);

                    Set<String> attributeValues = values.get(attributeName);
                    attributeValues.add(value.toString());
                    values.put(attributeName, attributeValues);
                }
            }
        }

        // now sort the values
        Map<String, List<String>> sortedValues = new HashMap<>();
        for (String attributeName : getCategoricalFields(log)) {
            sortedValues.put(attributeName, new ArrayList<>(values.get(attributeName)));
        }

        return sortedValues;
    }

    public static List<String> getNumericValues(XLog log) {
        List<String> attributeNames = new ArrayList<>();

        for (XTrace trace : log) {
            XAttributeMap attributes = trace.getAttributes();

            for (String key : attributes.keySet()) {
                XAttribute attribute = attributes.get(key);
                Object value = XUtils.getAttributeValue(attribute);

                if (key.equals(XConceptExtension.ATTR_NAME.getKey()) || key.equals("cluster") || key.equals("label"))
                    continue;

                if (value instanceof Double || value instanceof Integer || value instanceof Float) {
                    attributeNames.add(key);
                }
            }
        }

        return attributeNames;
    }

}
