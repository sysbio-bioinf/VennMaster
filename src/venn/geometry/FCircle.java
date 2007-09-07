/*
 * VennMaster/geometry/Circle.java
 * 
 * Created on 01.07.2004
 * 
 */
package venn.geometry;

/**
 * @author muellera
 */
public class FCircle
implements FGeometricObject
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final FPoint center;
	private final double radius;
	
	public FCircle(FPoint center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	public FPoint center()
	{
		return center;
	}
	
	public double area()
	{
		return Math.PI*radius*radius;
	}
	
	public static double segmentArea(double radius, double alpha)
	{
		double 	b = radius * alpha,
				s = 2*radius*Math.sin(0.5*alpha),
				h = radius - 0.5*Math.sqrt(4.0*radius*radius-s*s);
		return 0.5*(b*radius-s*(radius-h));
	}
	
	/**
	 * Calculates the area of the intersection of both circles.
	 * 
	 * @param circle
	 * @return the intersection area.
	 */
	public double intersectionArea( FCircle circle )
	{	
		if( contains(circle) )
			return circle.area();
		if( circle.contains(this) )
			return area();
			
		double 	d = center.distance(circle.center);
		if( d > radius+circle.radius )
			return 0.0; // no intersection
		
		double	s = 0.5*(d+radius+circle.radius),
				alpha0 = 2.0*Math.sqrt((s-circle.radius)*(s-d)/(circle.radius*d)),
				alpha1 = 2.0*Math.sqrt((s-radius)*(s-d)/(radius*d));
		
		return segmentArea(radius,2.0*alpha0)+segmentArea(circle.radius,2.0*alpha1);
	}
	
	public boolean contains(FPoint p)
	{
		return center.qdist(p) <= radius*radius; 
	}
	
	public boolean contains(FCircle circle)
	{
		return center.distance(circle.center)+circle.radius <= radius;
	}
	
	public FRectangle getBoundingBox()
	{
		return new FRectangle(center.x-radius,center.y-radius,center.x+radius,center.y+radius);
	}
	
	public void paint(java.awt.Graphics g, ITransformer transformer)
	{
		java.awt.Point 	p = transformer.transform(new FPoint(center.x-radius,center.y-radius)),
						q = transformer.transform(new FPoint(center.x+radius,center.y+radius));
		g.drawOval(p.x,p.y,q.x-p.x,q.y-p.y);
	}
	
	public String toString()
	{
		return center+" r="+radius;
	}
	
	
	public static void main(String[] args)
	{
		FCircle 	a = new FCircle(new FPoint(1E-20,0.0),1.0),
				b = new FCircle(new FPoint(0,0.0),1.0);
				
		System.out.println(a);
		System.out.println(b);
		System.out.println("area : "+a.intersectionArea(b));
	}
}
