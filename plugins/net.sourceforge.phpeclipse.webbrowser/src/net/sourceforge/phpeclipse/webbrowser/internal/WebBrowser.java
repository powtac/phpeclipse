/**
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved. � This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html �* Contributors: IBM -
 * Initial API and implementation
 */

// TODO 1. Handle the sizing of a popup running in shelled out secondary window.
// TODO 2. Support printing: waiting on eclipse bug 47937/44823.
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.util.Iterator;

import net.sourceforge.phpeclipse.webbrowser.IURLMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

public class WebBrowser extends Composite {
	protected Composite toolbarComp;

	protected Composite statusComp;

	protected Combo combo;

	protected Clipboard clipboard;

	protected boolean showToolbar;

	protected ToolItem back;

	protected ToolItem forward;

	protected ToolItem stop;

	protected ToolItem favorites;

	protected ToolItem refresh;

	protected BusyIndicator busy;

	protected boolean showStatusbar;

	protected ProgressBar progress;

	protected Label status;

	private static int MAX_HISTORY = 50;

	protected static java.util.List history;

	protected Browser browser;

	protected Shell shell;

	protected WebBrowserEditor editor;

	protected String title;

	public WebBrowser(Composite parent, final boolean showToolbar,
			final boolean showStatusbar) {
		super(parent, SWT.NONE);

		this.showToolbar = showToolbar;
		this.showStatusbar = showStatusbar;

		GridLayout layout = new GridLayout();
		layout.marginHeight = 3;
		layout.marginWidth = 3;
		layout.horizontalSpacing = 3;
		layout.verticalSpacing = 3;
		layout.numColumns = 1;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		clipboard = new Clipboard(parent.getDisplay());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ContextIds.WEB_BROWSER);

		if (showToolbar) {
			toolbarComp = new Composite(this, SWT.NONE);
			GridLayout outerLayout = new GridLayout();
			outerLayout.numColumns = 2;
			outerLayout.marginWidth = 0;
			outerLayout.marginHeight = 0;
			toolbarComp.setLayout(outerLayout);
			toolbarComp.setLayoutData(new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
							| GridData.FILL_HORIZONTAL));

