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
package org.eclipse.wst.jsdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Test;

import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionJavadoc;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnJavadocTag;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.parser.JavadocTagConstants;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;

public class JavadocCompletionParserTest extends AbstractCompletionTest implements JavadocTagConstants {
	public static int INLINE_ALL_TAGS_LENGTH = 0;
	public static int BLOCK_ALL_TAGS_LENGTH = 0;
	static {
		for (int i=0; i<INLINE_TAGS_LENGTH; i++) {
			INLINE_ALL_TAGS_LENGTH += INLINE_TAGS[i].length;
		}
		for (int i=0; i<BLOCK_TAGS_LENGTH; i++) {
			BLOCK_ALL_TAGS_LENGTH += BLOCK_TAGS[i].length;
		}
	}

	CompletionJavadoc javadoc;

public JavadocCompletionParserTest(String testName) {
	super(testName);
}

protected void assertCompletionNodeResult(String source, String expected) {
	ASTNode completionNode = this.javadoc.getCompletionNode();
	assertNotNull("Javadoc should have a completion node!!!", completionNode);
	String actual = this.javadoc.getCompletionNode().toString();
	if (!expected.equals(actual)) {
		System.out.println("********************************************************************************");
		System.out.print(getName());
		System.out.println(" expect following result:");
    	String toDisplay = new String(org.eclipse.wst.jsdt.core.tests.util.Util.displayString(new String(actual), 2).toCharArray());
    	System.out.println(toDisplay);
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println(source);
	}
	assertEquals(
		"Completion node is not correct!",
		expected,
		actual
	);
}
protected void verifyCompletionInJavadoc(String source, String after) {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	CompletionParser parser = new CompletionParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		options,
		new DefaultProblemFactory(Locale.getDefault())));

	ICompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), "Test.java", null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	int cursorLocation = source.indexOf(after) + after.length() - 1;
	parser.dietParse(sourceUnit, compilationResult, cursorLocation);

	assertNotNull("Parser should have an assist node parent", parser.assistNodeParent);
	assertEquals("Expecting completion in javadoc!", CompletionJavadoc.class, parser.assistNodeParent.getClass());
	this.javadoc = (CompletionJavadoc) parser.assistNodeParent;
}

protected void verifyCompletionOnJavadocTag(char[] tag, char[][] expectedTags, boolean inline) {
	assertTrue("Invalid javadoc completion node!", this.javadoc.getCompletionNode() instanceof CompletionOnJavadocTag);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	StringBuffer expected = new StringBuffer("<CompleteOnJavadocTag:");
	if (inline) expected.append('{');
	expected.append('@');
	if (tag != null) expected.append(tag);
	if (inline) expected.append('}');
	if (expectedTags != null) {
		expected.append("\npossible tags:");
		int length = expectedTags.length;
		for (int i=0; i<length; i++) {
			expected.append("\n	- ");
			expected.append(expectedTags[i]);
		}
		expected.append('\n');
	}
	expected.append(">");
	if (expectedTags == null) {
		assertEquals("Invalid completion tag", expected.toString(), completionTag.toString());
	} else {
		String completionTagString = completionTag.toString();
		StringTokenizer completionTagTokenizer = new StringTokenizer(completionTagString, "\n");
		StringBuffer completionTagBuffer = new StringBuffer(completionTagString.length());
		boolean possibleLine = false, newLine = false;
		while (completionTagTokenizer.hasMoreTokens()) {
			String line = completionTagTokenizer.nextToken();
			if (line.startsWith("possible")) {
				if (!possibleLine) {
					possibleLine = true;
					completionTagBuffer.append("\npossible tags:");
				}
			} else {
				if (newLine) completionTagBuffer.append('\n');
				completionTagBuffer.append(line);
			}
			newLine = true;
		}
		assertEquals("Invalid completion tag", expected.toString(), completionTagBuffer.toString());
	}
}

protected void verifyAllTagsCompletion() {
	char[][] allTags = {
		// Block tags
		TAG_AUTHOR, TAG_DEPRECATED, TAG_EXCEPTION, TAG_PARAM, TAG_RETURN, TAG_SEE, TAG_VERSION,
		TAG_SINCE,
//		TAG_SERIAL, TAG_SERIAL_DATA, TAG_SERIAL_FIELD ,
		TAG_THROWS,
		// Inline tags
		TAG_LINK
	};
	verifyCompletionOnJavadocTag(null, allTags, false);
}

/**
 * @tests Test completions for javadoc tag names
 */
public void Xtest001() {
	String source = 
		"/**\n" +
		" * Completion on empty tag name:\n" +
		" * 	@\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@");
	verifyAllTagsCompletion();
}

public void test002() {
	String source = 
		"/**\n" +
		" * Completion on impossible tag name:\n" +
		" * 	@none\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@none");
	verifyCompletionOnJavadocTag("none".toCharArray(), null, false);
}

public void test003() {
	String source = 
		"/**\n" +
		" * Completion on one letter:\n" +
		" * 	@v\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@v");
	verifyCompletionOnJavadocTag(new char[] { 'v' }, new char[][] { TAG_VERSION }, false);
}

