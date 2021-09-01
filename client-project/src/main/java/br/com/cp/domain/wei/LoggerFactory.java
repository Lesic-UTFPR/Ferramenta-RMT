package br.com.cp.domain.wei;

public class LoggerFactory {
	
	public Logger createLogger(char type) {
		
		if(type == 'D') {
			return new DatabaseLogger();
		} else if(type == 'F') {

			final Logger logger = new FileLogger();
			
			return logger;
		} else {
			return null;
		}
		
	}

}
