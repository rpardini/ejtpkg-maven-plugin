package net.pardini.ejtpkg;

import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 18/01/13
 * Time: 01:38
 * To change this template use File | Settings | File Templates.
 */
public class EJTMojoTest {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void testOutsideMaven() throws Exception {
        EJTMojo ejtMojo = new EJTMojo();
        ejtMojo.createEjtDir(new File("target"), new File("target/classes"), "testWebAppName");
    }
}