public void test004() {
	String source = 
		"/**\n" +
		" * Completion with several letters:\n" +
		" * 	@deprec\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@deprec");
	verifyCompletionOnJavadocTag("deprec".toCharArray(), new char[][] { TAG_DEPRECATED }, false);
}

public void test005() {
	String source = 
		"/**\n" +
		" * Completion on full tag name:\n" +
		" * 	@link\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@link");
	verifyCompletionOnJavadocTag("link".toCharArray(), new char[][] { TAG_LINK }, false);
}

public void Xtest006() {
	String source = 
		"/**\n" +
		" * Completion on empty tag name @ but inside text\n" +
		" */\n" +
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@");
	verifyCompletionOnJavadocTag(null, new char[][] { TAG_LINK }, false);
}

public void Xtest007() {
	String source = 
		"/**\n" + 
		" * Completion on :\n" + 
		" * 	@\n" + 
		" * 		- with following lines:\n" + 
		" * 			+ \"@ {@link }\"\n" + 
		" * 			+ \"@ {@linkplain }\"\n" + 
		" * 			+ \"@ {@literal }\"\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@");
	verifyAllTagsCompletion();
}

/**
 * @tests Tests to verify completion node flags
 * @bug 113506: [javadoc][assist] No tag proposals when there is a prefix on a line
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=113506"
 */
public void test010() {
	String source = 
		"/**\n" + 
		" * @see \n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@see ");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:\n" + 
		"	infos:formal reference\n" + 
		">"
	);
}

public void test011() {
	String source = 
		"/**\n" + 
		" * {@link }\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@link ");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:\n" + 
		"	infos:formal reference\n" + 
		">"
	);
}
public void Xtest012() {
	String source =
		"/**\n" + 
		" * @see Str\n" + 
		" */\n" + 
		"public class Test {}\n";
	verifyCompletionInJavadoc(source, "Str");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:Str\n" + 
		"	infos:formal reference\n" + 
		">"
	);
}

public void test013() {
	String source =
		"/**\n" + 
		" * {@link Str}\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "Str");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:Str\n" + 
		"	infos:formal reference\n" + 
		">"
	);
}
public void test014() {
	String source =
		"/**\n" + 
		" * @see String Subclass of Obj\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "Obj");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:Obj\n" + 
		"	infos:text\n" + 
		">"
	);
}

public void test015() {
	String source =
		"/**\n" + 
		" * {@link String Subclass of Obj}\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "Obj");
	assertCompletionNodeResult(source,
		"<CompletionOnJavadocSingleTypeReference:Obj\n" + 
		"	infos:formal reference\n" + 
		">"
	);
}

public void test021() {
	String source =
		"/**\n" + 
		" * @see\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@se");
	verifyCompletionOnJavadocTag("se".toCharArray(), new char[][] { TAG_SEE/*, TAG_SERIAL, TAG_SERIAL_DATA, TAG_SERIAL_FIELD */}, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	assertEquals("Invalid tag start position", 7, completionTag.tagSourceStart);
	assertEquals("Invalid tag end position", 11, completionTag.tagSourceEnd+1);
}

public void test022() {
	String source =
		"/**\n" + 
		" * @see\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "@see");
	verifyCompletionOnJavadocTag("see".toCharArray(), new char[][] { TAG_SEE }, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	assertEquals("Invalid tag start position", 7, completionTag.tagSourceStart);
	assertEquals("Invalid tag end position", 11, completionTag.tagSourceEnd+1);
}

/**
 * @test Bug 114091: [assist][javadoc] eternal loop 
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114091"
 */
public void test025() {
	String source =
		"/**\n" + 
		" * {@</code>\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@");
	char[][] allTags = {
		TAG_LINK
	};
	verifyCompletionOnJavadocTag("".toCharArray(), allTags, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf('>');
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}

public void test026() {
	String source =
		"/**\n" + 
		" * {@li</code>\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@li");
	verifyCompletionOnJavadocTag("li".toCharArray(), new char[][] { TAG_LINK}, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf('>');
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}

public void test027() {
	String source =
		"/**\n" + 
		" * {@link</code>\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@link");
	verifyCompletionOnJavadocTag("link".toCharArray(), new char[][] { TAG_LINK }, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf('>');
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}
public void test028() {
	String source =
		"/**\n" + 
		" * {@|\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@");
	verifyCompletionOnJavadocTag("".toCharArray(), new char[][] { TAG_LINK }, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf('|');
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}

public void test029() {
	String source =
		"/**\n" + 
		" * {@li/\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@li");
	verifyCompletionOnJavadocTag("li".toCharArray(), new char[][] { TAG_LINK }, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf("/\n");
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}

public void test030() {
	String source =
		"/**\n" + 
		" * {@link+\n" + 
		" */\n" + 
		"function Test() {}\n";
	verifyCompletionInJavadoc(source, "{@link");
	verifyCompletionOnJavadocTag("link".toCharArray(), new char[][] { TAG_LINK }, false);
	CompletionOnJavadocTag completionTag = (CompletionOnJavadocTag) this.javadoc.getCompletionNode();
	int start = source.indexOf("{@");
	assertEquals("Invalid tag start position", start, completionTag.tagSourceStart);
	int end = source.indexOf('+');
	assertEquals("Invalid tag end position", end, completionTag.tagSourceEnd);
}
}
