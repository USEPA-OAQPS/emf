package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class RefreshSwingWorkerTasks extends SwingWorker<Object[], Void> {
    private Container parentContainer;
    private LightSwingWorkerPresenter presenter;
    private MessagePanel messagePanel;

    public RefreshSwingWorkerTasks(Container parentContainer, MessagePanel messagePanel, LightSwingWorkerPresenter presenter) {
        this.parentContainer = parentContainer;
        this.presenter = presenter;    
        this.messagePanel = messagePanel;
        this.parentContainer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(parentContainer, false);
        messagePanel.clear();
    }

    /*
     * Main task. Executed in background thread. Get objects.  
     * don't update gui here
     */
    @Override
    public Object[] doInBackground() throws EmfException  {
        if (presenter == null) return null; 
        return presenter.refreshProcessData();
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        try {
            if (presenter == null) return;
            //make sure something didn't happen
            presenter.refreshDisplay(get());
            
        } catch (InterruptedException e1) {
//            messagePanel.setError(e1.getMessage());
//            setErrorMsg(e1.getMessage());
        } catch (ExecutionException e1) {
//            messagePanel.setError(e1.getCause().getMessage());
//            setErrorMsg(e1.getCause().getMessage());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            e.printStackTrace();
        } finally {
//            this.parentContainer.setCursor(null); //turn off the wait cursor
//            this.parentContainer.
            ComponentUtility.enableComponents(parentContainer, true);
            this.parentContainer.setCursor(null); //turn off the wait cursor
        }
    }
}

 