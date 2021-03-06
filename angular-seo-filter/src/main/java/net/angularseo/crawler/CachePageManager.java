package net.angularseo.crawler;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.angularseo.AngularSEOConfig;
import net.angularseo.SEOFilter;
import net.angularseo.util.URLUtils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachePageManager {
	private static Logger logger = LoggerFactory.getLogger(SEOFilter.class);
	
	public static CachePageManager instance;
	private File cacheFolder;
	
	private CachePageManager(File cacheFolder) {
		this.cacheFolder = cacheFolder;
	}
	
	public synchronized static void init(File cacheFolder) {
		if (instance != null) {
			return;
		}
		
		instance = new CachePageManager(cacheFolder);
	}
	
	public static void save(String url, String pageSource, String encoding) {
		if (instance == null) {
			return;
		}
		
		url = URLUtils.escapeHashBang(url);
		String name = u2f(url);
		File f = new java.io.File(instance.cacheFolder, name);
		try {
			FileUtils.write(f, pageSource, encoding);
		} catch (IOException e) {
			logger.error("Save static page {} failed: {}", name, e.getMessage());
		}
	}
	
	public static String get(String url) {
		String pageSource = "<html><body></body></html>";
		if (instance == null) {
			return pageSource;
		}
		
		String name = u2f(url);
		File f = new java.io.File(instance.cacheFolder, name);
		try {
			pageSource = FileUtils.readFileToString(f, AngularSEOConfig.getConfig().encoding);
		} catch (IOException e) {
			logger.error("Load static page {} failed: {}", name, e.getMessage());
		}
		
		return pageSource;
	}
	
	/**
	 *  Url name to file name
	 */
	public static String u2f(String url) {
		url = url.replaceFirst("http://[^/]*/?", "/");
		String name = url.replaceAll("[\\\\/:\\*\\?<>|\"]", "_");
		name += ".html";
		return name;
	}
	
	public static void main(String[] args) {
		String str = "\\/:*?<>|\"";
		str = str.replaceAll("[\\\\/:\\*\\?<>|\"]", "_");
		System.out.println(str);
		
		String url = "http://www.abc.com/abc";
		url = url.replaceFirst("http://[^/]*/?", "/");
		System.out.println(url);
		
		url = "http://www.abc.com/abc?http://www.abc.com/";
		url = url.replaceFirst("(http://[^/]*).*", "$1");
		System.out.println(url);
		
		url = "http://www.abc.com/_23_21/a.html";
		String reg = "[\\_23|\\_21]+";
		System.out.println(url.matches(reg));
		
		url = "http://www.abc.com/_23/a.html";
		reg = "_23|_21";
		System.out.println(url.matches(reg));
		
		url = "http://www.abc.com/_21/a.html";
		reg = "_23|_21";
		System.out.println(url.matches(reg));
		
		url = "http://www.abc.com/#/a.html";
		reg = "_23|_21";
		System.out.println(url.matches(reg));
	}
}
