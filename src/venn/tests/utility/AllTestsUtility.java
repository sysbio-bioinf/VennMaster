/*
 * Created on 13.06.2005
 *
 */
package venn.tests.utility;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author muellera
 *
 */
public class AllTestsUtility {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for venn.tests.utility");
        //$JUnit-BEGIN$
        suite.addTestSuite(SetUtilityTest.class);
        suite.addTestSuite(SetUtilityTest1.class);
        //$JUnit-END$
        return suite;
    }
}
