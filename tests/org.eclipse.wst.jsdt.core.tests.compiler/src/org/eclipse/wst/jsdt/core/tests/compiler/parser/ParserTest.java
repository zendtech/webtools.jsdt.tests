/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.compiler.parser;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.wst.jsdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;

public class ParserTest extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 18 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public ParserTest(String name) {
	super(name);
}
public void Xtest001() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"function foo(){\n" +
			"	throws\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	throws\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", delete this token\n" + 
		"----------\n"
	);
}
public void Xtest002() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function foo(){\n" +
			"		throws new\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	throws new\n" + 
		"	       ^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n"
	);
}
public void Xtest003() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function foo(){\n" +
			"		throws new X\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	throws new X\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", throw expected\n" + 
		"----------\n"
	);
}
//public void test004() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	{\n" +
//			"		throws\n" +
//			"	}\n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 3)\n" + 
//		"	throws\n" + 
//		"	^^^^^^\n" + 
//		"Syntax error on token \"throws\", delete this token\n" + 
//		"----------\n"
//	);
//}
//public void test005() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	{\n" +
//			"		throws new\n" +
//			"	}\n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 3)\n" + 
//		"	throws new\n" + 
//		"	^^^^^^^^^^\n" + 
//		"Syntax error on tokens, delete these tokens\n" + 
//		"----------\n"
//	);
//}
public void Xtest006() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	{\n" +
			"		throws new X\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 3)\n" + 
		"	throws new X\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", throw expected\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 3)\n" + 
		"	throws new X\n" + 
		"	           ^\n" + 
		"Syntax error, unexpected end of initializer\n" + 
		"----------\n"
	);
}
//public void test007() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	void foo()throw {\n" +
//			"	}\n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 1)\n" + 
//		"	public class X {\n" + 
//		"	               ^\n" + 
//		"Syntax error, insert \"}\" to complete ClassBody\n" + 
//		"----------\n" + 
//		"2. ERROR in X.js (at line 2)\n" + 
//		"	void foo()throw {\n" + 
//		"	          ^^^^^\n" + 
//		"Syntax error on token \"throw\", { expected\n" + 
//		"----------\n"
//	);
//}
//public void test008() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	void foo()throw E {\n" +
//			"	}\n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 1)\n" + 
//		"	public class X {\n" + 
//		"	               ^\n" + 
//		"Syntax error, insert \"}\" to complete ClassBody\n" + 
//		"----------\n" + 
//		"2. ERROR in X.js (at line 2)\n" + 
//		"	void foo()throw E {\n" + 
//		"	          ^^^^^\n" + 
//		"Syntax error on token \"throw\", throws expected\n" + 
//		"----------\n" + 
//		"3. ERROR in X.js (at line 4)\n" + 
//		"	}\n" + 
//		"	^\n" + 
//		"Syntax error on token \"}\", delete this token\n" + 
//		"----------\n"
//	);
//}
public void Xtest009() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function foo(){\n" +
			"		throws e\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	throws e\n" + 
		"	^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n"
	);
}
//public void test010() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	void foo(){\n" +
//			"		throws e;\n" +
//			"	}\n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 3)\n" + 
//		"	throws e;\n" + 
//		"	^^^^^^\n" + 
//		"Syntax error on token \"throws\", throw expected\n" + 
//		"----------\n"
//	);
//}
//public void test011() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	public void foo(X, Object o, String s) {\n" +
//			"	}\n" +
//			"   public void bar(){}\n" + 
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 2)\n" + 
//		"	public void foo(X, Object o, String s) {\n" + 
//		"	                 ^\n" + 
//		"Syntax error on token \",\", . expected\n" + 
//		"----------\n"
//	);
//}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test012() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		;\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 2)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	             ^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		nls // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test013() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		//;\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 2)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	             ^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		nls // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47227
 */
