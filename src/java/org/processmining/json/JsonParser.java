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
package org.processmining.json;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

	public static final XEventClassifier STANDARDCLASSIFIER = new XEventAndClassifier(new XEventNameClassifier(),
			new XEventLifeTransClassifier());

	private XFactory factory = XFactoryRegistry.instance().currentDefault();
	
	public XLog parseJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		XLog xlog = factory.createLog();
		
		XAttribute logname = factory.createAttributeLiteral(XConceptExtension.KEY_NAME, "Imported from json",
				XConceptExtension.instance());
		xlog.getAttributes().put(logname.getKey(), logname);

		// classifiers
		xlog.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		xlog.getClassifiers().add(STANDARDCLASSIFIER);

		// add extensions
		xlog.getExtensions().add(XConceptExtension.instance());
		xlog.getExtensions().add(XTimeExtension.instance());
		xlog.getExtensions().add(XOrganizationalExtension.instance());

		ObjectMapper mapper = new ObjectMapper();
		JsonEventLog log = mapper.readValue(reader, JsonEventLog.class);

		// convert traces
		for (JsonTrace jtrace : log.getTraces()) {
			XTrace trace = factory.createTrace();

			// concept name
			XAttribute name = factory.createAttributeLiteral(XConceptExtension.KEY_NAME, jtrace.getId(),
					XConceptExtension.instance());
			trace.getAttributes().put(name.getKey(), name);

			// convert all attributes
			for (String attributeName : jtrace.getAttributes().keySet()) {
				XAttribute attribute = factory.createAttributeLiteral(attributeName,
						jtrace.getAttributes().get(attributeName).toString(), XConceptExtension.instance());
				trace.getAttributes().put(attribute.getKey(), attribute);
			}

			for (JsonEvent jevent : jtrace.getEvents()) {
				XEvent event = factory.createEvent();

				// add event name
				XAttribute eventName = factory.createAttributeLiteral(XConceptExtension.KEY_NAME, jevent.getName(),
						XConceptExtension.instance());
				event.getAttributes().put(eventName.getKey(), eventName);

				// add timestamp
				if (jevent.getTimestamp() != null) {
					XAttribute eventTimestamp = factory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP,
							jevent.getTimestamp(), XTimeExtension.instance());
					event.getAttributes().put(eventTimestamp.getKey(), eventTimestamp);
				}
				
				// add complete lifecycle
				XAttribute lifecycle = factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, "complete",
						XConceptExtension.instance());
				event.getAttributes().put(lifecycle.getKey(), lifecycle);
				
				// convert all event attributes
				for (String attributeName : jevent.getAttributes().keySet()) {
					if(attributeName.equals("user")) {
						XAttribute attribute = factory.createAttributeLiteral(XOrganizationalExtension.KEY_RESOURCE,
								jevent.getAttributes().get(attributeName).toString(), XOrganizationalExtension.instance());
						event.getAttributes().put(attribute.getKey(), attribute);
					} else {
						XAttribute attribute = factory.createAttributeLiteral(attributeName,
								jevent.getAttributes().get(attributeName).toString(), XConceptExtension.instance());
						event.getAttributes().put(attribute.getKey(), attribute);
					}
				}

				trace.add(event);
			}

			xlog.add(trace);
		}
		
		return xlog;
	}

	public XLog parseJson(String file) {
		// read json
		try (FileReader reader = new FileReader(file)) {

			return parseJson(reader);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static void main(String[] args) {

		JsonParser parser = new JsonParser();
		XLog log = parser.parseJson("test/resources/p2p-0.1-1.json");
		
		XesXmlGZIPSerializer serializer = new XesXmlGZIPSerializer();
		
		try(FileOutputStream fos = new FileOutputStream("test/resources/p2p-0.1-1.xes.gz")) {
			serializer.serialize(log, fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
