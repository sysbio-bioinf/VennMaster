/*
 * Created on 06.07.2004
 *
 */
package venn.geometry;

import java.util.LinkedList;

import venn.diagram.IIntersectionTreeVisitor;
import venn.diagram.IntersectionTreeNode;

/**
 * @author muellera
 *
 */
public class ConsistencyChecker implements IIntersectionTreeVisitor
{
	LinkedList		result;
	
	public ConsistencyChecker()
	{
		result = new LinkedList();
	}
	
	/**
	 * 
	 * @return A list of IntersectionTreeNode's where the polygons don't intersect but the sets do ...
	 */
	public LinkedList getResult()
	{
		return result;
	}
	
	/* (non-Javadoc)
	 * @see geometry.IntersectionTreeVisitor#visit(int, geometry.IntersectionTreeNode)
	 */
	public void visit(int depth, IntersectionTreeNode node)
	{
		if( node == null )
			return;
		
		if( !node.copy && node.card > 0 && node.nRight > 0 )
		{ 
            if( node.vennObject == null || node.vennObject.isEmpty() ) 
                result.add(node);
		}
	}
}
