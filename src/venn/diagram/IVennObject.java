/*
 * Created on 29.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package venn.diagram;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.BitSet;

import javax.swing.event.ChangeListener;

import venn.geometry.FPoint;
import venn.geometry.FRectangle;
import venn.geometry.ITransformer;

/**
 * A graphical representation of a data set (e.g. a single polygon or a single circle).
 * @author andre
 */
public interface IVennObject extends Cloneable, Serializable
{   
    public void setLock( boolean locked );
    
    public boolean getLock();
    
    /**
     * 
     * @return The current center (in F-coordinates)
     */
	public FPoint getOffset();
        
	
	/**
	 * Sets the center of this object to p.
	 * @param p
	 */
	public void setOffset(FPoint p);
    
    
    public double getScale();
    
    
    public void setScale( double scale );
    
    public double getRotation();
    
    public void setRotation(double _rotation);
    
    public double getRatio();
    
    public void setRatio(double _ratio);
    
    
    public FPoint getCenter();
	
	/**
	 * 
	 * @param p
	 * @return Returns true if the given point is contained in this graphical object.
	 */
	public boolean contains(FPoint p);	

	/**
	 * 
	 * @return Returns the graphical area of this object. In the optimal case
	 * the following optimality condition should hold:
	 * area() = cardinality() * areaFactor
	 */
	public double area();
	
	/**
	 * 
	 * @return getElements().cardinality()
	 */
	public int cardinality();
		
	/**
	 * 
	 * @return Returns the element indices in this VennObject representation.
	 * 
	 */
	public BitSet getElements();

	/**
	 * Paints the VennObject.
	 * @param g
	 * @param t
	 */
	public void directPaint( Graphics g, ITransformer t );
	
	/**
	 * 
	 * @param obj
	 * @return The graphical intersection of obj with this or null if there is no 
	 * intersection.
	 */
	public IVennObject intersect(IVennObject obj);
    
    
    
    public void setProperties( Object properties );
    
    public Object getProperties();

    public void setFillColor(Color color);    
    public Color getFillColor();
    
    public void setBorderColor(Color color);    
    public Color getBorderColor();
    
    public void addChangeListener( ChangeListener obj );
    public void removeChangeListener( ChangeListener obj );
    
    public void invalidate();

    /**
     * 
     * @return true if there is no graphical area in this object
     */
    public boolean isEmpty();


    public FRectangle getBoundingBox();
    
    /**
     * Assigns all important data (position and size) to this VennObject
     * 
     * @param source
     */
    public void assignState( IVennObject source );
    
    public IVennObject duplicate();
}
