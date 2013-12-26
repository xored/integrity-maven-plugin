package com.xored.maven.integrity;

import org.junit.Assert;
import org.junit.Test;

public class PathUtilsTest {

	@Test
	public void testGetCommonPath() {
		Assert.assertEquals("/home", PathUtils.getCommonPath("/home/a", "/home/b"));
		Assert.assertEquals("/", PathUtils.getCommonPath("/hme/a", "/home/b"));
		Assert.assertEquals("/", PathUtils.getCommonPath("/", "/"));
		Assert.assertEquals("/hh", PathUtils.getCommonPath("/hh", "/hh"));
	}

}
