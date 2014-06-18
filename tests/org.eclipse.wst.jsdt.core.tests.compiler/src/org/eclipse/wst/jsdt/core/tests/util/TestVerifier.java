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
package org.eclipse.wst.jsdt.core.tests.util;

import java.io.*;
import java.net.*;
/**
 * Verifies that the .class files resulting from a compilation can be loaded
 * in a VM and that they can be run.
 */
public class TestVerifier {
	public String failureReason;
	
	boolean reuseVM = true;
	String[] classpathCache;
	StringBuffer outputBuffer;
	StringBuffer errorBuffer;
	Socket socket;
public TestVerifier(boolean reuseVM) {
	this.reuseVM = reuseVM;
}
private boolean checkBuffers(String errorString, String outputString, String sourceFileName, String expectedSuccessOutputString) {

	if (errorString.trim().length() > 0) {
		this.failureReason =
			"Unexpected target error running resulting class file for "
				+ sourceFileName
				+ ":\n"
				+ errorString;
		return false;
	}
	String platformIndependantOutputString = Util.convertToIndependantLineDelimiter(outputString.trim());
	if (expectedSuccessOutputString != null && !Util.convertToIndependantLineDelimiter(expectedSuccessOutputString).equals(platformIndependantOutputString)) {
		System.out.println(Util.displayString(platformIndependantOutputString, 2));
		this.failureReason =
			"Unexpected output running resulting class file for "
				+ sourceFileName
				+ ":\n"
				+ "--[START]--\n"
				+ outputString
				+ "---[END]---\n";
		return false;
	}
	
	return true;
}

private boolean checkBuffersThrowingError(String errorString, String sourceFileName, String expectedSuccessOutputString) {

	if (errorString.length() > 0 && errorString.indexOf(expectedSuccessOutputString) != -1) {
		return true;
	}
	
	this.failureReason =
		"Expected error not thrown for "
			+ sourceFileName
			+ ":\n"
			+ expectedSuccessOutputString;
	return false;
}

private void compileVerifyTests(String verifierDir) {
	String fullyQualifiedName = VerifyTests.class.getName();

	int lastDot = fullyQualifiedName.lastIndexOf('.');
	String packageName = fullyQualifiedName.substring(0, lastDot);
	String simpleName = fullyQualifiedName.substring(lastDot + 1);
	
	String dirName = verifierDir.replace('\\', '/') + "/" + packageName.replace('.', '/');
	File dir = new File(dirName.replace('/', File.separatorChar));
	if (!dir.exists() && !dir.mkdirs()) {
		System.out.println("Could not create " + dir);
		return;
	}
	String fileName = dir + File.separator + simpleName + ".java";
	Util.writeToFile(this.getVerifyTestsCode(), fileName);
	org.eclipse.wst.jsdt.internal.compiler.batch.Main.compile("\"" + fileName + "\" -d \"" + verifierDir + "\" -classpath \"" + Util.getJavaClassLibsAsString() + "\"");
}
public void execute(String className, String[] classpaths) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();
}
protected void finalize() throws Throwable {
}
public String getExecutionOutput(){
	return outputBuffer.toString();
}

public String getExecutionError(){
	return errorBuffer.toString();
}
/**
 * Returns the code of the VerifyTests class.
 * 
 * IMPORTANT NOTE: DO NOTE EDIT BUT GENERATE INSTEAD (see below)
 *
 * To generate:
 * - export VerifyTests.java to d:/temp
 * - inspect org.eclipse.wst.jsdt.core.tests.util.Util.fileContentToDisplayString("d:/temp/VerifyTests.java", 2, true)
 */
