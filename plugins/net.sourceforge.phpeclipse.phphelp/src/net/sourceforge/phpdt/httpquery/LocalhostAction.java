package net.sourceforge.phpdt.httpquery;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpdt.httpquery.config.ConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

public class LocalhostAction extends HTTPQueryAction {

	public LocalhostAction() {
		super();
	}

	protected Configuration getConfiguration(String name) {
		Configuration conf = super.getConfiguration("Localhost");
		if (conf != null) {
			return conf;
		}
		ConfigurationWorkingCopy config = new ConfigurationWorkingCopy();
		config.setName("Localhost");
		config.setURL("http://localhost");
		config.setType(PHPHelpPlugin.HTTP_QUERY);
		return config;
	}

}