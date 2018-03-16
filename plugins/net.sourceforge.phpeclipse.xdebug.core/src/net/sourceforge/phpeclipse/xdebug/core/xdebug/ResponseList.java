package net.sourceforge.phpeclipse.xdebug.core.xdebug;

import java.util.HashMap;
import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugResponse;

public class ResponseList {
	private int fLastId;
	private HashMap fList;

	public ResponseList() {
		fLastId = -1;
		fList = new HashMap();
	}

	public synchronized void add(XDebugResponse response) {
		int id = response.getTransactionID();
		fList.put(new Integer(id), response);
		fLastId = id;
		notifyAll();
	}

	public synchronized XDebugResponse get(int id) {
		while (id > fLastId) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (fList.containsKey(new Integer(id)))
			return (XDebugResponse) fList.remove(new Integer(id));
		else
			return null;
	}
}