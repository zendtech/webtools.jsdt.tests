/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.core.infer.DefaultInferrenceProvider;
import org.eclipse.wst.jsdt.core.infer.IInferEngine;
import org.eclipse.wst.jsdt.core.infer.IInferEngineExtension;
import org.eclipse.wst.jsdt.core.infer.InferEngine;
import org.eclipse.wst.jsdt.core.infer.InferOptions;
import org.eclipse.wst.jsdt.core.infer.InferrenceProvider;
import org.eclipse.wst.jsdt.core.search.SearchDocument;
import org.eclipse.wst.jsdt.core.search.SearchParticipant;
import org.eclipse.wst.jsdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.wst.jsdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.wst.jsdt.core.tests.util.CompilerTestSetup;
import org.eclipse.wst.jsdt.core.tests.util.Util;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.Compiler;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ICompilerRequestor;
import org.eclipse.wst.jsdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.wst.jsdt.internal.compiler.IProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.SourceJavadocParser;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.batch.FileSystem;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.env.INameEnvironment;
import org.eclipse.wst.jsdt.internal.compiler.env.ISourceType;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.wst.jsdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.wst.jsdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.wst.jsdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.wst.jsdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.wst.jsdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.wst.jsdt.internal.oaametadata.LibraryAPIs;

