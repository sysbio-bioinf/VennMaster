/*
 * Created on 05.07.2004
 *
 */
package venn.diagram;

import java.util.*;
/**
 * Creates an array of node lists.
 * Returns only "active" nodes.
 * 
 * @author muellera
 *
 */
public class TreeTransformer 
implements IIntersectionTreeVisitor
{
	private ArrayList levels;
	
	public TreeTransformer()
	{
		levels = new ArrayList();
	}
	
	/**
	 * 
	 * @return An array list of LinkedLists with IntersectionTreeNodes.
	 * The 0th element of the array contains all single sets.
	 * The 1st element contains a list of all 2-intersections.
	 * The k-th element contains a list of all (k+1)-intersections.
	 */
	public ArrayList getResult()
	{
		return levels;
	}
	
	/* (non-Javadoc)
	 * @see geometry.IntersectionTreeVisitor#visit(int, geometry.IntersectionTreeNode)
	 */
	public void visit(int depth, IntersectionTreeNode node) 
	{
		if( node == null )
			return;
		if( node.copy || node.vennObject == null || node.nRight < 1 )
			return;
		
		
		if( node.nRight > 1 && node.setIndex >= 0 )
		{ 	// we have more than one intersection and the set is fully contained in another set
			return;
		}
		
		while( levels.size() < node.nRight )
		{
			levels.add(new LinkedList());
		}
		LinkedList nodes = (LinkedList)levels.get(node.nRight-1);

		/*
		if( node.setIndex >= 0 )
		{
			// finds a node with the given setIndex
			Iterator iter = nodes.iterator();
			IntersectionTreeNode n;
			while( iter.hasNext() )
			{
				n = (IntersectionTreeNode)iter.next();
				if( n.setIndex == node.setIndex )
				{ // the given node contains more sets ...
					if( n.nRight < node.nRight )
					{
						iter.remove();
						nodes.add(node);
						return;
					}
					else
					{
						if( n.nRight > node.nRight )
						{
							// the given node involves less sets than in the list
							return;
						}
						else
						{
							assert false : "should never end here!";
						}
					}
				}
			}
		}
		*/
		nodes.add(node);
	}
}
