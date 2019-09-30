package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.ManagedView;

public interface ModulePropertiesView extends ManagedView, ModuleDatasetsObserver, ModuleParametersObserver {

    void observe(ModulePropertiesPresenter presenter);
}
