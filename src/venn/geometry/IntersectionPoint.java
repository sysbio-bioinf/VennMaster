/*
 * VennMaster/geometry/IntersectionPoint.java
 * 
 * Created on 28.06.2004
 * 
 */
package venn.geometry;

/**
 * @author muellera
 */
public class IntersectionPoint
{
	public static final char 	UNKNOWN = '?',
								COLLINEAR = 'e', 	// segments collinearly overlap, shareing a point 
							 	ENDPOINT = 'v',	 	// an endpoint is on the other segment
								INTERSECTION = '1',	// normal intersection
							 	NOINTERSECTION = '0'; // no intersection
	
	public final char code;
	public final FPoint p,q;
	
	public IntersectionPoint(char code, FPoint p, FPoint q)
	{
		this.code = code;
		this.p = p;
		this.q = q;
	}
	
	public String toString()
	{
		switch( code )
		{
			case COLLINEAR:
				return "e ( "+p+" "+q+" )";	
				
			case ENDPOINT:
				return "v " + p.toString();
				
			case INTERSECTION:
				return p.toString();
				
			case NOINTERSECTION:
				return "~ "+p;
			
			default:
				return "[invalid]";			
		}
	}
}
