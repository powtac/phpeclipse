/***********************************************************************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/

package net.sourceforge.phpeclipse.xdebug.ui.views.logview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;
import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPluginImages;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
//import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
//import org.eclipse.swt.custom.TableTree;
//import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
//import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

public class LogView extends ViewPart implements ILogListener {
  public final static String ID_LOGVIEW = "net.sourceforge.phpdt.internal.debug.core.logview.LogView"; //$NON-NLS-1$

  //private TableTreeViewer tableTreeViewer;
  private /*Table*/TreeViewer tableTreeViewer;

  private ArrayList logs = new ArrayList();

  public static final String P_LOG_WARNING = "warning"; //$NON-NLS-1$

  public static final String P_LOG_ERROR = "error"; //$NON-NLS-1$

  public static final String P_LOG_INFO = "info"; //$NON-NLS-1$

  public static final String P_LOG_LIMIT = "limit"; //$NON-NLS-1$

  public static final String P_USE_LIMIT = "useLimit"; //$NON-NLS-1$

  public static final String P_SHOW_ALL_SESSIONS = "allSessions"; //$NON-NLS-1$

  private static final String P_COLUMN_1 = "column1"; //$NON-NLS-1$

  private static final String P_COLUMN_2 = "column2"; //$NON-NLS-1$

  private static final String P_COLUMN_3 = "column3"; //$NON-NLS-1$

  private static final String P_COLUMN_4 = "column4"; //$NON-NLS-1$

  public static final String P_ACTIVATE = "activate"; //$NON-NLS-1$

  private int MESSAGE_ORDER = -1;

  private int PLUGIN_ORDER = -1;

  private int DATE_ORDER = -1;

  public static byte MESSAGE = 0x0;

  public static byte PLUGIN = 0x1;

  public static byte DATE = 0x2;

  private static int ASCENDING = 1;

  private static int DESCENDING = -1;

  private Action clearAction;

  private Action copyAction;

  private Action readLogAction;

  private Action deleteLogAction;

  private Action exportAction;

  private Action importAction;

  private Action activateViewAction;

  private Action propertiesAction;

  private Action viewLogAction;

  private Action filterAction;

  private Clipboard clipboard;

  private IMemento memento;

  private File inputFile;

  private String directory;

  private /*Table*/TreeColumn column0;

  private TreeColumn column1;

  private TreeColumn column2;

  private TreeColumn column3;

  private TreeColumn column4;

  private static Font boldFont;

  private Comparator comparator;

  private Collator collator;

  // hover text
  private boolean canOpenTextShell;

  private Text textLabel;

  private Shell textShell;

  private boolean firstEvent = true;

  public LogView() {
    logs = new ArrayList();
    inputFile = Platform.getLogFileLocation().toFile();
  }

  public void createPartControl(Composite parent) {
    readLogFile();
    /*Table*/Tree tableTree = new /*Table*/Tree(parent, SWT.FULL_SELECTION);
    
    tableTree.setLayoutData(new GridData(GridData.FILL_BOTH));
    createColumns(tableTree/*get getColumns()*//*.get getTable()*/);
    createViewer(tableTree);
    createPopupMenuManager(tableTree);
    makeActions(tableTree/*.getTable()*/);
    fillToolBar();
    Platform.addLogListener(this);
    getSite().setSelectionProvider(tableTreeViewer);
    clipboard = new Clipboard(tableTree.getDisplay());
    tableTreeViewer.getTree().setToolTipText(""); // setToolTipText/* getTableTree().getTable()*/./*setToolTipText*/(""); //$NON-NLS-1$
    initializeFonts();
    applyFonts();
  }

  private void initializeFonts() {
    Font tableFont = tableTreeViewer.getTree().getFont(); // getTableTree().getFont();
    FontData[] fontDataList = tableFont.getFontData();
    FontData fontData;
    if (fontDataList.length > 0)
      fontData = fontDataList[0];
    else
      fontData = new FontData();
    fontData.setStyle(SWT.BOLD);
    boldFont = new Font(tableTreeViewer.getTree()/*getTableTree()*/.getDisplay(), fontData);
  }

  /*
   * Set all rows where the tableTreeItem has children to have a <b>bold </b> font.
   */
  private void applyFonts() {
    if (tableTreeViewer == null || tableTreeViewer.getTree()/*getTableTree()*/.isDisposed())
      return;
    int max = tableTreeViewer.getTree().getItemCount();
    int index = 0, tableIndex = 0;
    while (index < max) {
      LogEntry entry = null; //(LogEntry) tableTreeViewer.get getElementAt(index);
      if (entry == null)
        return;
      if (entry.hasChildren()) {
        tableTreeViewer.getTree()/*getTableTree()*/.getItems()[index].setFont(boldFont);
        tableIndex = applyChildFonts(entry, tableIndex);
      } else {
        tableTreeViewer.getTree()/*getTableTree()*/.getItems()[index].setFont(tableTreeViewer.getTree()/*getTableTree()*/.getFont());
      }
      index++;
      tableIndex++;
    }
  }

  private int applyChildFonts(LogEntry parent, int index) {
    if (!tableTreeViewer.getExpandedState(parent) || !parent.hasChildren())
      return index;
    LogEntry[] children = getEntryChildren(parent);
    for (int i = 0; i < children.length; i++) {
      index++;
      if (children[i].hasChildren()) {
        /*TableItem*/ TreeItem tableItem = getTableItem(index);
        if (tableItem != null) {
          tableItem.setFont(boldFont);
        }
        index = applyChildFonts(children[i], index);
      } else {
        /*TableItem*/ TreeItem tableItem = getTableItem(index);
        if (tableItem != null) {
          tableItem.setFont(tableTreeViewer.getTree()/*getTableTree()*/.getFont());
        }
      }
    }
    return index;
  }

  private LogEntry[] getEntryChildren(LogEntry parent) {
    Object[] entryChildren = parent.getChildren(parent);
    if (comparator != null)
      Arrays.sort(entryChildren, comparator);
    LogEntry[] children = new LogEntry[entryChildren.length];
    System.arraycopy(entryChildren, 0, children, 0, entryChildren.length);
    return children;
  }

  private TreeItem /*TableItem*/ getTableItem(int index) {
    /*TableItem[]*/ TreeItem[] tableItems = tableTreeViewer.getTree().getItems(); // /*getTableTree()*/.getTable().getItems();
    if (index > tableItems.length - 1)
      return null;
    return tableItems[index];
  }

  private void fillToolBar() {
    IActionBars bars = getViewSite().getActionBars();
    bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
    IToolBarManager toolBarManager = bars.getToolBarManager();
    toolBarManager.add(exportAction);
    toolBarManager.add(importAction);
    toolBarManager.add(new Separator());
    toolBarManager.add(clearAction);
    toolBarManager.add(deleteLogAction);
    toolBarManager.add(viewLogAction);
    toolBarManager.add(readLogAction);
    toolBarManager.add(new Separator());
    IMenuManager mgr = bars.getMenuManager();
    mgr.add(filterAction);
    mgr.add(new Separator());
    mgr.add(activateViewAction);
  }

  private void createViewer(/*Table*/Tree tableTree) {
    tableTreeViewer = new /*Table*/TreeViewer(tableTree);
    tableTreeViewer.setContentProvider(new LogViewContentProvider(this));
    tableTreeViewer.setLabelProvider(new LogViewLabelProvider());
    tableTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent e) {
        handleSelectionChanged(e.getSelection());
        if (propertiesAction.isEnabled())
          ((EventDetailsDialogAction) propertiesAction).resetSelection();
      }
    });
    tableTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        ((EventDetailsDialogAction) propertiesAction).setComparator(comparator);
        propertiesAction.run();
      }
    });
    tableTreeViewer.addTreeListener(new ITreeViewerListener() {
      public void treeCollapsed(TreeExpansionEvent event) {
        applyFonts();
      }

      public void treeExpanded(TreeExpansionEvent event) {
        applyFonts();
      }
    });
    addMouseListeners();
    tableTreeViewer.setInput(Platform.class);
  }

  private void createPopupMenuManager(/*Table*/Tree tableTree) {
    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener() {
      public void menuAboutToShow(IMenuManager mng) {
        fillContextMenu(mng);
      }
    };
    popupMenuManager.addMenuListener(listener);
    popupMenuManager.setRemoveAllWhenShown(true);
    Menu menu = popupMenuManager.createContextMenu(tableTree);
    tableTree.setMenu(menu);
  }

  private void createColumns(/*Table*/ Tree table) {
    column0 = new TreeColumn(table, SWT.NULL);
    column0.setText(""); //$NON-NLS-1$
    column1 = new TreeColumn(table, SWT.NULL);
    column1.setText(XDebugUIPlugin.getString("LogView.column.severity")); //$NON-NLS-1$
    column2 = new TreeColumn(table, SWT.NULL);
    column2.setText(XDebugUIPlugin.getString("LogView.column.message")); //$NON-NLS-1$
    column2.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        MESSAGE_ORDER *= -1;
        ViewerSorter sorter = getViewerSorter(MESSAGE);
        tableTreeViewer.setSorter(sorter);
        collator = sorter.getCollator();
        boolean isComparatorSet = ((EventDetailsDialogAction) propertiesAction).resetSelection(MESSAGE, MESSAGE_ORDER);
        setComparator(MESSAGE);
        if (!isComparatorSet)
          ((EventDetailsDialogAction) propertiesAction).setComparator(comparator);
        applyFonts();
      }
    });
    column3 = new TreeColumn(table, SWT.NULL);
    column3.setText(XDebugUIPlugin.getString("LogView.column.plugin"));  //$NON-NLS-1$
    column3.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        PLUGIN_ORDER *= -1;
        ViewerSorter sorter = getViewerSorter(PLUGIN);
        tableTreeViewer.setSorter(sorter);
        collator = sorter.getCollator();
        boolean isComparatorSet = ((EventDetailsDialogAction) propertiesAction).resetSelection(PLUGIN, PLUGIN_ORDER);
        setComparator(PLUGIN);
        if (!isComparatorSet)
          ((EventDetailsDialogAction) propertiesAction).setComparator(comparator);
        applyFonts();
      }
    });
    column4 = new TreeColumn(table, SWT.NULL);
    column4.setText(XDebugUIPlugin.getString("LogView.column.date")); //$NON-NLS-1$
    column4.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (DATE_ORDER == ASCENDING) {
          DATE_ORDER = DESCENDING;
        } else {
          DATE_ORDER = ASCENDING;
        }
        ViewerSorter sorter = getViewerSorter(DATE);
        tableTreeViewer.setSorter(sorter);
        collator = sorter.getCollator();
        boolean isComparatorSet = ((EventDetailsDialogAction) propertiesAction).resetSelection(DATE, DATE_ORDER);
        setComparator(DATE);
        if (!isComparatorSet)
          ((EventDetailsDialogAction) propertiesAction).setComparator(comparator);
        applyFonts();
      }
    });
    TableLayout tlayout = new TableLayout();
    tlayout.addColumnData(new ColumnPixelData(21));
    tlayout.addColumnData(new ColumnPixelData(memento.getInteger(P_COLUMN_1).intValue()));
    tlayout.addColumnData(new ColumnPixelData(memento.getInteger(P_COLUMN_2).intValue()));
    tlayout.addColumnData(new ColumnPixelData(memento.getInteger(P_COLUMN_3).intValue()));
    tlayout.addColumnData(new ColumnPixelData(memento.getInteger(P_COLUMN_4).intValue()));
    table.setLayout(tlayout);
    table.setHeaderVisible(true);
  }

  private void makeActions(/*Table*/Tree table) {
    propertiesAction = new EventDetailsDialogAction(table.getShell(), tableTreeViewer);
    propertiesAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_PROPERTIES));
    propertiesAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_PROPERTIES));
    propertiesAction.setToolTipText(XDebugUIPlugin.getString("LogView.properties.tooltip")); //$NON-NLS-1$
    propertiesAction.setEnabled(false);
    clearAction = new Action(XDebugUIPlugin.getString("LogView.clear")) { //$NON-NLS-1$
      public void run() {
        handleClear();
      }
    };
    clearAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_CLEAR));
    clearAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_CLEAR));
    clearAction.setToolTipText(XDebugUIPlugin.getString("LogView.clear.tooltip")); //$NON-NLS-1$
    clearAction.setText(XDebugUIPlugin.getString("LogView.clear")); //$NON-NLS-1$
    readLogAction = new Action(XDebugUIPlugin.getString("LogView.readLog.restore")) { //$NON-NLS-1$
      public void run() {
        inputFile = Platform.getLogFileLocation().toFile();
        reloadLog();
      }
    };
    readLogAction.setToolTipText(XDebugUIPlugin.getString("LogView.readLog.restore.tooltip")); //$NON-NLS-1$
    readLogAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_READ_LOG));
    readLogAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_READ_LOG));
    deleteLogAction = new Action(XDebugUIPlugin.getString("LogView.delete")) { //$NON-NLS-1$
      public void run() {
        doDeleteLog();
      }
    };
    deleteLogAction.setToolTipText(XDebugUIPlugin.getString("LogView.delete.tooltip")); //$NON-NLS-1$
    deleteLogAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_REMOVE_LOG));
    deleteLogAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_REMOVE_LOG));
    deleteLogAction.setEnabled(inputFile.exists() && inputFile.equals(Platform.getLogFileLocation().toFile()));
    copyAction = new Action(XDebugUIPlugin.getString("LogView.copy")) { //$NON-NLS-1$
      public void run() {
        copyToClipboard(tableTreeViewer.getSelection());
      }
    };
    copyAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    filterAction = new Action(XDebugUIPlugin.getString("LogView.filter")) { //$NON-NLS-1$
      public void run() {
        handleFilter();
      }
    };
    filterAction.setToolTipText(XDebugUIPlugin.getString("LogView.filter")); //$NON-NLS-1$
    filterAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_FILTER));
    filterAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_FILTER));
    exportAction = new Action(XDebugUIPlugin.getString("LogView.export")) { //$NON-NLS-1$
      public void run() {
        handleExport();
      }
    };
    exportAction.setToolTipText(XDebugUIPlugin.getString("LogView.export.tooltip")); //$NON-NLS-1$
    exportAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_EXPORT));
    exportAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_EXPORT));
    importAction = new Action(XDebugUIPlugin.getString("LogView.import")) { //$NON-NLS-1$
      public void run() {
        handleImport();
      }
    };
    importAction.setToolTipText(XDebugUIPlugin.getString("LogView.import.tooltip")); //$NON-NLS-1$
    importAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_IMPORT));
    importAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_IMPORT));
    activateViewAction = new Action(XDebugUIPlugin.getString("LogView.activate")) { //$NON-NLS-1$
      public void run() {
      }
    };
    activateViewAction.setChecked(memento.getString(P_ACTIVATE).equals("true")); //$NON-NLS-1$
    viewLogAction = new Action(XDebugUIPlugin.getString("LogView.view.currentLog")) { //$NON-NLS-1$
      public void run() {
        if (inputFile.exists()) {
          if (inputFile.length() > LogReader.MAX_FILE_LENGTH) {
            OpenLogDialog openDialog = new OpenLogDialog(getViewSite().getShell(), inputFile);
            openDialog.create();
            openDialog.open();
          } else {
            boolean canLaunch = Program.launch(inputFile.getAbsolutePath());
            if (!canLaunch) {
              Program p = Program.findProgram(".txt"); //$NON-NLS-1$
              if (p != null)
                p.execute(inputFile.getAbsolutePath());
              else {
                OpenLogDialog openDialog = new OpenLogDialog(getViewSite().getShell(), inputFile);
                openDialog.create();
                openDialog.open();
              }
            }
          }
        }
      }
    };
    viewLogAction.setImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_OPEN_LOG));
    viewLogAction.setDisabledImageDescriptor(XDebugUIPluginImages.getImageDescriptor(XDebugUIPluginImages.IMG_OPEN_LOG));
    viewLogAction.setEnabled(inputFile.exists());
    viewLogAction.setToolTipText(XDebugUIPlugin.getString("LogView.view.currentLog.tooltip")); //$NON-NLS-1$
  }

  public void dispose() {
    Platform.removeLogListener(this);
    clipboard.dispose();
    LogReader.reset();
    boldFont.dispose();
    super.dispose();
  }

  private void handleImport() {
    FileDialog dialog = new FileDialog(getViewSite().getShell());
    dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
    if (directory != null)
      dialog.setFilterPath(directory);
    String path = dialog.open();
    if (path != null && new Path(path).toFile().exists()) {
      inputFile = new Path(path).toFile();
      directory = inputFile.getParent();
      IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          monitor.beginTask(XDebugUIPlugin.getString("LogView.operation.importing"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
          readLogFile();
        }
      };
      ProgressMonitorDialog pmd = new ProgressMonitorDialog(getViewSite().getShell());
      try {
        pmd.run(true, true, op);
      } catch (InvocationTargetException e) {
      } catch (InterruptedException e) {
      } finally {
        readLogAction.setText(XDebugUIPlugin.getString("LogView.readLog.reload")); //$NON-NLS-1$
        readLogAction.setToolTipText(XDebugUIPlugin.getString("LogView.readLog.reload")); //$NON-NLS-1$
        asyncRefresh(false);
        resetDialogButtons();
      }
    }
  }

  private void handleExport() {
    FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
    dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
    if (directory != null)
      dialog.setFilterPath(directory);
    String path = dialog.open();
    if (path != null) {
      if (!path.endsWith(".log")) //$NON-NLS-1$
        path += ".log"; //$NON-NLS-1$
      File outputFile = new Path(path).toFile();
      directory = outputFile.getParent();
      if (outputFile.exists()) {
    	  String message = "LogView.confirmOverwrite.message";
//        String message = PHPDebugUIPlugin.getFormattedMessage("LogView.confirmOverwrite.message", //$NON-NLS-1$
//            outputFile.toString());
        if (!MessageDialog.openQuestion(getViewSite().getShell(), exportAction.getText(), message))
          return;
      }
      copy(inputFile, outputFile);
    }
  }

  private void copy(File inputFile, File outputFile) {
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8")); //$NON-NLS-1$
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")); //$NON-NLS-1$
      while (reader.ready()) {
        writer.write(reader.readLine());
        writer.write(System.getProperty("line.separator")); //$NON-NLS-1$
      }
    } catch (IOException e) {
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (writer != null)
          writer.close();
      } catch (IOException e1) {
      }
    }
  }

  private void handleFilter() {
    FilterDialog dialog = new FilterDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), memento);
    dialog.create();
    dialog.getShell().setText(XDebugUIPlugin.getString("LogView.FilterDialog.title")); //$NON-NLS-1$
    if (dialog.open() == FilterDialog.OK)
      reloadLog();
  }

  private void doDeleteLog() {
    String title = XDebugUIPlugin.getString("LogView.confirmDelete.title"); //$NON-NLS-1$
    String message = XDebugUIPlugin.getString("LogView.confirmDelete.message"); //$NON-NLS-1$
    if (!MessageDialog.openConfirm(tableTreeViewer.getControl().getShell(), title, message))
      return;
    if (inputFile.delete()) {
      logs.clear();
      asyncRefresh(false);
      resetDialogButtons();
    }
  }

  public void fillContextMenu(IMenuManager manager) {
    manager.add(copyAction);
    manager.add(new Separator());
    manager.add(clearAction);
    manager.add(deleteLogAction);
    manager.add(viewLogAction);
    manager.add(readLogAction);
    manager.add(new Separator());
    manager.add(exportAction);
    manager.add(importAction);
    manager.add(new Separator());
    ((EventDetailsDialogAction) propertiesAction).setComparator(comparator);
    manager.add(propertiesAction);
  }

  public LogEntry[] getLogs() {
    return (LogEntry[]) logs.toArray(new LogEntry[logs.size()]);
  }

  protected void handleClear() {
    BusyIndicator.showWhile(tableTreeViewer.getControl().getDisplay(), new Runnable() {
      public void run() {
        logs.clear();
        asyncRefresh(false);
        resetDialogButtons();
      }
    });
  }

  protected void reloadLog() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(XDebugUIPlugin.getString("LogView.operation.reloading"), //$NON-NLS-1$
            IProgressMonitor.UNKNOWN);
        readLogFile();
      }
    };
    ProgressMonitorDialog pmd = new ProgressMonitorDialog(getViewSite().getShell());
    try {
      pmd.run(true, true, op);
    } catch (InvocationTargetException e) {
    } catch (InterruptedException e) {
    } finally {
      readLogAction.setText(XDebugUIPlugin.getString("LogView.readLog.restore")); //$NON-NLS-1$
      readLogAction.setToolTipText(XDebugUIPlugin.getString("LogView.readLog.restore")); //$NON-NLS-1$
      asyncRefresh(false);
      resetDialogButtons();
    }
  }

  private void readLogFile() {
    logs.clear();
    if (!inputFile.exists())
      return;
    if (inputFile.length() > LogReader.MAX_FILE_LENGTH)
      LogReader.parseLargeFile(inputFile, logs, memento);
    else
      LogReader.parseLogFile(inputFile, logs, memento);
  }

  public void logging(IStatus status, String plugin) { 
    if (!inputFile.equals(Platform.getLogFileLocation().toFile()))
      return;
    if (firstEvent) {
      readLogFile();
      asyncRefresh();
      firstEvent = false;
    } else {
      pushStatus(status);
    }
  }

  private void pushStatus(IStatus status) {
      LogEntry entry = new LogEntry(status);
      LogReader.addEntry(entry, logs, memento, true);
      asyncRefresh();
  }

  private void asyncRefresh() {
    asyncRefresh(true);
  }

  private void asyncRefresh(final boolean activate) {
    final Control control = tableTreeViewer.getControl();
    if (control.isDisposed())
      return;
    Display display = control.getDisplay();
    final ViewPart view = this;
    if (display != null) {
      display.asyncExec(new Runnable() {
        public void run() {
          if (!control.isDisposed()) {
            tableTreeViewer.refresh();
            deleteLogAction.setEnabled(inputFile.exists() && inputFile.equals(Platform.getLogFileLocation().toFile()));
            viewLogAction.setEnabled(inputFile.exists());
            if (activate && activateViewAction.isChecked()) {
              //IWorkbenchPage page = XDebugCorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
            	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
              if (page != null)
                page.bringToTop(view);
            }
          }
          applyFonts();
        }
      });
    }
  }

  public void setFocus() {
    if (tableTreeViewer != null && !tableTreeViewer.getTree()/*getTableTree()*/.isDisposed())
      tableTreeViewer.getTree().setFocus(); ///*getTableTree()*/.getTable().setFocus();
  }

  private void handleSelectionChanged(ISelection selection) {
    updateStatus(selection);
    copyAction.setEnabled(!selection.isEmpty());
    propertiesAction.setEnabled(!selection.isEmpty());
  }

  private void updateStatus(ISelection selection) {
    IStatusLineManager status = getViewSite().getActionBars().getStatusLineManager();
    if (selection.isEmpty())
      status.setMessage(null);
    else {
      LogEntry entry = (LogEntry) ((IStructuredSelection) selection).getFirstElement();
      status.setMessage(((LogViewLabelProvider) tableTreeViewer.getLabelProvider()).getColumnText(entry, 2));
    }
  }

  private void copyToClipboard(ISelection selection) {
    StringWriter writer = new StringWriter();
    PrintWriter pwriter = new PrintWriter(writer);
    if (selection.isEmpty())
      return;
    LogEntry entry = (LogEntry) ((IStructuredSelection) selection).getFirstElement();
    entry.write(pwriter);
    pwriter.flush();
    String textVersion = writer.toString();
    try {
      pwriter.close();
      writer.close();
    } catch (IOException e) {
    }
    if (textVersion.trim().length() > 0) {
      // set the clipboard contents
      clipboard.setContents(new Object[] { textVersion }, new Transfer[] { TextTransfer.getInstance() });
    }
  }

  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    if (memento == null)
      this.memento = XMLMemento.createWriteRoot("LOGVIEW"); //$NON-NLS-1$
    else
      this.memento = memento;
    initializeMemento();
  }

  private void initializeMemento() {
    if (memento.getString(P_USE_LIMIT) == null)
      memento.putString(P_USE_LIMIT, "true"); //$NON-NLS-1$
    if (memento.getInteger(P_LOG_LIMIT) == null)
      memento.putInteger(P_LOG_LIMIT, 50);
    if (memento.getString(P_LOG_INFO) == null)
      memento.putString(P_LOG_INFO, "true"); //$NON-NLS-1$
    if (memento.getString(P_LOG_WARNING) == null)
      memento.putString(P_LOG_WARNING, "true"); //$NON-NLS-1$
    if (memento.getString(P_LOG_ERROR) == null)
      memento.putString(P_LOG_ERROR, "true"); //$NON-NLS-1$
    if (memento.getString(P_SHOW_ALL_SESSIONS) == null)
      memento.putString(P_SHOW_ALL_SESSIONS, "true"); //$NON-NLS-1$
    Integer width = memento.getInteger(P_COLUMN_1);
    if (width == null || width.intValue() == 0)
      memento.putInteger(P_COLUMN_1, 20);
    width = memento.getInteger(P_COLUMN_2);
    if (width == null || width.intValue() == 0)
      memento.putInteger(P_COLUMN_2, 300);
    width = memento.getInteger(P_COLUMN_3);
    if (width == null || width.intValue() == 0)
      memento.putInteger(P_COLUMN_3, 150);
    width = memento.getInteger(P_COLUMN_4);
    if (width == null || width.intValue() == 0)
      memento.putInteger(P_COLUMN_4, 150);
    if (memento.getString(P_ACTIVATE) == null)
      memento.putString(P_ACTIVATE, "true"); //$NON-NLS-1$
  }

  public void saveState(IMemento memento) {
    if (this.memento == null || memento == null)
      return;
    this.memento.putInteger(P_COLUMN_1, column1.getWidth());
    this.memento.putInteger(P_COLUMN_2, column2.getWidth());
    this.memento.putInteger(P_COLUMN_3, column3.getWidth());
    this.memento.putInteger(P_COLUMN_4, column4.getWidth());
    this.memento.putString(P_ACTIVATE, activateViewAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    memento.putMemento(this.memento);
  }

  private void addMouseListeners() {
    Listener tableListener = new Listener() {
      public void handleEvent(Event e) {
        switch (e.type) {
        case SWT.MouseMove:
          onMouseMove(e);
          break;
        case SWT.MouseHover:
          onMouseHover(e);
          break;
        case SWT.MouseDown:
          onMouseDown(e);
          break;
        }
      }
    };
    int[] tableEvents = new int[] { SWT.MouseDown, SWT.MouseMove, SWT.MouseHover };
    for (int i = 0; i < tableEvents.length; i++) {
      tableTreeViewer.getTree().addListener(tableEvents[i], tableListener);
    }
  }

  private void makeHoverShell() {
    Control control = tableTreeViewer.getControl();
    textShell = new Shell(control.getShell(), SWT.NO_FOCUS | SWT.ON_TOP);
    Display display = textShell.getDisplay();
    textShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    GridLayout layout = new GridLayout(1, false);
    int border = ((control.getShell().getStyle() & SWT.NO_TRIM) == 0) ? 0 : 1;
    layout.marginHeight = border;
    layout.marginWidth = border;
    textShell.setLayout(layout);
    textShell.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Composite shellComposite = new Composite(textShell, SWT.NONE);
    layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    shellComposite.setLayout(layout);
    shellComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
    textLabel = new Text(shellComposite, SWT.WRAP | SWT.MULTI);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 100;
    gd.grabExcessHorizontalSpace = true;
    textLabel.setLayoutData(gd);
    Color c = control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    textLabel.setBackground(c);
    c = control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
    textLabel.setForeground(c);
    textLabel.setEditable(false);
    textShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        onTextShellDispose(e);
      }
    });
  }

  void onTextShellDispose(DisposeEvent e) {
    canOpenTextShell = true;
    setFocus();
  }

  void onMouseDown(Event e) {
    if (textShell != null && !textShell.isDisposed() && !textShell.isFocusControl()) {
      textShell.close();
      canOpenTextShell = true;
    }
  }

  void onMouseHover(Event e) {
    if (!canOpenTextShell)
      return;
    canOpenTextShell = false;
    Point point = new Point(e.x, e.y);
    /*Table*/Tree table = tableTreeViewer.getTree()/*getTableTree()*/;
    /*Table*/TreeItem item = table.getItem(point);
    if (item == null)
      return;
    String message = ((LogEntry) item.getData()).getStack();
    if (message == null)
      return;
    makeHoverShell();
    textLabel.setText(message);
    int x = point.x + 5;
    int y = point.y - (table.getItemHeight() * 2) - 20;
    textShell.setLocation(table.toDisplay(x, y));
    textShell.setSize(tableTreeViewer.getTree()/*getTableTree()*/.getSize().x - x, 125);
    textShell.open();
    setFocus();
  }

  void onMouseMove(Event e) {
    if (textShell != null && !textShell.isDisposed()) {
      textShell.close();
      canOpenTextShell = textShell.isDisposed() && e.x > column0.getWidth() && e.x < (column0.getWidth() + column1.getWidth());
    } else {
      canOpenTextShell = e.x > column0.getWidth() && e.x < (column0.getWidth() + column1.getWidth());
    }
  }

  public Comparator getComparator() {
    return comparator;
  }

  private void setComparator(byte sortType) {
    if (sortType == DATE) {
      comparator = new Comparator() {
        public int compare(Object e1, Object e2) {
          try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SS"); //$NON-NLS-1$
            Date date1 = formatter.parse(((LogEntry) e1).getDate());
            Date date2 = formatter.parse(((LogEntry) e2).getDate());
            if (DATE_ORDER == ASCENDING)
              return date1.before(date2) ? -1 : 1;
            return date1.after(date2) ? -1 : 1;
          } catch (ParseException e) {
          }
          return 0;
        }
      };
    } else if (sortType == PLUGIN) {
      comparator = new Comparator() {
        public int compare(Object e1, Object e2) {
          LogEntry entry1 = (LogEntry) e1;
          LogEntry entry2 = (LogEntry) e2;
          return collator.compare(entry1.getPluginId(), entry2.getPluginId()) * PLUGIN_ORDER;
        }
      };
    } else {
      comparator = new Comparator() {
        public int compare(Object e1, Object e2) {
          LogEntry entry1 = (LogEntry) e1;
          LogEntry entry2 = (LogEntry) e2;
          return collator.compare(entry1.getMessage(), entry2.getMessage()) * MESSAGE_ORDER;
        }
      };
    }
  }

  private ViewerSorter getViewerSorter(byte sortType) {
    if (sortType == PLUGIN) {
      return new ViewerSorter() {
        public int compare(Viewer viewer, Object e1, Object e2) {
          LogEntry entry1 = (LogEntry) e1;
          LogEntry entry2 = (LogEntry) e2;
          return super.compare(viewer, entry1.getPluginId(), entry2.getPluginId()) * PLUGIN_ORDER;
        }
      };
    } else if (sortType == MESSAGE) {
      return new ViewerSorter() {
        public int compare(Viewer viewer, Object e1, Object e2) {
          LogEntry entry1 = (LogEntry) e1;
          LogEntry entry2 = (LogEntry) e2;
          return super.compare(viewer, entry1.getMessage(), entry2.getMessage()) * MESSAGE_ORDER;
        }
      };
    } else {
      return new ViewerSorter() {
        public int compare(Viewer viewer, Object e1, Object e2) {
          try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SS"); //$NON-NLS-1$
            Date date1 = formatter.parse(((LogEntry) e1).getDate());
            Date date2 = formatter.parse(((LogEntry) e2).getDate());
            if (DATE_ORDER == ASCENDING)
              return date1.before(date2) ? -1 : 1;
            return date1.after(date2) ? -1 : 1;
          } catch (ParseException e) {
          }
          return 0;
        }
      };
    }
  }

  private void resetDialogButtons() {
    ((EventDetailsDialogAction) propertiesAction).resetDialogButtons();
  }
}