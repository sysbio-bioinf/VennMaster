/*
 * Created on 24.05.2005
 *
 */
package venn.diagram;

import java.awt.Color;
import java.util.BitSet;

import junit.framework.Assert;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import venn.AllParameters;
/**
 * 
 * Creates a Venn representation of a single set.
 * 
 * @author muellera
 *
 */
public class VennObjectFactory implements IVennObjectFactory 
{
    public final static int VIEW_POLYGON = 0;
    // public final static int VIEW_CIRCLE = 1;
    // public final static int VIEW_RECTANGLE = 2;
    
    private int view;
    
    private int numEdges;
    private double areaFactor;
    
    public VennObjectFactory()
    {
        this(VIEW_POLYGON);
    }
    
    /**
     * Generates a new DiagramView factory.
     * 
     * @param view
     */
    public VennObjectFactory( int view )
    {
        numEdges 	= 10;
        areaFactor 	= 0.001;
        this.view = view;
    }
    
    public void setPolygonParameters( int numEdges, double areaFactor )
    {
        this.numEdges = numEdges;
        this.areaFactor = areaFactor;
    }

//    /**
//     *  
//     *  @return a new Venn object corresponding to the given element set. 
//     */
//    public IVennObject create(int gid, BitSet elements) 
//    {
//        IVennObject obj = null;
//        
//        switch( view )
//        {
//        	case VIEW_POLYGON:
//        	    obj = new VennPolygonObject(numEdges,areaFactor,elements); 
//                break;
//        	    
//        	default:
//        	    throw new IllegalStateException("wrong view type");
//        }
//        Assert.assertNotNull( obj ); 
//
//        Color color;
//        float r=((float)gid % 4.0f)/4.0f;
//        float g=(((float)gid+ 1.0f) %4.0f)/4.0f;
//        float b=(((float)gid /3.0f) %4.0f) /4.0f;        
////        if(venn.getColorMode)
////        {
//        	 color = new Color(r,g,b,0.6f );
//             obj.setFillColor(color);
////        }
////        else
////        {        
////          float grayvalue = ( r+ b  +g)  / 3.0f;
////        	color = new Color(grayvalue,grayvalue,grayvalue,0.6f );
////          obj.setFillColor(color);
////        }
//
//        return obj;
//    }

    /* (non-Javadoc)
     * @see venn.diagram.IVennObjectFactory#create(int, java.util.BitSet)
     */
    public IVennObject create(int gid, BitSet elements) {
    	throw new NotImplementedException();
    }
    
    public IVennObject create(int gid, BitSet elements, int numGroups, AllParameters params) 
    {
        IVennObject obj = null;
        
        switch( view )
        {
        	case VIEW_POLYGON:
        	    obj = new VennPolygonObject(numEdges,areaFactor,elements, params.logNumElements); 
                break;
        	    
        	default:
        	    throw new IllegalStateException("wrong view type");
        }
        Assert.assertNotNull( obj ); 

    	Color color;
    	float r=((float)gid % 4.0f)/4.0f;
    	float g=(((float)gid+ 1.0f) %4.0f)/4.0f;
    	float b=(((float)gid /3.0f) %4.0f) /4.0f; 
        if(params.colormode)
        {        
        	color = new Color(r,g,b,0.6f );
        }
        else
        {
        	//float grayvalue = ( r+ b  +g)  / 3.0f;
        	float grayvalue = r*0.299f + g*0.584f + b*0.144f;
        	//float grayvalue = 1.0f/ ((float)numGroups + 1.0f) * ((float)gid + 1.0f);
        	color = new Color(grayvalue,grayvalue,grayvalue,0.6f );
        }
    	obj.setFillColor(color);
        return obj;
    }
}
