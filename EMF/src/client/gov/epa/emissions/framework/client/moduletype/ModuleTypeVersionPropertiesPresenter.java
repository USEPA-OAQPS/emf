package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;

public class ModuleTypeVersionPropertiesPresenter {

    private ModuleTypeVersionPropertiesView view;

    private EmfSession session;

    public ModuleTypeVersionPropertiesPresenter(EmfSession session, ModuleTypeVersionPropertiesView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    public DatasetType[] getDatasetTypes() {
        try {
            return session.dataCommonsService().getDatasetTypes();
        }
        catch (EmfException ex) {
            return new DatasetType[]{};
        }
    }

    private void closeView() {
        view.disposeView();
    }

    public ModuleType obtainLockedModuleType(int moduleTypeId) throws EmfException{
        return session.moduleService().obtainLockedModuleType(session.user(), moduleTypeId);
    }

    public ModuleType getModuleType(int id) throws EmfException{
        return session.moduleService().getModuleType(id);
    }

    public ModuleType releaseLockedModuleType(int moduleTypeId) {
        try {
            return session.moduleService().releaseLockedModuleType(session.user(), moduleTypeId);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    
    public ModuleType addModuleType(ModuleType moduleType) throws EmfException {
        return session.moduleService().addModuleType(moduleType);
    }

    public ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException {
        return session.moduleService().updateModuleTypeVersion(moduleTypeVersion, user);
    }

    public void displayModuleTypeVersionDatasetView(ModuleTypeVersionDatasetView view) {
        ModuleTypeVersionDatasetPresenter presenter = new ModuleTypeVersionDatasetPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    public void displayModuleTypeVersionParameterView(ModuleTypeVersionParameterView view) {
        ModuleTypeVersionParameterPresenter presenter = new ModuleTypeVersionParameterPresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionSubmoduleView(ModuleTypeVersionSubmoduleView view) {
        ModuleTypeVersionSubmodulePresenter presenter = new ModuleTypeVersionSubmodulePresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionDatasetConnectionView(ModuleTypeVersionDatasetConnectionView view) {
        ModuleTypeVersionDatasetConnectionPresenter presenter = new ModuleTypeVersionDatasetConnectionPresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionParameterConnectionView(ModuleTypeVersionParameterConnectionView view) {
        ModuleTypeVersionParameterConnectionPresenter presenter = new ModuleTypeVersionParameterConnectionPresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public Module obtainLockedModule(Module module) throws EmfException {
        return session.moduleService().obtainLockedModule(session.user(), module.getId());
    }
    
    public Module releaseLockedModule(Module module) throws EmfException {
        return session.moduleService().releaseLockedModule(session.user(), module.getId());
    }

    public Module updateModule(Module module) throws EmfException {
        return session.moduleService().updateModule(module);
    }
}
