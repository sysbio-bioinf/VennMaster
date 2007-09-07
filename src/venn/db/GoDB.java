/**
 * Package: go
 */
package venn.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

/*
import java.io.IOException;
import junit.framework.Assert;
import venn.db.GOCategoryProperties;
import venn.db.GODataFilter;
import venn.db.GeneOntologyReaderModel;
import venn.db.IVennDataModel;
import venn.db.VennFilteredDataModel;
import venn.geometry.FileFormatException;
*/


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


public class GoDB 
{
    private Connection  conn;
    private Statement   stat;
    private String      db_host,
                        db,
                        db_user,
                        db_password;
    static private DecimalFormat goFormat = new DecimalFormat("0000000");
    
    private GoDB()
    {
        conn = null;
        stat = null;

        
        db_host		= "macky";
        db			= "godb";
        db_user		= "godb";
        db_password	= "godb";

        /*
        db_host		= "discover.nci.nih.gov";
        db			= "GEEVS";
        db_user		= "deploy";
        db_password	= "selectonly";
        */
    }
    
    
    
	public static long parseGoID(String go)
	{
		if( go.equalsIgnoreCase("all") )
			return 0;
		
		if( go.length() < 3 || !go.startsWith("GO:") )
            throw new IllegalArgumentException("Illegal go term ID : '"+go+"'");
        
        return Integer.parseInt( go.substring(3) );		
	}
	
	public static String formatGoID(long goID)
	{
		if( goID == 0 )
			return "all";	// root node
		return "GO:"+goFormat.format(goID);
	}
    
   
    
    public void setConnectionParameters(String host, String db, String db_user, String db_password)
    {
    	this.db_host = host;
    	this.db = db;
    	this.db_user = db_user;
    	this.db_password = db_password;
    }
    
    public void finalize()
    {
        close();
    }
    
    public Connection getConnection()
    {
        if( !isConnected() )
        {
            connect();
        }
        return conn;
    }
    
    public Statement getStatement() throws SQLException
    {       
        if( getConnection() == null )
            return null;
     
        if( stat == null )
        {
            stat = getConnection().createStatement();
        }
        return stat;
    }
    
    
    public boolean isConnected()
    {
        return (conn != null);
    }
    
    public boolean connect()
    {
        close();
        // load the driver into memory
        //Class.forName("org.relique.jdbc.csv.CsvDriver");
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // create a connection. The first command line parameter is assumed to
            //  be the directory in which the .csv files are held
            //Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + args[0]);
            //
            conn = null;
            // "autoReconnect=true"
            conn = DriverManager.getConnection(
                    "jdbc:mysql://"+db_host+"/"+db+"?"+"user="+db_user+"&password="+db_password);
            
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn != null;
    }
    
    public void ensureConnection()
    {
    	if( ! isConnected() )
    	{
    		connect();
    	}
    }
    
