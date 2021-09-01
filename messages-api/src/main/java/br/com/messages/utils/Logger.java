package br.com.messages.utils;

import java.util.Optional;

public class Logger {
	
	private static Logger instance = null;
	
	private java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
	
	private Logger() {
		
	}
	
	public <T> void i(T target, String pattern) {
		logger.info(String.format("%s - %s", target.getClass().getSimpleName(), pattern));
	}
	
	public <T> void i(T target, String pattern, Object... values) {
		logger.info(String.format("%s - %s", target.getClass().getSimpleName(), String.format(pattern, values)));
	}
	
	public <T> void w(T target, String pattern) {
		logger.warning(String.format("%s - %s", target.getClass().getSimpleName(), pattern));
	}
	
	public <T> void w(T target, String pattern, Object... values) {
		logger.warning(String.format("%s - %s", target.getClass().getSimpleName(), String.format(pattern, values)));
	}
	
	public <T> void e(T target, String pattern) {
		logger.severe(String.format("%s - %s", target.getClass().getSimpleName(), pattern));
	}
	
	public <T> void e(T target, String pattern, Object... values) {
		logger.severe(String.format("%s - %s", target.getClass().getSimpleName(), String.format(pattern, values)));
	}
	
	
	public static Logger get() {
		return Optional.ofNullable(instance).orElse( (instance = new Logger()) );
	}

}
