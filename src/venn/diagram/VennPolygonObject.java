/*
 * Created on 30.05.2005
 *
 */
package venn.diagram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.BitSet;

import junit.framework.Assert;
import venn.AllParameters;
import venn.db.AbstractGOCategoryProperties;
import venn.geometry.FPoint;
import venn.geometry.FPolygon;
import venn.geometry.FRectangle;
import venn.geometry.ITransformer;

/**
 * A polygonal representation of a single set.
 * 
 * @author muellera
 *
 */
public class VennPolygonObject
extends AbstractVennObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    private final FPolygon  origPolygon;
    private FPolygon        cachedPolygon;
    private boolean         valid;


    private final double areaFactor;

    private boolean isIntersection;
    
    
    public VennPolygonObject( FPolygon polygon, BitSet elements, double areaFactor, boolean isIntersection, double rotation, double ratio )
    {
        super( elements );
        
        this.isIntersection = isIntersection;
        if(isIntersection) 
        { 
//        	System.out.println("is intersection");
        }
        this.areaFactor = areaFactor;
        setRotation(rotation);
        setRatio(ratio);
        
        origPolygon = polygon;
        cachedPolygon = polygon;
        valid = true;
    }
    
    
    // create a completely new ellipse
    public VennPolygonObject(double areaFactor, BitSet elements, boolean isIntersection)
    {
        super( elements );
        
        this.isIntersection = isIntersection;	// this is never true for this constructor
        // TODO ME throw exception if intersection
        
        this.areaFactor = areaFactor;
        
//      double radius = FPolygon.radiusNgon(numEdges,(double)elements.cardinality()/areaFactor);
        
        int card = elements.cardinality();
        if (venn.Main.params.logNumElements) card = AbstractGOCategoryProperties.log(card);
        
//        double radius = FPolygon.radiusNgon(params.numEdges,(double)card/areaFactor);
//        origPolygon = FPolygon.createNgon(params.numEdges,radius);   

        origPolygon = FPolygon.createEllipse(getRatio(), getRotation(), (double)card/areaFactor);

        invalidate();
    }

    
    // ME never used...
    public double getAreaFactor()
    {
        return areaFactor; 
    }
    
    
    /* (non-Javadoc)
     * @see venn.IVennObject#contains(venn.geometry.FPoint)
     */
    public boolean contains(FPoint p)
    {       
        FPolygon poly = getPolygon();
        if( poly == null )
            return false;
        return poly.contains( p );
    }
    
    public void invalidate()
    {
        valid = false;
    }
    
    private void validate()
    {
        if( ! valid )
        {
            if( origPolygon != null )
            {
            	if(isIntersection) { cachedPolygon = (FPolygon)origPolygon.clone(); }        		// ME this never happens...
            	else	// ME polygon is an ellipse
            	{
                	cachedPolygon = FPolygon.createEllipse(getRatio(), getRotation(), (double)cardinality()/areaFactor);  
            	}
                cachedPolygon.scale( getScale() );
                cachedPolygon.translate( getOffset() );
            } 
            else
            {
                cachedPolygon = null;
            }
            valid = true;
        }
    }
    
    public FPolygon getPolygon()
    {
        validate();
        return cachedPolygon;
    }

    /* (non-Javadoc)
     * @see venn.IVennObject#directPaint(java.awt.Graphics, venn.geometry.ITransformer)
     */
    public void directPaint(Graphics g, ITransformer t)
    {
        if( isEmpty() || t == null )
            return;
        FPolygon poly = getPolygon();
        if( (poly != null) && (poly.getSize() > 0 ) )
        {
        	
	            Polygon p = poly.transform(t);
	//            System.out.println(x);
	            
	            g.setColor( getFillColor() );
	            g.fillPolygon(p);
	            g.setColor( getBorderColor() );
	            g.drawPolygon(p);
        }
    }

    /* (non-Javadoc)
     * @see venn.IVennObject#intersect(venn.IVennObject)
     */
    public IVennObject intersect(IVennObject obj) 
    {
        Assert.assertNotNull( obj );
        
        VennPolygonObject other = (VennPolygonObject)obj;
        
        FPolygon    poly = null;
        
        if( getPolygon() != null && other.getPolygon() != null )
        {
            poly = getPolygon().intersect(other.getPolygon());
        }
        
        // intersect elements
        BitSet            elem = (BitSet)getElements().clone();
        elem.and( obj.getElements() );
        
        VennPolygonObject newObj = new VennPolygonObject( poly, elem, areaFactor,true, 0.0, 0.0 );
        
        // interpolate colors
        float[] a = new float[4], 
                b = new float[4];
        
        getFillColor().getRGBComponents(a);
        obj.getFillColor().getRGBComponents(b);
        
        newObj.setFillColor( new Color( 0.5f*(a[0]+b[0]),0.5f*(a[1]+b[1]),
                                        0.5f*(a[2]+b[2]),0.5f*(a[3]+b[3]) ) );
        
        return newObj;
    }

    /* (non-Javadoc)
     * @see venn.IVennObject#area()
     */
    public double area()
    {
        FPolygon poly = getPolygon();
        if( poly != null )
            return areaFactor * poly.area();
        else
            return 0.0;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( super.toString() );
        buf.append( "\n" );
        
        return buf.toString();
    }

    public boolean isEmpty() 
    {
        FPolygon p = getPolygon();
        return (p==null) || (p.getSize() == 0);
    }

    public FRectangle getBoundingBox()
    {
        validate();
        if( cachedPolygon != null )
            return cachedPolygon.getBoundingBox();
        
        return null;
    }
    
    public IVennObject duplicate()
    {
        IVennObject dup = new VennPolygonObject( origPolygon, getElements(), areaFactor, isIntersection, getRotation(), getRatio() );
        
        dup.setLock( getLock() );
        
        return dup;
    }

    public FPoint getCenter() 
    {
    	validate();
        return getPolygon().center();
    }
}
