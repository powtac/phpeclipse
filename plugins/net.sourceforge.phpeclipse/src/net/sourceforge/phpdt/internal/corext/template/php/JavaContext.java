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
package net.sourceforge.phpdt.internal.corext.template.php;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.internal.corext.Assert;
import net.sourceforge.phpdt.internal.corext.template.php.CompilationUnitCompletion.LocalVariable;
import net.sourceforge.phpdt.internal.corext.util.Strings;
import net.sourceforge.phpdt.internal.ui.preferences.CodeFormatterPreferencePage;
import net.sourceforge.phpdt.internal.ui.text.template.contentassist.MultiVariable;
import net.sourceforge.phpdt.internal.ui.util.ExceptionHandler;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.swt.widgets.Shell;

/**
 * A context for java source.
 */
public class JavaContext extends CompilationUnitContext {

	/** The platform default line delimiter. */
	private static final String PLATFORM_LINE_DELIMITER = System
			.getProperty("line.separator"); //$NON-NLS-1$

	/** A code completion requestor for guessing local variable names. */
	private CompilationUnitCompletion fCompletion;

	/**
	 * Creates a java template context.
	 * 
	 * @param type
	 *            the context type.
	 * @param document
	 *            the document.
	 * @param completionOffset
	 *            the completion offset within the document.
	 * @param completionLength
	 *            the completion length.
	 * @param compilationUnit
	 *            the compilation unit (may be <code>null</code>).
	 */
	public JavaContext(TemplateContextType type, IDocument document,
			int completionOffset, int completionLength,
			ICompilationUnit compilationUnit) {
		super(type, document, completionOffset, completionLength,
				compilationUnit);
	}

	/**
	 * Returns the indentation level at the position of code completion.
	 */
	private int getIndentation() {
		int start = getStart();
		IDocument document = getDocument();
		try {
			IRegion region = document.getLineInformationOfOffset(start);
			String lineContent = document.get(region.getOffset(), region
					.getLength());
			return Strings.computeIndent(lineContent,
					CodeFormatterPreferencePage.getTabSize());
			// return Strings.computeIndent(lineContent,
			// CodeFormatterUtil.getTabWidth());
		} catch (BadLocationException e) {
			return 0;
		}
	}

	/*
	 * @see TemplateContext#evaluate(Template template)
	 */
	public TemplateBuffer evaluate(Template template)
			throws BadLocationException, TemplateException {

		if (!canEvaluate(template))
			throw new TemplateException(JavaTemplateMessages
					.getString("Context.error.cannot.evaluate")); //$NON-NLS-1$

		TemplateTranslator translator = new TemplateTranslator() {
			/*
			 * @see org.eclipse.jface.text.templates.TemplateTranslator#createVariable(java.lang.String,
			 *      java.lang.String, int[])
			 */
			protected TemplateVariable createVariable(String type, String name,
					int[] offsets) {
				return new MultiVariable(type, name, offsets);
			}
		};
		TemplateBuffer buffer = translator.translate(template);

		getContextType().resolve(buffer, this);
		String lineDelimiter = null;
		try {
			lineDelimiter = getDocument().getLineDelimiter(0);
		} catch (BadLocationException e) {
		}

		if (lineDelimiter == null)
			lineDelimiter = PLATFORM_LINE_DELIMITER;
		IPreferenceStore prefs = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		// axelcl start
		// boolean useCodeFormatter =
		// prefs.getBoolean(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER);
		boolean useCodeFormatter = false;
		// axelcl end

		JavaFormatter formatter = new JavaFormatter(lineDelimiter,
				getIndentation(), useCodeFormatter);
		formatter.format(buffer, this);
		// debug start
		// String res = buffer.getString();
		// res = res.replaceAll("\n","/n");
		// res = res.replaceAll("\t","/t");
		// System.out.println(res);
		// debug end
		return buffer;
	}

	/*
	 * @see TemplateContext#canEvaluate(Template templates)
	 */
	public boolean canEvaluate(Template template) {
		String key = getKey();

		if (fForceEvaluation)
			return true;

		return template.matches(key, getContextType().getId())
				&& key.length() != 0
				&& template.getName().toLowerCase().startsWith(
						key.toLowerCase());
	}

	public boolean canEvaluate(String identifier) {
		String prefix = getKey();
		return identifier.toLowerCase().startsWith(prefix.toLowerCase());
	}

