package org.jenkinsci.plugins.buildanalysis.mongo;

import hudson.util.IOUtils;

import java.io.IOException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.NotImplementedException;
import org.jenkinsci.plugins.buildanalysis.dao.GlobalDAO;
import org.jenkinsci.plugins.buildanalysis.model.GlobalInfo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

public class MongoGlobalDAO implements GlobalDAO {
	
	private final DBCollection coll;
	
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_NUM_PROJECTS = "numberOfProjetcs";
	private static final String KEY_NUM_BUILDS = "numberOfBuilds";
	private static final String KEY_NUM_SLAVES = "numberOfSlaves";
	private static final String KEY_NUM_OFFLINE_SLAVES = "numberOfOfflineSlaves";
	private static final String KEY_NUM_IDLE_SLAVES = "numberOfIdleSlaves";
	
	public MongoGlobalDAO(DBCollection coll) {
		this.coll = coll;
	}
	
	public void create(GlobalInfo globalInfo) {
		coll.insert(getDbObject(globalInfo));
	}
	
	public void update(GlobalInfo globalInfo) {
    	throw new NotImplementedException();
    }
	
	public static BasicDBObject getDbObject(GlobalInfo globalInfo) {
		BasicDBObject doc = new BasicDBObject();
		doc.put(KEY_TIMESTAMP, globalInfo.getTimestamp());
		doc.put(KEY_NUM_PROJECTS, globalInfo.getNumberOfProjetcs());
		doc.put(KEY_NUM_BUILDS, globalInfo.getNumberOfBuilds());
		doc.put(KEY_NUM_SLAVES, globalInfo.getNumberOfSlaves());
		doc.put(KEY_NUM_OFFLINE_SLAVES, globalInfo.getNumberOfOfflineSlaves());
		doc.put(KEY_NUM_IDLE_SLAVES, globalInfo.getNumberOfIdleSlaves());
		return doc;
	}
	
	public JSONObject getSeries() {
		JSONArray slaves = new JSONArray();
		JSONArray offline = new JSONArray();
		JSONArray idle = new JSONArray();
		
    	MapReduceOutput mr = mapReduce();
    	for(DBObject o : mr.results()) {
    		System.out.println(o);
    		BasicDBObject value = (BasicDBObject)o.get("value");
    		String date = value.getString("date"); 
    		Double slavesAvg = (Double)value.get("slaves");
    		Double offlineAvg = (Double)value.get("offline");
    		Double idleAvg = (Double)value.get("idle");
    		
    		JSONArray slavesPoint = new JSONArray();
    		slavesPoint.add(date);
    		slavesPoint.add(slavesAvg);
    		slaves.add(slavesPoint);
    		
    		JSONArray offlinePoint = new JSONArray();
    		offlinePoint.add(date);
    		offlinePoint.add(offlineAvg);
    		offline.add(offlinePoint);
    		
    		JSONArray idlePoint = new JSONArray();
    		idlePoint.add(date);
    		idlePoint.add(idleAvg);
    		idle.add(idlePoint);
    		
    	}
    	
    	JSONObject jo = new JSONObject();
    	jo.put("slaves", slaves);
    	jo.put("offline", offline);
    	jo.put("idle", idle);
    	
		return jo;
	}

	private MapReduceOutput mapReduce() {
    	String map = "";
    	String reduce = "";
    	ClassLoader classLoader = getClass().getClassLoader();
    	try {
    		map = IOUtils.toString(classLoader.getResourceAsStream("js/MapReduce/mapTest.js"));
    		reduce = IOUtils.toString(classLoader.getResourceAsStream("js/MapReduce/reduceTest.js"));
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	MapReduceOutput out = coll.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, null);
    	return out;
    }
	
}
