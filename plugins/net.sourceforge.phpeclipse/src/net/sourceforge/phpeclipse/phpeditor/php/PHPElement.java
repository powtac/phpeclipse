package net.sourceforge.phpeclipse.phpeditor.php;

/**
 * @author choochter
 */
public abstract class PHPElement {

	private String ElementName;

	private String ElementUsage;

	// Setters
	public final void setName(String ElementName) {
		this.ElementName = ElementName;
	}

	public final void setUsage(String usage) {
		this.ElementUsage = usage;
	}

	// Getters
	public final String getName() {
		return ElementName;
	}

	public final String getUsage() {
		return ElementUsage;
	}

	public String getHoverText() {
		return "<b>" + getUsage() + "</b>";
	}

	public PHPElement() {
	}

	public PHPElement(String name, String usage) {
		setName(name);
		if ((usage == null) || (usage.equals(""))) {
			setUsage(name + " - ");
		} else {
			setUsage(usage);
		}
	}

}
