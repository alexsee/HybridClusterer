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
package org.processmining.extensions;

import org.processmining.contexts.uitopia.UIPluginContext;

public class AbstractPlugin {
	
	private UIPluginContext context;

	
	public void setContext(UIPluginContext context) {
		this.context = context;
	}
	
	public UIPluginContext getContext() {
		return context;
	}
	
	public void log(String text) {
		context.log(text);
	}
	
	public void setProgressInterminate(boolean makeIndeterminate) {
		context.getProgress().setIndeterminate(makeIndeterminate);
	}

}