private String getVerifyTestsCode() {
	return 
		"/*******************************************************************************" +
		" * Copyright (c) 2000, 2005 IBM Corporation and others." +
		" * All rights reserved. This program and the accompanying materials" +
		" * are made available under the terms of the Eclipse Public License v1.0" +
		" * which accompanies this distribution, and is available at" +
		" * http://www.eclipse.org/legal/epl-v10.html" +
		" *" +
		" * Contributors:" +
		" *     IBM Corporation - initial API and implementation" +
		" *******************************************************************************/" +
		"package org.eclipse.wst.jsdt.core.tests.util;\n" + 
		"\n" + 
		"import java.lang.reflect.*;\n" + 
		"import java.io.*;\n" + 
		"import java.net.*;\n" + 
		"import java.util.*;\n" + 
		"\n" + 
		"/******************************************************\n" + 
		" * \n" + 
		" * IMPORTANT NOTE: If modifying this class, copy the source to TestVerifier#getVerifyTestsCode()\n" + 
		" * (see this method for details)\n" + 
		" * \n" + 
		" ******************************************************/\n" + 
		"\n" + 
		"public class VerifyTests {\n" + 
		"	int portNumber;\n" + 
		"	Socket socket;\n" + 
		"\n" + 
		"/**\n" + 
		" * NOTE: Code copied from junit.util.TestCaseClassLoader.\n" + 
		" *\n" + 
		" * A custom class loader which enables the reloading\n" + 
		" * of classes for each test run. The class loader\n" + 
		" * can be configured with a list of package paths that\n" + 
		" * should be excluded from loading. The loading\n" + 
		" * of these packages is delegated to the system class\n" + 
		" * loader. They will be shared across test runs.\n" + 
		" * <p>\n" + 
		" * The list of excluded package paths is specified in\n" + 
		" * a properties file \"excluded.properties\" that is located in \n" + 
		" * the same place as the TestCaseClassLoader class.\n" + 
		" * <p>\n" + 
		" * <b>Known limitation:</b> the VerifyClassLoader cannot load classes\n" + 
		" * from jar files.\n" + 
		" */\n" + 
		"\n" + 
		"\n" + 
		"public class VerifyClassLoader extends ClassLoader {\n" + 
		"	/** scanned class path */\n" + 
		"	private String[] fPathItems;\n" + 
		"	\n" + 
		"	/** excluded paths */\n" + 
		"	private String[] fExcluded= {};\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Constructs a VerifyClassLoader. It scans the class path\n" + 
		"	 * and the excluded package paths\n" + 
		"	 */\n" + 
		"	public VerifyClassLoader() {\n" + 
		"		super();\n" + 
		"		String classPath= System.getProperty(\"java.class.path\");\n" + 
		"		String separator= System.getProperty(\"path.separator\");\n" + 
		"		\n" + 
		"		// first pass: count elements\n" + 
		"		StringTokenizer st= new StringTokenizer(classPath, separator);\n" + 
		"		int i= 0;\n" + 
		"		while (st.hasMoreTokens()) {\n" + 
		"			st.nextToken();\n" + 
		"			i++;\n" + 
		"		}\n" + 
		"		// second pass: split\n" + 
		"		fPathItems= new String[i];\n" + 
		"		st= new StringTokenizer(classPath, separator);\n" + 
		"		i= 0;\n" + 
		"		while (st.hasMoreTokens()) {\n" + 
		"			fPathItems[i++]= st.nextToken();\n" + 
		"		}\n" + 
		"\n" + 
		"	}\n" + 
		"	public java.net.URL getResource(String name) {\n" + 
		"		return ClassLoader.getSystemResource(name);\n" + 
		"	}\n" + 
		"	public InputStream getResourceAsStream(String name) {\n" + 
		"		return ClassLoader.getSystemResourceAsStream(name);\n" + 
		"	}\n" + 
		"	protected boolean isExcluded(String name) {\n" + 
		"		// exclude the \"java\" packages.\n" + 
		"		// They always need to be excluded so that they are loaded by the system class loader\n" + 
		"		if (name.startsWith(\"java\"))\n" + 
		"			return true;\n" + 
		"			\n" + 
		"		// exclude the user defined package paths\n" + 
		"		for (int i= 0; i < fExcluded.length; i++) {\n" + 
		"			if (name.startsWith(fExcluded[i])) {\n" + 
		"				return true;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		return false;	\n" + 
		"	}\n" + 
		"	public synchronized Class loadClass(String name, boolean resolve)\n" + 
		"		throws ClassNotFoundException {\n" + 
		"			\n" + 
		"		Class c= findLoadedClass(name);\n" + 
		"		if (c != null)\n" + 
		"			return c;\n" + 
		"		//\n" + 
		"		// Delegate the loading of excluded classes to the\n" + 
		"		// standard class loader.\n" + 
		"		//\n" + 
		"		if (isExcluded(name)) {\n" + 
		"			try {\n" + 
		"				c= findSystemClass(name);\n" + 
		"				return c;\n" + 
		"			} catch (ClassNotFoundException e) {\n" + 
		"				// keep searching\n" + 
		"			}\n" + 
		"		}\n" + 
		"		File file= locate(name);\n" + 
		"		if (file == null)\n" + 
		"			throw new ClassNotFoundException();\n" + 
		"		byte data[]= loadClassData(file);\n" + 
		"		c= defineClass(name, data, 0, data.length);\n" + 
		"		if (resolve) \n" + 
		"			resolveClass(c);\n" + 
		"		return c;\n" + 
		"	}\n" + 
		"	private byte[] loadClassData(File f) throws ClassNotFoundException {\n" + 
		"		try {\n" + 
		"			//System.out.println(\"loading: \"+f.getPath());\n" + 
		"			FileInputStream stream= new FileInputStream(f);\n" + 
		"			\n" + 
		"			try {\n" + 
		"				byte[] b= new byte[stream.available()];\n" + 
		"				stream.read(b);\n" + 
		"				stream.close();\n" + 
		"				return b;\n" + 
		"			}\n" + 
		"			catch (IOException e) {\n" + 
		"				throw new ClassNotFoundException();\n" + 
		"			}\n" + 
		"		}\n" + 
		"		catch (FileNotFoundException e) {\n" + 
		"			throw new ClassNotFoundException();\n" + 
		"		}\n" + 
		"	}\n" + 
		"	/**\n" + 
		"	 * Locate the given file.\n" + 
		"	 * @return Returns null if file couldn\'t be found.\n" + 
		"	 */\n" + 
		"	private File locate(String fileName) { \n" + 
		"		fileName= fileName.replace(\'.\', \'/\')+\".class\";\n" + 
		"		File path= null;\n" + 
		"		\n" + 
		"		if (fileName != null) {\n" + 
		"			for (int i= 0; i < fPathItems.length; i++) {\n" + 
		"				path= new File(fPathItems[i], fileName);\n" + 
		"				if (path.exists())\n" + 
		"					return path;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n" + 
		"	\n" + 
		"public void loadAndRun(String className) throws Throwable {\n" + 
		"	//System.out.println(\"Loading \" + className + \"...\");\n" + 
		"	Class testClass = new VerifyClassLoader().loadClass(className);\n" + 
		"	//System.out.println(\"Loaded \" + className);\n" + 
		"	try {\n" + 
		"		Method main = testClass.getMethod(\"main\", new Class[] {String[].class});\n" + 
		"		//System.out.println(\"Running \" + className);\n" + 
		"		main.invoke(null, new Object[] {new String[] {}});\n" + 
		"		//System.out.println(\"Finished running \" + className);\n" + 
		"	} catch (NoSuchMethodException e) {\n" + 
		"		return;\n" + 
		"	} catch (InvocationTargetException e) {\n" + 
		"		throw e.getTargetException();\n" + 
		"	}\n" + 
		"}\n" + 
		"public static void main(String[] args) throws IOException {\n" + 
		"	VerifyTests verify = new VerifyTests();\n" + 
		"	verify.portNumber = Integer.parseInt(args[0]);\n" + 
		"	verify.run();\n" + 
		"}\n" + 
		"public void run() throws IOException {\n" + 
		"	ServerSocket server = new ServerSocket(this.portNumber);\n" + 
		"	this.socket = server.accept();\n" + 
		"	this.socket.setTcpNoDelay(true);\n" + 
		"	server.close();\n" + 
		"\n" + 
		"	DataInputStream in = new DataInputStream(this.socket.getInputStream());\n" + 
		"	final DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());\n" + 
		"	while (true) {\n" + 
		"		final String className = in.readUTF();\n" + 
		"		Thread thread = new Thread() {\n" + 
		"			public void run() {\n" + 
		"				try {\n" + 
		"					loadAndRun(className);\n" + 
		"					out.writeBoolean(true);\n" + 
		"					System.err.println(VerifyTests.class.getName());\n" + 
		"					System.out.println(VerifyTests.class.getName());\n" + 
		"				} catch (Throwable e) {\n" + 
		"					e.printStackTrace();\n" + 
		"					try {\n" + 
		"						System.err.println(VerifyTests.class.getName());\n" + 
		"						System.out.println(VerifyTests.class.getName());\n" + 
		"						out.writeBoolean(false);\n" + 
		"					} catch (IOException e1) {\n" + 
		"						// ignore\n" + 
		"					}\n" + 
		"				}\n" + 
		"			}\n" + 
		"		};\n" + 
		"		thread.start();\n" + 
		"	}\n" + 
		"}\n" + 
		"}\n";
}
/**
 * Loads and runs the given class.
 * Return whether no exception was thrown while running the class.
 */
