package br.com.metrics.domain.metrics;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.metrics.domain.metrics.report.CK;
import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public class LOCTest extends BaseTest {

	private static CKReport report;

	@BeforeClass
	public static void setUp() {
		report = new CK().calculate(fixturesDir() + "/cbo");
	}

	@Test
	public void countLinesIgnoringEmptyLines() {
		CKNumber a = report.getByClassName("cbo.Coupling1");
		Assert.assertEquals(11, a.getLoc());
	}

}
