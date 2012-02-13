/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.mojo;

import com.excilys.ebi.gatling.ant.GatlingTask;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.codehaus.plexus.util.StringUtils.join;

/**
 * Mojo to execute Gatling.
 *
 * @author <a href="mailto:nicolas.huray@gmail.com">Nicolas Huray</a>
 * @goal execute
 * @phase integration-test
 * @description Gatling Maven Plugin
 */
public class GatlingMojo extends AbstractMojo {

    public static final String[] DEFAULT_INCLUDES = {"*.txt", "*.scala"};

    /**
     * Runs simulation but does not generate reports.
     * By default false.
     *
     * @parameter expression="${gatling.noReports}" alias="nr" default-value="false"
     * @description Runs simulation but does not generate reports
     */
    protected boolean noReports;

    /**
     * Generates the reports for the simulation in this folder.
     *
     * @parameter expression="${gatling.reportsOnly}" alias="ro"
     * @description Generates the reports for the simulation in this folder
     */
    protected File reportsOnly;

    /**
     * Uses this file as the configuration file.
     *
     * @parameter expression="${gatling.configFile}" alias="cf" default-value="${basedir}/src/main/resources/gatling.conf"
     * @description Uses this file as the configuration file
     */
    protected File configFile;

    /**
     * Uses this folder to discover simulations that could be run
     *
     * @parameter expression="${gatling.simulationsFolder}" alias="sf" default-value="${basedir}/src/main/resources/simulations"
     * @description Uses this folder to discover simulations that could be run
     */
    protected File simulationsFolder;

    /**
     * Sets the list of include patterns to use in directory scan for simulations.
     * Relative to simulationsFolder.
     *
     * @parameter
     * @description Include patterns to use in directory scan for simulations
     */
    protected List<String> includes;

    /**
     * Sets the list of exclude patterns to use in directory scan for simulations.
     * Relative to simulationsFolder.
     *
     * @parameter
     * @description Exclude patterns to use in directory scan for simulations
     */
    protected List<String> excludes;

    /**
     * A comma-separated list of simulations to run.
     * This takes precedence over the includes / excludes parameters.
     *
     * @parameter expression="${gatling.simulations}" alias="s"
     * @description A comma-separated list of simulations to run
     */
    protected String simulations;

    /**
     * Uses this package to start the simulations
     *
     * @parameter expression="${gatling.simulationsPackage}" alias="sp"
     * @description Uses this package to start the simulations
     */
    protected String simulationsPackage;

    /**
     * Uses this folder as the folder where feeders are stored
     *
     * @parameter expression="${gatling.dataFolder}" alias="df" default-value="${basedir}/src/main/resources/data"
     * @description Uses this folder as the folder where feeders are stored
     */
    protected File dataFolder;

    /**
     * Uses this folder as the folder where request bodies are stored
     *
     * @parameter expression="${gatling.requestBodiesFolder}" alias="bf" default-value="${basedir}/src/main/resources/request-bodies"
     * @description Uses this folder as the folder where request bodies are stored
     */
    protected File requestBodiesFolder;

    /**
     * Uses this folder as folder for assets
     *
     * @parameter expression="${gatling.assetsFolder}" alias="af"
     * @description Uses this folder as folder for assets
     */
    protected File assetsFolder;

    /**
     * Uses this folder as the folder where results are stored
     *
     * @parameter expression="${gatling.resultsFolder}" alias="rf" default-value="${basedir}/target/gatling/results"
     * @description Uses this folder as the folder where results are stored
     */
    protected File resultsFolder;


    /**
     * Extra JVM arguments to pass when running Gatling.
     *
     * @parameter
     */
    protected List<String> jvmArgs;

    /**
     * Will cause the project build to look successful, rather than fail, even if there are Gatling test failures.
     * This can be useful on a continuous integration server, if your only option to be able to collect output files,
     * is if the project builds successfully.
     *
     * @parameter expression="${gatling.failOnError}"
     */
    protected boolean failOnError = true;


    /**
     * The Maven Project
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject mavenProject;

    /**
     * The plugin dependencies.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List<Artifact> pluginArtifacts;


    /**
     * Executes Gatling simulations.
     */
    public void execute() throws MojoExecutionException {
        // Prepare environment
        prepareEnvironment();

        GatlingTask gatling = gatling(gatlingArgs(), jvmArgs());
        try {
            gatling.execute();
        } catch (Exception e) {
            if (failOnError) {
                throw new MojoExecutionException("Gatling failed.", e);
            }
        }
    }

    protected void prepareEnvironment() throws MojoExecutionException {
        // Create  results directories
        resultsFolder.mkdirs();
    }


    public GatlingTask gatling(List<String> args, List<String> jvmArgs) throws MojoExecutionException {
        GatlingTask gatling = new GatlingTask();
        gatling.setProject(getProject());

        // Set Gatling Arguments
        for (String arg : args) {
            if (arg != null) {
                Commandline.Argument argument = gatling.createArg();
                argument.setValue(arg);
            }
        }

        // Set JVM Arguments
        for (String jvmArg : jvmArgs) {
            if (jvmArg != null) {
                Commandline.Argument argument = gatling.createJvmarg();
                argument.setValue(jvmArg);
            }
        }

        return gatling;
    }


