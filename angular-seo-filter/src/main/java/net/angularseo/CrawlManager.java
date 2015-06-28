package net.angularseo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;

import javax.servlet.UnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlManager {
	private Logger logger = LoggerFactory.getLogger(CrawlManager.class);
	
	public final static String PROP_LAST_CACHED_TIME = "last_cached_time";
	
	private Properties cacheProperties;
	private String cacheFilePath;
	
	/**
	 * Schedule the crawl task to crawl the site periodically and generate the static page in the cache path
	 * 
	 * @param interval the unit is hour
	 * @param cachePath
	 * @throws UnavailableException
	 */
	public void schedule(int interval, String cachePath) throws UnavailableException {
		// Get cache path
		File cacheFolder = new File(cachePath);
		if (!cacheFolder.exists()) {
			boolean success = cacheFolder.mkdirs();
			if (!success) {
				throw new UnavailableException("Cannot schedule crawl task, cachePath not exists and failed to create it: " + cachePath);
			}
		}
		
		loadCacheProperties(cacheFolder);
		
		// Check the last cache time
		long nextTime = getNextTime(cacheFolder, interval);
		Timer timer = new Timer();
		logger.info("AngularSEO crawl task is scheduled on " + new Date(System.currentTimeMillis() + nextTime).toString());
        timer.schedule(new CrawlTask(this, interval), nextTime);
	}
	
	private long getNextTime(File cacheFolder, int interval) throws UnavailableException {
		String lastCacheTimeStr = cacheProperties.getProperty("last_cached_time", "0");
		long lastCacheTime = Long.parseLong(lastCacheTimeStr);
		
		long diff = System.currentTimeMillis() - lastCacheTime;
		if (diff >= interval * 3600L * 1000L) {
			return 0;
		}
		else {
			return interval * 3600L * 1000L - diff;
		}
	}
	
	private void loadCacheProperties(File cacheFolder) throws UnavailableException {
		if (cacheProperties != null) {
			return;
		}
		
		File f = new File(cacheFolder.getPath() + "/cache.properties");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				throw new UnavailableException("Cannot create file in cache path: " + f);
			}
		}
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			Properties p = new Properties();
			p.load(in);
			cacheProperties = p;
			cacheFilePath = f.getAbsolutePath();
		} catch (Exception e) {
			throw new UnavailableException("Cannot load cache.properties in cache path: " + e.getMessage());
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void updateCachedTime() {
		cacheProperties.setProperty(CrawlManager.PROP_LAST_CACHED_TIME, Long.toString(System.currentTimeMillis()));
		try {
			FileOutputStream out = new FileOutputStream(cacheFilePath);
			cacheProperties.store(out, "AngularSEO Cralwer");
		} catch (Exception e) {
			logger.warn("Update cache.properties failed: " + e.getMessage());
		}
	}
}
