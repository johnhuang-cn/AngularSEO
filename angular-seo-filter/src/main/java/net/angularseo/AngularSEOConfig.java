package net.angularseo;

public class AngularSEOConfig {
	private final static AngularSEOConfig config = new AngularSEOConfig();
	
	// The time to wait js dynamic page finish loading
	public int waitForPageLoad = 5;
	
	// The interval the crawler re-crawl the site to generate the static page
	// the unit is hour
	public int cacheTimeout = 24;
	
	// The folder to save the static html
	public String cachePath;

	// Page encoding, default is "UTF-8"
	public String encoding = "UTF-8";
	
	// Crawl depth, default is 2, index page and its sub link pages
	public int crawlDepth = 2;
	
	// Root url of this site, http://domain.name
	private String rootURL;
	
	public int maxCrawlThread = 5;
	
	private AngularSEOConfig() {
	}
	
	public static AngularSEOConfig getConfig() {
		return config;
	}
	
	public synchronized void setRootURL(String rootURL) {
		this.rootURL = rootURL;
	}
	
	public synchronized String getRootURL() {
		return rootURL;
	}
}
