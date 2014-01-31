package com.xored.maven.integrity;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class PathUtils {

	private PathUtils() {
	}

	/**
	 * Returns platform-specific representation of the given paths.
	 *
	 * @param paths
	 * 	paths with / as separator.
	 * @return platform-specific representation of the given paths.
	 */
	public static String[] toNative(String... paths) {
		return replace("/", File.separator, paths);
	}

	public static String toNative(String path) {
		return toNative(new String[]{path})[0];
	}

	public static String[] toUnix(String... paths) {
		return replace(File.separator, "/", paths);
	}

	private static String[] replace(String src, String dst, String... paths) {
		List<String> ret = new ArrayList<String>();
		for (String path : paths) {
			if (src.equals(dst)) {
				ret.add(path);
			} else {
				ret.add(path.replace(src, dst));
			}
		}
		return ret.toArray(new String[0]);
	}

	/**
	 * Returns the longest parent path of the given paths.
	 *
	 * @param paths
	 * 	paths
	 * @return the longest parent path of the given paths. Never <tt>null</tt>.
	 */
	public static String getCommonPath(String[] paths) {
		final String separator = File.separator;
		StringBuilder ret = new StringBuilder();
		String[][] folders = new String[paths.length][];
		for (int i = 0; i < paths.length; i++) {
			folders[i] = paths[i].split(Pattern.quote(separator));
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

	/**
	 * Returns the given absolute path relative to the given root directory. The logic is similar to {@link
	 * URI#relativize(URI)}.
	 *
	 * @param root
	 * 	root directory
	 * @param path
	 * 	absolute path
	 * @return the given absolute path relative to the given root directory. Never <tt>null</tt>.
	 */
	public static String relativizePath(File root, String path) {
		return relativizePaths(root, Arrays.asList(path)).iterator().next();
	}

	public static Set<String> relativizePaths(File root, Iterable<String> paths) {
		Set<String> ret = new HashSet<String>();
		final String separator = File.separator;
		final URI baseUri = root.toURI();
		for (String path : paths) {
			String rel = toNative(baseUri.relativize(new File(path).toURI()).getPath());
			if (rel.endsWith(separator)) {
				rel = rel.substring(0, rel.length() - separator.length());
			}
			ret.add(rel);
		}
		return ret;
	}

}
