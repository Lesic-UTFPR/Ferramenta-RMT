package br.com.metrics.domain.metrics;

import java.io.File;
import java.io.IOException;

abstract class BaseTest {

	protected static String fixturesDir() {
		try {
			String cfgFile = new File(BaseTest.class.getResource("/").getPath() + "../../fixtures/").getCanonicalPath();
			System.out.println(cfgFile);
			return cfgFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
