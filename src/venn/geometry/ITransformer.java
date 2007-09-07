/*
 * VennMaster/geometry/Transformer.java
 * 
 * Created on 25.06.2004
 * 
 */
package venn.geometry;

import java.io.Serializable;

import venn.utility.SystemUtility;

/**
 * A coordinate system transformer interface. Virtual coordinates <code>venn.geometry.FPoint</code>
 * are converted from and to Graphic context coordinates <code>java.awt.Point</code>.
 * @author muellera
 */
public abstract class ITransformer implements Serializable, Cloneable
{	
	/**
	 * Transforms a given virtual point to a concrete graphic context point.
	 * @param p
	 * @return A point in screen coordinates.
	 */
	public abstract java.awt.Point transform(FPoint p);
	
	/**
	 * 
	 * @param p
	 * @return The virtual coordinate <code>FPoint</code>.
	 */
	public abstract FPoint inverseTransform(java.awt.Point p);
    
    public Object clone()
    {
        return SystemUtility.serialClone(this);
    }
}
