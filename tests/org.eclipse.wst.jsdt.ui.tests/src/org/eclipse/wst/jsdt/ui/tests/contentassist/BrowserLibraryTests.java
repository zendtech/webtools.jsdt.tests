/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.ui.tests.contentassist;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.wst.jsdt.ui.tests.utils.TestProjectSetup;

public class BrowserLibraryTests extends TestCase {
	/**
	 * <p>
	 * This tests name
	 * </p>
	 */
	private static final String TEST_NAME = "Test Elements Defined by Browser Library";

	/**
	 * <p>
	 * Test project setup for this test.
	 * </p>
	 */
	private static TestProjectSetup fTestProjectSetup;
	
	/**
	 * <p>
	 * Default constructor
	 * <p>
	 * <p>
	 * Use {@link #suite()}
	 * </p>
	 * 
	 * @see #suite()
	 */
	public BrowserLibraryTests() {
		super(TEST_NAME);
	}

	/**
	 * <p>
	 * Constructor that takes a test name.
	 * </p>
	 * <p>
	 * Use {@link #suite()}
	 * </p>
	 * 
	 * @param name
	 *            The name this test run should have.
	 * 
	 * @see #suite()
	 */
	public BrowserLibraryTests(String name) {
		super(name);
	}

	/**
	 * <p>
	 * Use this method to add these tests to a larger test suite so set up and tear down can be
	 * performed
	 * </p>
	 * 
	 * @return a {@link TestSetup} that will run all of the tests in this class
	 *         with set up and tear down.
	 */

	public static Test suite() {
		TestSuite ts = new TestSuite(BrowserLibraryTests.class, TEST_NAME);
		
		fTestProjectSetup = new TestProjectSetup(ts, "ContentAssist", "root", false);
		
		return fTestProjectSetup;
	}

	public void testDocumentDotG() throws Exception {
		String[][] expectedProposals = new String[][] { { "getElementById(String elementId) : Element - Document",
				"getElementsByName(String elementName) : NodeList - HTMLDocument",
				"getElementsByTagName(String tagname) : NodeList - Document",
				"getElementsByTagNameNS(String namespaceURI, String localName) : NodeList - Document" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 0, 10, expectedProposals);
	}

	public void testAlert() throws Exception {
		String[][] expectedProposals = new String[][] { { "alert(String message) - Window" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 2, 2, expectedProposals);
	}

	public void testDocument() throws Exception {
		String[][] expectedProposals = new String[][] { { "document : HTMLDocument - Window" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 4, 3, expectedProposals);
	}

	public void testNavigator() throws Exception {
		String[][] expectedProposals = new String[][] { { "navigator : Navigator - Window" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 8, 3, expectedProposals);
	}

	public void testNavigatorDotA() throws Exception {
		String[][] expectedProposals = new String[][] { { "appName : String - Navigator",
				"appVersion : String - Navigator", "availHeight : Number - Navigator",
				"availWidth : Number - Navigator" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 6, 11, expectedProposals);
	}

	public void testNavigatorDotC_AfterJSDoc_InsideFunction() throws Exception {
		String[][] expectedProposals = new String[][] { { "colorDepth : Number - Navigator",
				"constructor : Function - Object", "cookieEnabled : Boolean - Navigator" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 22, 15, expectedProposals);
	}

	public void testFindNewObjectOnNavigator_SameFile() throws Exception {
		String[][] expectedProposals = new String[][] { { "colorDepth : Number - Navigator",
				"constructor : Function - Object", "contacts : Contacts - Navigator", "cookieEnabled : Boolean - Navigator" } };
		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_0.js", 27, 11, expectedProposals);
	}

	//	public void testFindNewObjectOnNavigator_OtherFile() throws Exception {
	//		String[][] expectedProposals = new String[][] { { "colorDepth : Number - Navigator",
	//				"constructor : Function - Object", "contacts - Navigator", "cookieEnabled : Boolean - Navigator" } };
	//		ContentAssistTestUtilities.runProposalTest(fTestProjectSetup, "TestBrowserLibrary_1.js", 0, 11, expectedProposals);
	//	}
}