private boolean loadAndRun(String className) {
	if (this.socket != null) {
		try {
			DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
			out.writeUTF(className);
			DataInputStream in = new DataInputStream(this.socket.getInputStream());
			try {
				boolean result = in.readBoolean();
				this.waitForFullBuffers();
				return result;
			} catch (SocketException e) {
				// connection was reset because target program has exited
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	return true;
}
/**
 * Verify that the class files created for the given test file can be loaded by
 * a virtual machine.
 */
public boolean verifyClassFiles(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths) {
	return verifyClassFiles(sourceFilePath, className, expectedSuccessOutputString, classpaths, null, null);
}
/**
 * Verify that the class files created for the given test file can be loaded by
 * a virtual machine.
 */
public boolean verifyClassFiles(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths, String[] programArguments, String[] vmArguments) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();
	if (this.reuseVM && programArguments == null) {
		this.loadAndRun(className);
	}
	
	this.failureReason = null;
	return this.checkBuffers(this.errorBuffer.toString(), this.outputBuffer.toString(), sourceFilePath, expectedSuccessOutputString);
}

/**
 * Verify that the class files created for the given test file can be loaded and run with an expected error contained
 * in the expectedSuccessOutputString string.
 */
public boolean verifyClassFilesThrowingError(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths, String[] programArguments, String[] vmArguments) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();
	if (this.reuseVM && programArguments == null) {
		this.loadAndRun(className);
	}
	
	this.failureReason = null;
	return this.checkBuffersThrowingError(this.errorBuffer.toString(), sourceFilePath, expectedSuccessOutputString);
}

/**
 * Wait until there is nothing more to read from the stdout or sterr.
 */
private void waitForFullBuffers() {
	String endString = VerifyTests.class.getName();
	int count = 50;
	int errorEndStringStart = this.errorBuffer.toString().indexOf(endString);
	int outputEndStringStart = this.outputBuffer.toString().indexOf(endString);
	while (errorEndStringStart == -1 || outputEndStringStart == -1) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		if (--count == 0) return;
		errorEndStringStart = this.errorBuffer.toString().indexOf(endString);
		outputEndStringStart = this.outputBuffer.toString().indexOf(endString);
	}
	this.errorBuffer.setLength(errorEndStringStart);
	this.outputBuffer.setLength(outputEndStringStart);
}
}
