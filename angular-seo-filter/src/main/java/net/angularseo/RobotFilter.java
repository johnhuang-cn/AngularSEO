package net.angularseo;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import net.angularseo.crawler.CachePageManager;
import net.angularseo.crawler.CrawlTaskManager;
import net.angularseo.util.UserAgentUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class RobotFilter
 */
public class RobotFilter implements Filter {
	
	// The time to wait js dynamic page finish loading
	private static int Default_WAIT_FOR_PAGE_LOAD = 5;
	// The interval the crawler re-crawl the site to generate the static page
	// the unit is hour
	private static int DEFAULT_CACHE_TIMEOUT = 24;
	
	private Logger logger = LoggerFactory.getLogger(RobotFilter.class);
	
	private boolean isFirst = true; 
	
    /**
     * Default constructor. 
     */
    public RobotFilter() {
    }

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// Set the execute path of phantomjs 
		String phanatomPath = fConfig.getInitParameter("phantomjs.binary.path");
		if (phanatomPath == null) {
			throw new UnavailableException("Please set the phantomjs.binary.path param for RobotFilter in web.xml");
		}
		File f = new File(phanatomPath);
		if (!f.exists()) {
			throw new UnavailableException("Cannot find phantomjs binary in given RobotFilter phantomjs.binary.path " + phanatomPath);
		}
    	System.setProperty("phantomjs.binary.path", phanatomPath);
    	
    	// Set the time to wait the page finish loading
    	String waitForPageLoadStr = fConfig.getInitParameter("waitForPageLoad");
    	int waitForPageLoad = Default_WAIT_FOR_PAGE_LOAD;
    	if (waitForPageLoadStr != null) {
    		try {
    			waitForPageLoad = Integer.parseInt(waitForPageLoadStr);
			} catch (NumberFormatException e) {
			}
    	}
    	
    	// Set customize robot user agent
    	String robotUserAgent = fConfig.getInitParameter("robotUserAgents");
    	UserAgentUtil.initCustomizeAgents(robotUserAgent);
    	
    	// Get cache timeout, crawler will re-crawl when cache timeout
    	String cacheTimeoutStr = fConfig.getInitParameter("cacheTimeout");
    	int cacheTimeout = DEFAULT_CACHE_TIMEOUT;
    	if (cacheTimeoutStr != null) {
    		try {
    			cacheTimeout = Integer.parseInt(cacheTimeoutStr);
			} catch (NumberFormatException e) {
			}
    	}
    	
    	// Get cache path
    	String cachePath = fConfig.getInitParameter("cachePath");
    	if (cachePath == null) {
    		throw new UnavailableException("Please set the cachePath param for RobotFilter in web.xml");
    	}
    	
    	// Get the default encoding of site
    	String encoding = fConfig.getInitParameter("encoding");
    	if (encoding == null) {
    		encoding = "UTF-8";
    	}
    	
    	// Get crawl depth
    	String crawlDepthStr = fConfig.getInitParameter("crawlDepth");
    	int crawlDepth = 2;
    	if (crawlDepthStr != null) {
    		try {
    			crawlDepth = Integer.parseInt(crawlDepthStr);
			} catch (NumberFormatException e) {
			}
    	}
    	
    	logger.info("RobotFilter started with {}, {}, {}, {}, {}", phanatomPath, waitForPageLoad, robotUserAgent, cacheTimeout, cachePath);
    	
    	AngularSEOConfig config = AngularSEOConfig.getConfig();
    	config.cachePath = cachePath;
    	config.cacheTimeout = cacheTimeout;
    	config.waitForPageLoad = waitForPageLoad;
    	config.encoding = encoding;
    	config.crawlDepth = crawlDepth;
    	
    	CrawlTaskManager.getInstance().schedule();
	}
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		if (isFirst) {
			String rootUrl = req.getRequestURL().toString();
			rootUrl = rootUrl.replaceFirst("(http://[^/]*).*", "$1");
			AngularSEOConfig.getConfig().setRootURL(rootUrl);
			isFirst = false;
		}
		
		String userAgent = req.getHeader("User-Agent");
		logger.info(userAgent); // TODO: remove this tmp code
		if (UserAgentUtil.isRobot(req) && isTextRequest(req)) {
			logger.info("Search engine robot request: {}", userAgent);
			logger.info("Load static html for robot: " + req.getRequestURL());
			String html = CachePageManager.get(req.getRequestURL().toString());
			if (html == null) {
				chain.doFilter(request, response);
			}
			else {
				response.setCharacterEncoding(AngularSEOConfig.getConfig().encoding);
				response.getWriter().write(html);
			}
		}
		else {
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Check if the request is for html page as far as possible
	 */
	private boolean isTextRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int p = uri.lastIndexOf("/");
		// requst for site default page
		if (p < 0) {
			return true;
		}
		
		String file = uri.substring(p + 1);
		p = file.indexOf(".");
		// without extention, usually is a html request
		if (p < 0) {
			return true;
		}
		
		String ext = file.substring(p + 1);
		if ("html".equals(ext) || "htm".equals(ext) || "jsp".equals(ext)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}
}
