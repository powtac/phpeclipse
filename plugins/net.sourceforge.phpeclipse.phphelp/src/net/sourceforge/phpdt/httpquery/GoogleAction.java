package net.sourceforge.phpdt.httpquery;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpdt.httpquery.config.ConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

public class GoogleAction extends HTTPQueryAction {

	public GoogleAction() {
		super();
	}

	protected Configuration getConfiguration(String name) {
		Configuration conf = super.getConfiguration("Google.com");
		if (conf != null) {
			return conf;
		}
		ConfigurationWorkingCopy config = new ConfigurationWorkingCopy();
		config.setName("Google.com");
		config.setURL("http://www.google.com/search?q=$text.selection");
		config.setType(PHPHelpPlugin.HTTP_QUERY);
		return config;
	}

}