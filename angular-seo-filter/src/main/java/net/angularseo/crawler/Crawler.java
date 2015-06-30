package net.angularseo.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.angularseo.AngularSEOConfig;
import net.angularseo.util.URLUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the static page of the given url
 * 
 * @author john.huang
 *
 */
public class Crawler implements Runnable {
	private Logger logger = LoggerFactory.getLogger(Crawler.class);
	
	private CrawlRequest request;
	
	public Crawler(CrawlRequest request) {
		this.request = request;
	}
	
	public void run() {
		logger.info("# Start crawl page {}", request.url);
		AngularSEOConfig config = AngularSEOConfig.getConfig();
		
		WebDriver driver = new PhantomJSDriver(DesiredCapabilities.phantomjs());
        driver.manage().window().setSize(new Dimension(1280, 2024));
        driver.manage().timeouts().implicitlyWait(config.waitForPageLoad, TimeUnit.SECONDS);
        
        driver.get(request.url);
        waitForLoad();
        
        if (request.depth > 0) {
        	driver = calculateSubLinks(driver);
        }
        
        String pageSource = driver.getPageSource();
        String currentUrl = driver.getCurrentUrl();

        // put page to cache folder
        String escapeUrl = URLUtils.escapeHashBang(currentUrl);
        CachePageManager.save(escapeUrl, pageSource, config.encoding);
        
        // The current url is matched with request url, the location may be changed by AngularJS script
        // need save both two
        if (!request.url.equals(currentUrl)) {
        	CachePageManager.save(URLUtils.escapeHashBang(request.url), pageSource, config.encoding);
        }
        
        driver.quit();
        logger.info("# Finished page {}", request.url);
	}
	
	private WebDriver calculateSubLinks(WebDriver driver) {
        // Get all <a href> links
        List<WebElement> eles = driver.findElements(By.tagName("a"));
        
        // Get all xpath of links
        String[] xpath = new String[eles.size()];
        for (int i = 0; i < eles.size(); i++) {
        	xpath[i] = getAbsoluteXPath((JavascriptExecutor)driver, eles.get(i));
        }
        
        logger.info("Got all {} link elements of {}", eles.size(), request.url);
        
        // Get true url of the url by clicking it
        HashMap<String, String> links = new HashMap<String, String>();
        for (int i = 0; i < xpath.length; i++) {
        	try {
				WebElement ele = driver.findElement(By.xpath(xpath[i]));
				String link = getTrueLink(driver, ele);
				logger.info("Got target url {} of link {}", link, i);
				links.put(xpath[i], link);
			} catch (Exception e) {
				// It may because of PhantomJS driver crash, so reload it and try again
				// PhantomJS driver usually crashed after about 20 cycles
				logger.info("PhantomJS driver crashed, restarting now, the following PhantomJS error log can be ingored");
				driver = restoreDriver(driver);
				
				try {
					WebElement ele = driver.findElement(By.xpath(xpath[i]));
					String link = getTrueLink(driver, ele);
					logger.info("Get target url {} of link {}", link, i);
					links.put(xpath[i], link);
				} catch (Exception e1) {
					logger.error("Link " + i + " cannot be found after back and reload, the link will be ignored: " + xpath[i]);
					continue;
				}
			}
        }
        
        // Set link with true url
        for (int i = 0; i < xpath.length; i++) {
        	try {
        		// It may because of PhantomJS driver crash, so reload it and try again
				// PhantomJS driver usually crashed after about 20 cycles
				WebElement ele = driver.findElement(By.xpath(xpath[i]));
				String url = links.get(xpath[i]);
				setAttribute((JavascriptExecutor)driver, ele, "href", URLUtils.escapeHashBang(url));
				
				// Crawl sub link pages
				CrawlTaskManager.getInstance().addCrawlRequest(new CrawlRequest(url, request.depth - 1));
			} catch (Exception e) {
				try {
					driver = restoreDriver(driver);
					WebElement ele = driver.findElement(By.xpath(xpath[i]));
					setAttribute((JavascriptExecutor)driver, ele, "href", URLUtils.escapeHashBang(links.get(xpath[i])));
				} catch (Exception e1) {
					continue;
				}
			}
        }
        
        return driver;
	}
	
