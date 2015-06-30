package net.angularseo.crawler;

public class CrawlRequest {
	public String url;
	public int depth;
	
	public CrawlRequest(String url, int depth) {
		this.url = url;
		this.depth = depth;
	}
}
