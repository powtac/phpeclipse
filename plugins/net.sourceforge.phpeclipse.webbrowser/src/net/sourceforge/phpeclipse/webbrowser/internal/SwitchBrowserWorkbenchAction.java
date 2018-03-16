/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.util.Iterator;

import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

/**
 * Action to open the Web broswer.
 */
public class SwitchBrowserWorkbenchAction implements
		IWorkbenchWindowPulldownDelegate2 {
	/**
	 * The menu created by this action
	 */
	private Menu fMenu;

	protected boolean recreateMenu = false;

	/**
	 * SwitchBrowserWorkbenchAction constructor comment.
	 */
	public SwitchBrowserWorkbenchAction() {
		super();
	}

	public void dispose() {
		setMenu(null);
	}

	/**
	 * Sets this action's drop-down menu, disposing the previous menu.
	 * 
	 * @param menu
	 *            the new menu
	 */
	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}

	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Adds the given action to the specified menu with an accelerator specified
	 * by the given number.
	 * 
	 * @param menu
	 *            the menu to add the action to
	 * @param action
	 *            the action to add
	 * @param accelerator
	 *            the number that should appear as an accelerator
	 */
	protected void addToMenu(Menu menu, IAction action, int accelerator) {
		StringBuffer label = new StringBuffer();
		if (accelerator >= 0 && accelerator < 10) {
			// add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
		}
		label.append(action.getText());
		action.setText(label.toString());
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}

	/**
	 * Fills the drop-down menu with favorites and launch history, launch
	 * shortcuts, and an action to open the launch configuration dialog.
	 * 
	 * @param menu
	 *            the menu to fill
	 */
	protected void fillMenu(Menu menu) {
		IWebBrowser current = BrowserManager.getInstance()
				.getCurrentWebBrowser();
		Iterator iterator = BrowserManager.getInstance().getWebBrowsers()
				.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			IWebBrowser browser = (IWebBrowser) iterator.next();
			addToMenu(menu, new SwitchDefaultBrowserAction(browser, browser
					.equals(current)), i++);
		}
	}

	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic history list
		fMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				// if (recreateMenu) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
				recreateMenu = false;
				// }
			}
		});
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void run(IAction action) {
	}

	public Menu getMenu(Menu parent) {
		setMenu(new Menu(parent));
		// fillMenu(fMenu);
		initMenu();
		return fMenu;
	}

	public Menu getMenu(Control parent) {
		setMenu(new Menu(parent));
		// fillMenu(fMenu);
		initMenu();
		return fMenu;
	}
}