/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.future.compiler.regression;
import org.eclipse.wst.jsdt.core.tests.compiler.regression.AbstractRegressionTest;

import junit.framework.Test;

public class ArrayTest extends AbstractRegressionTest {

	public ArrayTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}
	
	public static Class testClass() {
		return ArrayTest.class;
	}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int[] x= new int[] {,};\n" + 
		"}\n",
	});
}

/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test002() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        float[] tab = new float[] {-0.0f};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"-0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test003() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        float[] tab = new float[] {0.0f};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test004() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        int[] tab = new int[] {-0};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"0");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=37387
 */
//public void test005() {
//	this.runConformTest(
//		new String[] {
//			"X.java",
//			"public class X {\n" +
//			"	 private static final Object X[] = new Object[]{null,null};\n" +
//			"    public static void main(String[] args) {\n" +
//			"		System.out.println(\"SUCCESS\");\n" +
//			"    }\n" +
//			"}\n",
//		},
//		"SUCCESS");
//		
//	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//	String actualOutput = null;
//	try {
//		byte[] classFileBytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
//		actualOutput =
//			disassembler.disassemble(
//				classFileBytes,
//				"\n",
//				ClassFileBytesDisassembler.DETAILED); 
//	} catch (org.eclipse.wst.jsdt.core.util.ClassFormatException e) {
//		e.printStackTrace();
//		assertTrue("ClassFormatException", false);
//	} catch (IOException e) {
//		e.printStackTrace();
//		assertTrue("IOException", false);
//	}
//	
//	String expectedOutput = 
//		"  static {};\n" + 
//		"    0  iconst_2\n" + 
//		"    1  anewarray java.lang.Object [3]\n" + 
//		"    4  putstatic X.X : java.lang.Object[] [9]\n" + 
//		"    7  return\n" + 
//		"      Line numbers:\n" + 
//		"        [pc: 0, line: 2]\n" + 
//		"        [pc: 7, line: 1]\n";
//
//	int index = actualOutput.indexOf(expectedOutput);
//	if (index == -1 || expectedOutput.length() == 0) {
//		System.out.println(Util.displayString(actualOutput, 3));
//	}
//	if (index == -1) {
//		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
//	}
//}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=80597
 */
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		char[][][] array = new char[][][10];\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	char[][][] array = new char[][][10];\n" + 
		"	                                ^^\n" + 
		"Cannot specify an array dimension after an empty dimension\n" + 
		"----------\n");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85203
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	static long lfield;\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		lfield = args.length;\n" + 
			"		lfield = args(args).length;\n" + 
			"		\n" + 
			"	}\n" + 
			"	static String[] args(String[] args) {\n" + 
			"		return args;\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85125
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public String getTexts(int i) [] {\n" +
			"		String[] texts = new String[1];\n" +
			"		return texts; \n" +
			"	}\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
// check deep resolution of faulty initializer (no array expected type)
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120263 
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		X x = { 10, zork() };\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	X x = { 10, zork() };\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from int[] to X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	X x = { 10, zork() };\n" + 
		"	            ^^^^\n" + 
		"The method zork() is undefined for the type X\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124101
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	int i = {};\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	int i = {};\n" + 
		"	        ^^\n" + 
		"Type mismatch: cannot convert from Object[] to int\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148807 - variation
//public void test011() {
//	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) {
//		// there is a bug on 1.4 VMs which make them fail verification (see 148807)
//		return;
//	}
//	this.runConformTest(
//			new String[] {
//				"X.java",
//				"public class X {\n" + 
//				"	public static void main(String[] args) {\n" + 
//				"		try {\n" + 
//				"			Object[][] all = new String[1][];\n" + 
//				"			all[0] = new Object[0];\n" + 
//				"		} catch (ArrayStoreException e) {\n" + 
//				"			System.out.println(\"SUCCESS\");\n" + 
//				"		}\n" + 
//				"	}\n" + 
//				"}", // =================
//			},
//			"SUCCESS");
//		String expectedOutput =
//			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
//			"  // Stack: 3, Locals: 2\n" + 
//			"  public static void main(java.lang.String[] args);\n" + 
//			"     0  iconst_1\n" + 
//			"     1  anewarray java.lang.String[] [16]\n" + 
//			"     4  astore_1 [all]\n" + 
//			"     5  aload_1 [all]\n" + 
//			"     6  iconst_0\n" + 
//			"     7  iconst_0\n" + 
//			"     8  anewarray java.lang.Object [3]\n" + 
//			"    11  aastore\n" + 
//			"    12  goto 24\n" + 
//			"    15  astore_1 [e]\n" + 
//			"    16  getstatic java.lang.System.out : java.io.PrintStream [18]\n" + 
//			"    19  ldc <String \"SUCCESS\"> [24]\n" + 
//			"    21  invokevirtual java.io.PrintStream.println(java.lang.String) : void [26]\n" + 
//			"    24  return\n" + 
//			"      Exception Table:\n" + 
//			"        [pc: 0, pc: 12] -> 15 when : java.lang.ArrayStoreException\n" + 
//			"      Line numbers:\n" + 
//			"        [pc: 0, line: 4]\n" + 
//			"        [pc: 5, line: 5]\n" + 
//			"        [pc: 15, line: 6]\n" + 
//			"        [pc: 16, line: 7]\n" + 
//			"        [pc: 24, line: 9]\n" + 
//			"      Local variable table:\n" + 
//			"        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]\n" + 
//			"        [pc: 5, pc: 15] local: all index: 1 type: java.lang.Object[][]\n" + 
//			"        [pc: 16, pc: 24] local: e index: 1 type: java.lang.ArrayStoreException\n";
//
//	try {
//		File f = new File(OUTPUT_DIR + File.separator + "X.class");
//		byte[] classFileBytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(f);
//		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
//		int index = result.indexOf(expectedOutput);
//		if (index == -1 || expectedOutput.length() == 0) {
//			System.out.println(Util.displayString(result, 3));
//		}
//		if (index == -1) {
//			assertEquals("Wrong contents", expectedOutput, result);
//		}
//	} catch (org.eclipse.wst.jsdt.core.util.ClassFormatException e) {
//		assertTrue(false);
//	} catch (IOException e) {
//		assertTrue(false);
//	}		
//}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148807 - variation
//public void test012() {
//	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) {
//		// there is a bug on 1.4 VMs which make them fail verification (see 148807)
//		return;
//	}
//	this.runConformTest(
//		new String[] {
//			"X.java",
//			"import java.util.Map;\n" + 
//			"\n" + 
//			"public class X {\n" + 
//			"	Map fValueMap;\n" + 
//			"\n" + 
//			"	public static void main(String[] args) {\n" + 
//			"		System.out.println(\"SUCCESS\");\n" + 
//			"	}\n" + 
//			"	public Object[][] getAllChoices() {\n" + 
//			"		Object[][] all = new String[this.fValueMap.size()][];\n" + 
//			"		return all;\n" + 
//			"	}\n" + 
//			"}", // =================,
//		},
//		"SUCCESS");
//	String expectedOutput =
//	"  // Method descriptor #35 ()[[Ljava/lang/Object;\n" + 
//	"  // Stack: 1, Locals: 2\n" + 
//	"  public java.lang.Object[][] getAllChoices();\n" + 
//	"     0  aload_0 [this]\n" + 
//	"     1  getfield X.fValueMap : java.util.Map [36]\n" + 
//	"     4  invokeinterface java.util.Map.size() : int [38] [nargs: 1]\n" + 
//	"     9  anewarray java.lang.String[] [44]\n" + 
//	"    12  astore_1 [all]\n" + 
//	"    13  aload_1 [all]\n" + 
//	"    14  areturn\n" + 
//	"      Line numbers:\n" + 
//	"        [pc: 0, line: 10]\n" + 
//	"        [pc: 13, line: 11]\n" + 
//	"      Local variable table:\n" + 
//	"        [pc: 0, pc: 15] local: this index: 0 type: X\n" + 
//	"        [pc: 13, pc: 15] local: all index: 1 type: java.lang.Object[][]\n";
//
//try {
//	File f = new File(OUTPUT_DIR + File.separator + "X.class");
//	byte[] classFileBytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(f);
//	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
//	int index = result.indexOf(expectedOutput);
//	if (index == -1 || expectedOutput.length() == 0) {
//		System.out.println(Util.displayString(result, 3));
//	}
//	if (index == -1) {
//		assertEquals("Wrong contents", expectedOutput, result);
//	}
//} catch (org.eclipse.wst.jsdt.core.util.ClassFormatException e) {
//	assertTrue(false);
//} catch (IOException e) {
//	assertTrue(false);
//}		
//}
}