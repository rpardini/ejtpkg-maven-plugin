package net.pardini.ejtpkg;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * The Mojo.
 *
 * @goal ejt
 * @requiresProject
 */
public class EJTMojo extends AbstractMojo {

    /**
     * Directory the project.
     *
     * @parameter default-value="${basedir}"
     * @required
     */
    protected File projectBaseDir;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("Yeah! " + projectBaseDir);
    }
}