	public String getTrueLink(WebDriver driver, WebElement ele) {
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", ele);
		String link = driver.getCurrentUrl();
		
		// back to original url
		driver.navigate().back();
		return link;
	}
	
	public void waitForLoad() {
        try {
			Thread.sleep(AngularSEOConfig.getConfig().waitForPageLoad * 1000L);
		} catch (InterruptedException e) {
		}
	}
	
	public WebDriver restoreDriver(WebDriver driver) {
		driver.quit();
		driver =  new PhantomJSDriver(DesiredCapabilities.phantomjs());
		driver.get(request.url);
		waitForLoad();
		return driver;
	}
	
    public static void setAttribute(JavascriptExecutor driver, WebElement element, String attName, String attValue) {
        driver.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", 
                element, attName, attValue);
    }
    
    /**
     * From http://stackoverflow.com/questions/11986349/webdriver-selenium-api-identifying-a-webelement-in-xpath
     * 
     * @param driver
     * @param element
     * @return
     */
//    private static String getElementXPath(JavascriptExecutor driver, WebElement element) {
//        return (String) driver.executeScript("gPt=function(c){if(c.id!==''){return'id(\"'+c.id+'\")'}if(c===document.body){return c.tagName}var a=0;var e=c.parentNode.childNodes;for(var b=0;b<e.length;b++){var d=e[b];if(d===c){return gPt(c.parentNode)+'/'+c.tagName+'['+(a+1)+']'}if(d.nodeType===1&&d.tagName===c.tagName){a++}}};return gPt(arguments[0]).toLowerCase();", element);
//    }
    
    /**
     * From https://code.google.com/p/selenium/issues/detail?id=5520
     * 
     * @param driver
     * @param element
     * @return
     */
    public static String getAbsoluteXPath(JavascriptExecutor driver, WebElement element) {
		return (String) driver.executeScript(
				"function absoluteXPath(element) {"+
						"var comp, comps = [];"+
						"var parent = null;"+
						"var xpath = '';"+
						"var getPos = function(element) {"+
						"var position = 1, curNode;"+
						"if (element.nodeType == Node.ATTRIBUTE_NODE) {"+
						"return null;"+
						"}"+
						"for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"+
						"if (curNode.nodeName == element.nodeName) {"+
						"++position;"+
						"}"+
						"}"+
						"return position;"+
						"};"+

					    "if (element instanceof Document) {"+
					    "return '/';"+
					    "}"+
					
					    "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"+
					    "comp = comps[comps.length] = {};"+
					    "switch (element.nodeType) {"+
					    "case Node.TEXT_NODE:"+
					    "comp.name = 'text()';"+
					    "break;"+
					    "case Node.ATTRIBUTE_NODE:"+
					    "comp.name = '@' + element.nodeName;"+
					    "break;"+
					    "case Node.PROCESSING_INSTRUCTION_NODE:"+
					    "comp.name = 'processing-instruction()';"+
					    "break;"+
					    "case Node.COMMENT_NODE:"+
					    "comp.name = 'comment()';"+
					    "break;"+
					    "case Node.ELEMENT_NODE:"+
					    "comp.name = element.nodeName;"+
					    "break;"+
					    "}"+
					    "comp.position = getPos(element);"+
					    "}"+
					
					    "for (var i = comps.length - 1; i >= 0; i--) {"+
					    "comp = comps[i];"+
					    "xpath += '/' + comp.name.toLowerCase();"+
					    "if (comp.position !== null) {"+
					    "xpath += '[' + comp.position + ']';"+
					    "}"+
					    "}"+
					
					    "return xpath;"+
					
					"} return absoluteXPath(arguments[0]);", 
			element);
	}
}
