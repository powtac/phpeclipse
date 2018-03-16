package net.sourceforge.phpeclipse.phpeditor.php;

/**
 * @author Choochter
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>ObfuscatorIgnores. To enable and disable the creation
 * of type comments go to Window>Preferences>Java>Code Generation.
 */
public class PHPConstant extends PHPElement {
	private String fDescription;

	public void setDescription(String description) {
		this.fDescription = description;
	}

	public String getDescription() {
		return this.fDescription;
	}

	public String getHoverText() {
		return super.getHoverText() + "<br>" + getDescription();
	}

	public PHPConstant(String Name, String usage, String description) {
		super(Name, usage);
		setDescription(description);
	}
}