//public void test014() {
//	this.runNegativeTest(
//		new String[] {
//			"X.js",
//			"public class X {\n" +
//			"	public void foo() { \n" +
//			"		import java.lang.*;\n" +
//			"	} \n" +
//			"}\n"
//		},
//		"----------\n" + 
//		"1. ERROR in X.js (at line 3)\n" + 
//		"	import java.lang.*;\n" + 
//		"	^^^^^^\n" + 
//		"Syntax error on token \"import\", delete this token\n" + 
//		"----------\n" + 
//		"2. ERROR in X.js (at line 3)\n" + 
//		"	import java.lang.*;\n" + 
//		"	^^^^^^^^^^^^^^^^^\n" + 
//		"Syntax error on token(s), misplaced construct(s)\n" + 
//		"----------\n"
//	);
//}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void _test015() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"function foo() {\n" + 
			"// some code\n" + 
			"}\n" + 
			"/*\n" + 
			"// some comments\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 4)\n" + 
		"	/*\n" + 
		"// some comments\n" + 
		"\n" + 
		"	^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unexpected end of comment\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"function foo() {\n" + 
			"	var s = \""
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	var s = \"\n" + 
		"	        ^\n" + 
		"String literal is not properly closed by a matching quote\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"function foo() {\n" + 
			"	var c = '"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	var c = \'\n" + 
		"	        ^\n" + 
		"String literal is not properly closed by a matching quote\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"function foo() {\n" + 
			"	var c = '\\u0"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 2)\n" + 
		"	var c = \'\\u0\n" + 
		"	         ^^^\n" + 
		"Invalid unicode\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=12287
 */
public void Xtest019() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function doit() {\n" + 
			"		var foo = null;\n" + 
			"		foo[0] = \n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 3)\n" + 
		"	foo[0] = \n" + 
		"	     ^\n" + 
		"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 3)\n" + 
		"	foo[0] = \n" + 
		"	     ^\n" + 
		"Syntax error, insert \";\" to complete Statement\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38895
 */
public void Xtest020() {
	this.runNegativeTest(
		new String[] {
			"X.js",
			"	function main( args) {\n" + 
			"	}\n" + 
			"	function newLibraryEntry() {\n" + 
			
			"		if (sourceAttachmentPath != null) {\n" + 
			"			if (sourceAttachmentPath.isEmpty()) { && !\n" + 
			"sourceAttachmentPath.isAbsolute()) {\n" + 
			"			foo();\n" + 
			"		}\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	}\n" + 
			"	function foo() {\n" + 
			"	}\n" + 
			"	function bar() {\n" + 
			"	}\n" + 
			""
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 5)\n" + 
		"	if (sourceAttachmentPath.isEmpty()) { && !\n" + 
		"	                                      ^^\n" + 
		"Syntax error on token \"&&\", invalid (\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 6)\n" + 
		"	sourceAttachmentPath.isAbsolute()) {\n" + 
		"	                                   ^\n" + 
		"Syntax error on token \"{\", invalid AssignmentOperator\n" + 
		"----------\n"
	);
}
public void Xtest021() {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < 1000; i++) {
		buffer.append("var field_" + i + " = 0; \n");
	}
	for (int i = 0; i < 1000; i++) {
		if (i == 0)
			buffer.append("function method_" + i + "() { /* default */ } \n");
		else
			buffer.append("function method_" + i + "() { method_" + (i - 1) + "() \n");
	}
	
	Hashtable options = new Hashtable();
	options.put(CompilerOptions.OPTION_MaxProblemPerUnit, "10");
	this.runNegativeTest(
		new String[] {
			"X.js",
			buffer.toString()
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 1002)\n" + 
		"	void method_1() { method_0() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"2. ERROR in X.js (at line 1002)\n" + 
		"	void method_1() { method_0() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n" + 
		"3. ERROR in X.js (at line 1003)\n" + 
		"	void method_2() { method_1() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"4. ERROR in X.js (at line 1003)\n" + 
		"	void method_2() { method_1() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n" + 
		"5. ERROR in X.js (at line 1004)\n" + 
		"	void method_3() { method_2() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"6. ERROR in X.js (at line 1004)\n" + 
		"	void method_3() { method_2() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n" + 
		"7. ERROR in X.js (at line 1005)\n" + 
		"	void method_4() { method_3() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"8. ERROR in X.js (at line 1005)\n" + 
		"	void method_4() { method_3() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n" + 
		"9. ERROR in X.js (at line 1006)\n" + 
		"	void method_5() { method_4() \n" + 
		"	                           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"10. ERROR in X.js (at line 2001)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		options // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test023() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.js",
			"var f= 1;;\n"
		},
		"----------\n" + 
		"1. ERROR in X.js (at line 1)\n" + 
		"	var f= 1;;\n" + 
		"	         ^\n" + 
		"Unnecessary semicolon\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		options // custom options
	);
}
}
