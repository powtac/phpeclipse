package net.sourceforge.phpeclipse.xdebug.php.launching;


import java.util.List;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class PHPSourceLookupDirector extends AbstractSourceLookupDirector {

	public void initializeParticipants() {
		setFindDuplicates(true);
		addParticipants(new ISourceLookupParticipant[] {new PHPSourceLookupParticipant()});
	}
	
	public Object getSourceElement(Object element) {
		List sources = doSourceLookup(element);
		if(sources.size() == 1) {
			return sources.get(0);
		} else if(sources.size() > 1) {
			return resolveSourceElement(element, sources);
		} else { 
			return null;
		}
	}
}