    protected List<String> jvmArgs() {
        return (jvmArgs != null) ? jvmArgs : Collections.<String>emptyList();
    }

    protected List<String> gatlingArgs() throws MojoExecutionException {
        try {
            // Solves the simulations, if no simulation file is defined
            if (simulations == null) {
                simulations = resolveSimulations(simulationsFolder, includes, excludes);
            }

            if (simulations.length() == 0) {
                getLog().error("No simulations to run");
                throw new MojoFailureException("No simulations to run");
            }

            // Arguments
            List<String> args = new ArrayList<String>();
            args.addAll(asList(
                    "-cf", configFile.getCanonicalPath(),
                    "-df", dataFolder.getCanonicalPath(),
                    "-rf", resultsFolder.getCanonicalPath(),
                    "-bf", requestBodiesFolder.getCanonicalPath(),
                    "-sf", simulationsFolder.getCanonicalPath(),
                    "-s", simulations)
            );

            if (noReports) {
                args.add("-nr");
            }

            if (reportsOnly != null) {
                args.addAll(asList("-ro", reportsOnly.getCanonicalPath()));
            }

            if (assetsFolder != null) {
                args.addAll(asList("-af", assetsFolder.getCanonicalPath()));
            }

            if (simulationsPackage != null) {
                args.addAll(asList("-sp", simulationsPackage));
            }

            return args;
        } catch (Exception e) {
            throw new MojoExecutionException("Gatling failed.", e);
        }
    }

    /**
     * Resolve simulation files to execute from the simulation folder and includes/excludes.
     *
     * @return a comma separated String of simulation files.
     */
    protected String resolveSimulations(File simulationsFolder, List<String> includes, List<String> excludes) {
        DirectoryScanner scanner = new DirectoryScanner();

        // Set Base Directory
        scanner.setBasedir(simulationsFolder);

        // Resolve includes
        if (includes != null && !includes.isEmpty()) {
            scanner.setIncludes((String[]) includes.toArray(new String[includes.size()]));
        } else {
            scanner.setIncludes(DEFAULT_INCLUDES);
        }

        // Resolve excludes
        if (excludes != null && !excludes.isEmpty()) {
            scanner.setExcludes((String[]) excludes.toArray(new String[excludes.size()]));
        }

        // Resolve simulations to execute
        scanner.scan();

        return join(scanner.getIncludedFiles(), ",");
    }


    protected Project getProject() throws MojoExecutionException {
        Project project = new Project();
        project.setBaseDir(mavenProject.getBasedir());
        project.addBuildListener(new LogAdapter());
        try {
            Path classpath = new Path(project);
            append(classpath, pluginArtifacts);        // Add jars
            classpath.setPath(configFile.getParent()); // Set dirname of config file into the classpath
            getLog().debug("Gatling classpath : " + classpath);
            project.addReference("gatling.classpath", classpath);
            return project;
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Error resolving dependencies", e);
        }
    }


    protected void append(Path classPath, List<?> artifacts) throws DependencyResolutionRequiredException {
        List<String> list = new ArrayList<String>(artifacts.size());

        for (Object artifact : artifacts) {
            String path;
            if (artifact instanceof Artifact) {
                Artifact a = (Artifact) artifact;
                File file = a.getFile();
                if (file == null) {
                    throw new DependencyResolutionRequiredException(a);
                }
                path = file.getPath();
            } else {
                path = artifact.toString();
            }
            list.add(path);
        }

        Path p = new Path(classPath.getProject());
        p.setPath(join(list.iterator(), File.pathSeparator));
        classPath.append(p);
    }


    public class LogAdapter implements BuildListener {
        public void buildStarted(BuildEvent event) {
            log(event);
        }

        public void buildFinished(BuildEvent event) {
            log(event);
        }

        public void targetStarted(BuildEvent event) {
            log(event);
        }

        public void targetFinished(BuildEvent event) {
            log(event);
        }

        public void taskStarted(BuildEvent event) {
            log(event);
        }

        public void taskFinished(BuildEvent event) {
            log(event);
        }

        public void messageLogged(BuildEvent event) {
            log(event);
        }

        private void log(BuildEvent event) {
            int priority = event.getPriority();
            Log log = getLog();
            String message = event.getMessage();
            switch (priority) {
                case Project.MSG_ERR:
                    log.error(message);
                    break;

                case Project.MSG_WARN:
                    log.warn(message);
                    break;

                case Project.MSG_INFO:
                    log.info(message);
                    break;

                case Project.MSG_VERBOSE:
                    log.debug(message);
                    break;

                case Project.MSG_DEBUG:
                    log.debug(message);
                    break;

                default:
                    log.info(message);
                    break;
            }
        }
    }


}
