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

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class RobotFilter
 */
public class RobotFilter implements Filter {
	
	// The time to wait js dynamic page finish loading
	private static int WAIT_FOR_PAGE_LOAD = 5;
	// The interval the crawler re-crawl the site to generate the static page
	// the unit is hour
	private static int CACHE_TIMEOUT = 24;
	
	private Logger logger = LoggerFactory.getLogger(RobotFilter.class);
	
    /**
     * Default constructor. 
     */
    public RobotFilter() {
    }

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		String phanatomPath = fConfig.getInitParameter("phantomjs.binary.path");
		if (phanatomPath == null) {
			throw new UnavailableException("Please set the phantomjs.binary.path param for RobotFilter in web.xml");
		}
		File f = new File(phanatomPath);
		if (!f.exists()) {
			throw new UnavailableException("Cannot find phantomjs binary in given RobotFilter phantomjs.binary.path " + phanatomPath);
		}
		
		// Set the execute path of phantomjs 
    	System.setProperty("phantomjs.binary.path", phanatomPath);
    	
    	// Set the time to wait the page finish loading
    	String waitForPageLoad = fConfig.getInitParameter("waitForPageLoad");
    	if (waitForPageLoad != null) {
    		try {
				WAIT_FOR_PAGE_LOAD = Integer.parseInt(waitForPageLoad);
			} catch (NumberFormatException e) {
			}
    	}
    	
    	String robotUserAgent = fConfig.getInitParameter("robotUserAgents");
    	UserAgentUtil.initCustomizeAgents(robotUserAgent);
    	
    	// Get crawl setting
    	String cacheTimeout = fConfig.getInitParameter("cacheTimeout");
    	if (cacheTimeout != null) {
    		try {
    			CACHE_TIMEOUT = Integer.parseInt(cacheTimeout);
			} catch (NumberFormatException e) {
			}
    	}
    	
    	String cachePath = fConfig.getInitParameter("cachePath");
    	if (cachePath == null) {
    		throw new UnavailableException("Please set the cachePath param for RobotFilter in web.xml");
    	}
    	logger.info("RobotFilter started with {}, {}, {}, {}, {}", phanatomPath, WAIT_FOR_PAGE_LOAD, robotUserAgent, CACHE_TIMEOUT, cachePath);
    	
    	new CrawlManager().schedule(CACHE_TIMEOUT, cachePath);
	}
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String userAgent = req.getHeader("User-Agent");
		
		if (UserAgentUtil.isRobot(req) && isTextRequest(req)) {
			logger.info("Generating static html for search engine robot: " + userAgent);
			String html = getStaticHTML(req);
			response.getWriter().write(html);
		}
		else {
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Get static page by PhantomJSDriver
	 * 
	 * @param request
	 * @return
	 */
	private String getStaticHTML(HttpServletRequest request) {
    	// Create a new instance of the Firefox driver
        WebDriver driver = new PhantomJSDriver(DesiredCapabilities.phantomjs());
        // won't set it may cause invisible element issue
        driver.manage().window().setSize(new Dimension(1280, 1024));

        // get the static page
        String url = request.getRequestURL().toString();
        driver.get(url);

        // The angularjs page rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 5 seconds
        new WebDriverWait(driver, WAIT_FOR_PAGE_LOAD);
        
        String html = driver.getPageSource();
        logger.debug(html);
        
        //Close the browser
        driver.quit();
        
        return html;
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
		// do I missed sth.
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
