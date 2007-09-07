package venn.db;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.sql.Statement;
import java.util.List;

import junit.framework.Assert;


/*
http://www.godatabase.org/dev/index.html

Finding the shared parent of two nodes

SELECT tp.*,
p1.distance+p2.distance AS total_distance,
p1.distance AS d1,
p2.distance AS d2
FROM term AS t1
INNER JOIN graph_path AS p1 ON (t1.id=p1.term2_id)
INNER JOIN term AS tp       ON (p1.term1_id=tp.id)
INNER JOIN graph_path AS p2 ON (tp.id=p2.term1_id)
INNER JOIN term AS t2       ON (t2.id=p2.term2_id)
WHERE  t1.acc = 'GO:0008045'  AND  t2.acc = 'GO:0007474'
ORDER BY total_distance

Finding the distance between two nodes in the graph

SELECT 
  min(graph_path1.distance + graph_path2.distance) AS dist
FROM 
  graph_path AS graph_path1, graph_path AS graph_path2, 
  term AS t1, term AS t2
WHERE
  t1.acc = 'GO:0007165' and t2.acc = 'GO:0006629' and graph_path1.term2_id = t1.id
  and graph_path2.term2_id = t2.id and graph_path1.term1_id = graph_path2.term1_id

*/


public class HahnGoQuery 
{
	
  
	public static void main(String[] argv) 
    {
		// load data set
	  
        GoDB go = GoDB.getInstance();
        go.setConnectionParameters("macky", "godb", "godb", "godb");
        //go.setConnectionParameters("discover.nci.nih.gov", "GEEVS", "deploy", "deploy");
        try {
            // create a Statement object to execute the query with
            Statement stmt = go.getStatement();

            if( stmt == null )
                return;
            
            LineNumberReader is = new LineNumberReader(new FileReader("/home/mueller/stephan_hahn/db1.txt"));
            
            PrintStream os = new PrintStream(new File("/home/mueller/stephan_hahn/go_parents.txt"));
            
            boolean header = true;
            while( is.ready() )
            {
            	String line = is.readLine().trim();
            	if( header )
            	{
            		os.println(line+"\tParents");
            		header = false;
            		continue;
            	}
            	String[] v = line.split("\t");
            	Assert.assertEquals(v.length,3);
            	
            	String[] terms = v[2].split(" ");
            	
            	// lookup all terms
            	for( int i=0; i<terms.length; ++i )
            	{
            		if( !terms[i].startsWith("GO:") )
            			continue;
            		long goID = 0;
            		try {
            			goID = GoDB.parseGoID(terms[i]);
            		}
            		catch( Exception e )
            		{
            			System.err.println("cannot parse go ID at line : " + is.getLineNumber()+" entry #"+i);
            			e.printStackTrace();
            			throw e;
            		}
            		// find all parents
            		os.print(v[0]+"\t"+v[1]+"\t");
            		os.print(GoDB.formatGoID(goID)+"\t");
            		
            		List<Long> parents = go.findAllParents(goID);
            		for( Long id : parents )
            		{
            			os.print(GoDB.formatGoID(id)+" ");
            		}
            		os.println();
            		
            	}
        		System.out.println("#"+is.getLineNumber());            	
            }
            
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        	go.close();
        }
    }
}
