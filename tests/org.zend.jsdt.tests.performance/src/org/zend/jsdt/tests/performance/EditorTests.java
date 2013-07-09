/*******************************************************************************
 * Copyright (c) 2013 Zend Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Zend Technologies - initial API and implementation
 *******************************************************************************/
package org.zend.jsdt.tests.performance;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
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
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaEditor;
import org.junit.BeforeClass;
import org.zend.jsdt.tests.performance.utils.FileUtil;
import org.zend.jsdt.tests.performance.utils.ProjectUnzipUtility;

public class EditorTests extends TestCase {

	private static final String PROJECT_NAME = "project1";
	private static final String CONTENT_DIR = "src";
	private static final String ZIP_FOLDER = "resources";
	private static IProject fProject;
	private static Map<IFile, JavaEditor> fFileToEditorMap = new HashMap<IFile, JavaEditor>();

	public EditorTests(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new EditorTestsSetup(new TestSuite(EditorTests.class,
				"Tests JSDT Editor Performnace"));
	}

	public void testOpenFile1() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					IFile file = getFile("file1.js");
					JavaEditor editor = getEditor(file);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			protected void tearDown() {
				IFile file = getFile("file1.js");
				closeEditor(file);
			}
		};
		runner.run(this, 10, 1);
	}

	public void testCloseFile1() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void setUp() {
				try {
					IFile file = getFile("file1.js");
					JavaEditor editor = getEditor(file);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			protected void test() {
				IFile file = getFile("file1.js");
				closeEditor(file);
			}
		};
		runner.run(this, 10, 1);
	}

	public void testOpenFile3() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					IFile file = getFile("file3.js");
					JavaEditor editor = getEditor(file);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			protected void tearDown() {
				IFile file = getFile("file3.js");
				closeEditor(file);
			}
		};
		runner.run(this, 10, 1);
	}

	public void testCloseFile3() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void setUp() {
				try {
					IFile file = getFile("file3.js");
					JavaEditor editor = getEditor(file);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			protected void test() {
				IFile file = getFile("file3.js");
				closeEditor(file);
			}
		};
		runner.run(this, 10, 1);
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
					// standardizeLineEndings(editor);
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

	private static void closeEditor(IFile file) {
		JavaEditor editor = (JavaEditor) fFileToEditorMap.remove(file);

		if (editor != null)
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().closeEditor(editor, false);
	}

	private static void closeAllEditors() {
		Collection<JavaEditor> editors = fFileToEditorMap.values();
		for (JavaEditor editor : editors)
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().closeEditor(editor, true);
		fFileToEditorMap.clear();
	}

	private static class EditorTestsSetup extends TestSetup {

		private static final String WTP_AUTOTEST_NONINTERACTIVE = "wtp.autotest.noninteractive";
		private static String previousWTPAutoTestNonInteractivePropValue = null;

		public EditorTestsSetup(Test test) {
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
						.getActivePage().closeEditor(editor, true);

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