public abstract class AbstractRegressionTest extends AbstractCompilerTest
		implements StopableTestCase {
	// javac comparison related types, fields and methods - see runJavac for
	// details
	class Logger extends Thread {
		StringBuffer buffer;
		InputStream inputStream;
		String type;

		Logger(InputStream inputStream, String type) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = new StringBuffer();
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(this.inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					this.buffer./* append(this.type).append("->"). */append(
							line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected static int[] DIFF_COUNTERS = new int[3];
	protected static final String EVAL_DIRECTORY = Util.getOutputDirectory()
			+ File.separator + "eval";
	public static int INDENT = 2;
	protected static final String JAVA_NAME = File.pathSeparatorChar == ':' ? "java"
			: "java.exe";
	protected static final String JAVAC_NAME = File.pathSeparatorChar == ':' ? "javac"
			: "javac.exe";

	protected static String JAVAC_OUTPUT_DIR = Util.getOutputDirectory()
			+ File.separator + "javac";
	protected static String javacCommandLineHeader;
	protected static PrintWriter javacFullLog;
	// flags errors so that any error in a test case prevents
	// java execution
	private static String javacFullLogFileName;
	protected static String javaCommandLineHeader;

	// needed for multiple test calls within a single test method
	protected static boolean javacTestErrorFlag;

	protected static String javacTestName;

	protected static IPath jdkRootDirPath;

	public static final String OUTPUT_DIR = Util.getOutputDirectory()
			+ File.separator + "regression";

	public final static String PACKAGE_INFO_NAME = new String(
			TypeConstants.PACKAGE_INFO_NAME);

	public static boolean SHIFT = false;

	protected static final String SOURCE_DIRECTORY = Util.getOutputDirectory()
			+ File.separator + "source";

	public static final String INFERENCE_ENGINES = "InferenceEnginesOption";

	protected String[] classpaths;
//	protected boolean createdVerifier;
	protected INameEnvironment javaClassLib;
//	protected TestVerifier verifier;

	public AbstractRegressionTest(String name) {
		super(name);
	}

	// protected void checkClassFile(String className, String source, String
	// expectedOutput) throws ClassFormatException, IOException {
	// // this.checkClassFile("", className, source, expectedOutput,
	// ClassFileBytesDisassembler.SYSTEM);
	// }
	// protected void checkClassFile(String className, String source, String
	// expectedOutput, int mode) throws ClassFormatException, IOException {
	// this.checkClassFile("", className, source, expectedOutput, mode);
	// }
	// protected void checkClassFile(String directoryName, String className,
	// String disassembledClassName, String source, String expectedOutput, int
	// mode) throws ClassFormatException, IOException {
	// // compileAndDeploy(source, directoryName, className);
	// // try {
	// // File directory = new File(EVAL_DIRECTORY, directoryName);
	// // if (!directory.exists()) {
	// // assertTrue(".class file not generated properly in " + directory,
	// false);
	// // }
	// // File f = new File(directory, disassembledClassName + ".class");
	// // byte[] classFileBytes =
	// org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(f);
	// // ClassFileBytesDisassembler disassembler =
	// ToolFactory.createDefaultClassFileBytesDisassembler();
	// // String result = disassembler.disassemble(classFileBytes, "\n", mode);
	// // int index = result.indexOf(expectedOutput);
	// // if (index == -1 || expectedOutput.length() == 0) {
	// // System.out.println(Util.displayString(result, 3));
	// // }
	// // if (index == -1) {
	// // assertEquals("Wrong contents", expectedOutput, result);
	// // }
	// //
	// // try {
	// // FileInputStream stream = new FileInputStream(f);
	// // ClassFileReader.read(stream, className + ".class", true);
	// // stream.close();
	// // } catch
	// (org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFormatException e)
	// {
	// // e.printStackTrace();
	// // assertTrue("ClassFormatException", false);
	// // } catch (IOException e) {
	// // e.printStackTrace();
	// // assertTrue("IOException", false);
	// // }
	// // } finally {
	// // removeTempClass(className);
	// // }
	// }
	//
	// protected void checkClassFile(String directoryName, String className,
	// String source, String expectedOutput, int mode) throws
	// ClassFormatException, IOException {
	// this.checkClassFile(directoryName, className, className, source,
	// expectedOutput, mode);
	// }
	//
	protected void checkDisassembledClassFile(String fileName,
			String className, String expectedOutput) {
		// this.checkDisassembledClassFile(fileName, className, expectedOutput,
		// ClassFileBytesDisassembler.DETAILED);
	}

	protected void checkDisassembledClassFile(String fileName,
			String className, String expectedOutput, int mode) {
		// File classFile = new File(fileName);
		// if (!classFile.exists()) {
		// assertTrue(".class file doesn't exist", false);
		// }
		// String result = null;
		// try {
		// byte[] classFileBytes =
		// org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(classFile);
		// ClassFileBytesDisassembler disassembler =
		// ToolFactory.createDefaultClassFileBytesDisassembler();
		// result = disassembler.disassemble(classFileBytes, "\n", mode);
		// } catch (IOException e) {
		// e.printStackTrace();
		// assertTrue("Should not happen : ", false);
		// } catch (ClassFormatException e) {
		// e.printStackTrace();
		// assertTrue("Should not happen : ", false);
		// }
		// int index = result.indexOf(expectedOutput);
		// if (index == -1 || expectedOutput.length() == 0) {
		// System.out.println(Util.displayString(result, 2));
		// }
		// if (index == -1) {
		// assertEquals("Wrong contents", expectedOutput, result);
		// }
		//		
		// try {
		// FileInputStream stream = new FileInputStream(classFile);
		// ClassFileReader.read(stream, className + ".class", true);
		// stream.close();
		// } catch
		// (org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFormatException
		// e) {
		// e.printStackTrace();
		// assertTrue("ClassFormatException", false);
		// } catch (IOException e) {
		// e.printStackTrace();
		// assertTrue("IOException", false);
		// }
	}

	/*
	 * ###################################### Specific method to let tests Sun
	 * javac compilation available... #######################################
	 */
	protected void compileAndDeploy(String source, String directoryName,
			String className) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		if (directoryName != null && directoryName.length() != 0) {
			directory = new File(SOURCE_DIRECTORY, directoryName);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					System.out.println("Could not create " + directory);
					return;
				}
			}
		}
		String fileName = directory.getAbsolutePath() + File.separator
				+ className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuffer buffer = new StringBuffer().append("\"").append(fileName)
				.append("\" -d \"").append(EVAL_DIRECTORY);
		if (this.complianceLevel.compareTo(COMPLIANCE_1_5) < 0) {
			buffer.append("\" -1.4 -source 1.3 -target 1.2");
		} else {
			buffer.append("\" -1.5");
		}
		buffer.append(" -preserveAllLocals -nowarn -g -classpath \"").append(
				Util.getJavaClassLibsAsString()).append(SOURCE_DIRECTORY)
				.append("\"");
		org.eclipse.wst.jsdt.internal.compiler.batch.Main.compile(buffer
				.toString());
	}

	protected void dualPrintln(String message) {
		System.out.println(message);
		javacFullLog.println(message);
	}

	protected void executeClass(String sourceFile,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory, String[] vmArguments,
			Map customOptions, ICompilerRequestor clientRequestor) {

		// Compute class name by removing ".java" and replacing slashes with
		// dots
		String className = sourceFile.substring(0, sourceFile.length() - 5)
				.replace('/', '.').replace('\\', '.');
		if (className.endsWith(PACKAGE_INFO_NAME))
			return;

//		if (vmArguments != null) {
//			if (this.verifier != null) {
//				this.verifier.shutDown();
//			}
//			this.verifier = new TestVerifier(false);
//			this.createdVerifier = true;
//		}
//		boolean passed = this.verifier
//				.verifyClassFiles(sourceFile, className,
//						expectedSuccessOutputString, this.classpaths, null,
//						vmArguments);
//		assertTrue(this.verifier.failureReason, // computed by
//												// verifyClassFiles(...) action
//				passed);
//		if (vmArguments != null) {
//			if (this.verifier != null) {
//				this.verifier.shutDown();
//			}
//			this.verifier = new TestVerifier(false);
//			this.createdVerifier = true;
//		}
	}

	/*
	 * Returns the references in the given .class file.
	 */
	protected String findReferences(String classFilePath) {
		// check that "new Z().init()" is bound to "AbstractB.init()"
		final StringBuffer references = new StringBuffer(10);
		final SearchParticipant participant = new JavaSearchParticipant() {
			final SearchParticipant searchParticipant = this;

			public SearchDocument getDocument(final String documentPath) {
				return new SearchDocument(documentPath, this.searchParticipant) {
					public byte[] getByteContents() {
						try {
							return org.eclipse.wst.jsdt.internal.compiler.util.Util
									.getFileByteContent(new File(getPath()));
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}

					public char[] getCharContents() {
						// not used
						return null;
					}

					public String getEncoding() {
						// not used
						return null;
					}
				};
			}
		};
		SearchDocument document = participant.getDocument(new File(
				classFilePath).getPath());
		SourceIndexer indexer = new SourceIndexer(document) {
			protected void addIndexEntry(char[] category, char[] key) {
				references.append(category);
				references.append('/');
				references.append(key);
				references.append('\n');
			}
		};
		indexer.indexDocument();
		String computedReferences = references.toString();
		return computedReferences;
	}

	protected INameEnvironment[] getClassLibs() {
		String encoding = (String) getCompilerOptions().get(
				CompilerOptions.OPTION_Encoding);
		if ("".equals(encoding))
			encoding = null;

		INameEnvironment[] classLibs = new INameEnvironment[1];
		classLibs[0] = new FileSystem(this.classpaths, new String[] {}, // ignore
																		// initial
																		// file
																		// names
				encoding // default encoding
		);
		return classLibs;
	}

	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute,
				CompilerOptions.GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember,
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding,
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportFieldHiding,
				CompilerOptions.WARNING);
		defaultOptions
				.put(
						CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment,
						CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportWrongNumberOfArguments,
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal,
				CompilerOptions.PRESERVE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse,
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_Unresolved_Field,
				CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_Unresolved_Method,
				CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_Unresolved_Type,
				CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_ReportUninitializedLocalVariable, 
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportUninitializedGlobalVariable, 
				CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, 
				CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_SemanticValidation, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	protected String[] getDefaultClassPaths() {
		return Util.concatWithClassLibs(OUTPUT_DIR, false);
	}

	protected IErrorHandlingPolicy getErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
			}

			public boolean proceedOnErrors() {
				return true;
			}
		};
	}

	/*
	 * Will consider first the source units passed as arguments, then
	 * investigate the classpath: jdklib + output dir
	 */
	protected INameEnvironment getNameEnvironment(final String[] testFiles,
			String[] classPaths) {
		this.classpaths = classPaths == null ? getDefaultClassPaths()
				: classPaths;
		return new InMemoryNameEnvironment(testFiles, getClassLibs());
	}

	protected INameEnvironment getNameEnvironment(final String[] testFiles,
			String[] files, String[] classPaths) {
		this.classpaths = classPaths == null ? getDefaultClassPaths()
				: classPaths;
		InMemoryNameEnvironment inMemoryNameEnvironment = new InMemoryNameEnvironment(
				testFiles, getClassLibs());
		return inMemoryNameEnvironment;
	}

	protected IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}

	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		if (setUp instanceof RegressionTestSetup) {
			RegressionTestSetup regressionTestSetUp = (RegressionTestSetup) setUp;
			this.javaClassLib = regressionTestSetUp.javaClassLib;
//			this.verifier = regressionTestSetUp.verifier;
		}
	}

	/*
	 * Write given source test files in current output sub-directory. Use test
	 * name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void printFiles(String[] testFiles) {
		for (int i = 0, length = testFiles.length; i < length; i++) {
			System.out.println(testFiles[i++]);
			System.out.println(testFiles[i]);
		}
		System.out.println("");
	}

	protected void printJavacResultsSummary() {
		if (RUN_JAVAC) {
			Integer count = (Integer) TESTS_COUNTERS.get(CURRENT_CLASS_NAME);
			if (count != null) {
				int newCount = count.intValue() - 1;
				TESTS_COUNTERS.put(CURRENT_CLASS_NAME, new Integer(newCount));
				if (newCount == 0) {
					if (DIFF_COUNTERS[0] != 0 || DIFF_COUNTERS[1] != 0
							|| DIFF_COUNTERS[2] != 0) {
						dualPrintln("===========================================================================");
						dualPrintln("Results summary:");
					}
					if (DIFF_COUNTERS[0] != 0)
						dualPrintln("	- "
								+ DIFF_COUNTERS[0]
								+ " test(s) where Javac found errors/warnings but Eclipse did not");
					if (DIFF_COUNTERS[1] != 0)
						dualPrintln("	- "
								+ DIFF_COUNTERS[1]
								+ " test(s) where Eclipse found errors/warnings but Javac did not");
					if (DIFF_COUNTERS[2] != 0)
						dualPrintln("	- "
								+ DIFF_COUNTERS[2]
								+ " test(s) where Eclipse and Javac did not have same output");
					System.out.println("\n");
				}
			}
			dualPrintln("\n\nFull results sent to " + javacFullLogFileName);
			javacFullLog.flush();
		}
	}

	protected void removeTempClass(String className) {
		File dir = new File(SOURCE_DIRECTORY);
		String[] fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					Util.delete(SOURCE_DIRECTORY + File.separator
							+ fileNames[i]);
				}
			}
		}

		dir = new File(EVAL_DIRECTORY);
		fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					Util.delete(EVAL_DIRECTORY + File.separator + fileNames[i]);
				}
			}
		}

	}

	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString) {
		runConformTest(testFiles, expectedSuccessOutputString, null /*
																	 * no extra
																	 * class
																	 * libraries
																	 */,
				null /* no custom options */, null /* no custom requestor */,
				false /* do not skip javac for this peculiar test */);
	}

	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory) {
		runConformTest(testFiles, expectedSuccessOutputString, classLib,
				null /* no custom options */, null /* no custom requestor */,
				false /* do not skip javac for this peculiar test */);
	}

	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			Map customOptions, ICompilerRequestor clientRequestor) {
		runConformTest(testFiles, expectedSuccessOutputString, classLib,
				customOptions,
				clientRequestor, false /*
										 * do not skip javac for this peculiar
										 * test
										 */);
	}

	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			Map customOptions, ICompilerRequestor clientRequestor,
			boolean skipRhino) {
		// Non-javac part
		try {
			IProblemFactory problemFactory = getProblemFactory();
			Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR
					.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR
					+ File.separator, false, clientRequestor, false, /*
																	 * show
																	 * category
																	 */
			false /* show warning token */);

			Map options = getCompilerOptions();
			if (customOptions != null) {
				options.putAll(customOptions);
			}
			CompilerOptions compilerOptions = new CompilerOptions(options);
			compilerOptions.performMethodsFullRecovery = false;
			compilerOptions.performStatementsRecovery = false;
			Compiler batchCompiler = new Compiler(getNameEnvironment(
					new String[] {}, classLib), getErrorHandlingPolicy(),
					compilerOptions, requestor, problemFactory);
			compilerOptions.produceReferenceInfo = true;
			try {
				batchCompiler.compile(Util.compilationUnits(testFiles)); // compile
																			// all
																			// files
																			// together
			} catch (RuntimeException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
				throw e;
			}
			if (!requestor.hasErrors) {
				String sourceFile = testFiles[0];

				// Compute class name by removing ".js" and replacing slashes
				// with dots
				String className = sourceFile.substring(0,
						sourceFile.length() - 1).replace('/', '.').replace(
						'\\', '.');
				if (className.endsWith(PACKAGE_INFO_NAME))
					return;

//				boolean passed = this.verifier.verifyClassFiles(sourceFile,
//						className, expectedSuccessOutputString,
//						this.classpaths, null, vmArguments);
//				if (!passed) {
//					System.out.println(getClass().getName() + '#' + getName());
//					for (int i = 0; i < testFiles.length; i += 2) {
//						System.out.print(testFiles[i]);
//						System.out.println(" ["); //$NON-NLS-1$
//						System.out.println(testFiles[i + 1]);
//						System.out.println("]"); //$NON-NLS-1$
//					}
//				}
//				assertTrue(this.verifier.failureReason, // computed by
//														// verifyClassFiles(...)
//														// action
//						passed);
			} else {
				System.out.println(getClass().getName() + '#' + getName());
				System.out.println(Util.displayString(requestor.problemLog,
						INDENT, SHIFT));
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
				assertTrue("Unexpected problems: " + requestor.problemLog,
						false);
			}
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC && !skipRhino)
				runWithRhino(testFiles, "", expectedSuccessOutputString);
			// PREMATURE for now, skipping javac implies skipping the compile
			// and execution steps; yet, only cases for which the
			// execution step was a problem have been discovered so
			// far; may consider skipping the execution step only
		}
	}

	protected void runConformTest(String[] testFiles) {
		runConformTest(testFiles, null /* no expected output string */,
				null /* no extra class libraries */,
				null /* no custom options */, null /* no custom requestor */,
				false /* do not skip javac for this peculiar test */);
	}

	// PREMATURE consider whether conform tests throwing errors should
	// implement javac comparison or not
	protected void runConformTestThrowingError(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory, String[] vmArguments) {

		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));

		IProblemFactory problemFactory = getProblemFactory();
		Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR
				.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR
				+ File.separator, false, null/* no custom requestor */, false, /*
																			 * show
																			 * category
																			 */
		false /* show warning token */);

		CompilerOptions compilerOptions = new CompilerOptions(
				getCompilerOptions());
		compilerOptions.performMethodsFullRecovery = false;
		compilerOptions.performStatementsRecovery = false;
		Compiler batchCompiler = new Compiler(getNameEnvironment(
				new String[] {}, classLib), getErrorHandlingPolicy(),
				compilerOptions, requestor, problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		Throwable exception = null;
		try {
			batchCompiler.compile(Util.compilationUnits(testFiles)); // compile
																		// all
																		// files
																		// together
		} catch (RuntimeException e) {
			exception = e;
			throw e;
		} catch (Error e) {
			exception = e;
			throw e;
		} finally {

			if (!requestor.hasErrors) {
				String sourceFile = testFiles[0];

				// Compute class name by removing ".java" and replacing slashes
				// with dots
				String className = sourceFile.substring(0,
						sourceFile.length() - 5).replace('/', '.').replace(
						'\\', '.');

//				boolean passed = this.verifier.verifyClassFilesThrowingError(
//						sourceFile, className, expectedSuccessOutputString,
//						this.classpaths, null, vmArguments);
//				if (exception == null)
//					assertTrue(this.verifier.failureReason, // computed by
//															// verifyClassFiles(...)
//															// action
//							passed);
			} else {
				if (exception == null)
					assertTrue("Unexpected problems: " + requestor.problemLog,
							false);
			}
		}
	}

	/*
	 * Run Sun compilation using javac. Launch compilation in a thread and
	 * verify that it does not take more than 5s to perform it. Otherwise abort
	 * the process and log in console. TODO (maxime) not sure we really do that
	 * 5s cap any more. A semi verbose output is sent to the console that
	 * analyzes differences of behaviors between javac and Eclipse on a per test
	 * basis. A more verbose output is produced into a file which name is
	 * printed on the console. Such files can be compared between various javac
	 * releases to check potential changes. To enable such tests, specify the
	 * following VM properies in the launch configuration: -Drun.javac=enabled
	 * mandatory - tells the test suite to run javac tests -Djdk.root=<the root
	 * directory of the tested javac> optional - enables to find the javac that
	 * will be run by the tests suite; the root directory must be specified as
	 * an absolute path and should point to the JDK root, aka /opt/jdk1.5.0_05
	 * for Linux or c:/JDK_50 for Windows; in case this property is not
	 * specified, the tests suite will use the runtime JRE of the launching
	 * configuration. Note that enabling javac tests implies running into 1.5
	 * compliance level (without having to specify it into the VM properties.)
	 * TODO (maxime) consider impacts of Java 6
	 */
	protected void runJavac(String[] testFiles,
			final String expectedProblemLog,
			final String expectedSuccessOutputString,
			boolean shouldFlushOutputDirectory) {
		String testName = null;
		Process compileProcess = null;
		Process execProcess = null;
		try {
			// Init test name
			testName = testName();

			// Cleanup javac output dir if needed
			File javacOutputDirectory = new File(JAVAC_OUTPUT_DIR);
			if (shouldFlushOutputDirectory) {
				Util.delete(javacOutputDirectory);
			}

			// Write files in dir
			writeFiles(testFiles);

			// Prepare command line
			StringBuffer cmdLine = new StringBuffer(javacCommandLineHeader);
			// compute extra classpath
			String[] classpath = Util.concatWithClassLibs(JAVAC_OUTPUT_DIR,
					false);
			StringBuffer cp = new StringBuffer(" -classpath ");
			int length = classpath.length;
			for (int i = 0; i < length; i++) {
				if (i > 0)
					cp.append(File.pathSeparatorChar);
				if (classpath[i].indexOf(" ") != -1) {
					cp.append("\"" + classpath[i] + "\"");
				} else {
					cp.append(classpath[i]);
				}
			}
			cmdLine.append(cp);
			// add source files
			for (int i = 0; i < testFiles.length; i += 2) {
				// *.java is not enough (p1/X.java, p2/Y.java)
				cmdLine.append(' ');
				cmdLine.append(testFiles[i]);
			}

			// Launch process
			compileProcess = Runtime.getRuntime().exec(cmdLine.toString(),
					null, this.outputTestDirectory);

			// Log errors
			Logger errorLogger = new Logger(compileProcess.getErrorStream(),
					"ERROR");

			// Log output
			Logger outputLogger = new Logger(compileProcess.getInputStream(),
					"OUTPUT");

			// start the threads to run outputs (standard/error)
			errorLogger.start();
			outputLogger.start();

			// Wait for end of process
			int exitValue = compileProcess.waitFor();
			errorLogger.join(); // make sure we get the whole output
			outputLogger.join();

			// Report raw javac results
			if (!testName.equals(javacTestName)) {
				javacTestName = testName;
				javacTestErrorFlag = false;
				javacFullLog
						.println("-----------------------------------------------------------------");
				javacFullLog.println(CURRENT_CLASS_NAME + " " + testName);
			}
			if (exitValue != 0) {
				javacTestErrorFlag = true;
			}
			if (errorLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac err: ---");
				javacFullLog.println(errorLogger.buffer.toString());
			}
			if (outputLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac out: ---");
				javacFullLog.println(outputLogger.buffer.toString());
			}

			// Compare compilation results
			if (expectedProblemLog == null || expectedProblemLog.length() == 0) {
				// Eclipse found no error and no warning
				if (exitValue != 0) {
					// Javac found errors
					System.out
							.println("----------------------------------------");
					System.out
							.println(testName
									+ " - Javac has found error(s) but Eclipse expects conform result:\n");
					javacFullLog
							.println("JAVAC_MISMATCH: Javac has found error(s) but Eclipse expects conform result");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[0]++;
				} else {
					// Javac found no error - may have found warnings
					if (errorLogger.buffer.length() > 0) {
						System.out
								.println("----------------------------------------");
						System.out
								.println(testName
										+ " - Javac has found warning(s) but Eclipse expects conform result:\n");
						javacFullLog
								.println("JAVAC_MISMATCH: Javac has found warning(s) but Eclipse expects conform result");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					}
					if (expectedSuccessOutputString != null
							&& !javacTestErrorFlag) {
						// Neither Eclipse nor Javac found errors, and we have a
						// runtime
						// bench value
						StringBuffer javaCmdLine = new StringBuffer(
								javaCommandLineHeader);
						javaCmdLine.append(cp);
						javaCmdLine.append(' ').append(
								testFiles[0].substring(0, testFiles[0]
										.indexOf('.')));
						// assume executable class is name of first test file -
						// PREMATURE check if this is also the case in other
						// test fwk classes
						execProcess = Runtime.getRuntime().exec(
								javaCmdLine.toString(), null,
								this.outputTestDirectory);
						Logger logger = new Logger(
								execProcess.getInputStream(), "");
						// PREMATURE implement consistent error policy
						logger.start();
						exitValue = execProcess.waitFor();
						logger.join(); // make sure we get the whole output
						String javaOutput = logger.buffer.toString().trim();
						if (!expectedSuccessOutputString.equals(javaOutput)) {
							System.out
									.println("----------------------------------------");
							System.out
									.println(testName
											+ " - Javac and Eclipse runtime output is not the same:");
							javacFullLog
									.println("JAVAC_MISMATCH: Javac and Eclipse runtime output is not the same");
							dualPrintln("eclipse:");
							dualPrintln(expectedSuccessOutputString);
							dualPrintln("javac:");
							dualPrintln(javaOutput);
							System.out.println("\n");
							printFiles(testFiles); // PREMATURE consider
													// printing files to the log
													// as well
							DIFF_COUNTERS[2]++;
						}
					}
				}
			} else {
				// Eclipse found errors or warnings
				if (errorLogger.buffer.length() == 0) {
					System.out
							.println("----------------------------------------");
					System.out
							.println(testName
									+ " - Eclipse has found error(s)/warning(s) but Javac did not find any:");
					javacFullLog
							.println("JAVAC_MISMATCH: Eclipse has found error(s)/warning(s) but Javac did not find any");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else if (expectedProblemLog.indexOf("ERROR") > 0
						&& exitValue == 0) {
					System.out
							.println("----------------------------------------");
					System.out
							.println(testName
									+ " - Eclipse has found error(s) but Javac only found warning(s):");
					javacFullLog
							.println("JAVAC_MISMATCH: Eclipse has found error(s) but Javac only found warning(s)");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					System.out.println("javac:");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else {
					// PREMATURE refine comparison
					// TODO (frederic) compare warnings in each result and
					// verify they are similar...
					// System.out.println(testName+": javac has found warnings :");
					// System.out.print(errorLogger.buffer.toString());
					// System.out.println(testName+": we're expecting warning results:");
					// System.out.println(expectedProblemLog);
				}
			}
		} catch (InterruptedException e1) {
			if (compileProcess != null)
				compileProcess.destroy();
			if (execProcess != null)
				execProcess.destroy();
			System.out.println(testName
					+ ": Sun javac compilation was aborted!");
			javacFullLog
					.println("JAVAC_WARNING: Sun javac compilation was aborted!");
			e1.printStackTrace(javacFullLog);
		} catch (Throwable e) {
			System.out.println(testName
					+ ": could not launch Sun javac compilation!");
			e.printStackTrace();
			javacFullLog
					.println("JAVAC_ERROR: could not launch Sun javac compilation!");
			e.printStackTrace(javacFullLog);
			// PREMATURE failing the javac pass or comparison could also fail
			// the test itself
		} finally {
			// Clean up written file(s)
			Util.delete(outputTestDirectory);
		}
	}

	protected void runWithRhino(String[] testFiles, final String expectedProblemLog, final String expectedSuccessOutputString) {
		String testName = null;
		Process compileProcess = null;
		Process execProcess = null;
		try {
			// Init test name
			testName = testName();

			// Write files in dir
			writeFiles(testFiles);

			// Prepare command line
			StringBuffer cmdLine = new StringBuffer(javaCommandLineHeader);
			// compute extra classpath
			String[] classpath = Util.concatWithClassLibs(JAVAC_OUTPUT_DIR, false);
			List cptemp = new ArrayList(Arrays.asList(classpath));
			cptemp.add(Platform.getBundle("org.mozilla.javascript").getLocation().replaceAll("reference:", "").replaceAll("file:/", ""));
//			cptemp.add(Platform.getBundle("org.eclipse.wst.jsdt.core.tests.compiler").getLocation().replaceAll("reference:", "").replaceAll("file:/", ""));
//			cptemp.add(Platform.getBundle("org.eclipse.wst.jsdt.core.tests.compiler").getLocation().replaceAll("reference:", "").replaceAll("file:/", "")+"/bin/");
			classpath = (String[]) cptemp.toArray(new String[cptemp.size()]);
			StringBuffer cp = new StringBuffer(" -classpath ");
			int length = classpath.length;
			for (int i = 0; i < length; i++) {
				if (i > 0)
					cp.append(File.pathSeparatorChar);
				if (classpath[i].indexOf(" ") != -1) {
					cp.append("\"" + classpath[i] + "\"");
				}
				else {
					cp.append(classpath[i]);
				}
			}
			cmdLine.append(cp);
			cmdLine.append(' ');
//			cmdLine.append(RhinoParser.class.getName());
//			cmdLine.append(" ");
			cmdLine.append("org.mozilla.javascript.tools.shell.Main -w -f ");

			// add source files
			for (int i = 0; i < testFiles.length; i += 2) {
				// *.java is not enough (p1/X.js, p2/Y.js)
				cmdLine.append(' ');
				cmdLine.append(testFiles[i]);
			}


			// Launch process
			compileProcess = Runtime.getRuntime().exec(cmdLine.toString(), null, this.outputTestDirectory);

			// Log errors
			Logger errorLogger = new Logger(compileProcess.getErrorStream(), "ERROR");

			// Log output
			Logger outputLogger = new Logger(compileProcess.getInputStream(), "OUTPUT");

			// start the threads to run outputs (standard/error)
			errorLogger.start();
			outputLogger.start();

			// Wait for end of process
			int exitValue = compileProcess.waitFor();
			errorLogger.join(); // make sure we get the whole output
			outputLogger.join();
			if (errorLogger.buffer.length() > 0 || outputLogger.buffer.length() > 0) {
				javacFullLog.println(errorLogger.buffer.toString());
				printFiles(testFiles);
			}
			assertEquals(expectedProblemLog != null ? expectedProblemLog : "", errorLogger.buffer.toString());
			assertEquals(expectedSuccessOutputString != null ? expectedSuccessOutputString : "", outputLogger.buffer.toString());
		}
		catch (InterruptedException e1) {
			if (compileProcess != null)
				compileProcess.destroy();
			if (execProcess != null)
				execProcess.destroy();
			System.out.println(testName + ": Rhino parser was aborted!");
		}
		catch (IOException e) {
			System.out.println(testName + ": could not launch Rhino!");
			e.printStackTrace();
		}
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
		runNegativeTest(testFiles, expectedProblemLog, null, true);
		/*runNegativeTest(testFiles, expectedProblemLog, null 
															 * no extra class
															 * libraries
															 , true 
																	 * flush
																	 * output
																	 * directory
																	 ,
				null  no custom options ,
				false  do not generate output ,
				false  do not show category ,
				false  do not show warning token , false 
															 * do not skip javac
															 * for this peculiar
															 * test
															 , false 
																		 * do
																		 * not
																		 * perform
																		 * statements
																		 * recovery
																		 ,
				null);*/
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory) {
		runNegativeTest(testFiles, expectedProblemLog, classLib,
				shouldFlushOutputDirectory, null /* no custom options */,
				false /* do not generate output */,
				false /* do not show category */,
				false /* do not show warning token */, false /*
															 * do not skip javac
															 * for this peculiar
															 * test
															 */, false /*
																		 * do
																		 * not
																		 * perform
																		 * statements
																		 * recovery
																		 */,
				null);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions) {
		runNegativeTest(testFiles, expectedProblemLog, classLib,
				shouldFlushOutputDirectory, customOptions, false /*
																 * do not
																 * generate
																 * output
																 */,
				false /* do not show category */,
				false /* do not show warning token */, false /*
															 * do not skip javac
															 * for this peculiar
															 * test
															 */, false /*
																		 * do
																		 * not
																		 * perform
																		 * statements
																		 * recovery
																		 */,
				null);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput, boolean showCategory,
			boolean showWarningToken) {
		runNegativeTest(testFiles, expectedProblemLog, classLib,
				shouldFlushOutputDirectory, customOptions, generateOutput,
				showCategory, showWarningToken, false /*
													 * do not skip javac for
													 * this peculiar test
													 */, false /*
																 * do not
																 * perform
																 * statements
																 * recovery
																 */, null);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput, boolean showCategory,
			boolean showWarningToken, boolean skipJavac,
			boolean performStatementsRecovery, String[] otherFiles) {
		// Non-javac part
		try {
			if (shouldFlushOutputDirectory)
				Util.flushDirectoryContent(new File(OUTPUT_DIR));

			if (otherFiles == null)
				otherFiles = new String[] {};
			IProblemFactory problemFactory = getProblemFactory();
			Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR
					.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR
					+ File.separator, generateOutput,
					null/* no custom requestor */, showCategory,
					showWarningToken);
			Map options = getCompilerOptions();
			if (customOptions != null) {
				options.putAll(customOptions);
			}
			CompilerOptions compilerOptions = new CompilerOptions(options);
			compilerOptions.performMethodsFullRecovery = performStatementsRecovery;
			compilerOptions.performStatementsRecovery = performStatementsRecovery;
			Compiler batchCompiler = new Compiler(getNameEnvironment(
					otherFiles, classLib), getErrorHandlingPolicy(),
					compilerOptions, requestor, problemFactory);
			Object inferEngines = options.get(INFERENCE_ENGINES);
			if (inferEngines != null) {
				batchCompiler.parser.inferenceEngines = (IInferEngine[]) inferEngines;

			}
			batchCompiler.options.produceReferenceInfo = true;
			Throwable exception = null;
			try {
				batchCompiler.compile(Util.compilationUnits(testFiles)); // compile
																			// all
																			// files
																			// together
			} catch (RuntimeException e) {
				exception = e;
				throw e;
			} catch (Error e) {
				exception = e;
				throw e;
			} finally {
				String computedProblemLog = Util
						.convertToIndependantLineDelimiter(requestor.problemLog
								.toString());
				String platformIndependantExpectedLog = Util
						.convertToIndependantLineDelimiter(expectedProblemLog);
				if (!platformIndependantExpectedLog.equals(computedProblemLog)) {
					System.out.println(getClass().getName() + '#' + getName());
					System.out.println(Util.displayString(computedProblemLog,
							INDENT, SHIFT));
					for (int i = 0; i < testFiles.length; i += 2) {
						System.out.print(testFiles[i]);
						System.out.println(" ["); //$NON-NLS-1$
						System.out.println(testFiles[i + 1]);
						System.out.println("]"); //$NON-NLS-1$
					}
				}
				if (exception == null)
					assertEquals("Invalid problem log ",
							platformIndependantExpectedLog, computedProblemLog);
			}
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC && !skipJavac)
				runJavac(testFiles, expectedProblemLog, null,
						shouldFlushOutputDirectory);
		}
	}

	protected void runNegativeTestWithExecution(String[] testFiles,
			String expectedProblemLog, String expectedSuccessOutputString,
			String[] classLib, boolean shouldFlushOutputDirectory,
			String[] vmArguments, Map customOptions,
			ICompilerRequestor clientRequestor) {

		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));

		IProblemFactory problemFactory = getProblemFactory();
		Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR
				.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR
				+ File.separator, true, clientRequestor,
				false /* show category */, false /* show warning token */);

		Map options = getCompilerOptions();
		if (customOptions != null) {
			options.putAll(customOptions);
		}
		CompilerOptions compilerOptions = new CompilerOptions(options);
		compilerOptions.performMethodsFullRecovery = false;
		compilerOptions.performStatementsRecovery = false;
		Compiler batchCompiler = new Compiler(getNameEnvironment(
				new String[] {}, classLib), getErrorHandlingPolicy(),
				compilerOptions, requestor, problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		try {
			batchCompiler.compile(Util.compilationUnits(testFiles)); // compile
																		// all
																		// files
																		// together
		} catch (RuntimeException e) {
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" ["); //$NON-NLS-1$
				System.out.println(testFiles[i + 1]);
				System.out.println("]"); //$NON-NLS-1$
			}
			throw e;
		}
		assertTrue("Must have errors", requestor.hasErrors);

		String computedProblemLog = Util
				.convertToIndependantLineDelimiter(requestor.problemLog
						.toString());
		String platformIndependantExpectedLog = Util
				.convertToIndependantLineDelimiter(expectedProblemLog);
		if (!platformIndependantExpectedLog.equals(computedProblemLog)) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(Util.displayString(computedProblemLog, INDENT,
					SHIFT));
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" ["); //$NON-NLS-1$
				System.out.println(testFiles[i + 1]);
				System.out.println("]"); //$NON-NLS-1$
			}
			assertEquals("Invalid problem log ",
					platformIndependantExpectedLog, computedProblemLog);
		}

		String sourceFile = testFiles[0];

		// Compute class name by removing ".java" and replacing slashes with
		// dots
		String className = sourceFile.substring(0, sourceFile.length() - 5)
				.replace('/', '.').replace('\\', '.');
		if (className.endsWith(PACKAGE_INFO_NAME))
			return;

