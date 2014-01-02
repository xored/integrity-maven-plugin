package com.xored.maven.integrity;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PathUtils {

	private PathUtils() {
	}

	public static String getCommonPath(String[] paths) {
		final String separator = File.separator;
		StringBuilder ret = new StringBuilder();
		String[][] folders = new String[paths.length][];
		for (int i = 0; i < paths.length; i++) {
			folders[i] = paths[i].split(separator);
		}
		for (int j = 0; j < folders[0].length; j++) {
			String thisFolder = folders[0][j];
			boolean allMatched = true;
			for (int i = 1; i < folders.length && allMatched; i++) {
				if (folders[i].length <= j) {
					allMatched = false;
					break;
				}
				allMatched &= folders[i][j].equals(thisFolder); //check if it matched
			}
			if (allMatched) {
				ret.append(thisFolder);
				ret.append(separator);
			} else {
				break;
			}
		}
		if (0 == ret.length()) {
			ret.append(separator);
		} else if (ret.length() > separator.length()) {
			ret.delete(ret.length() - separator.length(), ret.length());
		}
		return ret.toString();
	}

	public static String relativizePath(File root, String path) {
		return relativizePaths(root, Arrays.asList(path)).iterator().next();
	}

	public static Set<String> relativizePaths(File root, Iterable<String> paths) {
		Set<String> ret = new HashSet<String>();
		final String separator = File.separator;
		final URI baseUri = root.toURI();
		for (String path : paths) {
			String rel = baseUri.relativize(new File(path).toURI()).getPath();
			if (rel.endsWith(separator)) {
				rel = rel.substring(0, rel.length() - separator.length());
			}
			ret.add(rel);
		}
		return ret;
	}

}
