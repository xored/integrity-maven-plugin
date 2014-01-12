package com.xored.maven.integrity;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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

	@Test
	public void testRelativize() {
		assertRelative("b", "/a", "/a/b");
	}

	private static void assertRelative(String expected, String root, String absolute) {
		Assert.assertEquals(expected, PathUtils.relativizePath(new File(root), absolute));
	}

}
