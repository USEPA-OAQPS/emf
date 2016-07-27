package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ModuleTypeVersionDatasetsTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionDatasetsTableData(Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets) {
        this.rows = createRows(moduleTypeVersionDatasets);
    }

    public String[] columns() {
        return new String[] { "Mode", "Name/Placeholder", "Dataset Type", "Description"};
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets) {
        List rows = new ArrayList();

        for (ModuleTypeVersionDataset element : moduleTypeVersionDatasets.values()) {
            Object[] values = { element.getMode(),
                                element.getPlaceholderName(),
                                element.getDatasetType().getName(),
                                getShortDescription(element) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(ModuleTypeVersionDataset dataset) {
        String description = dataset.getDescription();

        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}