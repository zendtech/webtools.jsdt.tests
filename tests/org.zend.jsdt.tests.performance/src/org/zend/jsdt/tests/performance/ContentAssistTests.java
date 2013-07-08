/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation, Zend Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.zend.jsdt.tests.performance;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;
import org.junit.BeforeClass;
import org.zend.jsdt.tests.performance.utils.FileUtil;
import org.zend.jsdt.tests.performance.utils.ProjectUnzipUtility;
import org.zend.jsdt.tests.performance.utils.StringUtils;

public class ContentAssistTests extends TestCase {

	private static final String PROJECT_NAME = "ContentAssist";
	private static final String CONTENT_DIR = "src";
	private static final String ZIP_FOLDER = "resources";
	private static IProject fProject;
	private static Map<IFile, JavaEditor> fFileToEditorMap = new HashMap<IFile, JavaEditor>();

	public ContentAssistTests(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new ContentAssistTestsSetup(new TestSuite(
				ContentAssistTests.class,
				"Tests JSDT Content Assist Performnace"));
	}

	public void testCA() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		};
		runner.run(this, 10, 5);
	}

	private static void runProposalTest(String fileName, int lineNum,
			int lineRelativeCharOffset, String[][] expectedProposals)
			throws Exception {

		IFile file = getFile(fileName);
		JavaEditor editor = getEditor(file);
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		int offset = doc.getLineOffset(lineNum) + lineRelativeCharOffset;

		ICompletionProposal[][] pages = getProposals(editor, offset,
				expectedProposals.length);

		verifyExpectedProposal(pages, expectedProposals);
	}

	private static IFile getFile(String name) {
		IFile file = fProject.getFile(CONTENT_DIR + IPath.SEPARATOR + name);
		assertTrue("Test file " + file + " can not be found", file.exists());

		return file;
	}

	private static JavaEditor getEditor(IFile file) {
		JavaEditor editor = (JavaEditor) fFileToEditorMap.get(file);

		if (editor == null) {
			try {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				IWorkbenchPage page = workbenchWindow.getActivePage();
				IEditorPart editorPart = IDE.openEditor(page, file,
						"org.eclipse.wst.jsdt.ui.CompilationUnitEditor", true);
				if (editorPart instanceof JavaEditor) {
					editor = (JavaEditor) editorPart;
				} else {
					fail("Unable to open intended editor: "
							+ editorPart.getClass().getName());
				}

				if (editor != null) {
					standardizeLineEndings(editor);
					fFileToEditorMap.put(file, editor);
				} else {
					fail("Could not open editor for " + file);
				}
			} catch (Exception e) {
				fail("Could not open editor for " + file + " exception: "
						+ e.getMessage());
			}
		}

		return editor;
	}

	private static ICompletionProposal[][] getProposals(JavaEditor editor,
			int offset, int pageCount) throws Exception {
		// setup the viewer
		JavaScriptSourceViewerConfiguration configuration = new JavaScriptSourceViewerConfiguration(
				JavaScriptPlugin.getDefault().getJavaTextTools()
						.getColorManager(), JavaScriptPlugin.getDefault()
						.getCombinedPreferenceStore(), editor,
				IJavaScriptPartitions.JAVA_PARTITIONING);
		ISourceViewer viewer = editor.getViewer();
		ContentAssistant contentAssistant = (ContentAssistant) configuration
				.getContentAssistant(viewer);

		// get the processor
		String partitionTypeID = viewer.getDocument().getPartition(offset)
				.getType();
		IContentAssistProcessor processor = contentAssistant
				.getContentAssistProcessor(partitionTypeID);

		// fire content assist session about to start
		Method privateFireSessionBeginEventMethod = ContentAssistant.class
				.getDeclaredMethod("fireSessionBeginEvent",
						new Class[] { boolean.class });
		privateFireSessionBeginEventMethod.setAccessible(true);
		privateFireSessionBeginEventMethod.invoke(contentAssistant,
				new Object[] { Boolean.TRUE });

		// get content assist suggestions
		ICompletionProposal[][] pages = new ICompletionProposal[pageCount][];
		for (int p = 0; p < pageCount; ++p) {
			pages[p] = processor.computeCompletionProposals(viewer, offset);
		}

		// fire content assist session ending
		Method privateFireSessionEndEventMethod = ContentAssistant.class
				.getDeclaredMethod("fireSessionEndEvent", null);
		privateFireSessionEndEventMethod.setAccessible(true);
		privateFireSessionEndEventMethod.invoke(contentAssistant, null);

		return pages;
	}

	private static void verifyExpectedProposal(ICompletionProposal[][] pages,
			String[][] expectedProposals) {
		StringBuffer error = new StringBuffer();
		for (int page = 0; page < expectedProposals.length; ++page) {
			for (int expected = 0; expected < expectedProposals[page].length; ++expected) {
				String expectedProposal = expectedProposals[page][expected];
				boolean found = false;
				for (int suggestion = 0; suggestion < pages[page].length
						&& !found; ++suggestion) {
					found = pages[page][suggestion].getDisplayString()
							.startsWith(expectedProposal);
				}

				if (!found) {
					error.append("\nExpected proposal was not found on page "
							+ page + ": '" + expectedProposal + "'");
				}
			}
		}

		// if errors report them
		if (error.length() > 0) {
			Assert.fail(error.toString());
		}
	}

	private static void standardizeLineEndings(ITextEditor editor) {
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		String contents = doc.get();
		contents = StringUtils.replace(contents, "\r\n", "\n");
		contents = StringUtils.replace(contents, "\r", "\n");
		doc.set(contents);
	}

	private static class ContentAssistTestsSetup extends TestSetup {

		private static final String WTP_AUTOTEST_NONINTERACTIVE = "wtp.autotest.noninteractive";
		private static String previousWTPAutoTestNonInteractivePropValue = null;

		public ContentAssistTestsSetup(Test test) {
			super(test);
		}

		@BeforeClass
		public void setUp() throws Exception {
			// Setup properties
			String noninteractive = System
					.getProperty(WTP_AUTOTEST_NONINTERACTIVE);
			if (noninteractive != null) {
				previousWTPAutoTestNonInteractivePropValue = noninteractive;
			} else {
				previousWTPAutoTestNonInteractivePropValue = "false";
			}
			System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, "true");

			// Setup project
			ProjectUnzipUtility fProjUtil = new ProjectUnzipUtility();
			Location platformLocation = Platform.getInstanceLocation();

			// Platform location may be null -- depends on "mode" of platform
			if (platformLocation != null) {
				File zipFile = FileUtil.makeFileFor(ZIP_FOLDER, PROJECT_NAME
						+ ProjectUnzipUtility.ZIP_EXTENSION, ZIP_FOLDER);
				fProjUtil.unzipAndImport(zipFile, platformLocation.getURL()
						.getPath());
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				fProject = root.getProject(PROJECT_NAME);

				if (!fProject.exists()) {
					fProject.create(new NullProgressMonitor());
				}
				if (!fProject.isOpen()) {
					fProject.open(new NullProgressMonitor());
				}
			}
		}

		public void tearDown() throws Exception {
			// Close the editors
			Collection<JavaEditor> editors = fFileToEditorMap.values();
			for (JavaEditor editor : editors)
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().closeEditor(editor, false);

			// Remove project
			fProject.delete(true, new NullProgressMonitor());

			// Restore properties
			if (previousWTPAutoTestNonInteractivePropValue != null) {
				System.setProperty(WTP_AUTOTEST_NONINTERACTIVE,
						previousWTPAutoTestNonInteractivePropValue);
			}
		}
	}

}
