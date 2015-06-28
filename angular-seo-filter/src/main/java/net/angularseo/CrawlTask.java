package net.angularseo;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlTask extends TimerTask {
	private Logger logger = LoggerFactory.getLogger(CrawlTask.class);
	
	private CrawlManager manager;
	private int interval;
	
	/**
	 * The task to crawl static pages
	 * @param p cache properties
	 * @param interval unit is hour
	 */
	public CrawlTask(CrawlManager manager, int interval) {
		this.manager = manager;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		logger.info("AngularSEO crawl task starting...");

		// TODO crawl whole site
		
		// update the time of this cache
		manager.updateCachedTime();
		
		// schedule next crawl
		Timer timer = new Timer();
		long nextTime = interval * 3600 * 1000L;
        timer.schedule(new CrawlTask(manager, interval), nextTime);
		logger.info("AngularSEO crawl task end, and next schedule time is " + new Date(System.currentTimeMillis() + nextTime).toString());
	}
}
