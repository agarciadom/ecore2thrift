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
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplate;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EglTemplateFactoryModuleAdapter;
import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.egl.status.StatusMessage;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.IEolExecutableModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import uk.ac.york.mondo.ecore2thrift.Activator;

/**
 * Reusable Eclipse job for running an EGL transformation on an annotated
 * <code>.ecore</code> metamodel, with an extra validation step before invoking
 * the template.
 */
public final class EGLJob extends Job {
	private static final String ECORE_URI = "http://www.eclipse.org/emf/2002/Ecore";
	private static final String PATH_TO_EVL = "/epsilon/ecore2thrift.evl";

	private final IFile ecore;
	private final File destFile;

	public EGLJob(String name, IFile ecore, File dest) {
		super(name);
		this.ecore = ecore;
		this.destFile = dest;
	}

	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Generating Thrift file from Ecore", 3);

		final File ecoreFile = ecore.getLocation().toFile();
		IStatus status = validateInput(monitor, ecoreFile);
		if (status != null) {
			return status;
		}

		final EglFileGeneratingTemplateFactory factory = new EglFileGeneratingTemplateFactory();
		status = loadModel(monitor, ecoreFile, factory);
		if (status != null) {
			return status;
		}

		status = runTemplate(monitor, factory);
		if (status != null) {
			return status;
		}

		status = refreshProject(monitor, ecore);
		if (status != null) {
			return status;
		}
		return Status.OK_STATUS;
	}

	private IStatus validateInput(IProgressMonitor monitor, final File ecoreFile) {
		try {
			monitor.subTask("Validating");
			for (IMarker marker : ecore.findMarkers(EValidator.MARKER, false, IResource.DEPTH_INFINITE)) {
				if (marker.getAttribute("secondary-marker-type", "")
						.equalsIgnoreCase("uk.ac.york.mondo.ecore2thift.validation")) {
					marker.delete();
				}
			}
			EvlModule validateModule = new EvlModule();
			addModelFromFile(validateModule, ecoreFile);
			validateModule.parse(GenerateThriftCommand.class.getResource(PATH_TO_EVL).toURI());
			validateModule.execute();

			List<UnsatisfiedConstraint> unsatisfiedConstraints = validateModule.getContext()
					.getUnsatisfiedConstraints();
			if (!unsatisfiedConstraints.isEmpty()) {
				boolean shouldStop = false;
				for (UnsatisfiedConstraint unsatisfiedConstraint : unsatisfiedConstraints) {
					IMarker marker = ecore.createMarker(EValidator.MARKER);
					if (unsatisfiedConstraint.getConstraint().isCritique()) {
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					} else {
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
						shouldStop = true;
					}
					marker.setAttribute(IMarker.MESSAGE, unsatisfiedConstraint.getMessage());
					marker.setAttribute("secondary-marker-type", "uk.ac.york.mondo.ecore2thift.validation");
				}
				if (shouldStop) {
					Display.getDefault().syncExec(new Runnable(){
						@Override
						public void run() {
							Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
							MessageDialog.openError(activeShell, "Validation errors",
									"There were validation errors in the .ecore file: please check the Problems view.");
						}
					});
					monitor.done();
					return Status.CANCEL_STATUS;
				}
			}
		} catch (Exception e) {
			Activator.getPlugin().logError("There was some error during validation.", e);
			return new Status(Status.ERROR, "ecore2thrift", "There was some error during validation", e);
		}
		return checkIfCancelled(monitor);
	}

	private IStatus checkIfCancelled(IProgressMonitor monitor) {
		monitor.worked(1);
		if (monitor.isCanceled()) {
			monitor.done();
			return Status.CANCEL_STATUS;
		}
		return null;
	}

	private IStatus loadModel(IProgressMonitor monitor, final File ecoreFile,
			final EglFileGeneratingTemplateFactory factory) {
		try {
			monitor.subTask("Loading model");
			final IEolExecutableModule eglModule = new EglTemplateFactoryModuleAdapter(factory);
			addModelFromFile(eglModule, ecoreFile);
		} catch (Exception e) {
			Activator.getPlugin().logError("There was an error while loading the model", e);
			return new Status(Status.ERROR, getName(), "There was an error while loading the model", e);
		}
		return checkIfCancelled(monitor);
	}

	private IStatus runTemplate(IProgressMonitor monitor, final EglFileGeneratingTemplateFactory factory) {
		try {
			monitor.subTask("Processing model");
			final URI ecore2thriftURI = GenerateThriftCommand.class.getResource("/epsilon/" + getName() + ".egl")
					.toURI(); // should I bother giving this a name?
			final EglFileGeneratingTemplate template = (EglFileGeneratingTemplate) factory.load(ecore2thriftURI);
			template.process();
			for (StatusMessage message : factory.getContext().getStatusMessages()) {
				Activator.getPlugin().logInfo(message.getMessage());
			}
			generateOutputFile(template, destFile);
		} catch (Exception e) {
			Activator.getPlugin().logError("There was some error while processing the model", e);
			monitor.done();
			return new Status(Status.ERROR, getName(), "There was some error while processing the model", e);
		}

		return checkIfCancelled(monitor);
	}

	private IStatus refreshProject(IProgressMonitor monitor, final IFile ecore) {
		monitor.subTask("Refreshing project");
		try {
			ecore.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// Not sure when this would be raised.
			// Something to do with situations when you can't refresh?
			monitor.done();
			Activator.getPlugin().logError("There was an error while refreshing the project", e);
			return new Status(Status.ERROR, "ecore2thrift", "There was an error while refreshing the project", e);
		}
		return null;
	}

	private void addModelFromFile(IEolExecutableModule eglModule, File file) throws EolModelLoadingException {
		final EmfModel model = new EmfModel();
		model.setModelFile(file.getAbsolutePath());
		model.setName(file.getName());
		model.setMetamodelUri(ECORE_URI);
		model.load();
		eglModule.getContext().getModelRepository().addModel(model);
	}

	private void generateOutputFile(EglFileGeneratingTemplate template, File of)
			throws IOException, EglRuntimeException {
		of.createNewFile(); // if we have to overwrite, just go for it
		template.generate(of.toURI().toString());
	}
}