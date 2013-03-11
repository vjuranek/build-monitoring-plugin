package org.jenkinsci.plugins.buildanalysis.monitor;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.listeners.RunListener;

import java.util.Date;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.buildanalysis.dao.BuildDAO;
import org.jenkinsci.plugins.buildanalysis.dao.DAOFactory;
import org.jenkinsci.plugins.buildanalysis.model.BuildInfo;
import org.jenkinsci.plugins.buildanalysis.utils.BuildUtils;
import org.jenkinsci.plugins.buildanalysis.utils.MonitorUtils;

@Extension
public class BuildListener extends RunListener<AbstractBuild<?,?>> implements Monitor {
	
    private BuildDAO buildDAO;
    
    public BuildListener() {
        try {
            this.buildDAO = MonitorUtils.getDaoFactory().getBuildDAO();
        } catch(Exception e) {
            this.buildDAO = null;
        }
    }
    
    public void onStarted(AbstractBuild<?,?> build, TaskListener listener) {
        if(this.buildDAO == null) {
            LOGGER.warning("Disabling Build listener monitor, check other log recored for details");
            disable();
            return;
        }
        System.out.println("Build monitor called!");
        BuildInfo buildInfo = new BuildInfo(BuildUtils.getJobName(build), build.number);
        buildInfo.setClassName(build.getParent().getClass().getName());
        buildInfo.setStartedTime(build.getTime());
        buildInfo.setJdkName(BuildUtils.getJdkName(build));
        buildInfo.setLabel(((AbstractProject<?,?>)build.getParent()).getAssignedLabelString());
        buildInfo.setBuildOn(BuildUtils.getBuildOn(build));
        buildInfo.setTriggerCauses(build.getCauses());
        buildInfo.setParameters(BuildUtils.getParameters(build));
        buildDAO.updateOnStarted(buildInfo);
    }
    
    public void onFinalized(AbstractBuild<?,?> build) {
        if(this.buildDAO == null) {
            LOGGER.warning("Disabling Build listener monitor, check other log recored for details");
            disable();
            return;
        }
        BuildInfo buildInfo = new BuildInfo(BuildUtils.getJobName(build), build.number);
        buildInfo.setFinishedTime(new Date(System.currentTimeMillis()));
        buildInfo.setResult(build.getResult());
        buildDAO.updateOnFinalized(buildInfo);
    }
    
    public void enable() {
        MonitorUtils.enable(this, all());
    }
    
    public void disable() {
        MonitorUtils.disable(this, all());
    }
    
    public static ExtensionList<BuildListener> getExtensionList() {
        return Jenkins.getInstance().getExtensionList(BuildListener.class);
    }

    private static final Logger LOGGER = Logger.getLogger(DAOFactory.class.getName());
}
