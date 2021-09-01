package br.com.metrics.domain.metrics;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.metrics.domain.metrics.report.CK;
import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public class WMCTest extends BaseTest {

	private static CKReport report;

	@BeforeClass
	public static void setUp() {
		report = new CK().calculate(fixturesDir() + "/wmc");
	}

	@Test
	public void countAllBranchInstructions() {

		CKNumber a = report.getByClassName("wmc.CC1");
		Assert.assertEquals(4, a.getWmc());
		CKNumber b = report.getByClassName("wmc.CC2");
		Assert.assertEquals(5, b.getWmc());
	}
}
