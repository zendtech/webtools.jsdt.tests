/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.compiler;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.tests.compiler.parser.SyntaxErrorTest;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.BasicAnalyseTests;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.BasicParserTests;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.BasicResolveTests;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.CharOperationTest;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.InferTypesTests;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.UtilTest;
import org.eclipse.wst.jsdt.core.tests.compiler.util.ExclusionTests;
import org.eclipse.wst.jsdt.core.tests.interpret.BasicInterpretTest;
import org.eclipse.wst.jsdt.core.tests.search.SearchTests;

/**
 * Run all compiler regression tests
 */
public class JSDTCompilerTests extends TestSuite {

static {
	JavaScriptCore.getPlugin().getPluginPreferences().setValue("semanticValidation", true);
}

public JSDTCompilerTests() {
	this("JavaScript Model Tests");
}

public JSDTCompilerTests(String testName) {
	super(testName);
}
public static Test suite() {

	ArrayList standardTests = new ArrayList();
	
	// regression tests
	standardTests.add(BasicParserTests.class);
	standardTests.add(InferTypesTests.class);
	standardTests.add(BasicResolveTests.class);
	standardTests.add(BasicAnalyseTests.class);
	standardTests.add(CharOperationTest.class);
	standardTests.add(UtilTest.class);
	
	// parser tests
	standardTests.add(SyntaxErrorTest.class);
	
	// interpret tests
	standardTests.add(BasicInterpretTest.class);
	

	TestSuite all = new TestSuite("JSDT 'Compiler' Tests");
	all.addTest(ExclusionTests.suite());
	
	for (Iterator iter = standardTests.iterator(); iter.hasNext();) {
		Class test = (Class) iter.next();
		all.addTestSuite(test); 
	}
	
	all.addTest(SearchTests.suite());
	return all;
} 
}
