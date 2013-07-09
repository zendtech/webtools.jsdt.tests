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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.wst.jsdt.core.JavaScriptCore;

public class JsdtPerformanceTests extends TestCase {

	static {
		JavaScriptCore.getPlugin().getPluginPreferences()
				.setValue("semanticValidation", true);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("JSDT Performance Tests");

		suite.addTest(EditorTests.suite());
		suite.addTest(ContentAssistTests.suite());
		return suite;
	}

}
