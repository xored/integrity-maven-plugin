package com.xored.maven.integrity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "verify-modules", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VerifyMojo extends AbstractMojo {

	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("VerifyMojoReactorProjects:");
		for (MavenProject p : reactorProjects) {
			getLog().info("    " + p.getName());
		}
	}

}
