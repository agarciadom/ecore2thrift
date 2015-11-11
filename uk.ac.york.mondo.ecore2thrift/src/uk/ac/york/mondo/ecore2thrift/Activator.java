/*******************************************************************************
 * Copyright (c) 2015 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nathan van Doorn - initial API and implementation
 *     Antonio Garcia-Dominguez - later improvements
 ******************************************************************************/
package uk.ac.york.mondo.ecore2thrift;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.FrameworkUtil;

public class Activator extends AbstractUIPlugin {
	private static String PLUGIN_ID;
	private static Activator activator;

	public void logError(String msg, Exception e) {
		getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
	}

	public void logInfo(String msg) {
		getLog().log(new Status(Status.INFO, PLUGIN_ID, msg));
	}

	public Activator() {
		activator = this;
		PLUGIN_ID = FrameworkUtil.getBundle(Activator.class).getSymbolicName();
	}

	public static Activator getPlugin() {
		return activator;
	}

}
