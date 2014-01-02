package com.xored.maven.integrity;

import org.junit.Assert;
import org.junit.Test;

public class PathUtilsTest {

	@Test
	public void testGetCommonPath() {
		Assert.assertEquals("/home", getCommonPath("/home/a", "/home/b"));
		Assert.assertEquals("/", getCommonPath("/hme/a", "/home/b"));
		Assert.assertEquals("/", getCommonPath("/", "/"));
		Assert.assertEquals("/hh", getCommonPath("/hh", "/hh"));

		String pref = "/Users/dima/itest/module-integrity/module-integrity-maven-plugin";
		Assert.assertEquals(pref, getCommonPath(pref + "/target", pref));
	}

	private static String getCommonPath(String... paths) {
		return PathUtils.getCommonPath(paths);
	}

}
