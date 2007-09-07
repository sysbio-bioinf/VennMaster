/*
 * VennMaster/geometry/GeometricObject.java
 * 
 * Created on 01.07.2004
 * 
 */
package venn.geometry;

import java.io.Serializable;

/**
 * @author muellera
 */
public interface FGeometricObject extends Serializable
{
	public FPoint center();
	public double area();
	public boolean contains(FPoint p);
	public FRectangle getBoundingBox();
	public void paint(java.awt.Graphics g, ITransformer t);
}
