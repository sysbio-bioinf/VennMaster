/*
 * VennMaster/geometry/Segment.java
 * 
 * Created on 25.06.2004
 * 
 */
package venn.geometry;

import java.io.Serializable;

/**
 * @author muellera
 */
public class FSegment implements Serializable
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final double EPSILON = 1E-20;

	protected FPoint a, b;

	public FSegment()
	{
	}

	public FSegment(FSegment seg)
	{
		a = seg.a;
		b = seg.b;
	}

	public FSegment(FPoint a, FPoint b)
	{
		this.a = a;
		this.b = b;
	}

	public FSegment(double ax, double ay, double bx, double by)
	{
		a = new FPoint(ax, ay);
		b = new FPoint(bx, by);
	}

	public FPoint getA()
	{
		return a;
	}

	public FPoint getB()
	{
		return b;
	}

	public void set(double ax,double ay,double bx,double by)
	{
		this.a = new FPoint(ax,ay);
		this.b = new FPoint(bx,by);
	}

	public void set(FPoint a, FPoint b)
	{
		this.a = a;
		this.b = b;
	}

	public void setA(FPoint a)
	{
		this.a = a;
	}

	public void setB(FPoint b)
	{
		this.b = b;
	}

	public void swap()
	{
		FPoint tmp = a;
		a = b;
		b = tmp;
	}

	public double intersectionNumerator(FSegment seg)
	{
		return (
			(seg.a.y - a.y) * (seg.b.x - seg.a.x)
				+ (a.x - seg.a.x) * (seg.b.y - seg.a.y));
	}

	public double intersectionDenominator(FSegment seg)
	{
		return (
			(b.y - a.y) * (seg.b.x - seg.a.x)
				+ (a.x - b.x) * (seg.b.y - seg.a.y));
	}

	/**
	 * 
	 * @param seg
	 * @return Scalar lambda so that the intersection point would be
	 * P = a + lambda * (b - a) 
	 */
	public double intersectionScalar(FSegment seg)
	{
		return intersectionNumerator(seg) / intersectionDenominator(seg);
	}

	/**
	 * 
	 * @param seg
	 * @return An IntersectionPoint or null if the segments are not collinear.
	 */
	public IntersectionPoint parallelIntersection(FSegment seg)
	{
		if (!FPoint.collinear(a, b, seg.a))
		{
			return null;
		}

		if (FPoint.between(a,b,seg.a) && FPoint.between(a,b,seg.b) )
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,seg.a,seg.b);
		
		if(FPoint.between(seg.a,seg.b,a) && FPoint.between(seg.a,seg.b,b))
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,a,b);
			
		if(FPoint.between(a,b,seg.a) && FPoint.between(seg.a,seg.b,b))
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,seg.a,b);

		if(FPoint.between(a,b,seg.a) && FPoint.between(seg.a,seg.b,a))
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,seg.a,a);
			
		if(FPoint.between(a,b,seg.b) && FPoint.between(seg.a,seg.b,b))
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,seg.b,b);
			
		if(FPoint.between(a,b,seg.b) && FPoint.between(seg.a,seg.b,a))
			return new IntersectionPoint(IntersectionPoint.COLLINEAR,seg.b,a);

		return null;
	}

	/**
	 * There exists an intersection point if both segments are NOT parallel.
	 * @param seg
	 * @return	'e': coolinearly overlap, sharing a point.
	 * 			'v': An endpoint of one segment is on the other segment but 'e' doesn't hold
	 * 			'1': The segments intersect properly
	 * 			null: The segments don't intersect
	 */
	public IntersectionPoint intersection(FSegment seg)
	{
		double denom = intersectionDenominator(seg);

		if (Math.abs(denom) <= EPSILON)
		{
			return parallelIntersection(seg);
		}

		double num = intersectionNumerator(seg);
		char code = IntersectionPoint.UNKNOWN;

		if (Math.abs(num) <= EPSILON || Math.abs(num - denom) <= EPSILON)
		{
			code = IntersectionPoint.ENDPOINT;
		}
		double s = num / denom;
		
		num = -seg.intersectionNumerator(this);
		double t = num / denom;

		if (Math.abs(num) <= EPSILON || Math.abs(num - denom) <= EPSILON)
			code = IntersectionPoint.ENDPOINT;

		if( code == IntersectionPoint.UNKNOWN )
		{
			if ((0.0 <= s) && (s <= 1.0) && (0.0 <= t) && (t <= 1.0))
			{
				code = IntersectionPoint.INTERSECTION;
			}
			else
			{
				if ((0.0 > s) || (s > 1.0) || (0.0 > t) || (t > 1.0))
					code = IntersectionPoint.NOINTERSECTION;
			}
		}
		//System.out.println("A "+ new Point(a.x+s*(b.x-a.x),a.y+s*(b.y-a.y)) +
		//					"B "+ new Point(seg.a.x+t*(seg.b.x-seg.a.x),seg.a.y+t*(seg.b.y-seg.a.y)) );

		return new IntersectionPoint(
					code,
					new FPoint(a.x + s * (b.x - a.x), a.y + s * (b.y - a.y)),null);
	}

	/**
	 * 
	 * @param p
	 * @return True if the given point lies on the closed segment. This function assumes it is 
	 * already known that abc are collinear.
	 */
	public boolean between(FPoint p)
	{
		if (a.x != b.x)
		{ // not vertical
			return ((a.x <= p.x) && (p.x <= b.x))
				|| ((a.x >= p.x) && (p.x >= b.x));
		}
		else
		{ // vertical 
			return ((a.y <= p.y) && (p.y <= b.y))
				|| ((a.y >= p.y) && (p.y >= b.y));
		}
	}

	private int sign(double x)
	{
		if (x < 0)
			return -1;
		else
		{
			if (x > 0)
				return +1;
			else
				return 0;
		}
	}

	/**
	 * 
	 * @param seg 
	 * @return True if the given segment intersects this segment.
	 */
	public boolean intersects(FSegment seg)
	{
		FPoint diff1 = seg.b.sub(seg.a);

		int sa1 = sign(a.sub(seg.a).crossProduct(diff1)),
			sb1 = sign(b.sub(seg.a).crossProduct(diff1));
		boolean straddles1 = (sa1 == 0) || (sb1 == 0) || (sa1 == -sb1);

		if (!straddles1)
			return false;

		FPoint diff2 = b.sub(a);
		int sa2 = sign(seg.a.sub(a).crossProduct(diff2)),
			sb2 = sign(seg.b.sub(a).crossProduct(diff2));

		return (sa2 == 0) || (sb2 == 0) || (sa2 == -sb2);
	}

	public String toString()
	{
		return "( " + a.toString() + " " + b.toString() + " )";
	}

	public static void main(String[] args)
	{
		FSegment S1 = new FSegment(0, 1, 1, 4), S2 = new FSegment(3, 1, 2, 2);

		//	S1.swap();		
		System.out.println("seg1 = " + S1);
		System.out.println("seg2 = " + S2);
		System.out.println("seg1|seg2? : " + S1);
		System.out.println("seg2|seg1? : " + S2);

		IntersectionPoint p1, p2;
		p1 = S1.intersection(S2);
		p2 = S2.intersection(S1);

		System.out.println(
			"lambda1 = " + S1.intersectionScalar(S2) + " p1 = " + p1);
		System.out.println(
			"lambda2 = " + S2.intersectionScalar(S1) + " p2 = " + p2);
		
		S1.set(0,0,5,0);
		S2.set(3,0,7,1);
		System.out.println(S1+" | " + S2+ " : " + S1.intersection(S2));
	}
}
