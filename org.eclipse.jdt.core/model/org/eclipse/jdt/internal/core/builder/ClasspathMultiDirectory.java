/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.core.Module;
import org.eclipse.jdt.internal.core.ModuleInfo;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathMultiDirectory extends ClasspathDirectory {

IContainer sourceFolder;
char[][] inclusionPatterns; // used by builders when walking source folders
char[][] exclusionPatterns; // used by builders when walking source folders
boolean hasIndependentOutputFolder; // if output folder is not equal to any of the source folders
public boolean ignoreOptionalProblems;

ClasspathMultiDirectory(IContainer sourceFolder, IContainer binaryFolder, char[][] inclusionPatterns, char[][] exclusionPatterns, boolean ignoreOptionalProblems, INameEnvironment env) {
	super(binaryFolder, true, null, null, env);

	this.sourceFolder = sourceFolder;
	this.inclusionPatterns = inclusionPatterns;
	this.exclusionPatterns = exclusionPatterns;
	this.hasIndependentOutputFolder = false;
	this.ignoreOptionalProblems = ignoreOptionalProblems;

	// handle the case when a state rebuilds a source folder
	if (this.inclusionPatterns != null && this.inclusionPatterns.length == 0)
		this.inclusionPatterns = null;
	if (this.exclusionPatterns != null && this.exclusionPatterns.length == 0)
		this.exclusionPatterns = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathMultiDirectory)) return false;

	ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
	if (this.module != md.module)
		if (this.module == null || !this.module.equals(md.module))
			return false;
	return this.ignoreOptionalProblems == md.ignoreOptionalProblems 
		&& this.sourceFolder.equals(md.sourceFolder) && this.binaryFolder.equals(md.binaryFolder)
		&& CharOperation.equals(this.inclusionPatterns, md.inclusionPatterns)
		&& CharOperation.equals(this.exclusionPatterns, md.exclusionPatterns);
}

protected boolean isExcluded(IResource resource) {
	if (this.exclusionPatterns != null || this.inclusionPatterns != null)
		if (this.sourceFolder.equals(this.binaryFolder))
			return Util.isExcluded(resource, this.inclusionPatterns, this.exclusionPatterns);
	return false;
}

public String toString() {
	return "Source classpath directory " + this.sourceFolder.getFullPath().toString() + //$NON-NLS-1$
		" with " + super.toString(); //$NON-NLS-1$
}

public void acceptModuleInfo(ICompilationUnit cu, Parser parser) {
	CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
	CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
	// Request could also come in when module-info has changed or removed.
	if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
		IModuleDeclaration decl = ModuleInfo.createModule(unit.moduleDeclaration);
		if (decl != null) {
			this.module = new Module(this, decl);
		}
	}
}
public void resetModule(IModule mod) {
	this.module = mod;
}

public IModule module() {
	return this.module;
}

}
