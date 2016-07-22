package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ModuleType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class NewModuleTypePresenter {

    private NewModuleTypeView view;

    private EmfSession session;

    public NewModuleTypePresenter(EmfSession session, NewModuleTypeView view) {
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
            return service().getDatasetTypes();
        }
        catch (EmfException ex) {
            return new DatasetType[]{};
        }
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(ModuleType moduleType) throws EmfException {
        service().addModuleType(moduleType);
        closeView();
    }

    public void displayNewModuleTypeVersionDatasetView(NewModuleTypeVersionDatasetView view) {
        NewModuleTypeVersionDatasetPresenter presenter = new NewModuleTypeVersionDatasetPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    public void displayNewModuleTypeVersionParameterView(NewModuleTypeVersionParameterView view) {
        NewModuleTypeVersionParameterPresenter presenter = new NewModuleTypeVersionParameterPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

}
