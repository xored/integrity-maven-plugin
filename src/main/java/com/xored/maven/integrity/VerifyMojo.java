package com.xored.maven.integrity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.MatchPatterns;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mojo(name = "verify-modules", defaultPhase = LifecyclePhase.PROCESS_SOURCES, aggregator = true)
public class VerifyMojo extends AbstractMojo {

	private static final String DEFAULT_INCLUDE = "pom.xml";

	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(property = "integrity.modules.includes")
	private String[] includes;

	@Parameter(property = "integrity.modules.excludes")
	private String[] excludes;

	@Parameter(alias = "case-sensitive", property = "integrity.modules.case-sensitive", defaultValue = "false")
	private boolean isCaseSensitive;

	private String[] nativeIncludes;
	private String[] nativeExcludes;

	private String[] extendedIncludes;
	private String[] extendedExcludes;

	private MatchPatterns[] includesPatterns;
	private MatchPatterns[] extendedIncludesPatterns;

	private Set<String> relativeKnownPaths;

	private String[] targetDirExcludes;

	private Set<String> missedPaths = new HashSet<String>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO: is there a better way to execute this mojo only once?
		if (reactorProjects.isEmpty() || !project.getBasedir().equals(reactorProjects.get(0).getBasedir())) {
			return;
		}

		if (null == includes || 0 == includes.length) {
			includes = new String[]{DEFAULT_INCLUDE};
		}

		nativeIncludes = PathUtils.toNative(includes);
		nativeExcludes = PathUtils.toNative(excludes);

		extendedIncludes = extendPatterns(nativeIncludes, false);
		extendedExcludes = extendPatterns(nativeExcludes, false);
		includesPatterns = createMatchPatterns(nativeIncludes);
		extendedIncludesPatterns = createMatchPatterns(extendedIncludes);

		Set<String> knownPaths = getKnownPaths();
		File root = getRoot(knownPaths);
		relativeKnownPaths = PathUtils.relativizePaths(root, knownPaths);

		Set<String> knownTargetPaths = getKnownTargetPaths();
		targetDirExcludes = extendPatterns(PathUtils.relativizePaths(root, knownTargetPaths).toArray(new String[0]),
			true);

		getLog().info("Detected project root: " + root);
		getLog().info("Searching for missed modules...");
		if (getLog().isDebugEnabled()) {
			getLog().debug("Found known paths: \n" + finePrint(relativeKnownPaths));
			getLog().debug("  Includes: " + Arrays.toString(nativeIncludes));
			getLog().debug("  Excludes: " + Arrays.toString(nativeExcludes));
			getLog().debug("  Targets: " + Arrays.toString(targetDirExcludes));
		}
		visitDir(root);
		if (missedPaths.isEmpty()) {
			getLog().info("  no missed modules found");
		} else {
			String[] missed = PathUtils.toUnix(missedPaths.toArray(new String[0]));
			Arrays.sort(missed);
			throw new MojoFailureException(null, "Some modules are missing", finePrint(Arrays.asList(missed)));
		}
	}

	private static MatchPatterns[] createMatchPatterns(String[] patterns) {
		MatchPatterns[] ret = new MatchPatterns[patterns.length];
		for (int i = 0; i < patterns.length; ++i) {
			ret[i] = MatchPatterns.from(patterns[i]);
		}
		return ret;
	}

	private static String[] extendPatterns(String[] patterns, boolean tailOnly) throws MojoFailureException {
		if (null == patterns) {
			return null;
		}
		String[] ret = new String[patterns.length];
		for (int i = 0; i < patterns.length; ++i) {
			ret[i] = extendPattern(patterns[i], tailOnly);
		}
		return ret;
	}

	private static String extendPattern(String pattern, boolean tailOnly) throws MojoFailureException {
		final String separator = File.separator;
		final String prefix = "**" + separator;
		final String suffix = separator + "**";
		String ret = pattern;
		if (ret.startsWith(separator)) {
			throw new MojoFailureException("Includes/excludes pattern must not start with " + separator + ": " +
				pattern);
		}
		if (ret.endsWith(separator)) {
			throw new MojoFailureException("Includes/excludes pattern must not end with " + separator + ": " +
				pattern);
		}
		if (ret.startsWith(prefix)) {
			throw new MojoFailureException("Includes/excludes pattern must not start with " + prefix + ": " + pattern);
		}
		if (tailOnly) {
			return ret + suffix;
		} else {
			return prefix + ret + suffix;
		}
	}

	private static String finePrint(Iterable<String> strings) {
		StringBuilder ret = new StringBuilder();
		for (String s : strings) {
			ret.append("* ");
			ret.append(s);
			ret.append("\n");
		}
		return ret.toString();
	}

	private static File getRoot(Set<String> paths) {
		return new File(PathUtils.getCommonPath(paths.toArray(new String[0])));
	}

	private Set<String> getKnownPaths() {
		Set<String> ret = new HashSet<String>();
		for (MavenProject p : reactorProjects) {
			ret.add(PathUtils.toNative(p.getBasedir().getAbsolutePath()));
		}
		return ret;
	}

	private Set<String> getKnownTargetPaths() {
		Set<String> ret = new HashSet<String>();
		for (MavenProject p : reactorProjects) {
			ret.add(PathUtils.toNative(p.getBuild().getDirectory()));
		}
		return ret;
	}

	private void visitDir(File dir) {
		for (File k : getFilesToProcess(dir)) {
			process(dir, k);
		}
	}

	private List<File> getFilesToProcess(File dir) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(dir);
		scanner.setIncludes(extendedIncludes);

		List<String> allExcludes = new ArrayList<String>();
		allExcludes.addAll(Arrays.asList(extendedExcludes));
		allExcludes.addAll(Arrays.asList(targetDirExcludes));

		scanner.setExcludes(allExcludes.toArray(new String[0]));
		scanner.setCaseSensitive(isCaseSensitive);
		scanner.scan();
		List<File> ret = new ArrayList<File>();
		for (String n : scanner.getIncludedFiles()) {
			ret.add(new File(dir, n));
		}
		return ret;
	}

	private void process(File dir, File f) {
		String p = PathUtils.toNative(f.getAbsolutePath());
		String rel = PathUtils.relativizePath(dir, p);
		String module = rel;
		for (int i = 0; i < extendedIncludesPatterns.length; ++i) {
			MatchPatterns mp = extendedIncludesPatterns[i];
			if (mp.matches(rel, isCaseSensitive)) {
				module = getUnmatchedPrefix(rel, includesPatterns[i]);
				break;
			}
		}
		if (!relativeKnownPaths.contains(module)) {
			missedPaths.add(module);
		}
	}

	private String getUnmatchedPrefix(String path, MatchPatterns p) {
		final String separator = File.separator;
		int i = -1;
		do {
			i = path.indexOf(separator, i + 1);
			if (p.matches(path.substring(i + 1), isCaseSensitive)) {
				return -1 == i ? "" : path.substring(0, i);
			}
		} while (i > -1);
		return path;
	}

}
