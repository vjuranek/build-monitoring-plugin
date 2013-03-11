package org.jenkinsci.plugins.buildanalysis.monitor;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.PeriodicWork;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.Slave;

import java.util.Date;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.buildanalysis.dao.DAOFactory;
import org.jenkinsci.plugins.buildanalysis.dao.GlobalDAO;
import org.jenkinsci.plugins.buildanalysis.model.GlobalInfo;
import org.jenkinsci.plugins.buildanalysis.utils.MonitorUtils;

@Extension
public class GlobalMonitor extends PeriodicWork implements Monitor {

    private static final int PERIOD_MINUTES = 1;
    
    private GlobalDAO globalDao;
    private MonitorStatus status;
    
    public GlobalMonitor() {
        try {
            this.globalDao = MonitorUtils.getDaoFactory().getGlobalDAO();
            this.status = MonitorStatus.RUNNING;
        } catch(Exception e) {
            globalDao = null;
            this.status = MonitorStatus.FAILED;
        }
    }
    
    public long getRecurrencePeriod() {
        return PERIOD_MINUTES * 60 * 1000L;
    }
    
    protected void doRun() throws Exception {
        if(globalDao == null) {
            if(status == MonitorStatus.RUNNING) {
                LOGGER.warning("Disabling Global monitor, check other log recored for details");
                disable();
                status = MonitorStatus.FAILED;
            }
            return;
        }
        
        GlobalInfo globalInfo = new GlobalInfo(new Date(System.currentTimeMillis()));
        Jenkins j = Jenkins.getInstance();
        
        globalInfo.setNumberOfProjetcs(j.getItems().size());
        
        int builds = 0;
        for(Item i : j.getItems()) {
        	if(i instanceof AbstractProject<?,?>)
        		builds += ((AbstractProject<?,?>) i).getBuilds().size();
        }
        globalInfo.setNumberOfBuilds(builds);
        
        globalInfo.setNumberOfSlaves(j.getNodes().size());
        
        int offlineNodes = 0;
        int idleNodes = 0;
        for(Node n : j.getNodes()) {
        	if(((Slave)n).getComputer().isOffline())
        		offlineNodes++;
        	if(((Slave)n).getComputer().isIdle())
        		idleNodes++;
        }
        globalInfo.setNumberOfOfflineSlaves(offlineNodes);
        globalInfo.setNumberOfIdleSlaves(idleNodes);
        
        globalDao.create(globalInfo);
    }
    
    public void enable() {
        MonitorUtils.enable(this, all());
    }
    
    public void disable() {
        MonitorUtils.disable(this, all());
    }
    
    private static final Logger LOGGER = Logger.getLogger(DAOFactory.class.getName());

}

