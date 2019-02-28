package gov.epa.emissions.framework.client.search;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSearchPresenter;
import gov.epa.emissions.framework.client.casemanagement.CaseSearchWindow;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;

public class AdvancedSearchWindow<F, M> extends ReusableInteralFrame implements AdvancedSearchView<F, M> {

    private AdvancedSearchPresenter<F,M> presenter;

    private MessagePanel messagePanel;

    private TextField name;

    private TextField desc;

    private ComboBox keyword;

    private ComboBox creatorsBox;

    private ComboBox dsTypesBox;

    private ComboBox projectsCombo;

    private TextField value;

    private TextField qaStep;
    private TextField qaStepArguments;

    private boolean selectSingleCase = true;
    private Case[] usedByCases = null;
    private int[] usedByCasesID = null;
    private TextField caseName;
    private JTextArea dataValueFilter;

    private String preText;

    private DatasetType[] allDSTypes;

    private DatasetType dsType;

    private EmfConsole parent;


    private JPanel mainPanel;

    private Keyword[] keywords;

    private User[] users;

    private Project[] projects;

    private boolean showUsersFilter;
    private boolean showDataTypesFilter;
    private boolean showNameFilter;
    private boolean showDescriptionFilter;
    private boolean showProjectsFilter;
    private boolean showKeywordsFilter;
    private boolean showCasesFilter;
    private boolean showControlProgramsFilter;
    private boolean showDatasetWhereFilter;
    private boolean showDatasetsFilter;
    private boolean showCaseNameFilter;
    private boolean showQAStepNameFilter;
    private boolean showQAStepArgumentsFilter;

    private ListSelectionType usersFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType dataTypesFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
//    private ListSelectionType showNameFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
//    private ListSelectionType showDescriptionFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType projectsFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType keywordsFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType casesFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType controlProgramsFilterSelectionType = ListSelectionType.SINGLE_SELECTION;
    private ListSelectionType datasetsFilterSelectionType = ListSelectionType.SINGLE_SELECTION;


    static final private Dimension frameDim = new Dimension(680, 560);

    public AdvancedSearchWindow(String title, EmfConsole parentConsole, DesktopManager desktopManager, DatasetType dsType) {
        super(title, frameDim, desktopManager);
        parent = parentConsole;
        this.dsType = dsType;
    }

    public void display() {
        this.mainPanel = createLayout();
        getContentPane().add(this.mainPanel);
        super.display();
        new PopulateSwingWorker(mainPanel, messagePanel).execute();
    }

    private class PopulateSwingWorker extends GenericSwingWorker<Void> {

            public PopulateSwingWorker(Container parentContainer, MessagePanel messagePanel) {
                super(parentContainer, messagePanel);
            }
            
            @Override
            public Void doInBackground() throws EmfException  {
                allDSTypes = presenter.getDSTypes();
                keywords = presenter.getKeywords();
                users = presenter.getUsers();
                projects = presenter.getProjects();
                return null;
            }
    
            @Override
            public void done() {
                try {
                    //finishes background processing...
                    get();
                    
                    //populate combo boxes
                    dsTypesBox.resetModel(allDSTypes);
                    keyword.resetModel(keywords);
                    creatorsBox.resetModel(users);
                    projectsCombo.resetModel(projects);
                    
                } catch (InterruptedException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getCause().getMessage());
                } finally {
                    super.finalize();            }
            }
        }
    
    private JPanel createLayout() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        try {
            panel.add(createSearchPanel(), BorderLayout.CENTER);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        panel.add(createControlPanel(), BorderLayout.SOUTH);
        panel.setPreferredSize(frameDim);

        return panel;
    }

    private Component createSearchPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGen = new SpringLayoutGenerator();

        Dimension dim = new Dimension(350, 60);

        dsTypesBox = new ComboBox("Select one", new DatasetType[] {});
        dsTypesBox.setPreferredSize(dim);
        dsTypesBox.setSelectedItem(this.dsType);

        keyword = new ComboBox("Select one", new Keyword[] {});
        keyword.setPreferredSize(dim);
         
        creatorsBox = new ComboBox("Select one", new User[] {});
        creatorsBox.setPreferredSize(dim);
        
        projectsCombo = new ComboBox("Select one", new Project[] {});
        projectsCombo.setPreferredSize(dim);
       
        name = new TextField("namefilter", 30);      
        desc = new TextField("descfilter", 30);
        value = new TextField("keyvalue", 30);
        value.setToolTipText("Please select a Keyword for this field to be valid.");
        qaStep = new TextField("qaStep", 30);
        qaStep.setToolTipText("QA step name contains.");
                
        if (preText != null)
            name.setText(preText);
