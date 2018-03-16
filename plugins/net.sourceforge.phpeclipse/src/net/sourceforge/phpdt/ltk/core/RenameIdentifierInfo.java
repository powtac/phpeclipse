// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.core;

import net.sourceforge.phpdt.internal.core.SourceMethod;

import org.eclipse.core.resources.IFile;

/**
 * <p>
 * an info object that holds the information that is passed from the user to the
 * refactoring.
 * </p>
 * 
 */
public class RenameIdentifierInfo {

	// the offset of the property to be renamed in the file
	private int offset;

	// the new name for the property
	private String newName;

	// the old name of the property (as selected by the user)
	private String oldName;

	// the file that contains the property to be renamed
	private IFile sourceFile;

	// whether the refactoring should also change the identifier
	// in corresponding PHP files in the same project
	private boolean updateProject;

	// whether the refactoring should also update PHP files in other
	// projects than the current one
	private boolean allProjects;

	private boolean renameDQString;

	private boolean renamePHPdoc;

	private boolean renameOtherComments;

	private SourceMethod method;

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(final String newName) {
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(final String oldName) {
		this.oldName = oldName;
	}

	public IFile getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(final IFile sourceFile) {
		this.sourceFile = sourceFile;
	}

	public boolean isAllProjects() {
		return allProjects;
	}

	public void setAllProjects(final boolean allProjects) {
		this.allProjects = allProjects;
	}

	public boolean isUpdateProject() {
		return updateProject;
	}

	public void setUpdateProject(final boolean updateBundle) {
		this.updateProject = updateBundle;
	}

	public SourceMethod getMethod() {
		return method;
	}

	public void setMethod(SourceMethod method) {
		this.method = method;
	}

	public boolean isRenameDQString() {
		return renameDQString;
	}

	public void setRenameDQString(boolean renameDQString) {
		this.renameDQString = renameDQString;
	}

	public boolean isRenameOtherComments() {
		return renameOtherComments;
	}

	public void setRenameOtherComments(boolean renameOtherComments) {
		this.renameOtherComments = renameOtherComments;
	}

	public boolean isRenamePHPdoc() {
		return renamePHPdoc;
	}

	public void setRenamePHPdoc(boolean renamePHPdoc) {
		this.renamePHPdoc = renamePHPdoc;
	}
}