			// create the top line, with a combo box for history and a "go"
			// button
			Composite top = new Composite(toolbarComp, SWT.NONE);
			GridLayout topLayout = new GridLayout();
			topLayout.numColumns = 2;
			topLayout.marginWidth = 0;
			topLayout.marginHeight = 0;
			top.setLayout(topLayout);
			top.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER
					| GridData.FILL_HORIZONTAL));

			combo = new Combo(top, SWT.DROP_DOWN);

			updateHistory();

			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent we) {
					try {
						if (combo.getSelectionIndex() != -1)
							setURL(combo.getItem(combo.getSelectionIndex()),
									false);
					} catch (Exception e) {
					}
				}
			});
			combo.addListener(SWT.DefaultSelection, new Listener() {
				public void handleEvent(Event e) {
					setURL(combo.getText());
				}
			});
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			PlatformUI.getWorkbench().getHelpSystem().setHelp(combo,
					ContextIds.WEB_BROWSER_URL);

			ToolBar toolbar = new ToolBar(top, SWT.FLAT);
			fillToolBar(toolbar);

			new ToolItem(toolbar, SWT.SEPARATOR);

			busy = new BusyIndicator(toolbarComp, SWT.NONE);
			busy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		}

		// create a new SWT Web browser widget, checking once again to make sure
		// we can use it in this environment
		// if (WebBrowserUtil.canUseInternalWebBrowser() &
		// WebBrowserUtil.isInternalBrowserOperational())
		if (WebBrowserUtil.isInternalBrowserOperational())
			this.browser = new Browser(this, SWT.NONE);
		else {
			WebBrowserUtil.openError(WebBrowserUIPlugin
					.getResource("%errorCouldNotLaunchInternalWebBrowser"));
			return;
		}

		if (showToolbar) {
			back.setEnabled(browser.isBackEnabled());
			forward.setEnabled(browser.isForwardEnabled());
		}

		PlatformUI.getWorkbench().getHelpSystem().setHelp(browser,
				ContextIds.WEB_BROWSER_WEB);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		browser.setLayoutData(data);

		if (showStatusbar)
			createStatusArea(this);

		addBrowserListeners();
	}

	/**
	 * 
	 */
	protected void addBrowserListeners() {
		if (showStatusbar) {
			// respond to Browser StatusTextEvents events by updating the status
			// Text label
			browser.addStatusTextListener(new StatusTextListener() {
				public void changed(StatusTextEvent event) {
					status.setText(event.text);
				}
			});
		}

		/**
		 * Add listener for new window creation so that we can instead of
		 * opening a separate new window in which the session is lost, we can
		 * instead open a new window in a new shell within the browser area
		 * thereby maintaining the session.
		 */
		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				Shell shell2 = new Shell(getDisplay());
				shell2.setLayout(new FillLayout());
				shell2.setText(WebBrowserUIPlugin
						.getResource("%viewWebBrowserTitle"));
				shell2.setImage(getShell().getImage());
				WebBrowser browser2 = new WebBrowser(shell2, showToolbar,
						showStatusbar);
				browser2.shell = shell2;
				event.browser = browser2.browser;
				shell2.open();
			}
		});

		browser.addCloseWindowListener(new CloseWindowListener() {
			public void close(WindowEvent event) {
				// if shell is not null, it must be a secondary popup window,
				// else its an editor window
				if (shell != null)
					shell.dispose();
				else {
					// #1365431 (toshihiro) editor.closeEditor(); causes NPE
					if (editor != null)
						editor.closeEditor();
				}
			}
		});

		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent event) {
				if (event.total == 0)
					return;

				boolean done = (event.current == event.total);

				int percentProgress = event.current * 100 / event.total;
				if (showStatusbar) {
					if (done)
						progress.setSelection(0);
					else
						progress.setSelection(percentProgress);
				}

				if (showToolbar) {
					if (!busy.isBusy()
							&& (percentProgress > 0 && percentProgress < 100)) {
						busy.setBusy(true);
					}
					// Once the progress hits 100 percent, done, set busy to
					// false
					else if (busy.isBusy() && done) {
						busy.setBusy(false);
					}
				}
			}

			public void completed(ProgressEvent event) {
				if (showStatusbar)
					progress.setSelection(0);
				if (showToolbar) {
					busy.setBusy(false);
					back.setEnabled(browser.isBackEnabled());
					forward.setEnabled(browser.isForwardEnabled());
				}
			}
		});

		if (showToolbar) {
			browser.addLocationListener(new LocationListener() {
				public void changed(LocationEvent event) {
					if (!event.top)
						return;
					if (!isHome()) {
						combo.setText(event.location);
						addToHistory(event.location);
						updateHistory();
					} else
						combo.setText("");
				}

				public void changing(LocationEvent event) {
				}
			});
		}

		browser.addTitleListener(new TitleListener() {
			public void changed(TitleEvent event) {
				title = event.title;
			}
		});
	}

	/**
	 * Return the underlying browser control.
	 * 
	 * @return org.eclipse.swt.browser.Browser
	 */
	public Browser getBrowser() {
		return browser;
	}

	/**
	 * 
	 */
	protected void forward() {
		browser.forward();
	}

	/**
	 * 
	 */
	protected void back() {
		browser.back();
	}

	/**
	 * 
	 */
	protected void stop() {
		browser.stop();
	}

	/**
	 * 
	 */
	protected void navigate(String url) {
		Trace.trace(Trace.FINER, "Navigate: " + url);
		if (url != null && url.equals(getURL())) {
			refresh();
			return;
		}
		browser.setUrl(url);
	}

	/**
	 * Refresh the currently viewed page.
	 */
	public void refresh() {
		browser.refresh();
	}

	protected void setURL(String url, boolean browse) {
		Trace.trace(Trace.FINEST, "setURL: " + url + " " + browse);
		if (url == null) {
			home();
			return;
		}

		if (url.endsWith(WebBrowserPreference.getHomePageURL().substring(9)))
			return;

		// check URL maps
		Iterator iterator = WebBrowserUtil.getURLMaps().iterator();
		String newURL = null;
		while (iterator.hasNext() && newURL == null) {
			try {
				IURLMap map = (IURLMap) iterator.next();
				newURL = map.getMappedURL(url);
			} catch (Exception e) {
			}
		}
		if (newURL != null)
			url = newURL;

		if (browse) {
			navigate(url);

			addToHistory(url);
			updateHistory();
		}
	}

	protected void addToHistory(String url) {
		if (history == null)
			history = WebBrowserPreference.getInternalWebBrowserHistory();
		int found = -1;
		int size = history.size();
		for (int i = 0; i < size; i++) {
			String s = (String) history.get(i);
			if (s.equals(url)) {
				found = i;
				break;
			}
		}

		if (found == -1) {
			if (size >= MAX_HISTORY)
				history.remove(size - 1);
			history.add(0, url);
			WebBrowserPreference.setInternalWebBrowserHistory(history);
		} else if (found != 0) {
			history.remove(found);
			history.add(0, url);
			WebBrowserPreference.setInternalWebBrowserHistory(history);
		}
	}

	public void setURL(String url) {
		setURL(url, true);
	}

	/**
	 * Creates the Web browser status area.
	 */
	private void createStatusArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 4;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Add a label for displaying status messages as they are received from
		// the control
		status = new Label(composite, SWT.SINGLE | SWT.READ_ONLY);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalIndent = 2;
		status.setLayoutData(gridData);

		// Add a progress bar to display downloading progress information
		progress = new ProgressBar(composite, SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 100;
		gridData.heightHint = 10;
		progress.setLayoutData(gridData);
	}

	/**
	 * 
	 */
	public void dispose() {
		super.dispose();

		showStatusbar = false;
		showToolbar = false;

		if (busy != null)
			busy.dispose();
		busy = null;

		browser = null;
	}

	/**
	 * Populate the toolbar.
	 * 
	 * @param toolbar
	 *            org.eclipse.swt.widgets.ToolBar
	 */
	private void fillToolBar(final ToolBar toolbar) {
		ToolItem go = new ToolItem(toolbar, SWT.NONE);
		go.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_GO));
		go.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_GO));
		go.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_GO));
		go
				.setToolTipText(WebBrowserUIPlugin
						.getResource("%actionWebBrowserGo"));
		go.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setURL(combo.getText());
			}
		});

		new ToolItem(toolbar, SWT.SEPARATOR);

		favorites = new ToolItem(toolbar, SWT.DROP_DOWN);
		favorites.setImage(ImageResource
				.getImage(ImageResource.IMG_ELCL_NAV_FAVORITES));
		favorites.setHotImage(ImageResource
				.getImage(ImageResource.IMG_CLCL_NAV_FAVORITES));
		favorites.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_FAVORITES));
		favorites.setToolTipText(WebBrowserUIPlugin
				.getResource("%actionWebBrowserFavorites"));

		favorites.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.ARROW) {
					Rectangle r = favorites.getBounds();
					showFavorites(toolbar, toolbar.toDisplay(r.x, r.y
							+ r.height));
				} else
					addFavorite();
			}
		});

		// create back and forward actions
		back = new ToolItem(toolbar, SWT.NONE);
		back.setImage(ImageResource
				.getImage(ImageResource.IMG_ELCL_NAV_BACKWARD));
		back.setHotImage(ImageResource
				.getImage(ImageResource.IMG_CLCL_NAV_BACKWARD));
		back.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_BACKWARD));
		back.setToolTipText(WebBrowserUIPlugin
				.getResource("%actionWebBrowserBack"));
		back.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				back();
			}
		});

		forward = new ToolItem(toolbar, SWT.NONE);
		forward.setImage(ImageResource
				.getImage(ImageResource.IMG_ELCL_NAV_FORWARD));
		forward.setHotImage(ImageResource
				.getImage(ImageResource.IMG_CLCL_NAV_FORWARD));
		forward.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_FORWARD));
		forward.setToolTipText(WebBrowserUIPlugin
				.getResource("%actionWebBrowserForward"));
		forward.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				forward();
			}
		});

		// create refresh, stop, and print actions
		stop = new ToolItem(toolbar, SWT.NONE);
		stop.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_STOP));
		stop.setHotImage(ImageResource
				.getImage(ImageResource.IMG_CLCL_NAV_STOP));
		stop.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_STOP));
		stop.setToolTipText(WebBrowserUIPlugin
				.getResource("%actionWebBrowserStop"));
		stop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				stop();
			}
		});

		refresh = new ToolItem(toolbar, SWT.NONE);
		refresh.setImage(ImageResource
				.getImage(ImageResource.IMG_ELCL_NAV_REFRESH));
		refresh.setHotImage(ImageResource
				.getImage(ImageResource.IMG_CLCL_NAV_REFRESH));
		refresh.setDisabledImage(ImageResource
				.getImage(ImageResource.IMG_DLCL_NAV_REFRESH));
		refresh.setToolTipText(WebBrowserUIPlugin
				.getResource("%actionWebBrowserRefresh"));
		refresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refresh();
			}
		});
	}

	protected void addFavorite() {
		java.util.List list = WebBrowserPreference
				.getInternalWebBrowserFavorites();
		Favorite f = new Favorite(title, browser.getUrl());
		if (!list.contains(f)) {
			list.add(f);
			WebBrowserPreference.setInternalWebBrowserFavorites(list);
		}
	}

	protected void showFavorites(Control parent, Point p) {
		Menu perspectiveBarMenu = null;
		if (perspectiveBarMenu == null) {
			Menu menu = new Menu(parent);

			// locked favorites
			Iterator iterator = WebBrowserUtil.getLockedFavorites().iterator();
			if (iterator.hasNext()) {
				while (iterator.hasNext()) {
					final Favorite f = (Favorite) iterator.next();
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText(f.getName());
					item.setImage(ImageResource
							.getImage(ImageResource.IMG_FAVORITE));
					item.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							setURL(f.getURL());
						}
					});
				}

				new MenuItem(menu, SWT.SEPARATOR);
			}

			iterator = WebBrowserPreference.getInternalWebBrowserFavorites()
					.iterator();
			if (!iterator.hasNext()) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(WebBrowserUIPlugin
						.getResource("%actionWebBrowserNoFavorites"));
			}
			while (iterator.hasNext()) {
				final Favorite f = (Favorite) iterator.next();
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(f.getName());
				item.setImage(ImageResource
						.getImage(ImageResource.IMG_FAVORITE));
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						setURL(f.getURL());
					}
				});
			}

			new MenuItem(menu, SWT.SEPARATOR);

			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(WebBrowserUIPlugin
					.getResource("%actionWebBrowserOrganizeFavorites"));
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					OrganizeFavoritesDialog dialog = new OrganizeFavoritesDialog(
							shell);
					dialog.open();
				}
			});

			perspectiveBarMenu = menu;
		}

		if (perspectiveBarMenu != null) {
			perspectiveBarMenu.setLocation(p.x, p.y);
			perspectiveBarMenu.setVisible(true);
		}
	}

	public void home() {
		navigate(WebBrowserPreference.getHomePageURL());
	}

	/**
	 * Returns true if the homepage is currently being displayed.
	 * 
	 * @return boolean
	 */
	protected boolean isHome() {
		return getURL() != null
				&& getURL().endsWith(
						WebBrowserPreference.getHomePageURL().substring(9));
	}

	public String getURL() {
		return browser.getUrl();
	}

	/**
	 * Update the history list to the global copy.
	 */
	protected void updateHistory() {
		if (combo == null)
			return;

		String temp = combo.getText();
		if (history == null)
			history = WebBrowserPreference.getInternalWebBrowserHistory();

		String[] historyList = new String[history.size()];
		history.toArray(historyList);
		combo.setItems(historyList);

		combo.setText(temp);
	}

	public void addProgressListener(ProgressListener listener) {
		browser.addProgressListener(listener);
	}

	public void addStatusTextListener(StatusTextListener listener) {
		browser.addStatusTextListener(listener);
	}

	public void addTitleListener(TitleListener listener) {
		browser.addTitleListener(listener);
	}
}