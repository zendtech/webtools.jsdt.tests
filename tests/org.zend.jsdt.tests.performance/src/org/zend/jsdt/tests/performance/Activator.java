/*******************************************************************************
 * Copyright (c) 2013 Zend Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zend Technologies - initial API and implementation
 *******************************************************************************/
package org.zend.jsdt.tests.performance;

import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {

	private static Activator plugin;

	public Activator() {
		super();
		plugin = this;
	}

	public static Activator getDefault() {
		return plugin;
	}

}
