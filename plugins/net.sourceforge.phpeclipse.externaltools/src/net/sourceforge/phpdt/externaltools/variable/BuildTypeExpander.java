package net.sourceforge.phpdt.externaltools.variable;

public class BuildTypeExpander implements IVariableTextExpander {

	public String getText(String varTag, String varValue,
			ExpandVariableContext context) {
		return context.getBuildType();
	}

}
