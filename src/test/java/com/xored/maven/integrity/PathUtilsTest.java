package com.xored.maven.integrity;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class PathUtilsTest {

	@Test
	public void testGetCommonPath() {
		Assert.assertEquals(n("/home"), getCommonPath("/home/a", "/home/b"));
		Assert.assertEquals(n("/"), getCommonPath("/hme/a", "/home/b"));
		Assert.assertEquals(n("/"), getCommonPath("/", "/"));
		Assert.assertEquals(n("/hh"), getCommonPath("/hh", "/hh"));

		String pref = "/Users/dima/itest/module-integrity/module-integrity-maven-plugin";
		Assert.assertEquals(n(pref), getCommonPath(pref + "/target", pref));
	}

	private static String getCommonPath(String... paths) {
		return PathUtils.getCommonPath(n(paths));
	}

	@Test
	public void testRelativize() {
		assertRelative("b", "/a", "/a/b");
	}

	private static void assertRelative(String expected, String root, String absolute) {
		Assert.assertEquals(n(expected), PathUtils.relativizePath(new File(n(root)), n(absolute)));
	}

	private static String n(String path) {
		return n(new String[]{path})[0];
	}

	private static String[] n(String... paths) {
		return PathUtils.toNative(paths);
	}

}
