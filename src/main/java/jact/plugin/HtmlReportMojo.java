package jact.plugin;

import jact.depUtils.ProjectDependencies;
import jact.depUtils.ProjectDependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static jact.core.HtmlAugmenter.createDependencyReports;
import static jact.core.HtmlAugmenter.extractReportAndMoveDirs;
import static jact.utils.CommandExecutor.copyJacocoCliJar;
import static jact.utils.CommandExecutor.executeJacocoCLI;


/**
 * JACT HTML Report:
 * Generates a complete code coverage report including all
 * dependencies along with their transitive dependencies.
 */
@Mojo(name = "html-report", defaultPhase = LifecyclePhase.INSTALL, threadSafe = false)
public class HtmlReportMojo extends AbstractReportMojo {

    @Override
    public void doExecute() throws MojoExecutionException {

        // Print out packages and their classes
        getLog().info("Packages in project:");
        for (Map.Entry<String, Set<String>> entry : getProjectPackagesAndClasses().entrySet()) {
            getLog().info("- " + entry.getKey());
            for (String className : entry.getValue()) {
                getLog().info("  - " + className);
            }
        }


        getLog().info("STARTING: JACT - Java Complete Coverage Tracker");
        getLog().info("JARNAME: " + getOutputJarName());
        //String outputDirectory = project.getBuild().getOutputDirectory();

        Map<String, ProjectDependency> projectDependenciesMap =
                ProjectDependencies.getAllProjectDependencies("./target/jact-report/", true, getDepFilterParam());


        // Execute JaCoCoCLI to create the report WITH dependencies
        getLog().info("Copying the `jacococli.jar` to the project.");
        try {
            copyJacocoCliJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        getLog().info("Creating the complete coverage report.");
        executeJacocoCLI(getOutputJarName(), true);

        getLog().info("Organizing the complete coverage report.");
        try {
            extractReportAndMoveDirs(projectDependenciesMap, getProjectPackagesAndClasses(), getLocalRepoPath(), getProjId());
            createDependencyReports(projectDependenciesMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLog().info("JACT: HTML Report Successfully Generated!");
    }
}