	/*
	 * @see DocumentTemplateContext#getCompletionPosition();
	 */
	public int getStart() {

		try {
			IDocument document = getDocument();

			if (getCompletionLength() == 0) {

				int start = getCompletionOffset();
				while ((start != 0)
						&& Character.isUnicodeIdentifierPart(document
								.getChar(start - 1)))
					start--;

				if ((start != 0)
						&& (Character.isUnicodeIdentifierStart(document
								.getChar(start - 1)) || (document
								.getChar(start - 1) == '$')))
					start--;

				return start;

			} else {

				int start = getCompletionOffset();
				int end = getCompletionOffset() + getCompletionLength();

				while (start != 0
						&& Character.isUnicodeIdentifierPart(document
								.getChar(start - 1)))
					start--;
				if ((start != 0)
						&& (Character.isUnicodeIdentifierStart(document
								.getChar(start - 1)) || (document
								.getChar(start - 1) == '$')))
					start--;
				while (start != end
						&& Character.isWhitespace(document.getChar(start)))
					start++;

				if (start == end)
					start = getCompletionOffset();

				return start;
			}

		} catch (BadLocationException e) {
			return super.getStart();
		}
	}

	/*
	 * @see net.sourceforge.phpdt.internal.corext.template.DocumentTemplateContext#getEnd()
	 */
	public int getEnd() {

		if (getCompletionLength() == 0)
			return super.getEnd();

		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != end
					&& Character.isWhitespace(document.getChar(end - 1)))
				end--;