//        creatorsBox.setSelectedItem(presenter.getUser());

        int filterCount = 0;
        if (showNameFilter) {
            layoutGen.addLabelWidgetPair("Name contains:", name, panel);
            ++filterCount;
        }
        if (showDescriptionFilter) {
            layoutGen.addLabelWidgetPair("Description contains:", desc, panel);
            ++filterCount;
        }
        if (showUsersFilter) {
            layoutGen.addLabelWidgetPair("Creator:", creatorsBox, panel);
            ++filterCount;
        }
        if (showDataTypesFilter) {
            layoutGen.addLabelWidgetPair("Dataset type:", dsTypesBox, panel);
            ++filterCount;
        }
        if (showKeywordsFilter) {
            layoutGen.addLabelWidgetPair("Keyword:", keyword, panel);
            ++filterCount;
        }
        if (showKeywordsFilter) {
            layoutGen.addLabelWidgetPair("Keyword value:", value, panel);
            ++filterCount;
        }
        if (showQAStepNameFilter) {
            layoutGen.addLabelWidgetPair("QA name contains:", qaStep, panel);
            ++filterCount;
        }
        if (showQAStepArgumentsFilter) {
            layoutGen.addLabelWidgetPair("Search QA arguments:", datasetPanel(), panel);
            ++filterCount;
        }
        if (showProjectsFilter) {
            layoutGen.addLabelWidgetPair("Project:", projectsCombo, panel);
            ++filterCount;
        }
        if (showCasesFilter) {
            layoutGen.addLabelWidgetPair("Used by Case Inputs:", casePanel(), panel);
            ++filterCount;
        }
        if (showDatasetWhereFilter) {
            layoutGen.addLabelWidgetPair("Data Value Filter:", dataValueFilterPanel(dim), panel);
            ++filterCount;
        }

        // Lay out the panel.
        layoutGen.makeCompactGrid(panel, filterCount, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel datasetPanel() {

        qaStepArguments = new TextField("qaStepArguments", 30);
        qaStepArguments.setToolTipText("QA step argument contains.");
        
        Button selectButton = new AddButton("Find Dataset", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5, 0));
         
        invPanel.add(qaStepArguments, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }
    
    private JPanel casePanel() {

        caseName = new TextField("caseName", 30);
        caseName.setToolTipText("Choose a case that uses the dataset as an input");
        
        Button findCaseButton = new AddButton("Find Case", findCaseAction());
        findCaseButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5, 0));
         
        invPanel.add(caseName, BorderLayout.LINE_START);
        invPanel.add(findCaseButton);
        return invPanel;
    }
    
    private JPanel dataValueFilterPanel(Dimension dim) {

        dataValueFilter = new JTextArea();
        dataValueFilter.setToolTipText("Set data value filter in free SQL format e.g. FIPS='37001' and SCC like '102005%'");

        dataValueFilter.setWrapStyleWord(true);
        dataValueFilter.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane( dataValueFilter);
        scrollPane.setMinimumSize(new Dimension(100,50));
        scrollPane.setPreferredSize(dim);
        
        JPanel p = new JPanel(new BorderLayout(5, 0));
         
        p.add(scrollPane, BorderLayout.LINE_START);
        return p;
    }
    
    private Action findCaseAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                findCaseWindow();
            }
        };
    }

    protected Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                datasetSelectionWindow();
            }
        };
    }
    
    protected void findCaseWindow() {
        
        try {
            
            CaseSearchWindow view = new CaseSearchWindow( parent);

            CaseSearchPresenter caseSearchPresenter = new CaseSearchPresenter(view, presenter.getSession());
            
            caseSearchPresenter.display(null, selectSingleCase);
            if (view.shouldCreate())
                setCases(caseSearchPresenter.getCases());

        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    private void setCases(Case[] cases) {
        usedByCases = cases;
        if (usedByCases == null || usedByCases.length == 0) {
            return;
        }
        if (usedByCases != null && usedByCases.length > 0) {
            caseName.setText(usedByCases[0].getName());
            if ( selectSingleCase) {
                usedByCasesID = new int[usedByCases.length];
                for (int i=0; i<usedByCases.length; i++) {
                    usedByCasesID[i]=usedByCases[i].getId();
                }
            } else {
                usedByCasesID = new int[1];
                usedByCasesID[0]=usedByCases[0].getId();
            }
        }
    }
    
    protected void datasetSelectionWindow() {
        
        try {
            
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parent);
            InputDatasetSelectionPresenter selectDatasetPresenter = new InputDatasetSelectionPresenter(view, presenter.getSession(), allDSTypes);
            
            selectDatasetPresenter.display(null, true);
            if (view.shouldCreate())
                setDatasets(selectDatasetPresenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    private void setDatasets(EmfDataset[] datasets) {
        if (datasets == null || datasets.length == 0) {
            return;
        }
        if (datasets != null || datasets.length > 0) 
            qaStepArguments.setText(datasets[0].getName());
    }
    

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        Button okButton = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                            messagePanel.clear();
                            if (!checkFields())
                                return;
                            //perform search in a SwingWorker
                            new UnconditionalSearchSwingWorker(mainPanel, messagePanel).execute();
            }
        });
        okButton.setToolTipText("Search similar dataset(s)");
        panel.add(okButton);

        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                windowClosing();
            }
        });
        
        Button clearButoon = new Button("Clear", new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                clearFields();
            }
        });
        
        panel.add(clearButoon);
        
        panel.add(closeButton, BorderLayout.LINE_END);
        getRootPane().setDefaultButton(okButton);

        controlPanel.add(panel, BorderLayout.CENTER);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        return controlPanel;
    }

    private class UnconditionalSearchSwingWorker extends GenericSwingWorker<EmfDataset[]> {
    
            public UnconditionalSearchSwingWorker(Container parentContainer, MessagePanel messagePanel) {
                super(parentContainer, messagePanel);
            }
            
            @Override
            public EmfDataset[] doInBackground() throws EmfException {
                return search(getDataset(),qaStep.getText(), qaStepArguments.getText(), usedByCasesID, dataValueFilter.getText(), false);
            }
    
            @Override
            public void done() {
                try {
                    if (caseName.getText().trim().isEmpty())
                        usedByCasesID = new int[] {};
    
                    //make sure something didn't happen
                    EmfDataset[] datasets = get();
                    
                    if (datasets.length == 1 && datasets[0].getName().startsWith("Alert!!! More than 300 datasets selected.")) {
                        String msg = "Number of datasets > 300. Would you like to continue?";
                        int option = JOptionPane.showConfirmDialog(parent, msg, "Warning", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (option == JOptionPane.NO_OPTION)
                            return;
                        //kick off another swingworker for second step...
                        new ConditionalSearchSwingWorker(mainPanel, messagePanel).execute();
                        return;
                    }
    
                    DatasetType type = (DatasetType)dsTypesBox.getSelectedItem();
                    presenter.refreshViewOnSearch(datasets, getDstype(datasets, type), name.getText());
                    messagePanel.setMessage("Found " + datasets.length + " datasets meeting your search criteria");
                    
                } catch (InterruptedException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getCause().getMessage());
                } finally {
                    super.finalize();            }
            }
        }
        
        private class ConditionalSearchSwingWorker extends GenericSwingWorker<EmfDataset[]> {
    
            public ConditionalSearchSwingWorker(Container parentContainer, MessagePanel messagePanel) {
                super(parentContainer, messagePanel);
            }
            
            @Override
            public EmfDataset[] doInBackground() throws EmfException {
                return search(getDataset(),qaStep.getText(),qaStepArguments.getText(), usedByCasesID, dataValueFilter.getText(), true);
            }
    
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    EmfDataset[] datasets = get();
                    
                    DatasetType type = (DatasetType)dsTypesBox.getSelectedItem();
                    presenter.refreshViewOnSearch(datasets, getDstype(datasets, type), name.getText());
                    messagePanel.setMessage("Found " + datasets.length + " datasets meeting your search criteria");
    
                } catch (InterruptedException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
    //                setErrorMsg(e1.getCause().getMessage());
                } finally {
                    super.finalize();            }
            }
        }
      
    private void clearFields() {
        messagePanel.clear();
        name.setText("");
        desc.setText("");
        value.setText("");
        creatorsBox.setSelectedIndex(0);
        dsTypesBox.setSelectedIndex(0);
        keyword.setSelectedIndex(0);
        value.setText("");
        qaStep.setText("");
        qaStepArguments.setText("");
        projectsCombo.setSelectedIndex(0);
        dataValueFilter.setText("");
        caseName.setText("");
        usedByCasesID = new int[] {};
    }
    
    public void setNameText(String name) {
        preText = name;
    }

    private boolean checkFields() {
        if ((name.getText() == null || name.getText().trim().isEmpty())
                && (desc.getText() == null || desc.getText().trim().isEmpty())
                && creatorsBox.getSelectedItem() == null
                && dsTypesBox.getSelectedItem() == null
                && keyword.getSelectedItem() == null
                && (qaStep.getText() == null || qaStep.getText().trim().isEmpty())
                && (qaStepArguments.getText() == null || qaStepArguments.getText().trim().isEmpty())
                && projectsCombo.getSelectedItem() == null
                && (caseName.getText() == null || caseName.getText().trim().isEmpty())
                && (dataValueFilter.getText()==null || dataValueFilter.getText().trim().isEmpty())) {
            messagePanel.setError("Please specify at least one criteria for advanced search.");
            return false;
        }
        if ( dataValueFilter.getText()!=null 
                && !dataValueFilter.getText().trim().isEmpty()
                && dsTypesBox.getSelectedItem() == null) {
            messagePanel.setError("Please select a Dataset Type if you want to use Data Value Filter.");
            return false;
        }
        return true;
    }

    private EmfDataset getDataset() {
        EmfDataset ds = new EmfDataset();
        ds.setName(name.getText());
        ds.setDescription(desc.getText());
        ds.setDatasetType(getSelectedDSType());
        Keyword kw = (Keyword) keyword.getSelectedItem();

        if (kw != null) {
            KeyVal kv = new KeyVal(kw, value.getText());
            ds.setKeyVals(new KeyVal[] { kv });
        }    
        
        User user = (User) creatorsBox.getSelectedItem();
        if (user != null) {
            ds.setCreator(user.getUsername());
        }
        Project project = (Project) projectsCombo.getSelectedItem();

        if (project != null) {
            ds.setProject(project);
        }
        
        return ds;
    }
    
    private DatasetType getDstype(EmfDataset[] datasets, DatasetType type) {
        if (datasets.length == 0)
            return type;
        
        DatasetType temp = datasets[0].getDatasetType();
        
        for (EmfDataset ds : datasets)
            if (!ds.getDatasetType().equals(temp))
                return new DatasetType("All");
            
        return temp;
    }
    
    private DatasetType getSelectedDSType() {
        DatasetType selected = (DatasetType)dsTypesBox.getSelectedItem();
        
        if (selected != null && selected.getName().equals("All"))
            return null;

        return selected;
    }

    private EmfDataset[] search(EmfDataset dataset, String qaStep,String qaArgument, int[] usedByCasesId, String dataValueFilter, boolean unconditional) throws EmfException {
        return presenter.advSearch4Datasets(dataset, qaStep, qaArgument, usedByCasesID, dataValueFilter, unconditional);
    }

    @Override
    public void display(M[] modelItems) {

    }

        public void observe(AdvancedSearchPresenter<F,M> presenter) {
        this.presenter = presenter;
    }

    @Override
    public void refresh(M[] modelItems) {

    }

    @Override
    public String getNameContains() {
        return null;
    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void clearMessage() {

    }

    @Override
    public void notifyAdvancedSearchOff() {

    }

    @Override
    public void populate() {

    }

    @Override
    public void showUsers(boolean show, ListSelectionType selectionType) {
        this.showUsersFilter = show;
        this.usersFilterSelectionType = selectionType;
    }

    @Override
    public void showDataTypes(boolean show, ListSelectionType selectionType) {
        this.showDataTypesFilter = show;
        this.dataTypesFilterSelectionType = selectionType;
    }

    @Override
    public void showName(boolean show) {
        this.showNameFilter = show;
    }

    @Override
    public void showDescription(boolean show) {
        this.showDescriptionFilter = show;
    }

    @Override
    public void showProjects(boolean show, ListSelectionType selectionType) {
        this.showProjectsFilter = show;
        this.projectsFilterSelectionType = selectionType;
    }

    @Override
    public void showKeywords(boolean show, ListSelectionType selectionType) {
        this.showKeywordsFilter = show;
        this.keywordsFilterSelectionType = selectionType;
    }

    @Override
    public void showCases(boolean show, ListSelectionType selectionType) {
        this.showCasesFilter = show;
        this.casesFilterSelectionType = selectionType;
    }

    @Override
    public void showControlPrograms(boolean show, ListSelectionType selectionType) {
        this.showControlProgramsFilter = show;
        this.controlProgramsFilterSelectionType = selectionType;
    }

    public void windowClosing() {
        super.windowClosing();
        presenter.notifyAdvancedSearchOff();
    }

}
