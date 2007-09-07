/*
 * VennMaster/geometry/TreeQuery.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

import java.util.LinkedList;

import venn.geometry.FPoint;

import junit.framework.Assert;

/**
 * Finds nodes in an {@link venn.diagram.IntersectionTree}
 * @author muellera
 */
public class TreeQuery
{
	private IntersectionTree tree;
	
	/**
	 * 
	 * @param tree
     * 
	 */
	public TreeQuery(IntersectionTree tree)
	{
		this.tree = tree;
	}
	
	/**
	 * Finds the IntersectionTreeNode which contains the point q.
	 * Only the deepest node will be returned.
	 *  
	 * @param q
	 * @return null if no node was found.
	 */
	public IntersectionTreeNode findPolygonNode(FPoint q)
	{
		FindDeepest finder = new FindDeepest(q);
		
		tree.accept(finder);
		
		return finder.getValue();
	}
	
	/**
	 * 
	 * @param q
	 * @return A list of all found nodes.
	 */
	public LinkedList findAllNodes(FPoint q)
	{
		FindAll finder = new FindAll(q);
		
		tree.accept(finder);
		
		return finder.getValue();
	}
	
	private class FindDeepest implements IIntersectionTreeVisitor
	{
		private FPoint point;
		private int bestLevel;
		private IntersectionTreeNode bestNode;
		
		public FindDeepest(FPoint point)
		{
			this.point = point;
			bestLevel = 0;
			bestNode = null;
		}
		
		public IntersectionTreeNode getValue()
		{			
			return bestNode;
		}
		
		/* (non-Javadoc)
		 * @see geometry.IntersectionTreeVisitor#visit(int, geometry.IntersectionTreeNode)
		 */
		public void visit(int depth, IntersectionTreeNode node)
		{
			if( node.vennObject != null && !node.copy )
			{
				if( node.nRight >= bestLevel && node.card > 0 )
				{
                    if( node.vennObject == null || node.vennObject.isEmpty() )
                        return;
                    
					if( node.vennObject.contains(point) )
					{
						bestNode = node;
						bestLevel = node.nRight;
					}
				}
			}
		}
	}
	
	private class FindAll implements IIntersectionTreeVisitor
	{
		private FPoint point;
		private LinkedList result;
		
		public FindAll(FPoint point)
		{
			this.point = point;
			result = new LinkedList();
		}
		
		public LinkedList getValue()
		{			
			return result;
		}
		
		/* (non-Javadoc)
		 * @see geometry.IntersectionTreeVisitor#visit(int, geometry.IntersectionTreeNode)
		 */
		public void visit(int depth, IntersectionTreeNode node)
		{
			Assert.assertNotNull( node );
			if( node.vennObject != null && !node.copy )
			{
                if( node.vennObject == null || node.vennObject.isEmpty() )
                    return;
                
                if( node.vennObject.contains(point) )
                {
					result.add(node);
				}
			}
		}
	}
	
}
