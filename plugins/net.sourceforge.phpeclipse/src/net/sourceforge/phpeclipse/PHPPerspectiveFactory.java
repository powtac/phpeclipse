package net.sourceforge.phpeclipse;

import net.sourceforge.phpeclipse.webbrowser.views.BrowserView;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PHPPerspectiveFactory implements IPerspectiveFactory {
	public static final String ID_PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

	// see bug 63563

	public PHPPerspectiveFactory() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT,
				(float) 0.25, editorArea); //$NON-NLS-1$
		folder.addView(IPageLayout.ID_RES_NAV);
		IFolderLayout outputfolder = layout.createFolder("bottom",
				IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);

		outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		outputfolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		outputfolder.addView(IPageLayout.ID_BOOKMARKS);
		outputfolder.addView(BrowserView.ID_BROWSER);
		outputfolder.addPlaceholder(ID_PROGRESS_VIEW);

		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.75,
				editorArea);
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addShowViewShortcut(BrowserView.ID_BROWSER);

		// views - search
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - debugging
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		// new actions - PHP project creation wizards
		layout
				.addNewWizardShortcut("net.sourceforge.phpeclipse.wizards.PHPFileWizard"); //$NON-NLS-1$
		layout
				.addNewWizardShortcut("net.sourceforge.phpeclipse.wizards.NewWizardProjectCreation"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		// perspective shortcuts
		layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective");
	}
}