    public void close()
    {
        if( conn != null )
        {
            try {
                if( stat != null ) {
                    stat.close();
                }
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stat = null;
            conn = null;
        }
    }
    
    /*
     	+-----------------------+
		| term_type             |
		+-----------------------+
		| association_qualifier |
		| biological_process    |
		| cellular_component    |
		| gene_ontology         |
		| molecular_function    |
		| relationship          |
		| sequence              |
		| synonym_type          |
		| universal             |
		+-----------------------+

     */
    public String getTermType( long goID ) throws SQLException
    {	
    	ensureConnection();
    	Statement stmt = getStatement();
    	if( stmt == null )
    		return null;

    	DecimalFormat format = new DecimalFormat("0000000");
    	ResultSet results = null;
    	try {
    		results = stmt.executeQuery("SELECT * FROM term WHERE acc='GO:"+format.format(goID)+"'");

    		while (results.next()) 
    		{
    			return results.getString("term_type");
    		}
    		results.close();

    	} catch (SQLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    	return null;
    }
    
    public long termIDfromGO( long goID ) throws SQLException
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
            return -1;
        
        DecimalFormat format = new DecimalFormat("0000000");
        ResultSet results = null;
        try {
            results = stmt.executeQuery(
                        "SELECT * FROM term WHERE acc='GO:"+format.format(goID)+"'");
            
            while (results.next()) 
            {
                return results.getLong("ID");
            }
            results.close();
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                
        return -1;
    }
    

    
    /**
     * Finds the shared parent of two nodes
     * http://www.godatabase.org/dev/index.html
     * @param goID
     * @return goIDs of all parents
     * @throws SQLException 
     */
    public List<Long> findParents(long goID ) throws SQLException 
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
        	throw new IllegalStateException("Cannot get statement");
        
        ResultSet results = stmt.executeQuery(
            "SELECT parent.* FROM "+
            "term AS child, term2term, term AS parent " + 
            "WHERE child.acc='"+formatGoID(goID)+"' AND " +
            "parent.id = term2term.term1_id AND " +
            "child.id  = term2term.term2_id");
        
        // showResult( results );
        LinkedList<Long> list = new LinkedList<Long>();
        results.beforeFirst();
        while( results.next() )
        {
        	
            long id = parseGoID(results.getString("acc"));
            list.add(id);
        }
        
        return list;
    }
    
    
    /*
     * Recursively finds all parents
     */
    public List<Long> findAllParents(long goID) throws SQLException
    {
    	ensureConnection();
    	LinkedList<Long> result = new LinkedList<Long>();
    	TreeSet<Long> set = new TreeSet<Long>();
    	Queue<Long> Q = new LinkedList<Long>();
    	
    	Q.add(goID);
    	while( !Q.isEmpty() )
    	{
    		long id = Q.poll();
        	List<Long> tt = findParents(id);
        	// remove from tt all items from set
        	tt.removeAll(set);
        	Q.addAll(tt);
        	result.addAll(tt);
        	set.addAll(tt);
    	}
    	
    	return result;
    }
    
    /**
     * Finds the shared parent of two nodes
     * http://www.godatabase.org/dev/index.html
     * @param goID1
     * @param goID2
     * @return ID of the next parent
     * @throws SQLException 
     */
    public long findSharedParent( long goID1, long goID2 ) throws SQLException 
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
            return -1;
        
        DecimalFormat format = new DecimalFormat("0000000");
        
        ResultSet results = stmt.executeQuery(
            "SELECT DISTINCT tp.*, "+
            "p1.distance+p2.distance AS total_distance, "+
            "p1.distance AS d1, "+
            "p2.distance AS d2 "+
            "FROM term AS t1 "+
            "INNER JOIN graph_path AS p1 ON (t1.id=p1.term2_id) "+
            "INNER JOIN term AS tp       ON (p1.term1_id=tp.id) "+
            "INNER JOIN graph_path AS p2 ON (tp.id=p2.term1_id) "+
            "INNER JOIN term AS t2       ON (t2.id=p2.term2_id) "+
            "WHERE  t1.acc = 'GO:"+format.format(goID1)+
            "' AND  t2.acc = 'GO:"+format.format(goID2)+"' "+
            "ORDER BY total_distance");
        
        showResult( results );
        
        results.beforeFirst();
        while( results.next() )
        {
             String goID = results.getString("acc");
             if( goID.startsWith("GO:") )
            	 	return Integer.parseInt(goID.substring(3));
             else
            	 	return 0;
        }
        
        return -1;
    }
    
    public int findMinDistanceToSharedParent( long goID1, long goID2 ) throws SQLException 
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
            return -1;
        
        ResultSet results = stmt.executeQuery(	
            "SELECT DISTINCT tp.*, "+
            "p1.distance+p2.distance AS total_distance, "+
            "p1.distance AS d1, "+
            "p2.distance AS d2 "+
            "FROM term AS t1 "+
            "INNER JOIN graph_path AS p1 ON (t1.id=p1.term2_id) "+
            "INNER JOIN term AS tp       ON (p1.term1_id=tp.id) "+
            "INNER JOIN graph_path AS p2 ON (tp.id=p2.term1_id) "+
            "INNER JOIN term AS t2       ON (t2.id=p2.term2_id) "+
            "WHERE  t1.acc = '"+formatGoID(goID1)+
            "' AND  t2.acc = '"+formatGoID(goID2)+"' "+
            "ORDER BY total_distance");
        
