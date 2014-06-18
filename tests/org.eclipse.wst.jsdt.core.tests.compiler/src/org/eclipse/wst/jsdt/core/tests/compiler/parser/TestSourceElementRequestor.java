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

import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.internal.compiler.ISourceElementRequestor;

public class TestSourceElementRequestor implements ISourceElementRequestor {
/**
 * DummySourceElementRequestor constructor comment.
 */
public TestSourceElementRequestor() {
	super();
}
/**
 * acceptConstructorReference method comment.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * acceptFieldReference method comment.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * acceptImport method comment.
 */
public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand) {}
/**
 * acceptLineSeparatorPositions method comment.
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * acceptMethodReference method comment.
 */
public void acceptMethodReference(char[] methodName, int sourcePosition) {}
/**
 * acceptPackage method comment.
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {}
/**
 * acceptProblem method comment.
 */
public void acceptProblem(CategorizedProblem problem) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
/**
 * enterCompilationUnit method comment.
 */
public void enterCompilationUnit() {}
/**
 * enterConstructor method comment.
 */
public void enterConstructor(MethodInfo methodInfo) {}
/**
 * enterField method comment.
 */
public void enterField(FieldInfo fieldInfo) {}
/**
 * enterMethod method comment.
 */
public void enterMethod(MethodInfo methodInfo) {}
/**
 * enterType method comment.
 */
public void enterType(TypeInfo typeInfo) {}
/**
 * exitCompilationUnit method comment.
 */
public void exitCompilationUnit(int declarationEnd) {}
/**
 * exitConstructor method comment.
 */
public void exitConstructor(int declarationEnd) {}
/**
 * exitField method comment.
 */
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {}
/**
 * exitMethod method comment.
 */
public void exitMethod(int declarationEnd, int defaultValueStart, int defaultValueEnd) {}

/**
 * enterInitializer method comment.
 */
public void enterInitializer(int sourceStart, int sourceEnd) {
}

/**
 * exitInitializer method comment.
 */
public void exitInitializer(int sourceEnd) {
}
/**
 * exitType method comment.
 */
public void exitType(int declarationEnd) {}

}
