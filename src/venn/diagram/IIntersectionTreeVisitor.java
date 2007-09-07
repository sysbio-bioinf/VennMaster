/*
 * VennMaster/geometry/IntersectionTreeVisitor.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

/**
 * Visitor concept for the {@link venn.diagram.IntersectionTree} class.
 * @author muellera
 */
public interface IIntersectionTreeVisitor
{
	public void visit(int depth, IntersectionTreeNode node);

}
