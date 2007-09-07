/*
 * VennMaster/geometry/AffineTransformer.java
 * 
 * Created on 25.06.2004
 * 
 */
package venn.geometry;

/**
 * @author muellera
 */
public class AffineTransformer extends ITransformer
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private FPoint 	offset,
					scale;
	
	public AffineTransformer()
	{
		offset = new FPoint(0,0);
		scale = new FPoint(1,1);
	}
	
	public AffineTransformer(FPoint offset,FPoint scale)
	{
		this.offset = offset;
		this.scale = scale;
	}
	
	/* (non-Javadoc)
	 * @see geometry.Transformer#transform(geometry.Point)
	 */
	public java.awt.Point transform(FPoint p)
	{
		if( p == null )
			return null;
		
		//FPoint tmp = p.add(offset).scale(scale);
        FPoint tmp = p.scale(scale).add(offset);
		
		return new java.awt.Point((int)Math.round(tmp.x),(int)Math.round(tmp.y));
	}
	
	public FPoint inverseTransform(java.awt.Point p)
	{
		if( p == null )
			return null;
		FPoint tmp = new FPoint(p.x,p.y);
		return tmp.scale(scale.reciprocal()).sub(offset);
	}
	
	public void setOffset(FPoint offset)
	{
		if( offset == null )
			throw new IllegalArgumentException("offset must not be null");
		this.offset = offset;
	}
	
	public void setScale(FPoint scale)
	{
		if( scale == null )
			throw new IllegalArgumentException("scale must not be null");		
		this.scale = scale;
	}
}
