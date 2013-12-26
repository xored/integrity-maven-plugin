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

	@Parameter(property = "integrity.modules.includes")
	private String[] includes;

	@Parameter(property = "integrity.modules.excludes")
	private String[] excludes;

	private Set<String> modulePaths;
	private Set<String> missedPaths = new HashSet<String>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (null == includes || 0 == includes.length) {
			includes = new String[]{DEFAULT_INCLUDE};
		}
		modulePaths = getIncludedModulePaths();
		File root = getRoot(modulePaths);
		getLog().info("Project root: " + root);
		getLog().info("Recognized module paths: " + modulePaths);
		getLog().info("Searching for missed modules...");
		getLog().info("  includes: " + Arrays.toString(includes));
		getLog().info("  excludes: " + Arrays.toString(excludes));
		visitDir(root);
		if (!missedPaths.isEmpty()) {
			throw new MojoFailureException(null, "Some modules are missed", "" + missedPaths);
		}
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
