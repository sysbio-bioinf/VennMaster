/*
 * Created on 13.06.2005
 *
 */
package venn.tests.db;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author muellera
 *
 */
public class AllTestsDb {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for venn.tests.db");
        //$JUnit-BEGIN$
        suite.addTestSuite(VennDataSplitterTest.class);
        //$JUnit-END$
        return suite;
    }
}
