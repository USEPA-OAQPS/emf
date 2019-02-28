package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.*;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyWindow;
import gov.epa.emissions.framework.client.cost.controlstrategy.groups.StrategyGroupManagerView;
import gov.epa.emissions.framework.client.cost.controlstrategy.groups.StrategyGroupManagerWindow;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyWindow;
import gov.epa.emissions.framework.client.data.dataset.DatasetSearchWindow;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.client.search.AdvancedSearchPresenter;
import gov.epa.emissions.framework.client.search.AdvancedSearchView;
import gov.epa.emissions.framework.client.search.AdvancedSearchWindow;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyFilter;
import gov.epa.emissions.framework.ui.*;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.emissions.commons.CommonDebugLevel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ControlStrategyManagerWindow extends ReusableInteralFrame implements ControlStrategyManagerView,
        RefreshObserver, SearchObserver<ControlStrategyFilter, ControlStrategy>, Runnable {

    private ControlStrategiesManagerPresenter presenter;

    private ControlStrategiesTableData tableData;

    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;

    //private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private EditControlStrategyView[] editControlStrategyViews = {};

    private ViewControlStrategyView[] viewControlStrategyViews = {};

    private volatile Thread populateThread;

    private static final List<String> COLUMN_NAMES_TO_FORMAT = new ArrayList<String>();
    
    private JFileChooser chooser;

    private TextField textFilter;

    static {
        COLUMN_NAMES_TO_FORMAT.add("Total Cost");
        COLUMN_NAMES_TO_FORMAT.add("Reduction (tons)");
        COLUMN_NAMES_TO_FORMAT.add("Average Cost Per Ton");
    }

    public ControlStrategyManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Strategy Manager", new Dimension(850, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ControlStrategiesManagerPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void display(ControlStrategy[] controlStrategies) {
        doLayout(controlStrategies, this.session);
        super.display();
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        try {
            presenter.loadControlMeasures();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all control measures.");
        }
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            if (view != null) view.endControlMeasuresRefresh();
        }
        this.populateThread = null;
    }

    public void refresh(ControlStrategy[] controlStrategies) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(controlStrategies);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
        setCursor(Cursor.getDefaultCursor());
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            if (editControlStrategyViews != null) {
                view.startControlMeasuresRefresh();
            }
        }
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    @Override
    public ControlStrategy[] doSearch(final ControlStrategyFilter modelFilter) {

        // Long running methods ...
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        class SearchControlStrategyTask extends SwingWorker<ControlStrategy[], Void> {

            private Container parentContainer;

            public SearchControlStrategyTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            // Main task. Executed in background thread. Don't update GUI here
            @Override
            public ControlStrategy[] doInBackground() throws EmfException  {

                return presenter.doSearch(modelFilter);
            }

            // Executed in event dispatching thread
            @Override
            public void done() {
                try {
                    refresh(get());
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new SearchControlStrategyTask(this).execute();


        return new ControlStrategy[0];
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void doLayout(ControlStrategy[] controlStrategies, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(controlStrategies, parentConsole, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(ControlStrategy[] controlStrategies, EmfConsole parentConsole, EmfSession session) {

        setupTableModel(controlStrategies);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());

        JTable innerTable = table.getTable();
        TableColumnModel columnModel = innerTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {

            TableColumn column = columnModel.getColumn(i);
            String[] headerValue = (String[]) column.getHeaderValue();
            if (headerValue != null && headerValue.length > 0 && headerValue[0] != null
                    && COLUMN_NAMES_TO_FORMAT.contains(headerValue[0])) {

                ControlStrategyCustomFormat format = new ControlStrategyCustomFormat("#,##0");
                FormattedCellRenderer formattedCellRenderer = new FormattedCellRenderer(format, SwingConstants.CENTER);
                column.setCellRenderer(formattedCellRenderer);
            }
        }

        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(ControlStrategy[] controlStrategies){
        tableData = new ControlStrategiesTableData(controlStrategies);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }
    
    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Control Strategies", messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);



        JPanel topPanel = new JPanel(new BorderLayout());
        textFilter = new gov.epa.emissions.commons.gui.TextField("textfilter", 10);
        textFilter.setEditable(true);
        textFilter.addActionListener(typeAction());

        Button advButton = new Button("Advanced", new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                advancedSearch();
                //notifyAdvancedSearch();
            }
        });
        advButton.setToolTipText("Advanced search");

        JPanel advPanel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel("Name Contains:");
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        advPanel.add(jlabel, BorderLayout.WEST);
        advPanel.add(textFilter, BorderLayout.CENTER);
        advPanel.add(advButton, BorderLayout.EAST);
        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

//        topPanel.add(getDSTypePanel("Show Datasets of Type:", dsTypesBox), BorderLayout.LINE_START);
        topPanel.add(advPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(topPanel);

        return panel;
    }

    private void advancedSearch() {
        AdvancedSearchWindow<ControlStrategyFilter, ControlStrategy> view = new AdvancedSearchWindow<ControlStrategyFilter, ControlStrategy>("Advanced Dataset Search", parentConsole, desktopManager, null);
        AdvancedSearchPresenter<ControlStrategyFilter, ControlStrategy> presenter = new AdvancedSearchPresenter<ControlStrategyFilter, ControlStrategy>(session, ControlStrategy.class);

        if (textFilter != null && textFilter.getText() != null && !textFilter.getText().trim().isEmpty())
            view.setNameText(textFilter.getText().trim());

        view.showUsers(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);
        view.showDataTypes(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);
        view.showDescription(true);
        view.showName(true);
        view.showCases(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);
        view.showControlPrograms(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);
        view.showKeywords(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);
        view.showProjects(true, AdvancedSearchView.ListSelectionType.SINGLE_SELECTION);

        presenter.doDisplay(view);

//        view.observe(presenter);
//        view.display();
    }

    private Action typeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRefresh();
                } catch (EmfException e1) {
                    messagePanel.setError("something happened");
                }
            }
        };
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new BoxLayout(crudPanel, BoxLayout.Y_AXIS));
        
        JPanel row1Panel = new JPanel();
        row1Panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 2));
        
        JPanel row2Panel = new JPanel();
        row2Panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        String message = "You have asked to open a lot of windows. Do you want to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        row1Panel.add(viewButton(confirmDialog));
        row1Panel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewStrategy();
            }
        });
        row1Panel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRemove();
                } catch (EmfException exception) {
                    messagePanel.setError(exception.getMessage());
                }
            }
        });
        row1Panel.add(removeButton);
        
        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                    try {
                        copySelectedStrategy();
                    } catch (EmfException excp) {
                        messagePanel.setError("Error copying control strategies: " + excp.getMessage());
                    }
            }
        });
        row1Panel.add(copyButton);

        Button compareButton = new Button("Compare", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    compareControlStrategies();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        row1Panel.add(compareButton);
        
        Button summaryButton = new Button("Summarize", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    summarizeControlStrategies();
                } catch (EmfException e1) {
                    e1.printStackTrace();
                }
            }
        });
        row2Panel.add(summaryButton);
        
        Button resultsButton = new Button("Export Results", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    exportStrategyDetailedResults();
                } catch (EmfException e1) {
                    e1.printStackTrace();
                }
            }
        });
        row2Panel.add(resultsButton);
        
        Button groupsButton = new Button("Strategy Groups", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    StrategyGroupManagerView view = new StrategyGroupManagerWindow(parentConsole, session, desktopManager);
                    presenter.doDisplayStrategyGroups(view);
                } catch (EmfException exception) {
                    messagePanel.setError(exception.getMessage());
                }
            }
        });
        row2Panel.add(groupsButton);
        
        crudPanel.add(row1Panel);
        crudPanel.add(row2Panel);

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editControlStrategies();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewControlStrategies();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        return viewButton;
    }

    private void editControlStrategies() {
        messagePanel.clear();
        List controlStrategies = selected();
        if (controlStrategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Strategies");
            return;
        }
        editControlStrategyViews = new EditControlStrategyView[controlStrategies.size()]; 
        for (int i = 0; i < controlStrategies.size(); i++) {
            ControlStrategy controlStrategy = (ControlStrategy) controlStrategies.get(i);
            EditControlStrategyView view = new EditControlStrategyWindow(desktopManager, session, parentConsole);
            editControlStrategyViews[i] = view;
            try {
                presenter.doEdit(view, controlStrategy);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void viewControlStrategies() {
        
        this.messagePanel.clear();
        
        List controlStrategies = selected();
        if (controlStrategies.isEmpty()) {
            this.messagePanel.setMessage("Please select one or more Control Strategies");
        }
        else {

            this.viewControlStrategyViews = new ViewControlStrategyView[controlStrategies.size()]; 
            
            for (int i = 0; i < controlStrategies.size(); i++) {
                
                ControlStrategy controlStrategy = (ControlStrategy) controlStrategies.get(i);
                ViewControlStrategyView view = new ViewControlStrategyWindow(desktopManager, session, parentConsole);
                this.viewControlStrategyViews[i] = view;
                
                try {
                    presenter.doView(view, controlStrategy);
                } catch (EmfException e) {
                    if ( CommonDebugLevel.DEBUG_CMIMPORT) {
                        System.out.println(" === Exception 1 ===");
                        e.printStackTrace();
                    }
                    
                    messagePanel.setError("Exception occured: " + e.getMessage());
                } catch ( Exception e) {
                    if ( CommonDebugLevel.DEBUG_CMIMPORT) {
                        System.out.println(" === Exception 2 ===");
                        e.printStackTrace();
                    }
                    
                    messagePanel.setError("Exception occured: " + e.getMessage());
                }
            }
        }
        
    }
    
    protected void doRemove() throws EmfException {
        messagePanel.clear();
        ControlStrategy[] records = (ControlStrategy[])selected().toArray(new ControlStrategy[0]);

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }
        
        String title = "Confirm Deletion";
        String message = "For the selected strateg" + (records.length == 1 ? "y" : "ies") + ", "
                + "which output datasets should be removed?";
        JCheckBox deleteResults = new JCheckBox("Strategy results, messages, and summaries");
        JCheckBox deleteCntlInvs = new JCheckBox("Controlled inventories");
        Object[] contents = {message, deleteResults, deleteCntlInvs};
        int selection = JOptionPane.showConfirmDialog(parentConsole, contents, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.OK_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            int[] ids = new int[records.length];
            for (int i = 0; i < records.length; i++) {
                ids[i] = records[i].getId(); 
            }
            try {
                presenter.doRemove(ids, deleteResults.isSelected(), deleteCntlInvs.isSelected());
            } catch (EmfException ex) {
                throw ex;
            } finally {
                doRefresh();
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    private void compareControlStrategies() throws EmfException {
        List strategies = selected();

        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select at least one control strategy to compare");
            return;
        }

        int[] ids = new int[strategies.size()];
        
        for (int i = 0; i < strategies.size(); ++i) {
            ids[i] = ((ControlStrategy)strategies.get(i)).getId();
        }
        presenter.viewControlStrategyComparisonResult(ids, "");
    }
    
    private void summarizeControlStrategies() throws EmfException {
        List strategies = selected();
        
        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select at least one control strategy to summarize.");
            return;
        }
        
        int[] ids = new int[strategies.size()];
        
        for (int i = 0; i < strategies.size(); ++i) {
            ids[i] = ((ControlStrategy)strategies.get(i)).getId();
        }
        
        // ask user for output file
        File localFile = null;
        
        // reuse file chooser so previous selections are preserved
        if (chooser == null) {
            chooser = new JFileChooser();

            // set initial location to user's temp dir
            UserPreference preferences = new DefaultUserPreferences();
            String tempDir = preferences.localTempDir();
            if (tempDir == null || tempDir.isEmpty())
                tempDir = System.getProperty("java.io.tmpdir");
            File tempDirFile = new File(tempDir);
            chooser.setCurrentDirectory(tempDirFile);

            chooser.setSelectedFile(new File("strategy_summary.csv"));
        }

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        localFile = chooser.getSelectedFile();
        if (localFile.exists()) {
            String message = "The selected file already exists. Do you want to overwrite it?";
            ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
            if (!confirmDialog.confirm()) return;
        }
        
        presenter.summarizeControlStrategies(ids, localFile);
        messagePanel.setMessage("Saved control strategy summary file: " + localFile.getName());
    }
    
    private void exportStrategyDetailedResults() throws EmfException {
        List strategies = selected();
        
        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select at least one control strategy to export results.");
            return;
        }
        
        // prompt for file name prefix
        String prefix = JOptionPane.showInputDialog(parentConsole, 
                "Enter a prefix for the exported filenames (leave blank for no prefix).", 
                "Use filename prefix?",
                JOptionPane.PLAIN_MESSAGE);
        if (prefix == null) return; // user clicked Cancel
        
        presenter.exportControlStrategyResults(strategies, prefix);
        messagePanel.setMessage("Results export started. Check the Status window for updates.");
    }

    private void copySelectedStrategy() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List strategies = selected();
        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more control strategies.");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy element = (ControlStrategy) iter.next();
            
            try {
                presenter.doSaveCopiedStrategies(element.getId(), session.user());
//                ControlStrategy coppied = (ControlStrategy)DeepCopy.copy(element);
//                coppied.setName("Copy of " + element.getName());
//                presenter.doSaveCopiedStrategies(coppied, element.getName());
            } catch (Exception e) {
//                setCursor(Cursor.getDefaultCursor());
                messagePanel.setError(e.getMessage());
                error = true;
            }
        }
        if (!error) doRefresh();
        setCursor(Cursor.getDefaultCursor());
    }

    private List selected() {
        return table.selected();
    }

    private void createNewStrategy() {
        ControlStrategyView view = new ControlStrategyWindow(parentConsole, session, desktopManager);
        presenter.doNew(view);   
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }
    
    public void displayControlStrategyComparisonResult(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }
}
