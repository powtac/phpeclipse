package net.sourceforge.phpdt.httpquery;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpdt.httpquery.config.ConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

public class KodersAction extends HTTPQueryAction {

	public KodersAction() {
		super();
	}

	protected Configuration getConfiguration(String name) {
		Configuration conf = super.getConfiguration("Koders.com");
		if (conf != null) {
			return conf;
		}
		ConfigurationWorkingCopy config = new ConfigurationWorkingCopy();
		config.setName("Koders.com");
		config.setURL("http://koders.com/?s=$text.selection");
		config.setType(PHPHelpPlugin.HTTP_QUERY);
		return config;
	}
}