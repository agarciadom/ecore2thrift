/*******************************************************************************
 * Copyright (c) 2015 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nathan van Doorn - initial API and implementation
 *     Antonio Garcia-Dominguez - extract EGLJob into separate class
 ******************************************************************************/
package uk.ac.york.mondo.ecore2thrift.handlers;

/**
 * Generates a <code>.thrift</code> file from an annotated <code>.ecore</code>
 * metamodel.
 */
public class GenerateMarkdownCommand extends AbstractGenerateCommand {

	@Override
	protected String getEGLScriptName() {
		return "ecore2markdown";
	}

	@Override
	protected String getFileExtension() {
		return "md";
	}

}