//		if (vmArguments != null) {
//			if (this.verifier != null) {
//				this.verifier.shutDown();
//			}
//			this.verifier = new TestVerifier(false);
//			this.createdVerifier = true;
//		}
//		boolean passed = this.verifier
//				.verifyClassFiles(sourceFile, className,
//						expectedSuccessOutputString, this.classpaths, null,
//						vmArguments);
//		if (!passed) {
//			String platformIndependantExpectedSuccessOutputString = Util
//					.convertToIndependantLineDelimiter(expectedSuccessOutputString);
//			String platformIndependantFailureReason = Util
//					.convertToIndependantLineDelimiter(this.verifier.failureReason);
//			if (platformIndependantFailureReason
//					.indexOf(platformIndependantExpectedSuccessOutputString) == -1) {
//				System.out.println(getClass().getName() + '#' + getName());
//				System.out.println(Util.displayString(
//						platformIndependantFailureReason, INDENT, SHIFT));
//				assertEquals("Invalid runtime log ",
//						platformIndependantExpectedSuccessOutputString,
//						platformIndependantFailureReason);
//				System.out.println(getClass().getName() + '#' + getName());
//				for (int i = 0; i < testFiles.length; i += 2) {
//					System.out.print(testFiles[i]);
//					System.out.println(" ["); //$NON-NLS-1$
//					System.out.println(testFiles[i + 1]);
//					System.out.println("]"); //$NON-NLS-1$
//				}
//			}
//		} else if (vmArguments != null) {
//			if (this.verifier != null) {
//				this.verifier.shutDown();
//			}
//			this.verifier = new TestVerifier(false);
//			this.createdVerifier = true;
//		}
	}

	protected void setUp() throws Exception {
		super.setUp();
//		if (this.verifier == null) {
//			this.verifier = new TestVerifier(true);
//			this.createdVerifier = true;
//		}
		if (RUN_JAVAC) {
			if (isFirst()) {
				if (javacFullLog == null) {
					// One time initialization of javac related concerns
					// compute command lines and extract javac version
					String jdkRootDirectory = System.getProperty("jdk.root");
					if (jdkRootDirectory == null)
						jdkRootDirPath = (new Path(Util.getJREDirectory()))
								.removeLastSegments(1);
					else
						jdkRootDirPath = new Path(jdkRootDirectory);

					StringBuffer cmdLineHeader = new StringBuffer(
							jdkRootDirPath.append("bin").append(JAVA_NAME)
									.toString()); // PREMATURE replace JAVA_NAME
													// and JAVAC_NAME with
													// locals? depends on
													// potential reuse
					javaCommandLineHeader = cmdLineHeader.toString();
					cmdLineHeader = new StringBuffer(jdkRootDirPath.append(
							"bin").append(JAVAC_NAME).toString());
					cmdLineHeader.append(" -classpath . ");
					// start with the current directory which contains the
					// source files
					Process compileProcess = Runtime.getRuntime().exec(
							cmdLineHeader.toString() + " -version", null, null);
					Logger versionLogger = new Logger(compileProcess
							.getErrorStream(), "");
					// PREMATURE implement consistent error policy
					versionLogger.start();
					compileProcess.waitFor();
					versionLogger.join(); // make sure we get the whole output
					String version = versionLogger.buffer.toString();
					int eol = version.indexOf('\n');
					version = version.substring(0, eol);
					cmdLineHeader.append(" -d ");
					cmdLineHeader
							.append(JAVAC_OUTPUT_DIR.indexOf(" ") != -1 ? "\""
									+ JAVAC_OUTPUT_DIR + "\""
									: JAVAC_OUTPUT_DIR);
					cmdLineHeader
							.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable
																					// recommended
																					// warnings
					// REVIEW consider enabling all warnings instead? Philippe
					// does not see
					// this as ez to use (too many changes in logs)
					javacCommandLineHeader = cmdLineHeader.toString();
					new File(Util.getOutputDirectory()).mkdirs();
					// TODO maxime check why this happens to miss in some cases
					javacFullLogFileName = Util.getOutputDirectory()
							+ File.separatorChar
							+ version.replace(' ', '_')
							+ "_"
							+ (new SimpleDateFormat("yyyyMMdd_HHmmss"))
									.format(new Date()) + ".txt";
					javacFullLog = new PrintWriter(new FileOutputStream(
							javacFullLogFileName));
					javacFullLog.println(version); // so that the contents is
													// self sufficient
					System.out
							.println("***************************************************************************");
					System.out
							.println("* Rhino output archived into file:");
					System.out.println("* " + javacFullLogFileName);
					System.out
							.println("***************************************************************************");
				}
				// per class initialization
				CURRENT_CLASS_NAME = getClass().getName();
				dualPrintln("***************************************************************************");
				System.out
						.print("* Comparison with Rhino compiler for class ");
				dualPrintln(CURRENT_CLASS_NAME.substring(CURRENT_CLASS_NAME
						.lastIndexOf('.') + 1)
						+ " ("
						+ TESTS_COUNTERS.get(CURRENT_CLASS_NAME)
						+ " tests)");
				System.out
						.println("***************************************************************************");
				DIFF_COUNTERS[0] = 0;
				DIFF_COUNTERS[1] = 0;
				DIFF_COUNTERS[2] = 0;
			}
		}
	}

	public void stop() {
//		this.verifier.shutDown();
	}

	protected void tearDown() throws Exception {
//		if (this.createdVerifier) {
//			this.stop();
//		}
		// clean up output dir
		File outputDir = new File(OUTPUT_DIR);
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		}
		super.tearDown();
		if (RUN_JAVAC) {
			printJavacResultsSummary();
		}
	}

	protected void runBasicTest(String[] testFiles) {
		runBasicTest(testFiles, null /* no extra class libraries */, null /*
																		 * no
																		 * custom
																		 * options
																		 */,
				null /* no custom requestor */);
	}

	protected void runBasicTest(String[] testFiles, String[] classLib,
			Map customOptions, ICompilerRequestor clientRequestor) {
		// Non-javac part
		try {

			IProblemFactory problemFactory = getProblemFactory();
			Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR
					.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR
					+ File.separator, false, clientRequestor, false, /*
																	 * show
																	 * category
																	 */
			false /* show warning token */);

			Map options = getCompilerOptions();
			if (customOptions != null) {
				options.putAll(customOptions);
			}
			CompilerOptions compilerOptions = new CompilerOptions(options);
			compilerOptions.performStatementsRecovery = false;
			Compiler batchCompiler = new Compiler(getNameEnvironment(
					new String[] {}, classLib), getErrorHandlingPolicy(),
					compilerOptions, requestor, problemFactory);
			compilerOptions.produceReferenceInfo = true;
			try {
				batchCompiler.compile(Util.compilationUnits(testFiles)); // compile
																			// all
																			// files
																			// together
			} catch (RuntimeException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
				throw e;
			}
			if (!requestor.hasErrors) {
//				String sourceFile = testFiles[0];
//				boolean passed = true;
//				if (!passed) {
//					System.out.println(getClass().getName() + '#' + getName());
//					for (int i = 0; i < testFiles.length; i += 2) {
//						System.out.print(testFiles[i]);
//						System.out.println(" ["); //$NON-NLS-1$
//						System.out.println(testFiles[i + 1]);
//						System.out.println("]"); //$NON-NLS-1$
//					}
//				}
//				assertTrue(this.verifier.failureReason, // computed by
//														// verifyClassFiles(...)
//														// action
//						passed);
			} else {
				System.out.println(getClass().getName() + '#' + getName());
				System.out.println(Util.displayString(requestor.problemLog,
						INDENT, SHIFT));
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
				assertTrue("Unexpected problems: " + requestor.problemLog,
						false);
			}
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
		}
	}

	static class TestParser extends Parser {

		public TestParser(IProblemFactory problemFactory,
				CompilerOptions options, boolean optimizeStringLiterals,
				boolean useSourceJavadocParser) {

			super(new ProblemReporter(DefaultErrorHandlingPolicies
					.exitAfterAllProblems(), options, problemFactory),
					optimizeStringLiterals);

			// we want to notify all syntax error with the acceptProblem API
			// To do so, we define the record method of the ProblemReporter
			this.problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.exitAfterAllProblems(),
					options, problemFactory) {
				public void record(CategorizedProblem problem,
						CompilationResult unitResult, ReferenceContext context) {
					unitResult.record(problem, context); // TODO (jerome)
															// clients are
															// trapping problems
															// either through
															// factory or
															// requestor... is
															// result storing
															// needed?
					System.out.println("PARSER ERROR: " + problem.toString());
					assertTrue("unexpected parse Error", false);
				}
			};
			this.options = options;
			// set specific javadoc parser
			if (useSourceJavadocParser) {
				this.javadocParser = new SourceJavadocParser(this);

			}
		}

		public CompilationUnitDeclaration parseCompilationUnit(
				ICompilationUnit unit, boolean fullParse) {

			boolean old = diet;

			try {
				diet = !fullParse;
				CompilationResult compilationUnitResult = new CompilationResult(
						unit, 0, 0, this.options.maxProblemsPerUnit);
				CompilationUnitDeclaration parsedUnit = parse(unit,
						compilationUnitResult);
				int initialStart = this.scanner.initialPosition;
				int initialEnd = this.scanner.eofPosition;
				if (fullParse) {
					diet = false;
					this.getMethodBodies(parsedUnit);
				}
				this.scanner.resetTo(initialStart, initialEnd);
				assertTrue(this.expressionPtr < 0);
				assertTrue(this.expressionLengthPtr < 0);
				assertTrue(this.astPtr < 0);
				assertTrue(this.astLengthPtr < 0);
				assertTrue(this.intPtr < 0);
				return parsedUnit;
			} catch (AbortCompilation e) {
				// ignore this exception
			} finally {
				diet = old;
			}
			return null;
		}
	}

	protected CompilationUnitDeclaration runParseTest(String s,
			String testName, String expected) {
		// Non-javac part
		try {

			char[] source = s.toCharArray();
			TestParser parser = new TestParser(new DefaultProblemFactory(Locale
					.getDefault()), new CompilerOptions(getCompilerOptions()),
					true/* optimize string literals */, false);

			ICompilationUnit sourceUnit = new CompilationUnit(source, testName,
					null);

			CompilationUnitDeclaration compUnit = parser.parseCompilationUnit(
					sourceUnit, true);
			if (expected != null) {
				String result = compUnit.toString();
				assertEquals(expected, result);
			}
			return compUnit;
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
		}
	}

	protected CompilationUnitDeclaration runJSDocParseTest(String s,
			String testName, String expected) {
		// Non-javac part
		try {

			char[] source = s.toCharArray();
			TestParser parser = new TestParser(new DefaultProblemFactory(Locale
					.getDefault()), new CompilerOptions(getCompilerOptions()),
					true/* optimize string literals */, false);

			parser.javadocParser.checkDocComment = true;
			ICompilationUnit sourceUnit = new CompilationUnit(source, testName,
					null);

			CompilationUnitDeclaration compUnit = parser.parseCompilationUnit(
					sourceUnit, true);
			if (expected != null) {
				String result = compUnit.toString();
				assertEquals(expected, result);
			}
			return compUnit;
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
		}
	}

	protected CompilationUnitDeclaration runInferTest(String s,
			String testName, String expected, InferOptions inferOptions) {

		return runInferTest(s, testName, expected, inferOptions,
				new InferrenceProvider[] {new DefaultInferrenceProvider()});
	}
	
	protected CompilationUnitDeclaration runInferTest(String s,
			String testName, String expected, InferOptions inferOptions,
			InferrenceProvider inferrenceProvider) {
		
		return runInferTest(s, testName, expected, inferOptions, inferrenceProvider, false);
	}
	
	protected CompilationUnitDeclaration runInferTest(String s,
			String testName, String expected, InferOptions inferOptions,
			InferrenceProvider inferrenceProvider, boolean useDefaultToo) {
		
		InferrenceProvider[] providers;
		if(useDefaultToo) {
			providers = new InferrenceProvider[] {new DefaultInferrenceProvider(), inferrenceProvider};
		} else {
			providers = new InferrenceProvider[] {inferrenceProvider};
		}
		return runInferTest(s, testName, expected, inferOptions, providers);
	}

	private CompilationUnitDeclaration runInferTest(String s,
			String testName, String expected, InferOptions inferOptions,
			InferrenceProvider[] inferrenceProviders) {
		// Non-javac part
		try {

			char[] source = s.toCharArray();
			CompilerOptions compilerOptions = new CompilerOptions(
					getCompilerOptions());
			compilerOptions.inferOptions = inferOptions;
			TestParser parser = new TestParser(new DefaultProblemFactory(Locale
					.getDefault()), compilerOptions, true/* optimize string literals */, false);

			parser.javadocParser.checkDocComment = true;

			ICompilationUnit sourceUnit = new CompilationUnit(source, testName,
					null);

			CompilationUnitDeclaration compUnit = parser.parseCompilationUnit(
					sourceUnit, true);

			for(int i = 0; i < inferrenceProviders.length; ++i) {
				InferEngine inferEngine = (InferEngine)inferrenceProviders[i].getInferEngine();

				inferEngine.initialize();
				if (inferEngine instanceof IInferEngineExtension)
					((IInferEngineExtension) inferEngine).setCompilationUnit(compUnit, parser.scanner.getSource());
				else
					inferEngine.setCompilationUnit(compUnit);
				inferEngine.doInfer();
			}
			
			if (expected != null) {
				StringBuffer sb = new StringBuffer();
				compUnit.printInferredTypes(sb);
				String result = sb.toString();
				assertEquals(expected, result);
			}
			return compUnit;
			// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
		}
	}
	
	/**
	 * <p>Runs a JUnit test which includes inferring and building the type bindings for the given source.</p>
	 * 
	 * @param source JS source to infer and build
	 * @param expectedInfference expected inferred type after inferring and building
	 * @param inferOptions {@link InferOptions} to use when inferring and building
	 * @param inferrenceProvider {@link InferrenceProvider} to use when inferring
	 * @return 
	 */
	protected CompilationUnitDeclaration[] runInferAndBuildBindingsTest(String source, String expectedInfference, InferOptions inferOptions,
			InferrenceProvider inferrenceProvider) {
		
		CompilationUnitDeclaration[] compUnits = this.runInferAndBuildBindingsTest(
				new String[] {source},
				new String[] {expectedInfference},
				inferOptions,
				new InferrenceProvider[] {inferrenceProvider});
		
		return compUnits;
	}
	
	/**
	 * <p>Runs a JUnit test which includes inferring and building the type bindings for the given source.</p>
	 * 
	 * <p>Can be used to test multiple JS sources together.</p>
	 * 
	 * @param sources JS sources to infer and build
	 * @param expectedInfferences expected inferred types after inferring and building
	 * @param inferOptions {@link InferOptions} to use when inferring and building
	 * @param inferrenceProviders {@link InferrenceProvider}s to use when inferring
	 * @return 
	 */
	protected CompilationUnitDeclaration[] runInferAndBuildBindingsTest(String[] sources, String[] expectedInfferences, InferOptions inferOptions,
			InferrenceProvider[] inferrenceProviders) {

		IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
		CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
		compilerOptions.inferOptions = inferOptions;
		TestParser parser = new TestParser(problemFactory, compilerOptions, true, false);

		parser.javadocParser.checkDocComment = true;
		
		ProblemReporter reporter = new DoNothingProblemReporter(compilerOptions, problemFactory);
		INameEnvironment nameEnv = getNameEnvironment(sources, null);
		ITypeRequestor requestor = new TypeRequestor(inferrenceProviders, compilerOptions, reporter);
		
		CompilationUnitDeclaration[] compUnits = new CompilationUnitDeclaration[sources.length];
		LookupEnvironment env = new LookupEnvironment(requestor, compilerOptions, reporter, nameEnv);
		for(int i = 0; i < sources.length; ++i) {
			String fileName = "/Test/" + i + ".js";
			ICompilationUnit sourceUnit = new CompilationUnit(sources[i].toCharArray(), fileName, null);
			compUnits[i] = parser.parseCompilationUnit(sourceUnit, true);
			
			for(int j = 0; j < inferrenceProviders.length; ++j) {
				InferEngine inferEngine = (InferEngine)inferrenceProviders[j].getInferEngine();

				inferEngine.initialize();
				if (inferEngine instanceof IInferEngineExtension)
					((IInferEngineExtension) inferEngine).setCompilationUnit(compUnits[i], parser.scanner.getSource());
				else
					inferEngine.setCompilationUnit(compUnits[i]);
				inferEngine.doInfer();
			}
			
			env.buildTypeBindings(compUnits[i], null);
		}
		
		env.completeTypeBindings();
		
		//assert inferred types equal expected inferred types
		for(int i = 0; i < compUnits.length; ++i) {
			if (expectedInfferences[i] != null) {
				StringBuffer sb = new StringBuffer();
				compUnits[i].printInferredTypes(sb);
				String result = sb.toString();
				
				assertEquals(expectedInfferences[i], result);
			}
		}
		return compUnits;
	}
	
	private class DoNothingProblemReporter extends ProblemReporter {

		public DoNothingProblemReporter(CompilerOptions options, IProblemFactory problemFactory) {
			super(DefaultErrorHandlingPolicies.exitAfterAllProblems(), options, problemFactory);
		}

		public void record(CategorizedProblem problem,
				CompilationResult unitResult, ReferenceContext context) {
			
		}
	}
	
	private class TypeRequestor implements ITypeRequestor {

		private InferrenceProvider[] fInferrenceProviders;
		private CompilerOptions fOptions;
		private ProblemReporter fReporter;
		
		public TypeRequestor(InferrenceProvider[] inferrenceProviders, CompilerOptions options, ProblemReporter reporter) {
			
			this.fInferrenceProviders = inferrenceProviders;
			this.fOptions = options;
			this.fReporter = reporter;
		}
		
		public void accept(IBinaryType binaryType,
				PackageBinding packageBinding,
				AccessRestriction accessRestriction) {
		}

		public void accept(ICompilationUnit unit,
				AccessRestriction accessRestriction) {
		}

		public void accept(ISourceType[] sourceType,
				PackageBinding packageBinding,
				AccessRestriction accessRestriction) {
		}

		public void accept(LibraryAPIs libraryMetaData) {
		}

		public CompilationUnitDeclaration doParse(ICompilationUnit unit,
				AccessRestriction accessRestriction) {

			Parser p = new InferParser();
			CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, 0);
			
			return p.parse(unit,compilationUnitResult);
		}
		
		private class InferParser extends Parser {
			public InferParser() {
				super(TypeRequestor.this.fReporter, true);
			}

			public void initializeInferenceEngine(CompilationUnitDeclaration compilationUnitDeclaration) {
				this.inferenceEngines = new IInferEngine[TypeRequestor.this.fInferrenceProviders.length];
				for(int i = 0; i < this.inferenceEngines.length; ++i) {
					this.inferenceEngines[i] = TypeRequestor.this.fInferrenceProviders[i].getInferEngine();
				}
				
				for (int i = 0; i <  this.inferenceEngines.length; i++) {
					this.inferenceEngines[i].initializeOptions(this.options.inferOptions);
				}
			}
		}
	
	}
}
