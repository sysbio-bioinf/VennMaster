/*
 * VennMaster/geometry/IntersectionTreeNode.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

import java.util.BitSet;

/**
 * @author muellera
 */
public class IntersectionTreeNode
{
	public IVennObject vennObject;    //!< null means: the whole plane (Omega)
	public BitSet   path;		//!< which path leads to this subset (contains a one for each involved source set = right turn)
	public int      card,
                    setIndex;	//!< < 0 if this is an intersection set, otherwise it is a pointer to the original sets
    public double   area;
	
	public IntersectionTreeNode parent, 
								leftChild, 	// contains a child not including a polygon 
								rightChild; // contains a child intersecting a polygon
								
	public boolean copy;	// set true if this polygon is only copied (happens for the left nodes)
	public int 	nLeft, 		// number of left turns to this node 
				nRight; 	// number of right turns to this node (== number of merged polygons)
	
	public IntersectionTreeNode()
	{
		setIndex = -1;
        card = 0;
        area = 0.0;
	}
	
	private void clear()
	{
		vennObject = null;
	}
	
	public void invalidate()
	{
		if( leftChild != null )
			leftChild.invalidate();
		if( rightChild != null )
			rightChild.invalidate();
		clear();
	}
        
    

	/**
	 * @see IIntersectionTreeVisitor
	 */
	void accept(int level, IIntersectionTreeVisitor visitor)
	{
		visitor.visit(level, this);
		if( leftChild != null )
			leftChild.accept(level + 1, visitor);

		if( rightChild != null )
			rightChild.accept(level + 1, visitor);
	}
		
	public void showDebug()
	{
		if( nRight > 0 )
			showDebugInfo();
		if( leftChild != null )
			leftChild.showDebug();
		
		if( rightChild != null )
			rightChild.showDebug();
	}
	
	public void showDebugInfo()
	{
		StringBuffer info = new StringBuffer(); 

		if( setIndex >= 0 ) 
			info.append(" setIndex="+setIndex);

		if( copy )
		{
			System.out.println(info);
		}
		else
		{
			info.append(" P="+vennObject);
			System.err.println(info);
		}
	}
	
	public String pathToString()
	{
		int level = nLeft + nRight;
		if( level <= 0 || path == null )
			return "/";
		
		StringBuffer buf = new StringBuffer();
		buf.append("/");
		for( int i=0; i < level; ++i )
		{
			if( path.get(i) )
			{
				buf.append("R");
			}
			else
			{
				buf.append("L");
			}
		}
		return buf.toString();
	}
    
    
}
