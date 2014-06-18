/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.rewrite.describing;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.BodyDeclaration;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.FieldDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.Modifier;
import org.eclipse.wst.jsdt.core.dom.PrimitiveType;
import org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration;
import org.eclipse.wst.jsdt.core.dom.Type;
import org.eclipse.wst.jsdt.core.dom.TypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationFragment;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.wst.jsdt.core.tests.model.AbstractJavaModelTests;

/**
  */
public class ASTRewritingTest extends AbstractJavaModelTests {
	/** @deprecated using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;
	
	protected IJavaScriptProject project1;
	protected IPackageFragmentRoot sourceFolder;
	
	public static Test suite() {
		TestSuite suite= new TestSuite(ASTRewritingTest.class.getName());
		suite.addTest(ASTRewritingExpressionsTest.allTests());
		//suite.addTest(ASTRewritingInsertBoundTest.allTests());
		//suite.addTest(ASTRewritingMethodDeclTest.allTests());
		//suite.addTest(ASTRewritingMoveCodeTest.allTests());
		//suite.addTest(ASTRewritingStatementsTest.allTests());
		//suite.addTest(ASTRewritingTrackingTest.allTests());
		//suite.addTest(ASTRewritingJavadocTest.allTests());
		suite.addTest(ASTRewritingGroupNodeTest.allTests());
		suite.addTest(SourceModifierTest.allTests());
		suite.addTest(LineCommentOffsetsTest.allTests());
		return suite;
	}

	
	public ASTRewritingTest(String name) {
		super(name);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		IJavaScriptProject proj= createJavaProject("P", new String[] {"src"});
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaScriptCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaScriptCore.COMPILER_COMPLIANCE, JavaScriptCore.VERSION_1_5);
		proj.setOption(JavaScriptCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaScriptCore.ERROR);
		proj.setOption(JavaScriptCore.COMPILER_SOURCE, JavaScriptCore.VERSION_1_5);
		proj.setOption(JavaScriptCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaScriptCore.VERSION_1_5);

		this.project1 = proj;
		this.sourceFolder = this.getPackageFragmentRoot("P", "src");
		
		waitUntilIndexesReady();
	}
	
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}
		
	protected JavaScriptUnit createAST(IJavaScriptUnit cu) {
		ASTParser parser= ASTParser.newParser(AST_INTERNAL_JLS2);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (JavaScriptUnit) parser.createAST(null);
	}
	
	protected JavaScriptUnit createAST3(IJavaScriptUnit cu) {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (JavaScriptUnit) parser.createAST(null);
	}
	
	protected String evaluateRewrite(IJavaScriptUnit cu, ASTRewrite rewrite) throws Exception {
		Document document1= new Document(cu.getSource());
		TextEdit res= rewrite.rewriteAST(document1, cu.getJavaScriptProject().getOptions(true));
		res.apply(document1);
		String content1= document1.get();
		
		Document document2= new Document(cu.getSource());
		TextEdit res2= rewrite.rewriteAST();
		res2.apply(document2);
		String content2= document2.get();
		
		assertEquals(content1, content2);
		
		return content1;
	}
	
	
	public static void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}
	
	public static TypeDeclaration findTypeDeclaration(JavaScriptUnit astRoot, String simpleTypeName) {
		return (TypeDeclaration) findAbstractTypeDeclaration(astRoot, simpleTypeName);
	}
	
	public static AbstractTypeDeclaration findAbstractTypeDeclaration(JavaScriptUnit astRoot, String simpleTypeName) {
		List types= astRoot.types();
		for (int i= 0; i < types.size(); i++) {
			AbstractTypeDeclaration elem= (AbstractTypeDeclaration) types.get(i);
			if (simpleTypeName.equals(elem.getName().getIdentifier())) {
				return elem;
			}
		}
		return null;
	}
	
	
	public static FunctionDeclaration findMethodDeclaration(JavaScriptUnit astRoot, String simpleTypeName) {
		List statements= astRoot.statements();
		for (int i= 0; i < statements.size(); i++) {
			Object obj=statements.get(i);
			if (obj instanceof FunctionDeclaration) {
							FunctionDeclaration elem= (FunctionDeclaration)obj; 
							if (simpleTypeName.equals(elem.getName().getIdentifier())) {
								return elem;
							}
			}
		}
		return null;
	}


	public static FunctionDeclaration findMethodDeclaration(TypeDeclaration typeDecl, String methodName) {
		FunctionDeclaration[] methods= typeDecl.getMethods();
		for (int i= 0; i < methods.length; i++) {
			if (methodName.equals(methods[i].getName().getIdentifier())) {
				return methods[i];
			}
		}
		return null;
	}
	
	public static SingleVariableDeclaration createNewParam(AST ast, String name) {
		SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
		newParam.setType(ast.newPrimitiveType(PrimitiveType.FLOAT));
		newParam.setName(ast.newSimpleName(name));
		return newParam;
	}
	
	/** @deprecated using deprecated code */
	private void setModifiers(BodyDeclaration bodyDeclaration, int modifiers) {
		bodyDeclaration.setModifiers(modifiers);
	}

	/** @deprecated using deprecated code */
	private void setReturnType(FunctionDeclaration methodDeclaration, Type type) {
		methodDeclaration.setReturnType(type);
	}
	
	protected FieldDeclaration createNewField(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration newFieldDecl= ast.newFieldDeclaration(frag);
		if (ast.apiLevel() == AST_INTERNAL_JLS2) {
			setModifiers(newFieldDecl, Modifier.PRIVATE);
		} else {
			newFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		newFieldDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
		return newFieldDecl;
	}
	
	protected FunctionDeclaration createNewMethod(AST ast, String name, boolean isAbstract) {
		FunctionDeclaration decl= ast.newFunctionDeclaration();
		decl.setName(ast.newSimpleName(name));
		if (ast.apiLevel() == AST_INTERNAL_JLS2) {
			setModifiers(decl, isAbstract ? (Modifier.ABSTRACT | Modifier.PRIVATE) : Modifier.PRIVATE);
			setReturnType(decl, ast.newPrimitiveType(PrimitiveType.VOID));
		} else {
			decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
			if (isAbstract) {
				decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
			}
			decl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		}
		SingleVariableDeclaration param= ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("str"));
		param.setType(ast.newSimpleType(ast.newSimpleName("String")));
		decl.parameters().add(param);
		decl.setBody(isAbstract ? null : ast.newBlock());
		return decl;
	}

}
