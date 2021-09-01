package br.com.metrics.domain.metrics.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.messages.utils.Logger;

public class LOCCalculator {

	private static Logger log = Logger.get();

	public int calculate(InputStream sourceCode) {

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sourceCode));
			int lines = 0;

			String line = null;
			do {
				line = reader.readLine();
				if (line != null && !empty(line))
					lines++;
			} while (line != null);
			reader.close();

			return lines;
		} catch (IOException e) {
			log.e(this, ToStringBuilder.reflectionToString(e));
			return 0;
		}
	}

	private boolean empty(String line) {
		String result = line.replace("\t", "").replace(" ", "").trim();
		return result.isEmpty();
	}

}
