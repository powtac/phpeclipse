/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaModel;
import net.sourceforge.phpdt.core.IJavaModelStatusConstants;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.IOpenable;
import net.sourceforge.phpdt.core.IParent;
import net.sourceforge.phpdt.core.ISourceRange;
import net.sourceforge.phpdt.core.ISourceReference;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.WorkingCopyOwner;
import net.sourceforge.phpdt.core.jdom.IDOMCompilationUnit;
import net.sourceforge.phpdt.core.jdom.IDOMNode;
import net.sourceforge.phpdt.internal.core.util.MementoTokenizer;
import net.sourceforge.phpdt.internal.core.util.Util;
import net.sourceforge.phpdt.internal.corext.Assert;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Root of Java element handle hierarchy.
 * 
 * @see IJavaElement
 */
public abstract class JavaElement extends PlatformObject implements
		IJavaElement {
	public static final char JEM_ESCAPE = '\\';

	public static final char JEM_JAVAPROJECT = '=';

	public static final char JEM_PACKAGEFRAGMENTROOT = Path.SEPARATOR;

	public static final char JEM_PACKAGEFRAGMENT = '<';

	public static final char JEM_FIELD = '^';

	public static final char JEM_METHOD = '~';

	public static final char JEM_INITIALIZER = '|';

	public static final char JEM_COMPILATIONUNIT = '{';

	// public static final char JEM_CLASSFILE = '(';
	public static final char JEM_TYPE = '[';

	public static final char JEM_PACKAGEDECLARATION = '%';

	public static final char JEM_IMPORTDECLARATION = '#';

	public static final char JEM_COUNT = '!';

	public static final char JEM_LOCALVARIABLE = '@';

	/**
	 * A count to uniquely identify this element in the case that a duplicate
	 * named element exists. For example, if there are two fields in a
	 * compilation unit with the same name, the occurrence count is used to
	 * distinguish them. The occurrence count starts at 1 (thus the first
	 * occurrence is occurrence 1, not occurrence 0).
	 */
	protected int occurrenceCount = 1;

	/**
	 * This element's type - one of the constants defined in
	 * IJavaLanguageElementTypes.
	 */
	// protected int fLEType = 0;
	/**
	 * This element's parent, or <code>null</code> if this element does not
	 * have a parent.
	 */
	protected JavaElement parent;

	/**
	 * This element's name, or an empty <code>String</code> if this element
	 * does not have a name.
	 */
	protected String name;

	protected static final Object NO_INFO = new Object();

	/**
	 * Constructs a handle for a java element with the given parent element and
	 * name.
	 * 
	 * @param parent
	 *            The parent of java element
	 * @param name
	 *            The name of java element
	 * 
	 * @exception IllegalArgumentException
	 *                if the type is not one of the valid Java element type
	 *                constants
	 * 
	 */
	protected JavaElement(JavaElement parent, String name)
			throws IllegalArgumentException {
		this.parent = parent;
		this.name = name;
	}

	/**
	 * @see IOpenable
	 */
	public void close() throws JavaModelException {
		JavaModelManager.getJavaModelManager().removeInfoAndChildren(this);
	}

	/**
	 * This element is being closed. Do any necessary cleanup.
	 */
	protected abstract void closing(Object info) throws JavaModelException;

	/*
	 * Returns a new element info for this element.
	 */
	protected abstract Object createElementInfo();

	/**
	 * Returns true if this handle represents the same Java element as the given
	 * handle. By default, two handles represent the same element if they are
	 * identical or if they represent the same type of element, have equal
	 * names, parents, and occurrence counts.
	 * 
	 * <p>
	 * If a subclass has other requirements for equality, this method must be
	 * overridden.
	 * 
	 * @see Object#equals
	 */
	public boolean equals(Object o) {

		if (this == o)
			return true;

		// Java model parent is null
		if (this.parent == null)
			return super.equals(o);
		if (o instanceof JavaElement) {
			// assume instanceof check is done in subclass
			JavaElement other = (JavaElement) o;
			return this.occurrenceCount == other.occurrenceCount
					&& this.name.equals(other.name)
					&& this.parent.equals(other.parent);
		}
		return false;
	}

	/**
	 * Returns true if this <code>JavaElement</code> is equivalent to the
	 * given <code>IDOMNode</code>.
	 */
	protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
		return false;
	}

	protected void escapeMementoName(StringBuffer buffer, String mementoName) {
		for (int i = 0, length = mementoName.length(); i < length; i++) {
			char character = mementoName.charAt(i);
			switch (character) {
			case JEM_ESCAPE:
			case JEM_COUNT:
			case JEM_JAVAPROJECT:
			case JEM_PACKAGEFRAGMENTROOT:
			case JEM_PACKAGEFRAGMENT:
			case JEM_FIELD:
			case JEM_METHOD:
			case JEM_INITIALIZER:
			case JEM_COMPILATIONUNIT:
				// case JEM_CLASSFILE:
			case JEM_TYPE:
			case JEM_PACKAGEDECLARATION:
			case JEM_IMPORTDECLARATION:
			case JEM_LOCALVARIABLE:
				buffer.append(JEM_ESCAPE);
			}
			buffer.append(character);
		}
	}

	/**
	 * @see IJavaElement
	 */
	public boolean exists() {

		try {
			getElementInfo();
			return true;
		} catch (JavaModelException e) {
		}
		return false;
	}

	/**
	 * Returns the <code>IDOMNode</code> that corresponds to this
	 * <code>JavaElement</code> or <code>null</code> if there is no
	 * corresponding node.
	 */
	public IDOMNode findNode(IDOMCompilationUnit dom) {
		int type = getElementType();
		if (type == IJavaElement.COMPILATION_UNIT || type == IJavaElement.FIELD
				|| type == IJavaElement.IMPORT_DECLARATION
				|| type == IJavaElement.INITIALIZER
				|| type == IJavaElement.METHOD
				|| type == IJavaElement.PACKAGE_DECLARATION
				|| type == IJavaElement.TYPE) {
			ArrayList path = new ArrayList();
			IJavaElement element = this;
			while (element != null
					&& element.getElementType() != IJavaElement.COMPILATION_UNIT) {
				if (element.getElementType() != IJavaElement.IMPORT_CONTAINER) {
					// the DOM does not have import containers, so skip them
					path.add(0, element);
				}
				element = element.getParent();
			}
			if (path.size() == 0) {
				try {
					if (equalsDOMNode(dom)) {
						return dom;
					} else {
						return null;
					}
				} catch (JavaModelException e) {
					return null;
				}
			}
			return ((JavaElement) path.get(0)).followPath(path, 0, dom
					.getFirstChild());
		} else {
			return null;
		}
	}

	/**
	 */
	protected IDOMNode followPath(ArrayList path, int position, IDOMNode node) {

		try {
			if (equalsDOMNode(node)) {
				if (position == (path.size() - 1)) {
					return node;
				} else {
					if (node.getFirstChild() != null) {
						position++;
						return ((JavaElement) path.get(position)).followPath(
								path, position, node.getFirstChild());
					} else {
						return null;
					}
				}
			} else if (node.getNextNode() != null) {
				return followPath(path, position, node.getNextNode());
			} else {
				return null;
			}
		} catch (JavaModelException e) {
			return null;
		}

	}

	/**
	 * @see IJavaElement
	 */
	public IJavaElement getAncestor(int ancestorType) {

		IJavaElement element = this;
		while (element != null) {
			if (element.getElementType() == ancestorType)
				return element;
			element = element.getParent();
		}
		return null;
	}

	/**
	 * Generates the element infos for this element, its ancestors (if they are
	 * not opened) and its children (if it is an Openable). Puts the newly
	 * created element info in the given map.
	 */
	protected abstract void generateInfos(Object info, HashMap newElements,
			IProgressMonitor pm) throws JavaModelException;

	/**
	 * @see IParent
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		return ((JavaElementInfo) getElementInfo()).getChildren();
	}

	/**
	 * Returns a collection of (immediate) children of this node of the
	 * specified type.
	 * 
	 * @param type -
	 *            one of constants defined by IJavaLanguageElementTypes
	 */
	public ArrayList getChildrenOfType(int type) throws JavaModelException {
		IJavaElement[] children = getChildren();
		int size = children.length;
		ArrayList list = new ArrayList(size);
		for (int i = 0; i < size; ++i) {
			JavaElement elt = (JavaElement) children[i];
			if (elt.getElementType() == type) {
				list.add(elt);
			}
		}
		return list;
	}

	/**
	 * @see IMember
	 */
	// public IClassFile getClassFile() {
	// return null;
	// }
	/**
	 * @see IMember
	 */
	public ICompilationUnit getCompilationUnit() {
		return null;
	}

	/**
	 * Returns the info for this handle. If this element is not already open, it
	 * and all of its parents are opened. Does not return null. NOTE: BinaryType
	 * infos are NOT rooted under JavaElementInfo.
	 * 
	 * @exception JavaModelException
	 *                if the element is not present or not accessible
	 */
	public Object getElementInfo() throws JavaModelException {
		return getElementInfo(null);
	}

	/**
	 * Returns the info for this handle. If this element is not already open, it
	 * and all of its parents are opened. Does not return null. NOTE: BinaryType
	 * infos are NOT rooted under JavaElementInfo.
	 * 
	 * @exception JavaModelException
	 *                if the element is not present or not accessible
	 */
	public Object getElementInfo(IProgressMonitor monitor)
			throws JavaModelException {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		Object info = manager.getInfo(this);
		if (info != null)
			return info;
		return openWhenClosed(createElementInfo(), monitor);
	}

	/**
	 * @see IAdaptable
	 */
	public String getElementName() {
		return name;
	}

	/*
	 * Creates a Java element handle from the given memento. The given token is
	 * the current delimiter indicating the type of the next token(s). The given
	 * working copy owner is used only for compilation unit handles.
	 */
	public abstract IJavaElement getHandleFromMemento(String token,
			MementoTokenizer memento, WorkingCopyOwner owner);

	/*
	 * Creates a Java element handle from the given memento. The given working
	 * copy owner is used only for compilation unit handles.
	 */
	public IJavaElement getHandleFromMemento(MementoTokenizer memento,
			WorkingCopyOwner owner) {
		if (!memento.hasMoreTokens())
			return this;
		String token = memento.nextToken();
		return getHandleFromMemento(token, memento, owner);
	}

	/*
	 * Update the occurence count of the receiver and creates a Java element
	 * handle from the given memento. The given working copy owner is used only
	 * for compilation unit handles.
	 */
	public IJavaElement getHandleUpdatingCountFromMemento(
			MementoTokenizer memento, WorkingCopyOwner owner) {
		this.occurrenceCount = Integer.parseInt(memento.nextToken());
		if (!memento.hasMoreTokens())
			return this;
		String token = memento.nextToken();
		return getHandleFromMemento(token, memento, owner);
	}

	/**
	 * @see IJavaElement
	 */
	public String getHandleIdentifier() {
		return getHandleMemento();
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	public String getHandleMemento() {
		StringBuffer buff = new StringBuffer(((JavaElement) getParent())
				.getHandleMemento());
		buff.append(getHandleMementoDelimiter());
		escapeMementoName(buff, getElementName());
		if (this.occurrenceCount > 1) {
			buff.append(JEM_COUNT);
			buff.append(this.occurrenceCount);
		}
		return buff.toString();
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected abstract char getHandleMementoDelimiter();

	/**
	 * @see IJavaElement
	 */
	public IJavaModel getJavaModel() {
		IJavaElement current = this;
		do {
			if (current instanceof IJavaModel)
				return (IJavaModel) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	/**
	 * @see IJavaElement
	 */
	public IJavaProject getJavaProject() {
		IJavaElement current = this;
		do {
			if (current instanceof IJavaProject)
				return (IJavaProject) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	/**
	 * Returns the occurrence count of the handle.
	 */
	protected int getOccurrenceCount() {
		return occurrenceCount;
	}

	/*
	 * @see IJavaElement
	 */
	public IOpenable getOpenable() {
		return this.getOpenableParent();
	}

	/**
	 * Return the first instance of IOpenable in the parent hierarchy of this
	 * element.
	 * 
	 * <p>
	 * Subclasses that are not IOpenable's must override this method.
	 */
	public IOpenable getOpenableParent() {

		return (IOpenable) parent;
	}

	/**
	 * @see IJavaElement
	 */
	public IJavaElement getParent() {
		return parent;
	}

	/*
	 * @see IJavaElement#getPrimaryElement()
	 */
	public IJavaElement getPrimaryElement() {
		return getPrimaryElement(true);
	}

	/*
	 * Returns the primary element. If checkOwner, and the cu owner is primary,
	 * return this element.
	 */
	public IJavaElement getPrimaryElement(boolean checkOwner) {
		return this;
	}

	/**
	 * Returns the element that is located at the given source position in this
	 * element. This is a helper method for
	 * <code>ICompilationUnit#getElementAt</code>, and only works on
	 * compilation units and types. The position given is known to be within
	 * this element's source range already, and if no finer grained element is
	 * found at the position, this element is returned.
	 */
	protected IJavaElement getSourceElementAt(int position)
			throws JavaModelException {
		if (this instanceof ISourceReference) {
			IJavaElement[] children = getChildren();
			int i;
			for (i = 0; i < children.length; i++) {
				IJavaElement aChild = children[i];

				if (aChild instanceof SourceRefElement) {
					SourceRefElement child = (SourceRefElement) children[i];
					ISourceRange range = child.getSourceRange();
					// if (child.name.equals("stopObject")||range==null ||
					// range.getOffset()<=0) {
					// System.out.println(child.name);
					// }
					if (position < range.getOffset() + range.getLength()
							&& position >= range.getOffset()) {
						if (child instanceof IParent) {
							return child.getSourceElementAt(position);
						} else {
							return child;
						}
					}
				}
			}
		} else {
			// should not happen
			Assert.isTrue(false);
		}
		return this;
	}

	/**
	 * Returns the SourceMapper facility for this element, or <code>null</code>
	 * if this element does not have a SourceMapper.
	 */
	// public SourceMapper getSourceMapper() {
	// return ((JavaElement)getParent()).getSourceMapper();
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.core.IJavaElement#getSchedulingRule()
	 */
	public ISchedulingRule getSchedulingRule() {
		IResource resource = getResource();
		if (resource == null) {
			class NoResourceSchedulingRule implements ISchedulingRule {
				public IPath path;

				public NoResourceSchedulingRule(IPath path) {
					this.path = path;
				}

				public boolean contains(ISchedulingRule rule) {
					if (rule instanceof NoResourceSchedulingRule) {
						return this.path
								.isPrefixOf(((NoResourceSchedulingRule) rule).path);
					} else {
						return false;
					}
				}

				public boolean isConflicting(ISchedulingRule rule) {
					if (rule instanceof NoResourceSchedulingRule) {
						IPath otherPath = ((NoResourceSchedulingRule) rule).path;
						return this.path.isPrefixOf(otherPath)
								|| otherPath.isPrefixOf(this.path);
					} else {
						return false;
					}
				}
			}
			return new NoResourceSchedulingRule(getPath());
		} else {
			return resource;
		}
	}

	/**
	 * @see IParent
	 */
	public boolean hasChildren() throws JavaModelException {
		// if I am not open, return true to avoid opening (case of a Java
		// project, a compilation unit or a class file).
		// also see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52474
		Object elementInfo = JavaModelManager.getJavaModelManager().getInfo(
				this);
		if (elementInfo instanceof JavaElementInfo) {
			return ((JavaElementInfo) elementInfo).getChildren().length > 0;
		} else {
			return true;
		}
	}

	/**
	 * Returns the hash code for this Java element. By default, the hash code
	 * for an element is a combination of its name and parent's hash code.
	 * Elements with other requirements must override this method.
	 */
	public int hashCode() {
		if (this.parent == null)
			return super.hashCode();
		return Util.combineHashCodes(this.name.hashCode(), this.parent
				.hashCode());
	}

	/**
	 * Returns true if this element is an ancestor of the given element,
	 * otherwise false.
	 */
	public boolean isAncestorOf(IJavaElement e) {
		IJavaElement parentElement = e.getParent();
		while (parentElement != null && !parentElement.equals(this)) {
			parentElement = parentElement.getParent();
		}
		return parentElement != null;
	}

	/**
	 * @see IJavaElement
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * @see IJavaElement
	 */
	public boolean isStructureKnown() throws JavaModelException {
		return ((JavaElementInfo) getElementInfo()).isStructureKnown();
	}

	/**
	 * Creates and returns and not present exception for this element.
	 */
	protected JavaModelException newNotPresentException() {
		return new JavaModelException(new JavaModelStatus(
				IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * Opens this element and all parents that are not already open.
	 * 
	 * @exception JavaModelException
	 *                this element is not present or accessible
	 */
	// protected void openHierarchy() throws JavaModelException {
	// if (this instanceof IOpenable) {
	// ((Openable) this).openWhenClosed(null);
	// } else {
	// Openable openableParent = (Openable)getOpenableParent();
	// if (openableParent != null) {
	// JavaElementInfo openableParentInfo = (JavaElementInfo)
	// JavaModelManager.getJavaModelManager().getInfo((IJavaElement)
	// openableParent);
	// if (openableParentInfo == null) {
	// openableParent.openWhenClosed(null);
	// } else {
	// throw newNotPresentException();
	// }
	// }
	// }
	// }
	/**
	 * This element has just been opened. Do any necessary setup.
	 */
	protected void opening(Object info) {
	}

	/*
	 * Opens an <code> Openable </code> that is known to be closed (no check for
	 * <code> isOpen() </code> ). Returns the created element info.
	 */
	protected Object openWhenClosed(Object info, IProgressMonitor monitor)
			throws JavaModelException {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		try {
			HashMap newElements = manager.getTemporaryCache();
			generateInfos(info, newElements, monitor);
			if (info == null) {
				info = newElements.get(this);
			}
			if (info == null) { // a source ref element could not be opened
				// close any buffer that was opened for the openable parent
				Iterator iterator = newElements.keySet().iterator();
				while (iterator.hasNext()) {
					IJavaElement element = (IJavaElement) iterator.next();
					if (element instanceof Openable) {
						((Openable) element).closeBuffer();
					}
				}
				throw newNotPresentException();
			}
			if (!hadTemporaryCache) {
				manager.putInfos(this, newElements);
			}
		} finally {
			if (!hadTemporaryCache) {
				manager.resetTemporaryCache();
			}
		}
		return info;
	}

	/**
	 */
	public String readableName() {
		return this.getElementName();
	}

	/**
	 * Removes all cached info from the Java Model, including all children, but
	 * does not close this element.
	 */
	// protected void removeInfo() {
	// Object info = JavaModelManager.getJavaModelManager().peekAtInfo(this);
	// if (info != null) {
	// if (this instanceof IParent) {
	// IJavaElement[] children = ((JavaElementInfo)info).getChildren();
	// for (int i = 0, size = children.length; i < size; ++i) {
	// JavaElement child = (JavaElement) children[i];
	// child.removeInfo();
	// }
	// }
	// JavaModelManager.getJavaModelManager().removeInfo(this);
	// }
	// }
	// /**
	// * Returns a copy of this element rooted at the given project.
	// */
	// public abstract IJavaElement rootedAt(IJavaProject project);
	/**
	 * Runs a Java Model Operation
	 */
	public static void runOperation(JavaModelOperation operation,
			IProgressMonitor monitor) throws JavaModelException {
		try {
			if (operation.isReadOnly()
					|| ResourcesPlugin.getWorkspace().isTreeLocked()) {
				operation.run(monitor);
			} else {
				// use IWorkspace.run(...) to ensure that a build will be done
				// in autobuild mode
				ResourcesPlugin.getWorkspace().run(operation, monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof JavaModelException) {
				throw (JavaModelException) ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e = ce.getStatus().getException();
					if (e instanceof JavaModelException) {
						throw (JavaModelException) e;
					}
				}
				throw new JavaModelException(ce);
			}
		}
	}

	/**
	 * Sets the occurrence count of the handle.
	 */
	protected void setOccurrenceCount(int count) {
		occurrenceCount = count;
	}

	protected String tabString(int tab) {
		StringBuffer buffer = new StringBuffer();
		for (int i = tab; i > 0; i--)
			buffer.append("  "); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Debugging purposes
	 */
	public String toDebugString() {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, NO_INFO);
		return buffer.toString();
	}

	/**
	 * Debugging purposes
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		toString(0, buffer);
		return buffer.toString();
	}

	/**
	 * Debugging purposes
	 */
	protected void toStringName(StringBuffer buffer) {
		buffer.append(getElementName());
		if (this.occurrenceCount > 1) {
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(this.occurrenceCount);
		}
	}

	/**
	 * Debugging purposes
	 */
	protected void toString(int tab, StringBuffer buffer) {
		// Object info = this.toStringInfo(tab, buffer);
		Object info = null;
		if (tab == 0) {
			this.toStringAncestors(buffer);
		}
		this.toStringChildren(tab, buffer, info);
	}

	/**
	 * Debugging purposes
	 */
	public String toStringWithAncestors() {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, NO_INFO);
		this.toStringAncestors(buffer);
		return buffer.toString();
	}

	/**
	 * Debugging purposes
	 */
	protected void toStringAncestors(StringBuffer buffer) {
		JavaElement parent = (JavaElement) this.getParent();
		if (parent != null && parent.getParent() != null) {
			buffer.append(" [in "); //$NON-NLS-1$
			parent.toStringInfo(0, buffer, NO_INFO);
			parent.toStringAncestors(buffer);
			buffer.append("]"); //$NON-NLS-1$
		}
	}

	/**
	 * Debugging purposes
	 */
	protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
		if (info == null || !(info instanceof JavaElementInfo))
			return;
		IJavaElement[] children = ((JavaElementInfo) info).getChildren();
		for (int i = 0; i < children.length; i++) {
			buffer.append("\n"); //$NON-NLS-1$
			((JavaElement) children[i]).toString(tab + 1, buffer);
		}
	}

	/**
	 * Debugging purposes
	 */
	// public Object toStringInfo(int tab, StringBuffer buffer) {
	// Object info = JavaModelManager.getJavaModelManager().peekAtInfo(this);
	// this.toStringInfo(tab, buffer, info);
	// return info;
	// }
	/**
	 * Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		buffer.append(this.tabString(tab));
		buffer.append(getElementName());
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
	}
}