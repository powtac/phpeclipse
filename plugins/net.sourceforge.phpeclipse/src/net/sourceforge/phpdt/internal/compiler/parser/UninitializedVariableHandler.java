package net.sourceforge.phpdt.internal.compiler.parser;

import java.util.ArrayList;

public class UninitializedVariableHandler {

	private class Function {
		private int count;

		private String name;

		public Function(String name, int count) {
			this.name = name;
			this.count = count;
		}
	}

	private String functionName = null;

	private int argumentCount = 0;

	private ArrayList functions = new ArrayList();

	public UninitializedVariableHandler() {
		add("ereg", 3);
		add("eregi", 3);
		add("fsockopen", 3);
		add("preg_match", 3);
		add("preg_match_all", 3);
		add("preg_replace", 5);
		add("preg_replace_callback", 5);
	}

	private void add(String name, int countFrom) {
		functions.add(new Function(name, countFrom));
	}

	protected boolean reportError() {
		if (functionName != null) {
			for (int i = 0; i < functions.size(); i++) {
				Function function = (Function) functions.get(i);
				if (functionName.equalsIgnoreCase(function.name)
						&& argumentCount >= function.count) {
					return false;
				}
			}
		}
		return true;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public void incrementArgumentCount() {
		argumentCount++;
	}
}
