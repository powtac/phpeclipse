package net.sourceforge.phpdt.internal.debug.core.model;

/*
 * Created on 17.04.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * @author Chris Admin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.util.Vector;

import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/**
 *
 */
public class PHPDBGEvalString {

	String 				  workStr;
	private PHPStackFrame fStackFrame;

	/**
	 *
	 */
	public PHPDBGEvalString(PHPStackFrame stack, String dataStr) {
		fStackFrame = stack;
		workStr = dataStr;
	}

	/**
	 *
	 * @param chstart
	 * @param chend
	 * @param startIdx
	 * @return
	 */
	String ExtractSubStr (char chstart, char chend, int startIdx) throws DebugException {
		int 	idx;
		int     i;
		String  rslt;
		Status  status;

		idx = startIdx;

		if (idx >= (workStr.length () - 1) ||
		    workStr.charAt (idx) != chstart) {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "worng startIdx!", null);

			throw new DebugException (status);
		}

		i = ++idx;
		i = workStr.indexOf(chend, i);

		if (i == -1) {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "endchar not found!", null);

			throw new DebugException (status);
		}
		rslt 	= workStr.substring (idx, i);
		workStr = workStr.substring (i + 1);

		return rslt;
	}

	/**
	 * @param slen
	 * @param startIdx
	 * @return
	 */
	String ExtractQuotedSubStr (int slen, int startIdx) throws DebugException {
		int 	idx;
		String	rslt;
		Status  status;

		idx = startIdx;

		if ((idx + slen + 1) >= workStr.length () ||
				workStr.charAt (idx)!= '"' ||
				workStr.charAt (idx + slen + 1) != '"') {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "no quoted substring found!", null);

			throw new DebugException (status);
		}

		rslt	= workStr.substring (idx + 1, idx + 1 + slen);
		workStr	= workStr.substring (idx + 2 + slen);

		return rslt;
	}

	/**
	 *
	 * @param chstart
	 * @param chend
	 * @apram startIdx
	 * @return
	 */
	int ExtractInt (char chstart, char chend, int startIdx) throws DebugException {
		String	subs;

		subs = ExtractSubStr (chstart, chend, startIdx);

		return (Integer.parseInt (subs));
	}

	/**
	 * @param name
	 * @param parent
	 * @param list       The list of PHPVariables
	 * @param var_list
	 * @param classname
	 * @param atype      The type of the variable (Either PEVT_ARRAY or PEVT_OBJECT)
	 * @return
	 */
	PHPVariable ParseEvalArray (String name, PHPVariable parent, Vector list, Vector var_list, String classname, int atype)  throws DebugException {
		long 		arritems;										// The number of items (or fields or entries) for the array (or short, array size)
		PHPVariable item;
		Vector 		subitems = null;
		Status      status;

		arritems = ExtractInt (':', ':', 0);						// Get the number of items/fields for the array
																	// E.g. :12: means there are 12 entries in array

		if ((workStr.length () > 0) &&                              // Is there still something to parse?
		    (workStr.charAt (0) != '{')) {                          // And the next character is not a '{', then output an error
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "no array startcharacter!", null);

			throw new DebugException (status);
		}

		workStr	= workStr.substring (1);                            // Remove the '{'
		item	= new PHPVariable (fStackFrame, name, parent, classname, atype, null);	// Create a new (empty) PHPVariable

		list.add (item);                                            // Add the newly created PHPVariable to list

		if (var_list != null) {                                     //
			var_list.add (item);                                    // Add the PHPVariable also to the var_list
		}

		if (arritems > 0) {                                         // If the array is not empty
			subitems = new Vector ();                               // Create a new child variable list for the array
		} else if (workStr.charAt (0) != '}') {                     // If the array is empty the next character has to be '}'
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "no array endcharacter!", null);

			throw new DebugException (status);
		}

		while ((workStr.length () > 0) &&                           // Is there still something to parse?
		       (workStr.charAt (0) != '}')) {                       // And the next character is not '}'
			Vector tmplst = new Vector ();                          // Create a temporary list

			parse ("", null, tmplst, null, false, 0);               // And parse the string for the array's name.

			if (tmplst.size () != 1) {                              // Parsing should return exactly on entry (which is the name)
				status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "no name found!", null);

				throw new DebugException (status);
			}
																	// Go for the array values
			parse (((PHPVariable) tmplst.elementAt (0)).getValue ().getValueString (), item, subitems, var_list, true, 0);
		}

		((PHPValue) item.getValue ()).addVariable (subitems);       // Append the list of all child variables to this PHPVariables PHPValue
		workStr = workStr.substring (1);                            // Remove the '}'

		return item;                                                // And return the PHPVariable we just build
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	void ParseEvalNULL (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		int 		idx;
		PHPVariable item;
		Status		status;

		idx = startIdx;

		if ((idx >= workStr.length ()) ||
		    (workStr.charAt (idx) != ';')) {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "NULL not found!", null);

			throw new DebugException(status);
		}

		workStr = workStr.substring (1);
		item    = new PHPVariable (fStackFrame, name, parent, "NULL", PHPValue.PEVT_UNKNOWN, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	boolean ParseEvalInt (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		String		subs;
		PHPVariable item;

		subs = ExtractSubStr (':', ';', startIdx);
		item = new PHPVariable (fStackFrame, name, parent, subs, PHPValue.PEVT_LONG, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}

		return true;
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	boolean ParseEvalDouble (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		String		subs;
		PHPVariable item;

		subs = ExtractSubStr (':', ';', startIdx);
		item = new PHPVariable (fStackFrame, name, parent, subs, PHPValue.PEVT_DOUBLE, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}

		return true;
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param MakePhpStr
	 * @param startIdx
	 */
	boolean ParseEvalString (String name, PHPVariable parent, Vector list, Vector var_list, boolean MakePhpStr, int startIdx)
					throws DebugException {
		int			slen;
		Status  	status;
		String		subs;
		PHPVariable item;

		slen = ExtractInt( ':', ':',startIdx);

		if ((workStr.length () <= slen) ||
		    (workStr.charAt (0) != '"')) {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (), Status.OK, "no String startcharecter!", null);

			throw new DebugException (status);
		}

		workStr = workStr.substring (1);
		subs 	= workStr.substring (0, slen);

		// replace \\ with \
		subs = subs.replaceAll ("\\\\\\\\","\\\\");

		if (workStr.charAt (slen) != '"') {
			status = new Status (Status.ERROR, PHPDebugCorePlugin.getUniqueIdentifier (),Status.OK, "no String endcharecter!", null);
			throw new DebugException (status);
		}

		workStr = workStr.substring (slen + 2);

/*		if (MakePhpStr) {
			ConvertToPhpString(subs, &subs);
		}
*/
		item = new PHPVariable (fStackFrame, name, parent, subs, PHPValue.PEVT_STRING, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}

		return true;
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	boolean ParseEvalBool (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		long		v;
		PHPVariable item;

		v    = ExtractInt (':', ';', startIdx);
		item = new PHPVariable (fStackFrame, name, parent, (v==0) ? ("FALSE") : ("TRUE"), PHPValue.PEVT_BOOLEAN, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}

		return true;
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	boolean ParseEvalObject (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		int		slen;
		String	classname;

		slen	  = ExtractInt (':', ':', startIdx);
		classname = ExtractQuotedSubStr (slen, startIdx);

		if ((int) classname.length () != slen) {
			return false;
		}

		ParseEvalArray (name,parent, list, var_list, classname,PHPValue.PEVT_OBJECT);

		return true;
	}

	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	boolean ParseEvalResource (String name, PHPVariable parent, Vector list, Vector var_list, int startIdx) throws DebugException {
		PHPVariable item;
		int			slen;
		String		restype;
		String		val;

		slen    = ExtractInt (':', ':', startIdx);
		restype = ExtractQuotedSubStr (slen, startIdx);
		val     = ExtractSubStr (':', ';', startIdx);

		item = new PHPVariable (fStackFrame, name, parent, restype + ":" + val, PHPValue.PEVT_RESOURCE, null);

		list.add (item);

		if (var_list != null) {
			var_list.add (item);
		}

		return true;
	}


	/**
	 *
	 * @param name
	 * @param parent
	 * @param list
	 * @param var_list
	 * @param startIdx
	 */
	private boolean ParseEvalRef(String name, PHPVariable parent, Vector list,
			Vector var_list, boolean isSoftRef, int startIdx)
			throws DebugException {
		int v;
		PHPVariable item;
		PHPVariable var_item;

		v = ExtractInt(':', ';', startIdx);
		item = new PHPVariable(fStackFrame, name, parent, "",
				isSoftRef ? PHPValue.PEVT_SOFTREF : PHPValue.PEVT_REF, null);
		v--; // ref ID is 1-based, EvalList is 0-based

		if ((var_list == null) || (v < 0) || (v >= var_list.size())) {
			//item.ref = item; // self-resolving
			return true;
		} else {
			var_item = (PHPVariable) var_list.get(v);

			PHPValue new_val = (PHPValue) var_item.getValue();
			if (isSoftRef) {
				// expand reduced structure to full tree
				// each value must have its appropriate parent
				try {
					new_val = copyItems(new_val);
				} catch (CloneNotSupportedException e) {
					// never occurs
				}
			}

			try {
				//item.setValue(var_item.getValue());
				//item.setReferenceType(var_item.getReferenceType());
				//((PHPValue) item.getValue()).setParent(item);
				item.setValue(new_val);
				item.setReferenceType(var_item.getReferenceType());
				new_val.setParent(item);
			} catch (DebugException e) {
				// never occurs
			}

			list.add(item);
		}

		return true;
	}

	/**
	 *
	 * @return The array of PHPVariables
	 */
	public PHPVariable[] getVars() {
		Vector list = new Vector();
		Vector var_list = new Vector();

		parse("", null, list, var_list, false, 0);

		return (PHPVariable[]) list.toArray(new PHPVariable[list.size()]); // Convert the list to an array and return the array
	}

	/**
	 *
	 * @return The PHPVariables as list
	 */
	public Vector getVariables() {
		Vector list = new Vector();
		Vector var_list = new Vector();

		parse("", null, list, var_list, false, 0);

		//debugDump(list, "");
		return list; // return the PHPVariable list
	}

	/**
	 *
	 *
	 *
	 * @param name        The name of the PHPVariable
	 * @param parent      The PHPVariable to which this parsing belongs
	 * @param list
	 * @param var_list
	 * @param MakePhpStr
	 * @param startIdx
	 */
	boolean parse (String name, PHPVariable parent, Vector list, Vector var_list, boolean MakePhpStr, int startIdx) {
		boolean ret_val = false;
		char    ch;

		if (startIdx >= workStr.length ()) {                        // Is there something to parse
			return false;                                           // No, then leave here
		}

		ch 		= workStr.charAt (startIdx);                        // The first character denotes the type of variable
		workStr = workStr.substring (1);                            // Remove the 'variable type' character

		try {
			switch (ch) {                                           // Switch according the 'variable type'
				case 'N': ParseEvalNULL   	(name, parent, list, var_list, startIdx); 				 break;
				case 'i': ParseEvalInt    	(name, parent, list, var_list, startIdx); 				 break;
				case 'd': ParseEvalDouble 	(name, parent, list, var_list, startIdx); 				 break;
				case 's': ParseEvalString 	(name, parent, list, var_list, MakePhpStr, startIdx); 	 break;
				case 'a': ParseEvalArray  	(name, parent, list, var_list, "", PHPValue.PEVT_ARRAY); break;
				case 'O': ParseEvalObject 	(name, parent, list, var_list, startIdx);				 break;
				case 'b': ParseEvalBool   	(name, parent, list, var_list, startIdx);				 break;
				case 'z': ParseEvalResource (name, parent, list, var_list, startIdx);				 break;
				case 'R': ParseEvalRef 		(name, parent, list, var_list, false, startIdx);		 break;
				case 'r': ParseEvalRef 		(name, parent, list, var_list, true, startIdx);			 break;
				case '?': ParseEvalUnknown(name, parent, list, var_list, startIdx);					 break;
			}
		} catch (DebugException e) {
			PHPDebugCorePlugin.log(e);
		}

/*		if (!ret_val) { // try to recover
			unsigned int i=*startIdx;
			while (i<str.length() && str[i]!='{' && str[i]!=';') i++;
			if (i<str.length() && str[i] == '{') {
				unsigned int cnt=1;
				i++;
				while (i<str.length() && cnt!=0) {
					if (str[i] == '{')
						cnt++;
					else if (str[i] == '}')
						cnt--;
					i++;
				}
			}
			*startIdx = i;
		}
*/
		return 	ret_val;											// Always false
	}

	/*
	 *
	 */
	private void ParseEvalUnknown(String name, PHPVariable parent, Vector list,
			Vector var_list, int startIdx) throws DebugException {

		if ((startIdx >= workStr.length()) || (workStr.charAt(startIdx) != ';')) {
			Status status = new Status(Status.ERROR, PHPDebugCorePlugin
					.getUniqueIdentifier(), Status.OK, "unexpected response",
					null);
			throw new DebugException(status);
		}

		workStr = workStr.substring(1);
		PHPVariable item = new PHPVariable(fStackFrame, name, parent, "?",
				PHPValue.PEVT_UNKNOWN, null);
		list.add(item);
		if (var_list != null) {
			var_list.add(item);
		}
	}

	/*
	 * Copy referenced items tree
	 */
	private PHPValue copyItems(PHPValue val) throws CloneNotSupportedException {
		PHPValue newVal = (PHPValue) val.clone();
		Vector vars = newVal.getChildVariables();
		Vector newVars = new Vector();
		for (int i = 0; i < vars.size(); i++) {
			PHPVariable newVar = (PHPVariable) ((PHPVariable) vars.get(i)).clone();
			try {
				newVar.setValue(copyItems((PHPValue) newVar.getValue()));
			} catch (DebugException e) {
				// never occurs
			}
			newVars.add(newVar);
		}
		val.setVariables(newVars);
		return newVal;
	}

//	private void debugDump(Vector list, String indent) {
//		for (int i = 0; i < list.size(); i++) {
//			PHPVariable var = (PHPVariable) list.get(i);
//			System.out.print(indent + var.getName());
//			PHPValue val = (PHPValue) var.getValue();
//			try {
//				if (val.hasVariables() && !var.getName().equals("['GLOBALS']")) {
//					System.out.println();
//					debugDump(val.getChildVariables(), indent + "    ");
//				} else {
//					PHPVariable parent = var.getParent();
//					System.out.println(val.getValueString() + " \t>>" + (parent == null ? "null" : parent.getLongName()));
//				}
//			} catch (DebugException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
