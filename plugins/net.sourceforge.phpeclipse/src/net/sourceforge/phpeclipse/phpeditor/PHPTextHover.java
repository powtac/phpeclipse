/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.phpdt.internal.corext.phpdoc.PHPDocUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;
import net.sourceforge.phpeclipse.builder.PHPIdentifierLocation;
import net.sourceforge.phpeclipse.phpeditor.php.PHPElement;
import net.sourceforge.phpeclipse.phpeditor.php.PHPFunction;
import net.sourceforge.phpeclipse.phpeditor.php.PHPWordExtractor;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

/**
 * Implementation for an <code>ITextHover</code> which hovers over PHP code.
 */
public class PHPTextHover implements ITextHover {
	private static HashMap functionDescriptions = null;

	private static HashMap identDescriptions = null;

	/**
	 * The current project; maybe <code>null</code> for preference pages
	 */
	private IProject fProject;

	public PHPTextHover(IProject project) {
		fProject = project;
	}

	/*
	 * (non-Javadoc) Method declared on ITextHover
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				if (hoverRegion.getLength() > -1) {
					String word = textViewer.getDocument().get(
							hoverRegion.getOffset(), hoverRegion.getLength());
					if (functionDescriptions == null) {
						functionDescriptions = new HashMap();
						identDescriptions = new HashMap();
						ArrayList syntaxbuffer = PHPSyntaxRdr.getSyntaxData();
						PHPElement elbuffer = null;
						if (syntaxbuffer != null) {
							for (int i = 0; i < syntaxbuffer.size(); i++) {
								elbuffer = (PHPElement) syntaxbuffer.get(i);
								if (elbuffer instanceof PHPFunction) {
									functionDescriptions.put(
											elbuffer.getName(), elbuffer
													.getHoverText());
								} else {
									identDescriptions.put(elbuffer.getName(),
											elbuffer.getHoverText());
								}
							}
						}
						//
						// while ((syntaxbuffer != null)
						// && (!syntaxbuffer.isEmpty() && ((elbuffer =
						// (PHPElement)
						// syntaxbuffer.remove(0)) != null))) {
						// functionDescriptions.put(elbuffer.getName(),
						// elbuffer.getHoverText());
						// }
					}
					String hoverInfo = (String) identDescriptions.get(word);
					if (hoverInfo == null & word.length() > 0) {
						hoverInfo = (String) functionDescriptions.get(word
								.toLowerCase());
					}
					if (hoverInfo == null && fProject != null) {
						// get the possible PHPDoc information from the index
						// file
						IdentifierIndexManager indexManager = PHPeclipsePlugin
								.getDefault().getIndexManager(fProject);
						List list = indexManager.getLocations(word);
						if (list.size() > 0) {
							try {
								PHPIdentifierLocation location;
								String filename;
								StringBuffer hoverInfoBuffer = new StringBuffer();
								String workspaceLocation;
								if (fProject != null) {
									workspaceLocation = fProject.getLocation()
											.toString() + '/';
								} else {
									// should never happen?
									workspaceLocation = PHPeclipsePlugin
											.getWorkspace().getRoot()
											.getLocation().toString();
								}
								// boolean foundPHPdoc = false;
								for (int i = 0; i < list.size(); i++) {
									location = (PHPIdentifierLocation) list
											.get(i);
									filename = workspaceLocation
											+ location.getFilename();
									PHPDocUtil.appendPHPDoc(hoverInfoBuffer,
											filename, location);
								}
								hoverInfo = hoverInfoBuffer.toString();
							} catch (Throwable e) {
								// ignore exceptions
								// e.printStackTrace();
							}
						}
					}
					return hoverInfo;
				}
				// } catch (BadLocationException x) {
			} catch (Exception x) {
			}
		}
		return null;
		// don't show this annoying text
		// return "empty selection";
	}

	/*
	 * (non-Javadoc) Method declared on ITextHover
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = PHPWordExtractor.findWord(textViewer.getDocument(),
				offset);
		// show the extracted word as a tooltip
		if (selection != null && selection.x <= offset
				&& offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}
}
