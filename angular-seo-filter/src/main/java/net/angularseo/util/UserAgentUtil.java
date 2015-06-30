package net.angularseo.util;

import javax.servlet.http.HttpServletRequest;

public class UserAgentUtil {
	static String DEFAULT_ROBOT_USER_AGENTS = "Googlebot|Mediapartners-Google|AdsBot-Google|bingbot|Baiduspider|yahooseeker";
	private static String[] ROBOT_USER_AGENTS = new String[]{};
	
	public static void initCustomizeAgents(String robotUserAgent) {
		String allAgents = DEFAULT_ROBOT_USER_AGENTS;
		if (robotUserAgent != null) {
			allAgents += robotUserAgent;
			DEFAULT_ROBOT_USER_AGENTS = allAgents;
		}
		ROBOT_USER_AGENTS = allAgents.split("\\|");
	}
	
	public static boolean isRobot(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null) {
			return false;
		}
		
		for (String key : ROBOT_USER_AGENTS) {
			if (userAgent.indexOf(key) >= 0) {
				return true;
			}
		}
		
		return false;
	}
	
//	/**
//	 * Refer to https://support.google.com/webmasters/answer/1061943?hl=en
//	 * 
//	 * @param userAgent
//	 * @return
//	 */
//	private static boolean isGoogleRobot(String userAgent) {
//		if (userAgent.indexOf("Googlebot") >= 0 || userAgent.indexOf("Googlebot") >= 0 || userAgent.indexOf("AdsBot-Google") >= 0) {
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Refer to http://useragentstring.com/pages/Bingbot/
//	 * 
//	 * @param userAgent
//	 * @return
//	 */
//	private static boolean isBingRobot(String userAgent) {
//		if (userAgent.indexOf("bingbot") >= 0) {
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Refer to http://useragentstring.com/pages/Baiduspider/
//	 * 
//	 * @param userAgent
//	 * @return
//	 */
//	private static boolean isBaiduRobot(String userAgent) {
//		if (userAgent.indexOf("Baiduspider") >= 0) {
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Refer to http://useragentstring.com/pages/YahooSeeker/
//	 * 
//	 * @param userAgent
//	 * @return
//	 */
//	private static boolean isYahooRobot(String userAgent) {
//		if (userAgent.indexOf("yahooseeker") >= 0) {
//			return true;
//		}
//		return false;
//	}
}
