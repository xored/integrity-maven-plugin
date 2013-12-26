package com.xored.maven.integrity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mojo(name = "verify-modules", defaultPhase = LifecyclePhase.PROCESS_SOURCES, aggregator = true)
public class VerifyMojo extends AbstractMojo {

	private static final String DEFAULT_INCLUDE = "**/pom.xml";

	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(property = "integrity.modules.includes")
	private String[] includes;

	@Parameter(property = "integrity.modules.excludes")
	private String[] excludes;

	private Set<String> modulePaths;
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
		modulePaths = getIncludedModulePaths();
		File root = getRoot(modulePaths);
		getLog().info("Project root: " + root);
		getLog().info("Searching for missed modules...");
		getLog().info("  includes: " + Arrays.toString(includes));
		getLog().info("  excludes: " + Arrays.toString(excludes));
		visitDir(root);
		if (!missedPaths.isEmpty()) {
			throw new MojoFailureException(null, "Some modules are missing",
				finePrint(relativizePaths(root, missedPaths)));
		}
	}

	private static Set<String> relativizePaths(File root, Iterable<String> paths) {
		Set<String> ret = new HashSet<String>();
		URI baseUri = root.toURI();
		for (String path : paths) {
			ret.add(baseUri.relativize(new File(path).toURI()).getPath());
		}
		return ret;
	}

	private static String finePrint(Iterable<String> strings) {
		StringBuilder ret = new StringBuilder();
		for (String s : strings) {
			ret.append(s);
			ret.append("\n");
		}
		return ret.toString();
	}

	private static File getRoot(Set<String> paths) {
		return new File(PathUtils.getCommonPath(paths.toArray(new String[0])));
	}

	private Set<String> getIncludedModulePaths() {
		Set<String> ret = new HashSet<String>();
		for (MavenProject p : reactorProjects) {
			ret.add(p.getBasedir().getAbsolutePath());
		}
		return ret;
	}

	private void visitDir(File dir) {
		for (File k : getFilesToProcess(dir)) {
			process(k);
		}
	}

	private List<File> getFilesToProcess(File dir) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(dir);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		scanner.setCaseSensitive(false);
		scanner.scan();
		List<File> ret = new ArrayList<File>();
		for (String n : scanner.getIncludedFiles()) {
			File f = new File(dir, n);
			if (!f.isDirectory()) {
				f = f.getParentFile();
			}
			ret.add(f);
		}
		return ret;
	}

	private void process(File dir) {
		String p = dir.getAbsolutePath();
		if (!modulePaths.contains(p)) {
			missedPaths.add(p);
		}
	}

}
