package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

import java.util.Date;
import java.util.List;

public interface ControlStrategyService extends EMFService {

    ControlStrategy[] getControlStrategies() throws EmfException;

    ControlStrategy[] getControlStrategies(BasicSearchFilter searchFilter) throws EmfException;
    
    ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException;
    
    StrategyType[] getStrategyTypes() throws EmfException;
    
    StrategyResultType[] getOptionalStrategyResultTypes() throws EmfException;
    
    int addControlStrategy(ControlStrategy element) throws EmfException;
    
//    void removeControlStrategies(ControlStrategy[] elements, User user) throws EmfException;

    void removeControlStrategies(int[] ids, User user) throws EmfException;

    void removeControlStrategies(int[] ids, User user, boolean deleteResults, boolean deleteCntlInvs) throws EmfException;

    ControlStrategy obtainLocked(User owner, int id) throws EmfException;

//    void releaseLocked(ControlStrategy locked) throws EmfException;

    void releaseLocked(User user, int id) throws EmfException;

    ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException;
    
    ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException;
    
    void runStrategy (User user, int controlStrategyId) throws EmfException;

    // 2014-10-26: export directory is unused - leaving in for compatibility with older clients
    void summarizeStrategy(User user, int controlStrategyId, String exportDirectory, StrategyResultType strategyResultType) throws EmfException;

    List<ControlStrategy> getControlStrategiesByRunStatus(String runStatus) throws EmfException;
    
    void stopRunStrategy(int controlStrategyId) throws EmfException;

//    void createInventory(User user, ControlStrategy controlStrategy, ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException;

    void createInventories(User user, ControlStrategy controlStrategy, 
            ControlStrategyResult[] controlStrategyResults, String namePrefix) throws EmfException;
    
    String controlStrategyRunStatus(int id) throws EmfException;

    int isDuplicateName(String name) throws EmfException;

    int copyControlStrategy(int id, User creator) throws EmfException;

    ControlStrategy getById(int id) throws EmfException;

    void setControlStrategyRunStatusAndCompletionDate(int id, String runStatus, Date completionDate) throws EmfException;
    //StrategyType[] getEquaitonTypes();
    
    Long getControlStrategyRunningCount() throws EmfException;

    public String getDefaultExportDirectory() throws EmfException;

    public String getStrategyRunStatus(int id) throws EmfException;
    
    public String getCoSTSUs() throws EmfException;

    String getControlStrategyComparisonResult(int[] controlStrategyIds) throws EmfException;
    
    String getControlStrategySummary(int[] controlStrategyIds) throws EmfException;
    
    // StrategyGroup methods
    StrategyGroup[] getStrategyGroups() throws EmfException;

    StrategyGroup obtainLockedGroup(User owner, int id) throws EmfException;

    void releaseLockedGroup(User user, int id) throws EmfException;
    
    int addStrategyGroup(StrategyGroup element) throws EmfException;
    
    StrategyGroup updateStrategyGroupWithLock(StrategyGroup element) throws EmfException;

    int isDuplicateGroupName(String name) throws EmfException;

    void removeStrategyGroups(int[] ids, User user) throws EmfException;
    
}
