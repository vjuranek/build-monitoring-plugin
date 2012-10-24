package org.jenkinsci.plugins.buildanalysis;

import hudson.Plugin;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;

import org.jenkinsci.plugins.buildanalysis.BuildAnalysis.BuildAnalysisDescriptor;
import org.jenkinsci.plugins.buildanalysis.dao.BuildDAO;
import org.jenkinsci.plugins.buildanalysis.dao.DAOFactory;
import org.jenkinsci.plugins.buildanalysis.dao.DbConfig;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * So far dummy class which server static content
 * 
 * @author vjuranek
 *
 */
public class BuildAnalysisPlugin extends Plugin {
	
	public void doQuery(StaplerRequest req, StaplerResponse res) throws Exception {
        DbConfig dbConfig = ((BuildAnalysisDescriptor)Jenkins.getInstance().getDescriptor(BuildAnalysis.class)).getDbConfig();
        BuildDAO buildDAO = DAOFactory.getDAOFactory(dbConfig).getBuildDAO();
        buildDAO.getBuilds("test-matrix");
	}
	
	
	public JSONArray getTestSerie() {
		JSONArray ja = new JSONArray();
		ja.add(new int[] {0, 2});
		ja.add(new int[] {1, 6});
		ja.add(new int[] {2, 7});
		ja.add(new int[] {3, 10});
		return ja;
	}

}
