package com.xored.maven.integrity;

public final class PathUtils {

	private PathUtils() {
	}

	public static String getCommonPath(String[] paths) {
		final String separator = "/";
		StringBuilder ret = new StringBuilder();
		String[][] folders = new String[paths.length][];
		for (int i = 0; i < paths.length; i++) {
			folders[i] = paths[i].split(separator);
		}
		for (int j = 0; j < folders[0].length; j++) {
			String thisFolder = folders[0][j];
			boolean allMatched = true;
			for (int i = 1; i < folders.length && allMatched; i++) {
				if (folders[i].length < j) {
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
}
