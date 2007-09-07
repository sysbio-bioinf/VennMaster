/*
 * VennMaster/geometry/OutputStreamVisitor.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

public class OutputStreamVisitor
	implements IIntersectionTreeVisitor 
{
	public void visit(int depth, IntersectionTreeNode node)
	{
        if( node.copy )
            return;
        
		StringBuffer buf = new StringBuffer();
		//for(int i=0;i<depth;++i)
		//	buf.append("   ");
        
        buf.append("card = "+node.card+"\n");
        buf.append("area = "+node.area+"\n");

        buf.append( node.pathToString() + "\n" );
        buf.append( "GROUPS " + node.path + "\n" );
      
		if( node.vennObject != null )
		{
			buf.append(node.vennObject.toString() );
		}
		else
		{
			buf.append("[]");
		}

		System.out.println(buf);		
	}
}
