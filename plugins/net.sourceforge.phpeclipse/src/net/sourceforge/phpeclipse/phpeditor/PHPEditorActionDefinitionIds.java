package net.sourceforge.phpeclipse.phpeditor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public interface PHPEditorActionDefinitionIds extends
		ITextEditorActionDefinitionIds {
	/**
	 * Action definition ID of the edit -> go to matching bracket action (value
	 * <code>"org.phpeclipse.phpdt.ui.edit.text.php.goto.matching.bracket"</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GOTO_MATCHING_BRACKET = "net.sourceforge.phpeclipse.ui.edit.text.php.goto.matching.bracket"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to next member action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.goto.next.member"</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GOTO_NEXT_MEMBER = "net.sourceforge.phpeclipse.ui.edit.text.php.goto.next.member"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to previous member action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.goto.previous.member"</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GOTO_PREVIOUS_MEMBER = "net.sourceforge.phpeclipse.ui.edit.text.php.goto.previous.member"; //$NON-NLS-1$

	/**
	 * Value: net.sourceforge.phpeclipse.phpeditor.comment
	 */
	public static final String COMMENT = "net.sourceforge.phpeclipse.phpeditor.comment";

	/**
	 * Value: net.sourceforge.phpeclipse.phpeditor.uncomment
	 */
	public static final String UNCOMMENT = "net.sourceforge.phpeclipse.phpeditor.uncomment";

	/**
	 * Action definition ID of the source -> toggle comment action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.toggle.comment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String TOGGLE_COMMENT = "net.sourceforge.phpeclipse.phpeditor.toggle.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> add block comment action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.add.block.comment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String ADD_BLOCK_COMMENT = "net.sourceforge.phpeclipse.phpeditor.add.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> remove block comment action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.remove.block.comment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String REMOVE_BLOCK_COMMENT = "net.sourceforge.phpeclipse.phpeditor.remove.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> indent action (value
	 * <code>"net.sourceforge.phpdt.ui.edit.text.java.indent"</code>).
	 */
	public static final String INDENT = "net.sourceforge.phpeclipse.phpeditor.indent"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> format action
	 */
	public static final String FORMAT = "net.sourceforge.phpeclipse.phpeditor.format"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> content assist proposal action (value
	 * <code>"org.phpeclipse.phpdt.ui.edit.text.php.content.assist. proposals"
	 * </code>).
	 */
	// public static final String CONTENT_ASSIST_PROPOSALS =
	// "net.sourceforge.phpeclipse.ui.edit.text.php.content.assist.proposals";
	// //$NON-NLS-1$
}
