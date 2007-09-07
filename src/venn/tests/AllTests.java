/*
 * Created on 13.06.2005
 *
 */
package venn.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author muellera
 *
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for venn.tests");
        //$JUnit-BEGIN$
        suite.addTest(venn.tests.db.AllTestsDb.suite());
        suite.addTest(venn.tests.utility.AllTestsUtility.suite());
        //$JUnit-END$
        return suite;
    }
}
