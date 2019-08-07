package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ModuleInternalDataset implements Serializable {

    private int id;

    private Module compositeModule;

    private String placeholderPath; // slash delimited list of submodule ids (at least one) and the placeholder name

    private String placeholderPathNames; // slash delimited list of submodule names (at least one) and the placeholder name

    private ModuleTypeVersionDataset moduleTypeVersionDataset;
    
    private boolean keep;
    
    private String datasetNamePattern;
    
    public void prepareForExport() {
        id = 0;
        moduleTypeVersionDataset = null;
    }
    
    public void prepareForImport() throws EmfException {
        String placeholderPathPieces[] = placeholderPathNames.split(" / ");
        if (placeholderPathPieces.length > 2) {
            // TODO: handle nested composite module types
            throw new EmfException("Can not import nested composite modules");
        }
        String typePlaceholderName = placeholderPathPieces[placeholderPathPieces.length - 1];
        for (ModuleTypeVersionSubmodule submodule : compositeModule.getModuleTypeVersion().getModuleTypeVersionSubmodules().values()) {
            if (submodule.getName().equals(placeholderPathPieces[0])) {
                setModuleTypeVersionDataset(submodule.getModuleTypeVersion().getModuleTypeVersionDataset(typePlaceholderName));
                setPlaceholderPath(submodule.getId() + "/" + typePlaceholderName);
                break;
            }
        }
        if (moduleTypeVersionDataset == null) {
            throw new EmfException("Could not match internal dataset " + placeholderPathNames);
        }
    }

    public ModuleInternalDataset deepCopy(Module newCompositeModule) {
        ModuleInternalDataset newModuleInternalDataset = new ModuleInternalDataset();
        newModuleInternalDataset.setCompositeModule(newCompositeModule);
        newModuleInternalDataset.setPlaceholderPath(placeholderPath);
        newModuleInternalDataset.setPlaceholderPathNames(placeholderPathNames);
        newModuleInternalDataset.setModuleTypeVersionDataset(moduleTypeVersionDataset);
        newModuleInternalDataset.setKeep(keep);
        newModuleInternalDataset.setDatasetNamePattern(datasetNamePattern);
        return newModuleInternalDataset;
    }

    public static boolean isValidDatasetNamePattern(String datasetNamePattern, ModuleTypeVersion moduleTypeVersion, final StringBuilder error) {
        return ModuleDataset.isValidDatasetNamePattern(datasetNamePattern, moduleTypeVersion, error);
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        boolean needsDatasetNamePattern = keep;
        boolean hasDatasetNamePattern = (datasetNamePattern != null) && (datasetNamePattern.trim().length() > 0);
        if (needsDatasetNamePattern) {
            if (!hasDatasetNamePattern) {
                error.append(String.format("The dataset name pattern for internal placeholder '%s' has not been set.", placeholderPathNames));
                return false;
            } else if (!isValidDatasetNamePattern(datasetNamePattern, moduleTypeVersionDataset.getModuleTypeVersion(), error)) {
                error.insert(0, String.format("The dataset name pattern for internal placeholder '%s' is invalid: ", placeholderPathNames));
                return false;
            }
        }
        return true;
    }

    
    public boolean isSimpleDatasetName() {
        return ModuleDataset.isSimpleDatasetName(datasetNamePattern);
    }

    public EmfDataset getEmfDataset(DataService dataService) {
        try {
            HistoryInternalDataset historyInternalDataset = null;
            History lastHistory = compositeModule.lastHistory();
            if (lastHistory != null) {
                if (History.SUCCESS.equals(lastHistory.getResult())) {
                    Map<String, HistoryInternalDataset> historyInternalDatasets = lastHistory.getHistoryInternalDatasets();
                    if (historyInternalDatasets.containsKey(placeholderPath)) {
                        historyInternalDataset = historyInternalDatasets.get(placeholderPath);
                    }
                }
            }
            if ((historyInternalDataset != null) && (historyInternalDataset.getDatasetId() != null)) {
                return dataService.getDataset(historyInternalDataset.getDatasetId());
            }
            if (isSimpleDatasetName()) {
                EmfDataset emfDataset = dataService.getDataset(datasetNamePattern);
                if (emfDataset != null && emfDataset.getDatasetType().equals(moduleTypeVersionDataset.getDatasetType()))
                    return emfDataset;
            }
        } catch (EmfException ex) {
            // ignore exception
        }
        
        return null;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Module getCompositeModule() {
        return compositeModule;
    }

    public void setCompositeModule(Module compositeModule) {
        this.compositeModule = compositeModule;
    }

    public String getPlaceholderPath() {
        return placeholderPath;
    }

    public void setPlaceholderPath(String placeholderPath) {
        this.placeholderPath = placeholderPath;
    }

    public String getPlaceholderPathNames() {
        return placeholderPathNames;
    }

    public void setPlaceholderPathNames(String placeholderPathNames) {
        this.placeholderPathNames = placeholderPathNames;
    }

    public String getDatasetNamePattern() {
        return datasetNamePattern;
    }

    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        return moduleTypeVersionDataset;
    }

    public void setModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        this.moduleTypeVersionDataset = moduleTypeVersionDataset;
    }

    public boolean getKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public void setDatasetNamePattern(String datasetNamePattern) {
        this.datasetNamePattern = datasetNamePattern;
    }

    public String getQualifiedName() {
        return compositeModule.getName() + "/" + placeholderPath;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleInternalDataset && ((ModuleInternalDataset) other).getQualifiedName().equals(getQualifiedName()));
    }

    public int compareTo(ModuleInternalDataset o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
