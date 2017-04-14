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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Generates a <code>.thrift</code> file from an annotated <code>.ecore</code>
 * metamodel.
 */
public class GenerateThriftCommand extends AbstractGenerateCommand {

	@Override
	protected String getEGLScriptName() {
		return "ecore2thrift";
	}

	@Override
	protected String getFileExtension() {
		return "thrift";
	}

}
