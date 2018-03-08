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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

@Plugin(name = "Import Json Event Log (.json)", returnLabels = { "Imported log" }, returnTypes = {
		XLog.class }, parameterLabels = { "File" }, userAccessible = true)

@UIImportPlugin(description = "Import Json Event Log (.json)", extensions = { "json", "gz" })
public class JsonParserPlugin extends AbstractImportPlugin {

	@Override
	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		
		try(InputStreamReader reader = (filename.toLowerCase().endsWith("gz")) ? new InputStreamReader(new GZIPInputStream(input)) : new InputStreamReader(input)) {
			JsonParser parser = new JsonParser();
			return parser.parseJson(reader);
		}
	}

}
