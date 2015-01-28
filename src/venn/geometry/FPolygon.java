/*
 * VennMaster/geometry/Polygon.java
 * 
 * Created on 25.06.2004
 * 
 */
package venn.geometry;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import junit.framework.Assert;
import venn.AllParameters;


/**
 * @author muellera
 */
public class FPolygon
implements FGeometricObject
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int numOfPoints;
	private FPoint[] points;
	private FPoint offset;
	
	private boolean areaValid;
	private double cachedArea;
	private boolean boundingBoxValid;
	private FRectangle cachedBoundingBox;
	
	public FPolygon()
	{
		areaValid = false;
		cachedArea = -1.0;
		boundingBoxValid = false;
		cachedBoundingBox = null;
	}
	
	public FPolygon(int numOfPoints)
	{
		reserve(numOfPoints);
	}
	
	public Object clone()
	{
		FPolygon tmp = new FPolygon();
		tmp.resize(numOfPoints);
		for(int i=0; i<numOfPoints; ++i)
		{
			tmp.points[i] = points[i];
		}
		
		return tmp;
	}
	
	public void invalidate()
	{
		areaValid = false;
		boundingBoxValid = false;
	}
	
	public void translate(FPoint diff)
	{
		invalidate();
		for(int i=0; i<numOfPoints;++i)
		{
			points[i] = points[i].add(diff);
		}
	}
	
	public void scale(double s)
	{
		invalidate();
		for(int i=0; i<numOfPoints;++i)
		{
			points[i] = points[i].multiply(s);
		}				
	}
	
    public void scale(FPoint center, double s)
    {
        invalidate();
        for(int i=0; i<numOfPoints;++i)
        {
            points[i] = points[i].sub(center).multiply(s).add(center);
        }               
    }
    
	public void scale(FPoint s)
	{
		invalidate();
		for(int i=0; i<numOfPoints;++i)
		{
			points[i] = points[i].scale(s);
		}		
	}
	
	public void invert()
	{
		invalidate();
		for(int i=0, j=numOfPoints-1; i<numOfPoints/2; ++i, --j)
		{
			FPoint tmp;
			tmp = points[i];
			points[i] = points[j];
			points[j] = tmp;
			
		}
	}
	
	public void reserve(int num)
	{
		if( points==null || num > points.length )
		{
			FPoint[] tmp = new FPoint[num];
				
			if( points != null )
			{
				for(int i=0; i<numOfPoints;++i)
				{
					tmp[i] = points[i];
				}
			}
			points = tmp;
		}
	}

	public void resize(int num)
	{
		if( points == null || num >= numOfPoints )
		{
			reserve((1+num)*2);
		}
		if( numOfPoints != num )
		{
			numOfPoints = num;
			invalidate();
		}
	}
	
	/**
	 * Adds a point p to the polygon (if the same point is not already on the
	 * last position).
	 * 
	 * @param p
	 */
	public void add(FPoint p)
	{
		if( p == null )
			throw new IllegalArgumentException("cannot add null");
		
		invalidate();
		
		int idx = numOfPoints;
		if( numOfPoints > 0 )
		{
			// check last point
			if( points[numOfPoints-1].equals(p) )
				return;
		}
		resize(numOfPoints+1);
		points[idx] = p;
	}
	
	/**
	 * 
	 * @return The area of the polygon. Will be positive on countercycled polygons.
	 */
	public double area()
	{
		if( areaValid )
			return cachedArea;
		
		double a = 0.0;
		int j;
		for( int i=0; i<numOfPoints; ++i)
		{
			j = (i+1)%numOfPoints;
			a += (points[j].x + points[i].x) * (points[j].y - points[i].y);
		}
		
		cachedArea = 0.5*a;
		areaValid = true;
		return cachedArea;
	}
	
	/**
	 * 
	 * @return A rectangle describing the surrounding box of this polygon.
	 */
	public FRectangle getBoundingBox()
	{
		if( points == null || points.length == 0 )
            return null;
			// throw new IllegalStateException("no points");
		
		if( boundingBoxValid )
			return cachedBoundingBox;
			
		double minX,minY,maxX,maxY;
		
		minX = points[0].x;
		maxX = minX;
		minY = points[0].y;
		maxY = minY;
		for(int i=1; i<numOfPoints; ++i)
		{
			if( points[i].x < minX )
			{
				minX = points[i].x;
			}
			else
			{
				if( points[i].x > maxX )
					maxX = points[i].x;
			}
			
			if( points[i].y < minY )
			{
				minY = points[i].y;
			}
			else
			{
				if( points[i].y > maxY )
					maxY = points[i].y;
			}
		}
		
		cachedBoundingBox = new FRectangle(minX,minY,maxX,maxY);
		boundingBoxValid = true;
		return cachedBoundingBox;
	}
	
	public void clear()
	{
		resize(0);
	}
	
	public int getSize()
	{
		return numOfPoints; 
	}
	
	public FPoint[] getPoints()
	{
		return points;
	}
	
	public FPoint getOffset()
	{
		return offset;
	}

	public void setOffset(FPoint offset)
	{
		if( this.offset != offset )
		{
			invalidate();
			this.offset = offset;
		}
	}
	
	/**
	 * 
	 * @return The center of gravity of this polygon.
	 */
	public FPoint center()
	{
		if( numOfPoints <= 0 | points == null )
			throw new IllegalStateException("polygon is empty");
		
		double sx = 0.0, sy = 0.0;
		
		for(int i=0; i<numOfPoints; ++i)
		{
			sx += points[i].x;
			sy += points[i].y;
		}
		sx /= (double)numOfPoints;
		sy /= (double)numOfPoints;
		return new FPoint(sx,sy);
	}
	

	/**
	 * 
	 * @param q
	 * @return 'i' if the given point lies in this polygon, 'o' if its outside and
	 * 	'e' if it lies on an edge.
	 */	
	public char polyContains(FPoint q)
	{
		if( numOfPoints < 3 )
			return 'o';
		
		if( !getBoundingBox().contains(q) )
			return 'o';

		int i, i1; /* point index; i1 = i-1 mod n */
		double x; /* x intersection of e with ray */
		int Rcross = 0; /* number of right edge/ray crossings */
		int Lcross = 0; /* number of left edge/ray crossings */

		/*
		 * Shift so that q is the origin. Note this destroys the polygon. This
		 * is done for pedogical clarity.
		 */
		//Polygon poly = (Polygon)clone();
		//poly.translate(q.negate());
		/* For each edge e=(i-1,i), see if crosses ray. */
		for( i = 0; i < getSize(); i++ )
		{
			/* First see if q=(0,0) is a vertex. */
			if( points[i].equals(q) )
				return 'v';

			i1 = (i + getSize() - 1) % getSize();
			/* printf("e=(%d,%d)\t", i1, i); */

			/* if e "straddles" the x-axis... */
			/*
			 * The commented-out statement is logically equivalent to the one
			 * following.
			 */
			/*
			 * if( ( ( P[i][Y] > 0 ) && ( P[i1][Y] <= 0 ) ) || ( ( P[i1][Y] > 0 ) && (
			 * P[i] [Y] <= 0 ) ) ) {
			 */

			if( (points[i].y > q.y) != (points[i1].y > q.y) )
			{
				/* e straddles ray, so compute intersection with ray. */
				x = ((points[i].x - q.x) * (points[i1].y - q.y) - (points[i1].x - q.x)
						* (points[i].y - q.y))
						/ (points[i1].y - points[i].y);
				/* printf("straddles: x = %g\t", x); */

				/* crosses ray if strictly positive intersection. */
				if( x > 0 )
					Rcross++;
			}
			/* printf("Right cross=%d\t", Rcross); */

			/* if e straddles the x-axis when reversed... */
			/*
			 * if( ( ( P[i] [Y] < 0 ) && ( P[i1][Y] >= 0 ) ) || ( ( P[i1][Y] < 0 ) && (
			 * P[i] [Y] >= 0 ) ) ) {
			 */

			if( (points[i].y < q.y) != (points[i1].y < q.y) )
			{
				/* e straddles ray, so compute intersection with ray. */
				x = ((points[i].x - q.x) * (points[i1].y - q.y) - (points[i1].x - q.x)
						* (points[i].y - q.y))
						/ (points[i1].y - points[i].y);
				/* printf("straddles: x = %g\t", x); */

				/* crosses ray if strictly positive intersection. */
				if( x < 0 )
					Lcross++;
			}
			/* printf("Left cross=%d\n", Lcross); */
		}

		/* q on the edge if left and right cross are not the same parity. */
		if( (Rcross % 2) != (Lcross % 2) )
			return 'e';

		/* q inside iff an odd number of crossings. */
		if( (Rcross % 2) == 1 )
			return 'i';
		else
			return 'o';
	}

	public boolean contains(FPoint p)
	{
        if( numOfPoints == 0 )
            return false;
        
		return !(polyContains(p) == 'o');
	}

	/**
	 * Intersection of two convex polygons. The polygons must be
	 * counterclockwise (so area() > 0) otherwise no intersection will be found.
	 * 
	 * This algorithm is a port of the C-source from O'Rourke (Geometric
	 * Algorithms in C).
	 * 
	 * @param poly
	 * @return The intersection polygon or null if the polygons don't intersect.
	 */
	public FPolygon intersect(FPolygon poly)
	{
		if( this == poly )
			return this;
		if( poly == null )
			return null;
        if( this.numOfPoints == 0 || poly.numOfPoints == 0 )
            return null;
        
		if( ! getBoundingBox().intersects(poly.getBoundingBox()) )
			return null;
		
		int i, j, i1, j1, ai, aj;
		char inflag = '?'; // status 'p','q','?'
		boolean firstpoint = true;
		FSegment sP = new FSegment(), sQ = new FSegment();
		FPoint origin = new FPoint(0, 0);
		int aHB, bHA, cross;
		FPolygon result = new FPolygon();

		if( getSize() < 2 || poly.getSize() < 2 )
			return null;

		i = j = 0;
		ai = aj = 0;

		do
		{
			i = i % getSize();
			j = j % poly.getSize();
			i1 = i - 1;
			if( i1 < 0 )
				i1 = getSize() - 1;
			j1 = j - 1;
			if( j1 < 0 )
				j1 = poly.getSize() - 1;

			sP.set(points[i1], points[i]);
			sQ.set(poly.points[j1], poly.points[j]);
			FPoint A = sP.b.sub(sP.a), B = sQ.b.sub(sQ.a);

			cross = FPoint.areaSign(origin, A, B);
			aHB = FPoint.areaSign(sQ.a, sQ.b, sP.b);
			bHA = FPoint.areaSign(sP.a, sP.b, sQ.b);

			IntersectionPoint isp = sP.intersection(sQ);

			/*
			 * System.out.println("Before Advance a="+i+" b="+j+" "+sP+" "+sQ+"
			 * inflag="+inflag); System.out.println("A "+sP+" B "+sQ);
			 * System.out.println("cross = "+cross+" aHB = "+aHB+" bHA = "+bHA);
			 * System.out.println(i+"," +j+" intersection " + isp);
			 */
			if( isp != null )
			{
				if( isp.code == IntersectionPoint.INTERSECTION
						|| isp.code == IntersectionPoint.ENDPOINT )
				{
					if( inflag == '?' && firstpoint )
					{
						ai = aj = 0;
						firstpoint = false;
					}
					// update inflag
					if( aHB > 0 )
					{
						inflag = 'p';
					}
					else
					{
						if( bHA > 0 )
							inflag = 'q';
					}
					//System.out.println("lineto "+isp.p);
					result.add(isp.p);
				}

				// ADVANCE
				if( (isp.code == IntersectionPoint.COLLINEAR)
						&& (A.scalarProduct(B) < 0) )
				{ // A and B overlap (oppositely directed)
					// System.out.println("lineto "+isp.p + " lineto "+isp.q);
					result.add(isp.p);
					result.add(isp.q);
					return result;
				}
			}

			if( (cross == 0) && (aHB < 0) && (bHA < 0) )
			{ // A and B parallel and separated
				// System.out.println("disjoint!!");
				return null;
			}

			if( (cross == 0) && (aHB == 0) && (bHA == 0) )
			{ // collinear
				if( inflag == 'p' )
				{
					++j;
					++aj;
				}
				else
				{
					++i;
					++ai;
				}
			}
			else
			{ // generic case
				if( cross >= 0 )
				{
					if( bHA > 0 )
					{
						++i;
						++ai;
						if( inflag == 'p' )
						{
							//	System.out.println("lineto "+sP.b);
							result.add(sP.b);
						}
					}
					else
					{
						++j;
						++aj;
						if( inflag == 'q' )
						{
							// 	System.out.println("lineto "+sQ.b);
							result.add(sQ.b);
						}
					}
				}
				else
				{ // cross < 0
					if( aHB > 0 )
					{
						++j;
						++aj;
						if( inflag == 'q' )
						{
							// 	System.out.println("lineto " + sQ.b);
							result.add(sQ.b);
						}
					}
					else
					{
						++i;
						++ai;
						if( inflag == 'p' )
						{
							//	System.out.println("lineto " + sP.b);
							result.add(sP.b);
						}
					}
				}
			}
			// System.out.println("After Advance a="+i+" b="+j+"
			// inflag="+inflag);
		}
		while( ((ai < getSize()) || (aj < poly.getSize()))
				&& (ai < 2 * getSize()) && (aj < 2 * poly.getSize()) );

		/*
		 * if( p0 != null && ! firstpoint ) System.out.println("lineto " + p0);
		 */

		if( inflag == '?' )
		{
			// System.out.println("no crossing!!");
			if( poly.polyContains(points[0]) == 'i' )
			{ // this polygon is contained in poly
				return this;
			}
			else
			{ // poly is contained in this polygon
				if( polyContains(poly.points[0]) == 'i' )
				{
					return poly;
				}
			}
		}

		return result;
	}

	public void paint(Graphics g, ITransformer t)
	{
		/*
		 * for(int i=0; i <getSize(); ++i ) { java.awt.Point p1 =
		 * t.transform(points[i]), p2 = t.transform(points[(i+1)%getSize()]);
		 * g.drawLine(p1.x,p1.y,p2.x,p2.y); }
		 */
		g.drawPolygon(transform(t));
	}

	public java.awt.Polygon transform(ITransformer t)
	{
        if( t == null )
            return null;
		java.awt.Polygon poly = new java.awt.Polygon();

		for( int i = 0; i < getSize(); ++i )
		{
			java.awt.Point p = t.transform(points[i]);
			poly.addPoint(p.x, p.y);
		}

		return poly;
	}

	public void paint(Graphics g)
	{
		paint(g, new AffineTransformer());
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < getSize(); ++i )
		{
			buf.append(points[i].toString());
		}
		return buf.toString();
	}

	public boolean equals(FPolygon poly)
	{
		if( poly == null )
			return false;
		if( poly == this )
			return true;

		if( getSize() != poly.getSize() )
			return false;
		if( poly.points == null && points == null )
			return true;

		for( int i = 0; i < getSize(); ++i )
		{

			if( !points[i].equals(poly.points[i]) )
				return false;
		}

		return true;
	}

	/**
	 * Creates an n-gon with n edges.
	 * 
	 * @param n
	 *            Number of edges
	 * @param r
	 *            Radius
     *            
	 * @return a new FPolygon object
	 */
	public static FPolygon createNgon(int n, double r)
	{
		if( n < 3 )
			throw new IllegalArgumentException("n must be >= 3");

		FPolygon poly = new FPolygon();
		poly.resize(n);
		for( int i = 0; i < poly.getSize(); ++i )
		{
			double phi = 2 * Math.PI * i / (double) poly.getSize();
			poly.getPoints()[i] = new FPoint(r * Math.cos(phi), r
					* Math.sin(phi));
		}
		return poly;
	}
	
	public static FPolygon createEllipse(double rat, double rot, double area)
	{
		AllParameters params = venn.Main.params;
		if( params.numEdges < 3 )
			throw new IllegalArgumentException("Number of edges must be >= 3");
		
		FPolygon poly = new FPolygon(params.numEdges);
		
		double ratio = rat >= 1.0 ? rat : (1 / rat);
		double rotation = rat >= 1.0 ? (rot % 180) : ((rot + 90) % 180);		// rotate 90° if width is supposed to be shorter than height 
        double stretchFactor = Math.sqrt(area / (Math.PI * ratio));		// adjustment so the area of the ellipse will be the area
        
        int templateRatioIndex = params.getTemplateRatioIndex(ratio);
		double templateRatio = params.getTemplateRatio(templateRatioIndex);	
		double[] templateVertices = params.getTemplateEllipses(templateRatioIndex);
		double[] halfOfThePoints = new double[params.numEdges];
		double[] allPoints = new double[params.numEdges * 2];
		
		// creating one half off the stretched & resized polygon coordinates
		for(int i = 0; i < (params.numEdges / 4); i++)
		{
			// stretch 1st quadrant
			halfOfThePoints[2 * i] = templateVertices[2 * i] * stretchFactor * ratio / templateRatio;
			halfOfThePoints[(2 * i) + 1] = templateVertices[(2 * i) + 1] * stretchFactor;
			// create 2nd quadrant from first
			halfOfThePoints[(params.numEdges) - ((1 + i) * 2)] = -halfOfThePoints[2 * i];
			halfOfThePoints[(params.numEdges) - ((1 + i) * 2) + 1] = halfOfThePoints[(2 * i) + 1];
		}
		// rotate points
		AffineTransform.getRotateInstance(Math.toRadians(rotation), 0.0, 0.0)
		  .transform(halfOfThePoints, 0, halfOfThePoints, 0, (params.numEdges / 2));

//		TODO [ME] enable offset in the creation?

		// create other half of the ellipse coordinates
		for(int i = 0; i < (params.numEdges); i += 2)
		{
			allPoints[i] = halfOfThePoints[i];
			allPoints[i + 1] = halfOfThePoints[i + 1];
			
			allPoints[(params.numEdges) + i] = -halfOfThePoints[i];
			allPoints[(params.numEdges) + i + 1] = -halfOfThePoints[i + 1];			
		}
		
		// create actual polygon
		for(int i = 0; i < params.numEdges; i++)
		{
			poly.add(new FPoint(allPoints[(2 * i)], allPoints[(2 * i) + 1]));
		}
		
		return poly;
	}

	/**
	 * 
	 * @param n
	 * @param r
	 * @return Area of an even n-gon.
	 */
	public static double areaNgon(int n, double r)
	{
		return (double) n * r * r * Math.sin(2.0 * Math.PI / (double) n) * 0.5;
	}

	/**
	 * 
	 * @param n
	 * @param area
	 * @return The radius of an n-gon with area <var>area </var>.
	 */
	public static double radiusNgon(int n, double area)
	{
		return Math.sqrt(2 * area
				/ ((double) n * Math.sin(2.0 * Math.PI / (double) n)));		// [ME] correct, but why not just "return Math.sqrt(area/Math.PI)" ???
	}

	/**
	 * 
	 * @param poly
	 * @return The bounding box of an array of polygons.
	 */
	public static FRectangle getBoundingBox(FPolygon[] poly)
	{
		if( poly == null || poly.length == 0 )
			return null;

		FRectangle bbox = null;
		for( int i = 0; i < poly.length; ++i )
		{
			if( bbox == null )
				bbox = poly[i].getBoundingBox();
			else
				bbox = bbox.union(poly[i].getBoundingBox());
		}
		return bbox;
	}

	public static void main(String[] args)
	{
		FPolygon p = new FPolygon(), q = new FPolygon();

		/*
		 * // i.house p.add(new Point(0, 0)); p.add(new Point(200, 0));
		 * p.add(new Point(200, 100)); p.add(new Point(100, 200)); p.add(new
		 * Point(0, 100));
		 * 
		 * 
		 * q.add(new Point(100,100)); q.add(new Point(300,100)); q.add(new
		 * Point(300,200)); q.add(new Point(100,200));
		 */

		// i.sqsq
		/*
		 * p.add(new Point(0, 0)); p.add(new Point(100, 0)); p.add(new
		 * Point(100, 100)); p.add(new Point(0, 100));
		 * 
		 * q.add(new Point(50, 50)); q.add(new Point(150, 50)); q.add(new
		 * Point(150, 150)); q.add(new Point(50, 150));
		 * 
		 * p.scale(1.0/100.0); q.scale(1.0/100.0);
		 */

//		p = FPolygon.createNgon(5, 0.2);
//		q = FPolygon.createNgon(6, 0.9);
//
//		System.out.println("Polygon P area = " + p.area());
//		System.out.println(p);
//
//		System.out.println("Polygon Q area = " + q.area());
//		System.out.println(q);
//
//		FPolygon inters = p.intersect(q);
//
//		System.out.println("Polygon P/Q area = " + inters.area());
//		System.out.println(inters);
		double A = 5.0;
		int n = 8;
		
		double r = radiusNgon(n, A);
		System.out.println("r: " + r);
	}
	
	public FPoint intersect(FSegment seg)
	{
        Assert.assertNotNull( seg );
        Assert.assertNotNull( points );
        Assert.assertTrue( points.length >= getSize() );
		FSegment tmp = new FSegment();
		IntersectionPoint point;
		
		for( int i=0; i<getSize(); ++i )
		{
			tmp.set(points[i],points[(i+1)%getSize()]);
			point = seg.intersection( tmp );
            if( point == null )
                return null;
			if( point.code == IntersectionPoint.INTERSECTION ||
					point.code == IntersectionPoint.ENDPOINT )
			{
				return point.p;
			}
		}
		
		return null;
	}
}