        // showResult( results );
        results.beforeFirst();
        int minDist = Integer.MAX_VALUE;
        while( results.next() )
        {
             int d1 = Integer.parseInt(results.getString("d1")),
             	 d2 = Integer.parseInt(results.getString("d2"));
             minDist = Math.min(minDist, Math.min(d1,d2));
        }
        
        return minDist;
    }    
    /**
     * Searches the minimum distance (number of edges) between two nodes.
     * 
     * @param goID1
     * @param goID2
     * @return
     * @throws SQLException
     */
    public long findDistance( long goID1, long goID2 ) throws SQLException
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
            return -1;
        
        ResultSet results = stmt.executeQuery(
                "SELECT min(graph_path1.distance + graph_path2.distance) AS dist " +
                "FROM graph_path AS graph_path1, graph_path AS graph_path2, " + 
                "term AS t1, term AS t2 " +  
                "WHERE t1.acc = '"+formatGoID(goID1)+
                "' AND t2.acc = '"+formatGoID(goID2)+
                "' and graph_path1.term2_id = t1.id "+
                "and graph_path2.term2_id = t2.id and graph_path1.term1_id = graph_path2.term1_id");        
        
        showResult( results );
        
        results.beforeFirst();
        while( results.next() )
        {        
        	String dist = results.getString("dist");
        	return Integer.parseInt(dist);
        }
        return -1;
    }
    
    public long findMinDistance( long goID1, long goID2 ) throws SQLException
    {
    	ensureConnection();
        Statement stmt = getStatement();
        if( stmt == null )
            return -1;
        
        ResultSet results = stmt.executeQuery(
                "SELECT min(graph_path1.distance+graph_path2.distance) AS dist " +
                "FROM graph_path AS graph_path1, graph_path AS graph_path2, " + 
                "term AS t1, term AS t2 " +  
                "WHERE t1.acc = '"+formatGoID(goID1)+
                "' AND t2.acc = '"+formatGoID(goID2)+
                "' and graph_path1.term2_id = t1.id "+
                "and graph_path2.term2_id = t2.id and graph_path1.term1_id = graph_path2.term1_id");        
        
        showResult( results );
        
        results.beforeFirst();
        while( results.next() )
        {        
        	String dist = results.getString("dist");
        	return Integer.parseInt(dist);
        }
        return -1;
    }    
    
    /**
     * Is goID2 a child (of degree > 0) of goID1 ?
     * Returns the number of edges between goID1 and goID2.
     * 
     * @param goID1
     * @param goID2
     * @return A value >= 0 describing the distance between goID1 -> goID2
     * @throws SQLException
     */
    public int isDescendant( long goID1, long goID2 ) throws SQLException
    {        
    	ensureConnection();
        ResultSet results = getStatement().executeQuery(
                "SELECT MIN(graph_path.distance) " +
                "FROM graph_path, term AS t1, term AS t2 " +  
                "WHERE t1.acc = 'GO:"+formatGoID(goID1)+
                "' AND t2.acc = 'GO:"+formatGoID(goID2)+
                "' AND graph_path.term1_id = t1.id AND graph_path.term2_id = t2.id");        

        //results.beforeFirst();
        while( results.next() )
        {
            
            String str = results.getString(1);
            if( str == null )
                return -1;
            else
                return Integer.parseInt(str);
        }
        return -1;        
    }
    
    static protected void showResult( ResultSet results ) throws SQLException
    {
        if( results == null )
        {
            System.out.println("NULL\n");
            return;
        }
        
        ResultSetMetaData meta = results.getMetaData();
        
        System.out.print("# ");
        for (int i = 1; i <= meta.getColumnCount(); ++i) {
            System.out.print("'"+meta.getColumnName(i) + "' ");
        }
        System.out.println();
        while (results.next()) 
        {
            for (int i = 1; i <= meta.getColumnCount(); ++i) {
                System.out.print("'"+results.getString(i) + "' ");
            }
            System.out.println();

            // System.out.println("ID= " + results.getString("ID") + " NAME=
            // " + results.getString("NAME"));
        }        
        System.out.println();
    }
    
    
    /* this is a singleton class! */
    private static GoDB instance = null;
    
    static GoDB getInstance()
    {
    	if( instance == null )
    	{
    		instance = new GoDB();
    	}
    	return instance;
    }
}
