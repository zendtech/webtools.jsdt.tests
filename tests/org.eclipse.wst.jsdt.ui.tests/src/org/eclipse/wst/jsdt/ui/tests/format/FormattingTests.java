/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.ui.tests.format;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IContentFormatterExtension;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.formatter.comment.SingleCommentLine;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;
import org.eclipse.wst.jsdt.ui.text.JavaScriptTextTools;

public class FormattingTests extends TestCase {
	public static final String DELIMITER = TextUtilities.getDefaultLineDelimiter(new Document());
	protected static final String PREFIX = SingleCommentLine.SINGLE_COMMENT_PREFIX;

	/** tools used to set up document for formatting */
	private static JavaScriptTextTools fJavaScriptTextTools;

	/** context used for formatting */
	private static FormattingContext fFormattingContext;

	/** formatter used for formatting */
	private static IContentFormatterExtension fFormatter;

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
	public FormattingTests() {
		super("Formatting Tests");
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
	public FormattingTests(String name) {
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
		TestSuite ts = new TestSuite(FormattingTests.class, "Formatting Tests");
		return new FormattingTestsSetup(ts);
	}

	public void testLongMultipleComments() {
		String beforeContents = "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
				+ DELIMITER
				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
				+ DELIMITER;
		String afterContents = PREFIX + "this is a really long comment that will have to be wrapped into multiple"
				+ DELIMITER + PREFIX + "lines because it is so very very long" + DELIMITER + PREFIX
				+ "this is a really long comment that will have to be wrapped into multiple" + DELIMITER + PREFIX
				+ "lines because it is so very very long" + DELIMITER;

		runFormatTest(beforeContents, afterContents);
	}

	public void testNestedLongMultipleComments() {
		String beforeContents = "dojo.declare(\"myDojo.Test\", [], {"
				+ DELIMITER
				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
				+ DELIMITER
				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
				+ DELIMITER
				+ DELIMITER
				+ "//this is a shorter comment"
				+ DELIMITER
				+ "constructor : function() {"
				+ DELIMITER
				+ "}"
				+ DELIMITER
				+ "//this is a really long comment that will have to be wrapped into multiple lines because it is so very very long"
				+ DELIMITER + "});";
		String afterContents = "dojo.declare(\"myDojo.Test\", [], {" + DELIMITER + "\t" + PREFIX
				+ "this is a really long comment that will have to be wrapped into multiple" + DELIMITER + "\t"
				+ PREFIX + "lines because it is so very very long" + DELIMITER + "\t" + PREFIX
				+ "this is a really long comment that will have to be wrapped into multiple" + DELIMITER + "\t"
				+ PREFIX + "lines because it is so very very long" + DELIMITER + DELIMITER + "\t" + PREFIX
				+ "this is a shorter comment" + DELIMITER + "\t" + "constructor : function() {" + DELIMITER + "\t"
				+ "}" + DELIMITER + PREFIX + "this is a really long comment that will have to be wrapped into multiple"
				+ DELIMITER + PREFIX + "lines because it is so very very long" + DELIMITER + "});";

		runFormatTest(beforeContents, afterContents);
	}

	/**
	 * <p>
	 * Formats the given <code>beforeContents</code> and compares it to the given
	 * <code>afterContents</code>
	 * </p>
	 * 
	 * @param beforeContents
	 *            format this contents and compare it to the given <code>afterContents</code>
	 * @param afterContents
	 *            compare this contents to the <code>beforeContents</code> after it has been
	 *            formated
	 */
	private static void runFormatTest(String beforeContents, String afterContents) {
		IDocument toFormat = new Document(beforeContents);
		fJavaScriptTextTools.setupJavaDocumentPartitioner(toFormat, IJavaScriptPartitions.JAVA_PARTITIONING);
		fFormatter.format(toFormat, fFormattingContext);
		assertEquals("The formatted document does not have the expected contents", afterContents, toFormat.get());
	}

	/**
	 * <p>
	 * This inner class is used to do set up and tear down before and after (respectively) all tests
	 * in the inclosing class have run.
	 * </p>
	 */
	private static class FormattingTestsSetup extends TestSetup {
		private static final String WTP_AUTOTEST_NONINTERACTIVE = "wtp.autotest.noninteractive";
		private static String previousWTPAutoTestNonInteractivePropValue = null;

		/**
		 * Default constructor
		 * 
		 * @param test
		 *            do setup for the given test
		 */
		public FormattingTestsSetup(Test test) {
			super(test);
		}

		/**
		 * <p>
		 * This is run once before all of the tests
		 * </p>
		 * 
		 * @see junit.extensions.TestSetup#setUp()
		 */
		public void setUp() throws Exception {
			// set up formatting tools
			fJavaScriptTextTools = JavaScriptPlugin.getDefault().getJavaTextTools();
			JavaScriptSourceViewerConfiguration config = new JavaScriptSourceViewerConfiguration(
					fJavaScriptTextTools.getColorManager(), JavaScriptPlugin.getDefault().getCombinedPreferenceStore(),
					null, IJavaScriptPartitions.JAVA_PARTITIONING);
			fFormatter = (IContentFormatterExtension) config.getContentFormatter(null);

			fFormattingContext = new FormattingContext();
			fFormattingContext.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, JavaScriptCore.getOptions());
			fFormattingContext.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.valueOf(true));

			// set non-interactive
			String noninteractive = System.getProperty(WTP_AUTOTEST_NONINTERACTIVE);
			if(noninteractive != null) {
				previousWTPAutoTestNonInteractivePropValue = noninteractive;
			} else {
				previousWTPAutoTestNonInteractivePropValue = "false";
			}
			System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, "true");
		}

		/**
		 * <p>
		 * This is run once after all of the tests have been run
		 * </p>
		 * 
		 * @see junit.extensions.TestSetup#tearDown()
		 */
		public void tearDown() throws Exception {
			// reset non-interactive
			if(previousWTPAutoTestNonInteractivePropValue != null) {
				System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, previousWTPAutoTestNonInteractivePropValue);
			}
		}
	}
}
