/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class SingleParameterizedTypeReference extends ArrayTypeReference {

	public TypeReference[] typeArguments;
	private boolean didResolve = false;
	
	public SingleParameterizedTypeReference(char[] name, TypeReference[] typeArguments, int dim, long pos){
		super(name, dim, pos);
		this.typeArguments = typeArguments;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		this.dimensions = dim;
		return this;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    public TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }	

	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append(token);
		output.append("<"); //$NON-NLS-1$
		int max = typeArguments.length - 1;
		for (int i= 0; i < max; i++) {
			typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		typeArguments[max].print(0, output);
		output.append(">"); //$NON-NLS-1$
		for (int i= 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
		}
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		// handle the error here
		this.constant = NotAConstant;
		if (this.didResolve) { // is a shared type reference which was already resolved
			if (this.resolvedType != null && !this.resolvedType.isValidBinding()) {
				return null; // already reported error
			}
			return this.resolvedType;
		} 
	    this.didResolve = true;
		ReferenceBinding currentType = null;
		TypeBinding type = scope.getType(token);
		if (!(type.isValidBinding())) {
			reportInvalidType(scope);
			return null;
		}
		currentType = (ReferenceBinding) type;
	    // check generic and arity
		TypeVariableBinding[] typeVariables = currentType.typeVariables();
		int argLength = this.typeArguments.length;
		TypeBinding[] argTypes = new TypeBinding[argLength];
		for (int j = 0; j < argLength; j++) {
		    argTypes[j] = this.typeArguments[j].resolveType(scope);
		}
		if (typeVariables == NoTypeVariables) { // check generic
				scope.problemReporter().nonGenericTypeCannotBeParameterized(this, currentType, argTypes);
				return null;
		} else if (argLength != typeVariables.length) { // check arity
				scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
				return null;
		}			
		// check argument type compatibility
		boolean argHasError = false;
		for (int j = 0; j < argLength; j++) {
		    if (!argTypes[j].isCompatibleWith(typeVariables[j])) {
		        argHasError = true;
				scope.problemReporter().typeMismatchError(argTypes[j], typeVariables[j], currentType, this.typeArguments[j]);
		    }
		}
		if (argHasError) return null;
		currentType = scope.createParameterizedType(currentType, argTypes);
		this.resolvedType = currentType;
		if (isTypeUseDeprecated(this.resolvedType, scope)) {
			reportDeprecatedType(scope);
		}		
		// array type ?
		if (this.dimensions > 0) {
			if (dimensions > 255) {
				scope.problemReporter().tooManyDimensions(this);
			}
			this.resolvedType = scope.createArray(currentType, dimensions);
		}
		return this.resolvedType;
	}	
}
