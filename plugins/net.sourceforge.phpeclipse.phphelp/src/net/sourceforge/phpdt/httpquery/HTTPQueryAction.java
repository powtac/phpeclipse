package net.sourceforge.phpdt.httpquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpdt.httpquery.config.ConfigurationManager;
import net.sourceforge.phpdt.httpquery.config.IConfiguration;
import net.sourceforge.phpdt.internal.ui.viewsupport.ListContentProvider;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class HTTPQueryAction extends AbstractHTTPQueryAction {

	public HTTPQueryAction() {
		super();
	}

	protected Configuration getConfiguration(String name) {
		List allConfigsList = ConfigurationManager.getInstance()
				.getConfigurations();
		ArrayList configsList = new ArrayList();
		for (int i = 0; i < allConfigsList.size(); i++) {
			IConfiguration temp = (IConfiguration) allConfigsList.get(i);
			if (temp.getType().equals(PHPHelpPlugin.HTTP_QUERY)) {
				if (name != null && temp.getName().equalsIgnoreCase(name)) {
					return (Configuration) temp;
				}
				configsList.add(temp);
			}
		}
		if (name != null) {
			return null;
		}
		Collections.sort(configsList);

		ListSelectionDialog listSelectionDialog = new ListSelectionDialog(
				PHPHelpPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getShell(), configsList,
				new ListContentProvider(), new LabelProvider(), "Select URL");
		listSelectionDialog.setTitle("Multiple configuration found");
		if (listSelectionDialog.open() == Window.OK) {
			Object[] configurations = listSelectionDialog.getResult();
			if (configurations != null) {
				for (int i = 0; i < configurations.length; i++) {
					return ((Configuration) configurations[i]); // .getURL();
				}
			}
		}
		return null;
	}

}