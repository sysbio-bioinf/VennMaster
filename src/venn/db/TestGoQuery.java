package venn.db;

import java.sql.Statement;

public class TestGoQuery 
{
	
	
	
	public static void main(String[] argv) 
    {
		// load data set
	  
        GoDB go = GoDB.getInstance();
        go.setConnectionParameters("macky", "godb", "godb", "godb");
        // go.setConnectionParameters("discover.nci.nih.gov", "GEEVS", "deploy", "selectonly");
        try {
            // create a Statement object to execute the query with
            Statement stmt = go.getStatement();

            if( stmt == null )
                return;
            long goID1, goID2;
            
            goID1 = 16265;
            goID2 = 5125;
            long dist = go.findMinDistanceToSharedParent(goID1, goID2);
            System.out.println("dist = "+dist);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        	go.close();
        }
    }
}
