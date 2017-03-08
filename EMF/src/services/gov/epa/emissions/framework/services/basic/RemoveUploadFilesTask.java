package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

@Component
public class RemoveUploadFilesTask {

    private Log log = LogFactory.getLog(RemoveUploadFilesTask.class);

    private HibernateSessionFactory sessionFactory;
    private int fileHoursToExpire = 12; //remove files older than 12 hours...

    public RemoveUploadFilesTask() {
        this.sessionFactory = HibernateSessionFactory.get();
    }

    @Scheduled(cron="0 0 0/12 * * ?")//kick off task at 12 AM and 12 PM
    public void removeUploadedFilesTask() {
        try {
            File tempDirectoryObj = new File(getTempDirectory() + File.separatorChar + "upload");

            //get user folders under main download folder
            String[] userFolders = tempDirectoryObj.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    //only get folders
                    return new File(dir, name).isDirectory();
                }
            });
            for (String userFolder : userFolders) {
                File userFolderObj = new File(tempDirectoryObj.getAbsolutePath() + File.separatorChar + userFolder);
                userFolderObj.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        //only get files, not directories (really which there shouldn't be any subdirs here)
                        File userFile = new File(dir, name);
                        if (userFile.isFile() && userFile.lastModified() <= (new Date().getTime() - fileHoursToExpire * 60 * 60 * 1000))
                            userFile.delete();
                        return true;
                    }
                });
            }


        } catch (Exception e) {
            //Important, suppress all errors so process doesn't kill anything...
            logError("Failed to remove file uploads", e);
        } finally {
            //
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private String getTempDirectory() {
        Session session = sessionFactory.getSession();
        String tempDirectory = "";
        try {
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null) {
                tempDirectory = eximTempDir.getValue();
            }
        } finally {
            session.close();
        }
        return tempDirectory;
    }

}
