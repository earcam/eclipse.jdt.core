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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileReader;

public class ClassFileReaderTest_1_5 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 83 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_5.class;
	}

	public ClassFileReaderTest_1_5(String name) {
		super(name);
	}

	/**
	 * @deprecated
	 */
	private void checkClassFileUsingInputStream(String directoryName, String className, String source, String expectedOutput, int mode) throws IOException {
		compileAndDeploy(source, directoryName, className);
		BufferedInputStream inputStream = null;
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, className + ".class");
			inputStream = new BufferedInputStream(new FileInputStream(f));
			IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(inputStream, IClassFileReader.ALL);
			assertNotNull(classFileReader);
			String result = ToolFactory.createDefaultClassFileDisassembler().disassemble(classFileReader, "\n", mode);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
			removeTempClass(className);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test001() throws ClassFormatException, IOException {
		String source =
			"public class X {\n" +
			"	X(String s) {\n" +
			"	}\n" +
			"	public void foo(int i, long l, String[][]... args) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V\n" + 
			"  // Stack: 0, Locals: 5\n" + 
			"  public void foo(int i, long l, java.lang.String[][]... args);\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" + 
			"        [pc: 0, pc: 1] local: i index: 1 type: int\n" + 
			"        [pc: 0, pc: 1] local: l index: 2 type: long\n" + 
			"        [pc: 0, pc: 1] local: args index: 4 type: java.lang.String[][][]\n" + 
			"}";
		checkClassFile("X", source, expectedOutput);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76472
	 */
	public void test002() throws ClassFormatException, IOException {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		long[] tab = new long[] {};\n" + 
			"		System.out.println(tab.clone());\n" + 
			"		System.out.println(tab.clone());\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  newarray long [11]\n" + 
			"     3  astore_1 [tab]\n" + 
			"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     7  aload_1 [tab]\n" + 
			"     8  invokevirtual long[].clone() : java.lang.Object [22]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" + 
			"    14  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    17  aload_1 [tab]\n" + 
			"    18  invokevirtual long[].clone() : java.lang.Object [22]\n" + 
			"    21  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" + 
			"    24  return\n";
		checkClassFile("X", source, expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test003() throws ClassFormatException, IOException {
		String source =
			"public class Y<W, U extends java.io.Reader & java.io.Serializable> {\n" + 
			"  U field;\n" +
			"  String field2;\n" +
			"  <T> Y(T t) {}\n" +
			"  <T> T foo(T t, String... s) {\n" + 
			"    return t;\n" + 
			"  }\n" + 
			"}";
		String expectedOutput =
			"public class Y<W,U extends Reader & Serializable> {\n" + 
			"  \n" + 
			"  U field;\n" + 
			"  \n" + 
			"  String field2;\n" + 
			"  \n" + 
			"  <T> Y(T t) {\n" + 
			"  }\n" + 
			"  \n" + 
			"  <T> T foo(T t, String... s) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test004() throws ClassFormatException, IOException {
		String source =
			"public class Y<W, U extends java.io.Reader & java.io.Serializable> {\n" + 
			"  U field;\n" +
			"  String field2;\n" +
			"  <T> Y(T t) {}\n" +
			"  <T> T foo(T t, String... s) {\n" + 
			"    return t;\n" + 
			"  }\n" + 
			"}";
		String expectedOutput =
			"public class Y<W,U extends java.io.Reader & java.io.Serializable> {\n" + 
			"  \n" + 
			"  U field;\n" + 
			"  \n" + 
			"  java.lang.String field2;\n" + 
			"  \n" + 
			"  <T> Y(T t) {\n" + 
			"  }\n" + 
			"  \n" + 
			"  <T> T foo(T t, java.lang.String... s) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test005() throws ClassFormatException, IOException {
		String source =
			"public class X {\n" +
			"	X(String s) {\n" +
			"	}\n" +
			"	public static void foo(int i, long l, String[][]... args) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V\n" + 
			"  // Stack: 0, Locals: 4\n" + 
			"  public static void foo(int i, long l, java.lang.String[][]... args);\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: i index: 0 type: int\n" + 
			"        [pc: 0, pc: 1] local: l index: 1 type: long\n" + 
			"        [pc: 0, pc: 1] local: args index: 3 type: java.lang.String[][][]\n" + 
			"}";
		checkClassFile("X", source, expectedOutput);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 */
	public void test006() throws ClassFormatException, IOException {
		String source =
			"public enum X { \n" + 
			"	\n" + 
			"	BLEU(10),\n" + 
			"	BLANC(20),\n" + 
			"	ROUGE(30);\n" +
			"	X(int i) {}\n" +
			"}\n";
		String expectedOutput =
			"public enum X {\n" + 
			"  \n" + 
			"  BLEU(0),\n" + 
			"  \n" + 
			"  BLANC(0),\n" + 
			"  \n" + 
			"  ROUGE(0),;\n" + 
			"  \n" + 
			"  private X(int i) {\n" + 
			"  }\n" + 
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test007() throws ClassFormatException, IOException {
		String source =
			"public enum X {\n" +
			"	BLEU(0) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLEU\";\n" +
			"		}\n" +
			"	},\n" +
			"	BLANC(1) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLANC\";\n" +
			"		}\n" +
			"	},\n" +
			"	ROUGE(2) {\n" +
			"		public String colorName() {\n" +
			"			return \"ROUGE\";\n" +
			"		}\n" +
			"	},;\n" +
			"	\n" +
			"	X(int i) {\n" +
			"	}\n" +
			"	abstract public String colorName();\n" +
			"}";
		String expectedOutput =
			"public enum X {\n" +
			"  \n" +
			"  BLEU(0),\n" +
			"  \n" +
			"  BLANC(0),\n" +
			"  \n" +
			"  ROUGE(0),;\n" +
			"  \n" +
			"  private X(int i) {\n" +
			"  }\n" +
			"  \n" +
			"  public abstract java.lang.String colorName();\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test008() throws ClassFormatException, IOException {
		String source =
			"interface I {\n" +
			"	String colorName();\n" +
			"}\n" +
			"public enum X implements I {\n" +
			"	BLEU(0) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLEU\";\n" +
			"		}\n" +
			"	},\n" +
			"	BLANC(1) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLANC\";\n" +
			"		}\n" +
			"	},\n" +
			"	ROUGE(2) {\n" +
			"		public String colorName() {\n" +
			"			return \"ROUGE\";\n" +
			"		}\n" +
			"	},;\n" +
			"	\n" +
			"	X(int i) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"public enum X implements I {\n" + 
			"  \n" + 
			"  BLEU(0),\n" + 
			"  \n" + 
			"  BLANC(0),\n" + 
			"  \n" + 
			"  ROUGE(0),;\n" + 
			"  \n" + 
			"  private X(int i) {\n" + 
			"  }\n" + 
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 */
	public void test009() throws ClassFormatException, IOException {
		String source =
			"@interface X {\n" +
			"	String firstName();\n" +
			"	String lastName() default \"Smith\";\n" +
			"}\n";
		String expectedOutput =
			"abstract @interface X {\n" + 
			"  \n" + 
			"  public abstract java.lang.String firstName();\n" + 
			"  \n" + 
			"  public abstract java.lang.String lastName() default \"Smith\";\n" + 
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 * @deprecated Using deprecated API
	 */
	public void test010() throws ClassFormatException, IOException {
		String source =
			"@interface X {\n" +
			"	String firstName();\n" +
			"	String lastName() default \"Smith\";\n" +
			"}\n";
		String expectedOutput =
			"abstract @interface X {\n" + 
			"  \n" + 
			"  public abstract java.lang.String firstName();\n" + 
			"  \n" + 
			"  public abstract java.lang.String lastName() default \"Smith\";\n" + 
			"}";
		checkClassFileUsingInputStream("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203577
	 */
	public void test011() throws ClassFormatException, IOException {
		String source =
			"import java.lang.annotation.Retention;\n" + 
			"import java.lang.annotation.RetentionPolicy;\n" + 
			"import java.lang.annotation.Target;\n" + 
			"\n" + 
			"@Target(value={})\n" + 
			"@Retention(value=RetentionPolicy.RUNTIME)\n" + 
			"public @interface X {}";
		String expectedOutput =
			"public abstract @interface X extends java.lang.annotation.Annotation {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 class: #6 java/lang/annotation/Annotation\n" + 
			"    constant #6 utf8: \"java/lang/annotation/Annotation\"\n" + 
			"    constant #7 utf8: \"SourceFile\"\n" + 
			"    constant #8 utf8: \"X.java\"\n" + 
			"    constant #9 utf8: \"RuntimeVisibleAnnotations\"\n" + 
			"    constant #10 utf8: \"Ljava/lang/annotation/Target;\"\n" + 
			"    constant #11 utf8: \"value\"\n" + 
			"    constant #12 utf8: \"Ljava/lang/annotation/Retention;\"\n" + 
			"    constant #13 utf8: \"Ljava/lang/annotation/RetentionPolicy;\"\n" + 
			"    constant #14 utf8: \"RUNTIME\"\n" + 
			"\n" + 
			"  RuntimeVisibleAnnotations: \n" + 
			"    #10 @java.lang.annotation.Target(\n" + 
			"      #11 value=[\n" + 
			"        ]\n" + 
			"    )\n" + 
			"    #12 @java.lang.annotation.Retention(\n" + 
			"      #11 value=java.lang.annotation.RetentionPolicy.RUNTIME(enum type #13.#14)\n" + 
			"    )\n" + 
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
}
