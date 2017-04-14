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
 * Generates a text file of a certain extension from an Ecore file that includes
 * the Thrift annotations.
 */
public abstract class AbstractGenerateCommand extends AbstractHandler implements IHandler {

	public AbstractGenerateCommand() {
		super();
	}

	protected abstract String getFileExtension();

	protected abstract String getEGLScriptName();

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection0 = HandlerUtil.getCurrentSelection(event);
		if (selection0 instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) selection0;
			final IFile ecore = (IFile) selection.getFirstElement();
			final File dest = ecore.getLocation().removeFileExtension().addFileExtension(getFileExtension()).toFile();

			Job job = new EGLJob(getEGLScriptName(), ecore, dest);
			job.setUser(true);
			job.schedule();
		}

		return null;
	}

}