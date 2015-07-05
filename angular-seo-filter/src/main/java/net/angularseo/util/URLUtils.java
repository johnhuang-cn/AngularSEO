package net.angularseo.util;

public class URLUtils {
	/**
	 * Restore the escaped url to Hashbang url
	 * Example:
	 * http://example.com/_23_21/a/b -> http://example.com/#!/a/b
	 * http://example.com/_23/a/b -> http://example.com/#/a/b
	 * 
	 * @param url
	 * @return
	 */
	public static String toHashBang(String url) {
		String hashBangUrl = url.replaceFirst("_23", "#");
		hashBangUrl = hashBangUrl.replaceFirst("_21", "!");
		return hashBangUrl;
	}
	
	/**
	 * Escape the Hashbang url
	 * example:
	 * http://example.com/#!/a/b -> http://example.com/_23_21/a/b
	 * http://example.com/#/a/b -> http://example.com/_23/a/b
	 * 
	 * @param url
	 * @return
	 */
	public static String escapeHashBang(String url) {
		String escapeUrl = url.replaceFirst("#", "_23");
        escapeUrl = escapeUrl.replaceFirst("!", "_21");
        return escapeUrl;
	}
	
	public static boolean isFromSearchEngine(String url) {
		return url.indexOf("_23") > 0 || url.indexOf("_21") > 0;
	}
}
