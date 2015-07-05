package net.angularseo.crawler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.angularseo.AngularSEOConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlTask extends TimerTask {
	private Logger logger = LoggerFactory.getLogger(CrawlTask.class);
	
	/**
	 * The task to crawl static pages
	 * @param p cache properties
	 * @param interval unit is hour
	 */
	public CrawlTask() {
	}
	
	@Override
	public void run() {
		logger.info("AngularSEO crawl task starting...");

		// Crawl whole site
		CrawlTaskManager manager = CrawlTaskManager.getInstance();
		
		// clear cralwed urls
		manager.clearUrls();
		
		AngularSEOConfig config = AngularSEOConfig.getConfig();
		while (config.getRootURL() == null) {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}
		}
		
		manager.addCrawlRequest(new CrawlRequest(config.getRootURL(), config.crawlDepth - 1));
		
		// Check if all tasks finished
		while (!manager.isFinished()) {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}
		}
		
		// Update the time of this cache
		manager.updateCachedTime();
		
		// Schedule next crawl
		Timer timer = new Timer();
		long nextTime = AngularSEOConfig.getConfig().cacheTimeout * 3600 * 1000L;
        timer.schedule(new CrawlTask(), nextTime);
		logger.info("AngularSEO crawl task end, and next schedule time is " + new Date(System.currentTimeMillis() + nextTime).toString());
	}
}
