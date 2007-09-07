/*
 * VennMaster/geometry/PaintVisitor.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

import java.awt.*;

import venn.geometry.ITransformer;

/**
 * Paints the contents of an IntersectionTree (all polygons)
 * 
 * @author muellera
 */
public class PaintVisitor implements IIntersectionTreeVisitor
{
	private Graphics g;
	private ITransformer transformer; 
    
	public PaintVisitor( Graphics g, ITransformer transformer )
	{
		this.g = g;
		this.transformer = transformer; 
	}
		
	
	/* (non-Javadoc)
	 * @see geometry.IntersectionTreeVisitor#visit(int, geometry.IntersectionTreeNode)
	 */
	public void visit(int depth, IntersectionTreeNode node)
	{
		if( node == null || node.vennObject == null )
			return;
		if( node.copy )
			return;
        
        //System.out.println( node.path.toString() );
        //System.out.println( node.vennObject );
                

		// g2d.setStroke(new BasicStroke(1.0f+(float)node.nRight));
		// g.setPaintMode();
        
        node.vennObject.directPaint(g,transformer);
	}
}
