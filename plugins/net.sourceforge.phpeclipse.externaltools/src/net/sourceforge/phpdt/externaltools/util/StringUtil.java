/*
 * Created on 28.06.2003
 *
 */
package net.sourceforge.phpdt.externaltools.util;

/**
 * some string utilities
 * 
 */
public class StringUtil {

	/**
	 * Replace each substring of str which matches findStr with replaceStr
	 * 
	 * @param str
	 *            the string the substrings should be replaced in
	 * @param findStr
	 *            the substring to be replaced
	 * @param replaceStr
	 *            the replacement
	 * @return the resultstring
	 */
	public static final String replaceAll(String str, String findStr,
			String replaceStr) {
		StringBuffer buf = new StringBuffer();

		int lastindex = 0;
		int indexOf = 0;
		while ((indexOf = str.indexOf(findStr, lastindex)) != -1) {
			buf.append(str.substring(lastindex, indexOf)).append(replaceStr);
			lastindex = indexOf + findStr.length();
		}
		buf.append(str.substring(lastindex));
		return buf.toString();
	}

}
