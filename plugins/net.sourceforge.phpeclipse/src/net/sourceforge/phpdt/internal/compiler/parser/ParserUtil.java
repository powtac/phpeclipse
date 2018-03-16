package net.sourceforge.phpdt.internal.compiler.parser;

import java.util.List;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.compiler.ast.ImportReference;
import net.sourceforge.phpdt.internal.compiler.ast.SingleTypeReference;

import org.eclipse.core.resources.IFile;

public class ParserUtil {

	public static SingleTypeReference getTypeReference(Scanner scanner,
			List includesList, char[] ident) {
		String identStr = new String(ident);
		ImportReference ir;
		IFile file = null;
		for (int i = 0; i < includesList.size(); i++) {
			ir = (ImportReference) includesList.get(i);
			file = ir.getFile();
			if (file != null) {
				ICompilationUnit unit = JavaCore
						.createCompilationUnitFrom(file);
				if (unit != null) {
					try {
						// TODO avoid recursion here. Sometimes we get a
						// java.lang.StackOverflowError
						IType[] types = unit.getAllTypes();
						if (types != null) {
							for (int j = 0; j < types.length; j++) {
								if (types[j].getElementName().equals(identStr)) {
									return new SingleTypeReference(
											file,
											ident,
											scanner
													.getCurrentTokenStartPosition(),
											scanner
													.getCurrentTokenEndPosition());
								}
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}
