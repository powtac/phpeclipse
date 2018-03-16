package net.sourceforge.phpeclipse.ui.overlaypages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.preferences.PHPMiscProjectPreferences;
import net.sourceforge.phpeclipse.ui.preferences.PHPPreviewProjectPreferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

public class ProjectPrefUtil {
	public static String getMiscProjectsPreferenceValue(IResource resource,
			String key) {
		return getOverlayedPrefProjectValue(resource,
				PHPMiscProjectPreferences.PREF_ID, key);
	}

	public static List getIncludePaths(IResource resource) {
		String includePaths = getMiscProjectsPreferenceValue(resource,
				IPreferenceConstants.PHP_INCLUDE_PATHS);
		ArrayList list = new ArrayList();
		if (includePaths != null) {
			StringTokenizer st = new StringTokenizer(includePaths,
					File.pathSeparator + "\n\r");//$NON-NLS-1$
			while (st.hasMoreElements()) {
				list.add(st.nextElement());
			}
		}
		return list;
	}

	public static IPath getDocumentRoot(IResource resource) {
		String documentRoot = getMiscProjectsPreferenceValue(resource,
				IPreferenceConstants.PHP_DOCUMENTROOT_PREF);
		IPath path = new Path(documentRoot);
		// documentRoot = documentRoot.replace('\\', '/');
		return path;
	}

	public static String getOverlayedPrefProjectValue(IResource resource,
			String pageId, String key) {
		IProject project = resource.getProject();
		String value = null;
		if (useProjectSettings(project, pageId)) {
			value = getProperty(resource, pageId, key);
		}
		if (value != null)
			return value;
		return WebUI.getDefault().getPreferenceStore().getString(key);
	}

	public static String getOverlayedPrefResourceValue(IResource resource,
			String pageId, String key) {
		String value = null;
		if (useProjectSettings(resource, pageId)) {
			value = getProperty(resource, pageId, key);
		}
		if (value != null)
			return value;
		return WebUI.getDefault().getPreferenceStore().getString(key);
	}

	public static boolean getPreviewBooleanValue(IResource resource, String key) {
		return getOverlayedPrefResourceValue(resource,
				PHPPreviewProjectPreferences.PREF_ID, key).equals("true");
	}

	public static String getPreviewStringValue(IResource resource, String key) {
		return getOverlayedPrefResourceValue(resource,
				PHPPreviewProjectPreferences.PREF_ID, key);
	}

	private static String getProperty(IResource resource, String pageId,
			String key) {
		try {
			return resource
					.getPersistentProperty(new QualifiedName(pageId, key));
		} catch (CoreException e) {
		}
		return null;
	}

	private static boolean useProjectSettings(IResource resource, String pageId) {
		String use = getProperty(resource, pageId,
				FieldEditorOverlayPage.USEPROJECTSETTINGS);
		return "true".equals(use);
	}
}