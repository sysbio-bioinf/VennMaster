/*
 * VennMaster/geometry/Point.java
 * 
 * Created on 25.06.2004
 * 
 */
package venn.geometry;

import java.io.Serializable;

/**
 * @author muellera
 */
public class FPoint implements Serializable
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final double EPSILON = 1E-20;
	
	public final double x,y;
	
	public FPoint()
	{
		x = 0.0;
		y = 0.0;
	}
	
	public FPoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public FPoint(FPoint p)
	{
		this.x = p.x;
		this.y = p.y;
	}
	
	/**
	 * 
	 * @param d
	 * @return A translated point by d
	 */
	public FPoint add(FPoint d)
	{
		return new FPoint(x + d.x,y + d.y);
	}
	
	/**
	 * 
	 * @param p
	 * @return Difference this-p
	 */
	public FPoint sub(FPoint p)
	{
		return new FPoint(x - p.x,y-p.y);
	}
	
	public FPoint scale(FPoint s)
	{
		return new FPoint(x*s.x,y*s.y);
	}
	

	/**
	 * 
	 * @return Inverse point
	 */
	public FPoint negate()
	{
		return new FPoint(-x,-y);
	}
	

	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	

	public FPoint multiply(double scalar)
	{
		return new FPoint(x*scalar,y * scalar);
	}
	
	/**
	 * 
	 * @param p
	 * @return Cross-product of this point with p.
	 */
	public double crossProduct(FPoint p)
	{
		return x*p.y-y*p.x;
	}
	
	/**
	 * 
	 * @param p
	 * @return Euclidean distance to the point p.
	 */
	public double distance(FPoint p)
	{
		double dx = p.x - x,
				dy = p.y - y;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	public double qdist(FPoint p)
	{
		double dx = p.x - x,
				dy = p.y - y;
		return dx*dx+dy*dy;		
	}
	
	public double scalarProduct(FPoint p)
	{
		return x*p.x+y*p.y;
	}
	
	public double norm()
	{
		return Math.sqrt(x*x+y*y);
	}
	
	public String toString()
	{
		return "[" + (double)Math.round(x*1000)/1000 + " " + (double)Math.round(y*1000)/1000 + "]";
	}
	
	public boolean equals(FPoint p)
	{
		return (x==p.x) && (y==p.y);
	}
	
	public FPoint reciprocal()
	{
		return new FPoint(1.0/x,1.0/y);
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return Area of the triangle (p1,p2,p3). The area will be > 0 if (p1,p2,p3) forms
	 * a counterclockwise cycle and < 0 if (p1,p2,p3) forms a counterclock cycle.
	 * The area will be 0 if the points are collinear.
	 */
	public static double triangleArea(FPoint a, FPoint b, FPoint c)
	{
		return (b.x-a.x)*(c.y-a.y) - (c.x-a.x)*(b.y-a.y);
	}
	
	public static int areaSign(FPoint a,FPoint b,FPoint c)
	{
		double area = triangleArea(a,b,c);
		if( area < -EPSILON )
			return -1;
		if( area > EPSILON )
			return +1;
			
		return 0;
	}
	
	
	public static boolean collinear(FPoint p1, FPoint p2, FPoint p3)
	{
		return Math.abs(triangleArea(p1,p2,p3)) <= EPSILON;
	}
	
	public static boolean between(FPoint a, FPoint b, FPoint c )
	{
		if (a.x != b.x)
		{ // not vertical
			return ((a.x <= c.x) && (c.x <= b.x))
				|| ((a.x >= c.x) && (c.x >= b.x));
		}
		else
		{ // vertical 
			return ((a.y <= c.y) && (c.y <= b.y))
				|| ((a.y >= c.y) && (c.y >= b.y));
		}
	}
	
	public static void main(String[] args)
	{
		FPoint a,b,c;
		a = new FPoint(0,0);
		b = new FPoint(2,1);
		c = new FPoint(1,3);
		
		System.out.println("area(a,b,c) = "+triangleArea(a,b,c));
		System.out.println("area(a,c,b) = "+triangleArea(a,c,b));
	}
	

}
