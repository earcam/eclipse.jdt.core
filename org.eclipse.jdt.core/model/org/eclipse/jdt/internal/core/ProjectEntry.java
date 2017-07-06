/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;

/**
 * Represents a project
 *
 */
public class ProjectEntry implements IModulePathEntry {

	JavaProject project;
	
	public ProjectEntry(JavaProject project) {
		// 
		this.project = project;
	}
	@Override
	public IModule getModule() {
		try {
			IModuleDescription module = this.project.getModuleDescription();
			if (module != null) {
				return (ModuleDescriptionInfo) ((JavaElement) module) .getElementInfo();
			}
		} catch (JavaModelException e) {
			// Proceed with null;
		}
		return null;
	}

	@Override
	public boolean equalsProject(IJavaProject otherProject) {
		return this.project.equals(otherProject);
	}

	@Override
	public boolean isAutomaticModule() {
		return false;
	}

	@Override
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		// TODO(SHMOD): verify (is unnamed handled correctly?)
		IModule mod = getModule();
		if (mod == null) {
			if (moduleName != null)
				return null;
		} else if (!String.valueOf(mod.name()).equals(moduleName)) {
			return null;
		}
		try {
			IJavaElement element = this.project.findElement(new Path(qualifiedPackageName.replace('.', '/')));
			if (element instanceof IPackageFragment)
				return mod != null ? new char[][] { mod.name() } : CharOperation.NO_CHAR_CHAR;
		} catch (JavaModelException e) {
			return null;
		}
		return null;
	}
}
