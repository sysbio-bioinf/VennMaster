/*
 * VennMaster/geometry/Rectangle.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.geometry;

import java.util.Random;

/**
 * @author muellera
 */
public class FRectangle
implements FGeometricObject
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    protected final FPoint a, b;
	
	public FRectangle()
	{
		a = new FPoint();
		b = new FPoint();
	}
	
	public FRectangle(FPoint a, FPoint b)
	{
		this(a.x,a.y,b.x,b.y);
	}
	
	public FRectangle(double ax,double ay,double bx,double by)
	{
		a = new FPoint(Math.min(ax,bx),Math.min(ay,by));
		b = new FPoint(Math.max(ax,bx),Math.max(ay,by));
	}
	
	/**
	 * 
	 * @param rect
	 * @return the union of both rectangles
	 */
	public FRectangle union(FRectangle rect)
	{
		return new FRectangle(	Math.min(getMinX(),rect.getMinX()),
								Math.min(getMinY(),rect.getMinY()),
								Math.max(getMaxX(),rect.getMaxX()),
								Math.max(getMaxY(),rect.getMaxY()) );
	}
	
	public boolean contains(FPoint p)
	{
		return (a.x <= p.x) && (p.x <= b.x) && (a.y <= p.y) && (p.y <= b.y);
	}
	
	public FRectangle getBoundingBox()
	{
		return this;
	}
	
	public boolean contains(FRectangle r)
	{
		return 	contains(r.topLeft()) &&
				contains(r.topRight()) &&
				contains(r.bottomLeft()) &&
				contains(r.bottomRight());
	}
	
	public boolean intersects(FRectangle r)
	{
		return (r.a.x <= b.x) && (a.x <= r.b.x) &&
				(r.a.y <= b.y) && (a.y <= r.b.y);
	}
	
	public FPoint restrict(FPoint q)
	{
		if(contains(q))
			return q;
			
		double x = q.x,y = q.y;
		if( x < a.x )
		{
			x = a.x;
		}
		else
		{
			if( x > b.x )
			{
				x = b.x;
			}
		}
		
		if( y < a.y )
		{
			y = a.y;
		}
		else
		{
			if( y > b.y )
			{
				y = b.y;
			}
		}
		return new FPoint(x,y);
	}
	
	public FPoint topLeft()
	{
		return a;
	}
	
	public FPoint topRight()
	{
		return new FPoint(b.x,a.y);
	}
	
	public FPoint bottomRight()
	{
		return b;
	}
	
	public FPoint bottomLeft()
	{
		return new FPoint(a.x,b.y);
	}
	
	public FPoint getA()
	{
		return a;
	}
	
	public FPoint getB()
	{
		return b;
	}
	
	public double getMinX()
	{
		return Math.min(a.x,b.x);
	}
	
	public double getMaxX()
	{
		return Math.max(a.x,b.x);
	}

	public double getMinY()
	{
		return Math.min(a.y,b.y);
	}
	
	public double getMaxY()
	{
		return Math.max(a.y,b.y);
	}

	public double getWidth()
	{
		return Math.abs(b.x-a.x);
	}
	
	public double getHeight()
	{
		return Math.abs(b.y-a.y);
	}

	/**
	 * @return The center of this rectangle.
	 */
	public FPoint center()
	{
		return new FPoint(0.5*(a.x+b.x),0.5*(a.y+b.y));
	}
	
	public double area()
	{
		return getWidth()*getHeight();
	}
	
	public void paint(java.awt.Graphics g, ITransformer transformer)
	{
		java.awt.Point 	pa = transformer.transform(a),
						pb = transformer.transform(b);
		
		g.drawRect(pa.x,pa.y,pb.x-pa.x,pb.y-pa.y);
				
	}
	
	public Object clone()
	{
		return new FRectangle(a.x,a.y,b.x,b.y);
	}
	
	public String toString()
	{
		return "[ "+a+" "+b+" ]";
	}

	/**
     * Chooses a random point (uniform distribution) from this rectangle
	 * @return a new FPoint
	 */
	public FPoint randomChoose(Random random)
	{
		return new FPoint(	a.x+random.nextFloat()*(b.x-a.x),
							a.y+random.nextFloat()*(b.y-a.y) );
	}
	
	/**
	 * Converts the rectangle into a Polygon with 4 points.
	 * @return a new FPolygon object.
	 */
	public FPolygon toPolygon()
	{
		FPolygon poly = new FPolygon(4);
		poly.add( topLeft() );
		poly.add( topRight() );
		poly.add( bottomRight() );
		poly.add( bottomLeft() );
		
		return poly;
	}
}