			return end;

		} catch (BadLocationException e) {
			return super.getEnd();
		}
	}

	/*
	 * @see net.sourceforge.phpdt.internal.corext.template.DocumentTemplateContext#getKey()
	 */
	public String getKey() {

		// if (getCompletionLength() == 0) {
		// return super.getKey();
		// }

		try {
			IDocument document = getDocument();

			int start = getStart();
			int end = getCompletionOffset();
			return start <= end ? document.get(start, end - start) : ""; //$NON-NLS-1$

		} catch (BadLocationException e) {
			return super.getKey();
		}
	}

	/**
	 * Returns the character before start position of completion.
	 */
	public char getCharacterBeforeStart() {
		int start = getStart();

		try {
			return start == 0 ? ' ' : getDocument().getChar(start - 1);

		} catch (BadLocationException e) {
			return ' ';
		}
	}

	private static void handleException(Shell shell, Exception e) {
		String title = JavaTemplateMessages
				.getString("JavaContext.error.title"); //$NON-NLS-1$
		if (e instanceof CoreException)
			ExceptionHandler.handle((CoreException) e, shell, title, null);
		else if (e instanceof InvocationTargetException)
			ExceptionHandler.handle((InvocationTargetException) e, shell,
					title, null);
		else {
			PHPeclipsePlugin.log(e);
			MessageDialog.openError(shell, title, e.getMessage());
		}
	}

	// private CompilationUnitCompletion getCompletion() {
	// ICompilationUnit compilationUnit= getCompilationUnit();
	// if (fCompletion == null) {
	// fCompletion= new CompilationUnitCompletion(compilationUnit);
	//			
	// if (compilationUnit != null) {
	// try {
	// compilationUnit.codeComplete(getStart(), fCompletion);
	// } catch (JavaModelException e) {
	// // ignore
	// }
	// }
	// }
	//		
	// return fCompletion;
	// }

	/**
	 * Returns the name of a guessed local array, <code>null</code> if no
	 * local array exists.
	 */
	// public String guessArray() {
	// return firstOrNull(guessArrays());
	// }
	/**
	 * Returns the name of a guessed local array, <code>null</code> if no
	 * local array exists.
	 */
	// public String[] guessArrays() {
	// CompilationUnitCompletion completion= getCompletion();
	// LocalVariable[] localArrays= completion.findLocalArrays();
	//				
	// String[] ret= new String[localArrays.length];
	// for (int i= 0; i < ret.length; i++) {
	// ret[ret.length - i - 1]= localArrays[i].name;
	// }
	// return ret;
	// }
	/**
	 * Returns the name of the type of a local array, <code>null</code> if no
	 * local array exists.
	 */
	// public String guessArrayType() {
	// return firstOrNull(guessArrayTypes());
	// }
	private String firstOrNull(String[] strings) {
		if (strings.length > 0)
			return strings[0];
		else
			return null;
	}

	/**
	 * Returns the name of the type of a local array, <code>null</code> if no
	 * local array exists.
	 */
	// public String[][] guessGroupedArrayTypes() {
	// CompilationUnitCompletion completion= getCompletion();
	// LocalVariable[] localArrays= completion.findLocalArrays();
	//		
	// String[][] ret= new String[localArrays.length][];
	//		
	// for (int i= 0; i < localArrays.length; i++) {
	// String type= getArrayTypeFromLocalArray(completion,
	// localArrays[localArrays.length - i - 1]);
	// ret[i]= new String[] {type};
	// }
	//		
	// return ret;
	// }
	/**
	 * Returns the name of the type of a local array, <code>null</code> if no
	 * local array exists.
	 */
	// public String[] guessArrayTypes() {
	// CompilationUnitCompletion completion= getCompletion();
	// LocalVariable[] localArrays= completion.findLocalArrays();
	//		
	// List ret= new ArrayList();
	//		
	// for (int i= 0; i < localArrays.length; i++) {
	// String type= getArrayTypeFromLocalArray(completion,
	// localArrays[localArrays.length - i - 1]);
	// if (!ret.contains(type))
	// ret.add(type);
	// }
	//		
	// return (String[]) ret.toArray(new String[ret.size()]);
	// }
	private String getArrayTypeFromLocalArray(
			CompilationUnitCompletion completion, LocalVariable array) {
		String arrayTypeName = array.typeName;
		String typeName = getScalarType(arrayTypeName);
		int dimension = getArrayDimension(arrayTypeName) - 1;
		Assert.isTrue(dimension >= 0);

		String qualifiedName = createQualifiedTypeName(array.typePackageName,
				typeName);
		String innerTypeName = completion.simplifyTypeName(qualifiedName);

		return innerTypeName == null ? createArray(typeName, dimension)
				: createArray(innerTypeName, dimension);
	}

	private static String createArray(String type, int dimension) {
		StringBuffer buffer = new StringBuffer(type);
		for (int i = 0; i < dimension; i++)
			buffer.append("[]"); //$NON-NLS-1$
		return buffer.toString();
	}

	private static String getScalarType(String type) {
		return type.substring(0, type.indexOf('['));
	}

	private static int getArrayDimension(String type) {

		int dimension = 0;
		int index = type.indexOf('[');

		while (index != -1) {
			dimension++;
			index = type.indexOf('[', index + 1);
		}

		return dimension;
	}

	private static String createQualifiedTypeName(String packageName,
			String className) {
		StringBuffer buffer = new StringBuffer();

		if (packageName.length() != 0) {
			buffer.append(packageName);
			buffer.append('.');
		}
		buffer.append(className);

		return buffer.toString();
	}

	/**
	 * Returns a proposal for a variable name of a local array element,
	 * <code>null</code> if no local array exists.
	 */
	// public String guessArrayElement() {
	// return firstOrNull(guessArrayElements());
	// }
	/**
	 * Returns a proposal for a variable name of a local array element,
	 * <code>null</code> if no local array exists.
	 */
	// public String[] guessArrayElements() {
	// ICompilationUnit cu= getCompilationUnit();
	// if (cu == null) {
	// return new String[0];
	// }
	//		
	// CompilationUnitCompletion completion= getCompletion();
	// LocalVariable[] localArrays= completion.findLocalArrays();
	//		
	// List ret= new ArrayList();
	//		
	// for (int i= 0; i < localArrays.length; i++) {
	// int idx= localArrays.length - i - 1;
	//			
	// LocalVariable var= localArrays[idx];
	//			
	// IJavaProject project= cu.getJavaProject();
	// String typeName= var.typeName;
	// String baseTypeName= typeName.substring(0, typeName.lastIndexOf('['));
	//
	// String indexName= getIndex();
	// String[] excludedNames= completion.getLocalVariableNames();
	// if (indexName != null) {
	// ArrayList excludedNamesList= new ArrayList(Arrays.asList(excludedNames));
	// excludedNamesList.add(indexName);
	// excludedNames= (String[])excludedNamesList.toArray(new
	// String[excludedNamesList.size()]);
	// }
	// String[] proposals= NamingConventions.suggestLocalVariableNames(project,
	// var.typePackageName, baseTypeName, 0, excludedNames);
	// for (int j= 0; j < proposals.length; j++) {
	// if (!ret.contains(proposals[j]))
	// ret.add(proposals[j]);
	// }
	// }
	//		
	// return (String[]) ret.toArray(new String[ret.size()]);
	// }
	/**
	 * Returns a proposal for a variable name of a local array element,
	 * <code>null</code> if no local array exists.
	 */
	// public String[][] guessGroupedArrayElements() {
	// ICompilationUnit cu= getCompilationUnit();
	// if (cu == null) {
	// return new String[0][];
	// }
	//		
	// CompilationUnitCompletion completion= getCompletion();
	// LocalVariable[] localArrays= completion.findLocalArrays();
	//		
	// String[][] ret= new String[localArrays.length][];
	//		
	// for (int i= 0; i < localArrays.length; i++) {
	// int idx= localArrays.length - i - 1;
	//			
	// LocalVariable var= localArrays[idx];
	//			
	// IJavaProject project= cu.getJavaProject();
	// String typeName= var.typeName;
	// int dim= -1; // we expect at least one array
	// int lastIndex= typeName.length();
	// int bracket= typeName.lastIndexOf('[');
	// while (bracket != -1) {
	// lastIndex= bracket;
	// dim++;
	// bracket= typeName.lastIndexOf('[', bracket - 1);
	// }
	// typeName= typeName.substring(0, lastIndex);
	//			
	// String indexName= getIndex();
	// String[] excludedNames= completion.getLocalVariableNames();
	// if (indexName != null) {
	// ArrayList excludedNamesList= new ArrayList(Arrays.asList(excludedNames));
	// excludedNamesList.add(indexName);
	// excludedNames= (String[])excludedNamesList.toArray(new
	// String[excludedNamesList.size()]);
	// }
	// String[] proposals= NamingConventions.suggestLocalVariableNames(project,
	// var.typePackageName, typeName, dim, excludedNames);
	//			
	// ret[i]= proposals;
	// }
	//		
	// return ret;
	// }
	/**
	 * Returns an array index name. 'i', 'j', 'k' are tried until no name
	 * collision with an existing local variable occurs. If all names collide,
	 * <code>null</code> is returned.
	 */
	// public String getIndex() {
	// CompilationUnitCompletion completion= getCompletion();
	// String[] proposals= {"i", "j", "k"}; //$NON-NLS-1$ //$NON-NLS-2$
	// //$NON-NLS-3$
	//		
	// for (int i= 0; i != proposals.length; i++) {
	// String proposal = proposals[i];
	//
	// if (!completion.existsLocalName(proposal))
	// return proposal;
	// }
	//
	// return null;
	// }
	/**
	 * Returns the name of a local collection, <code>null</code> if no local
	 * collection exists.
	 */
	// public String guessCollection() {
	// return firstOrNull(guessCollections());
	// }
	/**
	 * Returns the names of local collections.
	 */
	// public String[] guessCollections() {
	// CompilationUnitCompletion completion= getCompletion();
	// try {
	// LocalVariable[] localCollections= completion.findLocalCollections();
	// String[] ret= new String[localCollections.length];
	// for (int i= 0; i < ret.length; i++) {
	// ret[ret.length - i - 1]= localCollections[i].name;
	// }
	//			
	// return ret;
	//
	// } catch (JavaModelException e) {
	// JavaPlugin.log(e);
	// }
	//
	// return new String[0];
	// }
	/**
	 * Returns an iterator name ('iter'). If 'iter' already exists as local
	 * variable, <code>null</code> is returned.
	 */
	// public String getIterator() {
	// CompilationUnitCompletion completion= getCompletion();
	// String[] proposals= {"iter"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//		
	// for (int i= 0; i != proposals.length; i++) {
	// String proposal = proposals[i];
	//
	// if (!completion.existsLocalName(proposal))
	// return proposal;
	// }
	//
	// return null;
	// }
	// public void addIteratorImport() {
	// ICompilationUnit cu= getCompilationUnit();
	// if (cu == null) {
	// return;
	// }
	//	
	// try {
	// Position position= new Position(getCompletionOffset(),
	// getCompletionLength());
	// IDocument document= getDocument();
	// final String category= "__template_position_importer" +
	// System.currentTimeMillis(); //$NON-NLS-1$
	// IPositionUpdater updater= new DefaultPositionUpdater(category);
	// document.addPositionCategory(category);
	// document.addPositionUpdater(updater);
	// document.addPosition(position);
	//
	// CodeGenerationSettings settings=
	// JavaPreferencesSettings.getCodeGenerationSettings();
	// ImportsStructure structure= new ImportsStructure(cu,
	// settings.importOrder, settings.importThreshold, true);
	// structure.addImport("java.util.Iterator"); //$NON-NLS-1$
	// structure.create(false, null);
	//
	// document.removePosition(position);
	// document.removePositionUpdater(updater);
	// document.removePositionCategory(category);
	//			
	// setCompletionOffset(position.getOffset());
	// setCompletionLength(position.getLength());
	//			
	// } catch (CoreException e) {
	// handleException(null, e);
	// } catch (BadLocationException e) {
	// handleException(null, e);
	// } catch (BadPositionCategoryException e) {
	// handleException(null, e);
	// }
	// }
	/**
	 * Evaluates a 'java' template in thecontext of a compilation unit
	 */
	public static String evaluateTemplate(Template template,
			ICompilationUnit compilationUnit, int position)
			throws CoreException, BadLocationException, TemplateException {

		TemplateContextType contextType = PHPeclipsePlugin.getDefault()
				.getTemplateContextRegistry().getContextType("java"); //$NON-NLS-1$
		if (contextType == null)
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							PHPeclipsePlugin.PLUGIN_ID,
							IStatus.ERROR,
							JavaTemplateMessages
									.getString("JavaContext.error.message"), null)); //$NON-NLS-1$

		IDocument document = new Document();
		if (compilationUnit != null && compilationUnit.exists())
			document.set(compilationUnit.getSource());

		JavaContext context = new JavaContext(contextType, document, position,
				0, compilationUnit);
		context.setForceEvaluation(true);

		TemplateBuffer buffer = context.evaluate(template);
		if (buffer == null)
			return null;
		return buffer.getString();
	}

}
