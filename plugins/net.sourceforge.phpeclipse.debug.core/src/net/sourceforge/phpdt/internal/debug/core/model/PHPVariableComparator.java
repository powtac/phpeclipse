package net.sourceforge.phpdt.internal.debug.core.model;

import java.util.Comparator;

public class PHPVariableComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		PHPVariable left = (PHPVariable) arg0;
		PHPVariable right = (PHPVariable) arg1;
		return left.getName().compareTo(right.getName());
	}

}
