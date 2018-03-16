package net.sourceforge.phpdt.httpquery;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpdt.httpquery.config.ConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

public class PHPHelpAction extends HTTPQueryAction {

	public PHPHelpAction() {
		super();
	}

	protected Configuration getConfiguration(String name) {
		Configuration conf = super.getConfiguration("PHP Manual");
		if (conf != null) {
			return conf;
		}
		ConfigurationWorkingCopy config = new ConfigurationWorkingCopy();
		config.setName("PHP Manual");
		config
				.setURL("http://www.php.net/manual/en/function.$php.selection.php");
		config.setType(PHPHelpPlugin.HTTP_QUERY);
		return config;
	}
}