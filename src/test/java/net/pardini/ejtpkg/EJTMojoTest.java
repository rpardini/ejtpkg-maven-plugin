package net.pardini.ejtpkg;

import org.testng.annotations.Test;

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
    public void testExecute() throws Exception {
        EJTMojo ejtMojo = new EJTMojo();
        ejtMojo.execute();
    }
}
