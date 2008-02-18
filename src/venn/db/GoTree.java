package venn.db;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import venn.geometry.FileFormatException;

public class GoTree
{
	
	/*
	public class GoNode implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public final long parent;
		public LinkedList<GoNode> children;

		public GoNode( long parent )
		{
			this.parent = parent;
			children = new LinkedList<GoNode>();
		}
	}
*/
	
	public enum WhichNode {
		NONE, NODE1, NODE2
	}
	
	private Map<Long, Set<Long>> parentOf;
	
	

	public void clear()
	{
		if( parentOf != null )
		{
			parentOf.clear();
		}
	}
	
	public GoTree()
	{
		parentOf = new TreeMap<Long,Set<Long> >();
	}
	

	public Set<Long> getParentsOf(Long goID) {
		return parentOf.get(goID);
	}
	
	public void read(Reader reader)
	throws IOException, FileFormatException
	{
		clear();

		LineNumberReader in = new LineNumberReader(reader);

		String line;
		while( (line = in.readLine()) != null )
		{
			line = line.trim();

			if( line.length() > 0 )
			{
				String[] L = line.split("\t");

				if( L.length > 1 )
				{
					long[] ids = new long[L.length];
					for( int i=0; i<L.length; ++i )
					{
						if( L[i].startsWith("GO:") )
						{
							L[i] = L[i].substring(3);
						}
						ids[i] = Long.parseLong(L[i]);
					}
					insertNode(ids);
				}
			}
		}
	}

	private void insertNode( long[] ids ) 
	{
		if( ids.length > 0 )
		{
			Set<Long> L = null;
			final Long key = new Long(ids[0]);
			if( parentOf.containsKey( key ) )
			{
				L = parentOf.get( key );
			}
			else
			{
				L = new TreeSet<Long>();
			}
			for( int i=1; i<ids.length; ++i )
			{
				L.add( new Long(ids[i]) );
			}			
			parentOf.put( key, L );
		}
	}

	public void loadFromFile(String fileName)
	throws IOException, FileFormatException
	{
		read(new FileReader(fileName));
	}
	
	private boolean nodeExists( Long node )
	{
		return parentOf.containsKey(node);
	}
	
	private List< Set<Long> > getPathTo( Long node )
	{
		if( !nodeExists(node) )
		{
			return null; // illegal node
		}
		LinkedList< Set<Long> > L = new LinkedList< Set<Long> >();
		Set<Long> cutLine = new TreeSet<Long>();
		Set< Long > nnode = new TreeSet< Long >();
		nnode.add( node );
		L.add( nnode );		
		cutLine.add(node);
		while( !cutLine.isEmpty() ) 
		{
			Set<Long> parents = new TreeSet<Long>();
			for( Long P : cutLine )
			{
				Set<Long> par = parentOf.get(P);
				if( par != null )
				{
					parents.addAll( par );
				}
			}
			if( (parents == null) || parents.isEmpty() )
			{ // root reached
				break;
			}
			L.addLast( parents );
			cutLine = parents;
		}
		Set< Long > root = new TreeSet< Long >();
		root.add( new Long(0) );
		L.addLast( root ); // add root node
		
		return L;
	}
	
	private boolean setIntersects( Set<Long> S1, Set<Long> S2 )
	{
		for( Long x : S1 )
		{
			if( S2.contains( x ) )
			{
				return true;
			}
		}
		return false;
	}
	
	private int findMinDistanceToSharedParent( long goID1, long goID2 )
	{
		int d = Integer.MAX_VALUE;
		
		List<Set<Long>> path1 = getPathTo( goID1 );
		List<Set<Long>> path2 = getPathTo( goID2 );
		
		if( (path1==null) || (path2==null) )
		{
			return -1;
		}
		
		// find shared parent with minimum distance
		for( int i=0; i<path1.size(); ++i )
		{
			for( int j=0; j<path2.size(); ++j )
			{
				if( setIntersects(path1.get(i),path2.get(j)) )
				{
					int dd = Math.min(i,j);
					if( dd < d )
					{
						d = dd;
					}
				}
			}
		}
		
		return d;
	}

	public int findDistanceBetweenNodes( long goID1, long goID2 )
	{
		int d = Integer.MAX_VALUE;
		
		List<Set<Long>> path1 = getPathTo( goID1 );
		List<Set<Long>> path2 = getPathTo( goID2 );
		
		if( (path1==null) || (path2==null) )
		{
			return -1;
		}
		
		// find shared parent and add distances
		for( int i=0; i<path1.size(); ++i )
		{
			for( int j=0; j<path2.size(); ++j )
			{
				if( setIntersects(path1.get(i),path2.get(j)) )
				{
					int dd = i+j;
					if( dd < d )
					{
						d = dd;
					}
				}
			}
		}
		
		return d;
	}
	
	/**
	 * 
	 * @param goID1 node1
	 * @param goID2 node2
	 * @return WhichNode.NODE1 if goID1 is nearer to shared parent
	 * WhichNode.NODE2 if goID2 is nearer to shared parent
	 * shared parent can be node1 or node2
	 */
	public WhichNode findLessDistantNodeToSharedParent( long goID1, long goID2 )
	{
		int d = Integer.MAX_VALUE;
		WhichNode didx = WhichNode.NODE1;
		List<Set<Long>> path1 = getPathTo( goID1 );
		List<Set<Long>> path2 = getPathTo( goID2 );
		
		if( (path1==null) || (path2==null) )
		{
			return WhichNode.NONE;
		}
		
		// find shared parent with minimum distance
		for( int i=0; i<path1.size(); ++i )
		{
			for( int j=0; j<path2.size(); ++j )
			{
				if( setIntersects(path1.get(i),path2.get(j)) )
				{
					int dd = Math.min(i,j);
					if( dd < d )
					{
						d = dd;
						if(i<=j)
							didx=WhichNode.NODE1;
						else
							didx=WhichNode.NODE2;
					}
				}
			}
		}
		return didx;
	}		

	
	
	public static void main(String argv[])
	{
		GoTree go = new GoTree();
		try {
			go.loadFromFile("D:\\muellera\\Projects\\java\\VennMaster\\src\\venn\\data\\obo.out");
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		for( Long key : go.getTree().keySet() )
		{
			List<Long> parents = go.getTree().get(key);
			System.out.print(key+" :");
			for( Long p : parents )
			{
				System.out.print(" "+p);
			}
			System.out.println();
		}
		*/
		
		List< Set<Long> > path = go.getPathTo( new Long(5125) );
		for( Set<Long> L : path )
		{
			System.out.println();
			for( Long node : L )
			{
				System.out.print( node + " " );
			}
		}
		int dist = go.findMinDistanceToSharedParent(16265,5125);
		System.out.println("dist = "+dist);
	}
	
}
