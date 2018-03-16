package net.sourceforge.phpdt.internal.ui.text.java;

import java.util.Comparator;

public class PHPCompletionProposalComparator implements Comparator {

	private boolean fOrderAlphabetically;

	/**
	 * Constructor for CompletionProposalComparator.
	 */
	// public PHPCompletionProposalComparator() {
	// fOrderAlphabetically= false;
	// }
	public void setOrderAlphabetically(boolean orderAlphabetically) {
		fOrderAlphabetically = orderAlphabetically;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		IPHPCompletionProposal c1 = (IPHPCompletionProposal) o1;
		IPHPCompletionProposal c2 = (IPHPCompletionProposal) o2;
		if (!fOrderAlphabetically) {
			int relevanceDif = c2.getRelevance() - c1.getRelevance();
			if (relevanceDif != 0) {
				return relevanceDif;
			}
		}
		return c1.getDisplayString().compareToIgnoreCase(c2.getDisplayString());
	}

}
