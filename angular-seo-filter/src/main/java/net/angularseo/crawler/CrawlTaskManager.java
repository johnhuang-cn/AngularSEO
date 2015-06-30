package net.angularseo.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.UnavailableException;

import net.angularseo.AngularSEOConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlTaskManager {
	public final static String PROP_LAST_CACHED_TIME = "last_cached_time";
	private static CrawlTaskManager instance = new CrawlTaskManager();
	
	private Logger logger = LoggerFactory.getLogger(CrawlTaskManager.class);
	
	private Properties cacheProperties;
	private String cacheFilePath;
	private ThreadPoolExecutor executor;
	ArrayList<String> crawlUrls = new ArrayList<String>();
	
	private CrawlTaskManager() {
		 executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(AngularSEOConfig.getConfig().maxCrawlThread);
	}
	
	public static CrawlTaskManager getInstance() {
		return instance;
	}
	
	/**
	 * Schedule the crawl task to crawl the site periodically and generate the static page in the cache path
	 * 
	 * @param interval the unit is hour
	 * @param cachePath
	 * @throws UnavailableException
	 */
	public void schedule() throws UnavailableException {
		AngularSEOConfig config = AngularSEOConfig.getConfig();
		
		// Get cache path
		File cacheFolder = new File(config.cachePath);
		if (!cacheFolder.exists()) {
			boolean success = cacheFolder.mkdirs();
			if (!success) {
				throw new UnavailableException("Cannot schedule crawl task, cachePath not exists and failed to create it: " + config.cachePath);
			}
		}
		
		// Init cache manager
		CachePageManager.init(cacheFolder);
		
		loadCacheProperties(cacheFolder);
		
		// Check the last cache time
		long nextTime = getNextTime(cacheFolder, config.cacheTimeout);
		Timer timer = new Timer();
		logger.info("AngularSEO crawl task is scheduled on " + new Date(System.currentTimeMillis() + nextTime).toString());
        timer.schedule(new CrawlTask(), nextTime);
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
		cacheProperties.setProperty(CrawlTaskManager.PROP_LAST_CACHED_TIME, Long.toString(System.currentTimeMillis()));
		try {
			FileOutputStream out = new FileOutputStream(cacheFilePath);
			cacheProperties.store(out, "AngularSEO Cralwer");
		} catch (Exception e) {
			logger.warn("Update cache.properties failed: " + e.getMessage());
		}
	}
	
	public void addCrawlRequest(CrawlRequest req) {
		String rootUrl = AngularSEOConfig.getConfig().getRootURL();
		synchronized (crawlUrls) {
			if (crawlUrls.indexOf(req.url) < 0 && req.url.indexOf(rootUrl) == 0) { // avoid crawling out of site
				crawlUrls.add(req.url);
				executor.execute(new Crawler(req));
			}
		}
	}
	
	public void clearUrls() {
		synchronized (crawlUrls) {
			crawlUrls.clear();
		}
	}
	
	public boolean isFinished() {
		while (executor.getTaskCount() != executor.getCompletedTaskCount()) {
		    return false;
		}
		
		return true;
	}
}
