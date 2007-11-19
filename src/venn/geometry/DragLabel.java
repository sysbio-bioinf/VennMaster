/*
 * Created on 06.07.2004
 *
 */
package venn.geometry;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import junit.framework.Assert;

/**
 * @author muellera
 *
 */
public class DragLabel extends JLabel
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ITransformer transformer;
	private FPoint     position;
	private boolean withConnector;		// activated connection line
    private BitSet path;
	
	public DragLabel(ITransformer transformer, String text, BitSet path )
	{
		super(text);
		this.transformer = transformer;
		this.path = path;

		
		setForeground(Color.BLUE);
		setBackground(new Color(0xff,0xff,0x55,0xa0));
		setBorder(BorderFactory.createLineBorder(new Color(0.5f,0.5f,0.0f)));
		setOpaque(true);
		setHorizontalTextPosition(CENTER);
		setVerticalTextPosition(CENTER);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		updateBounds(getGraphics());
		position = new FPoint(0.0,0.0);
		withConnector = true;
	}
	
	public void setTransformer(ITransformer transformer) {
		this.transformer = transformer;
		if (transformer != null) {
			setRelativePosition(getRelativePosition());
		}
	}
	
	public void setRelativePosition(FPoint p)
	{
		if( p == null )
			throw new IllegalArgumentException("location must not be null");
		super.setLocation(transformer.transform(p));
		position = p;
		
	}
	
	public void setLocation(int x, int y)
	{
		if( transformer != null )
		{
			position = transformer.inverseTransform(new java.awt.Point(x,y));
		}
		super.setLocation(x,y);
	}
		
	public FPoint getRelativePosition()
	{
		return position;
	}

	public void paintComponent(Graphics g)
	{
		updateBounds(g);
		super.paintComponent(g);
	}
	
	private void updateBounds(Graphics g)
	{
		java.awt.Rectangle bounds = getBounds();
		String text = getText();
		if( text == null )
		{
			bounds.width = 50;
			bounds.height = 10;
		}
		else
		{
			FontMetrics metrics = getFontMetrics(getFont());
			Rectangle2D textBounds = metrics.getStringBounds(text,g);
			bounds.width = (int)Math.round(textBounds.getWidth()+20);
			bounds.height = (int)Math.round(textBounds.getHeight()+6);
		}
		java.awt.Point p = transformer.transform(position);
		if( p != null )
		{
			bounds.x = p.x;
			bounds.y = p.y;
		}
		setBounds(bounds);
	}
	    
    public BitSet getPath()
    {
        return path;
    }
	
    public void setPath(BitSet path) {
    	this.path = path;
    }
    
	public boolean getWithConnector()
	{
		return withConnector;
	}
	
	public void setWithConnector(boolean connect)
	{
		withConnector = connect;
	}
		
	public FRectangle getBoundaries(Graphics g)
	{
		Assert.assertNotNull(g);
		updateBounds(g);
		java.awt.Rectangle bd = getBounds();
		return new FRectangle(
						transformer.inverseTransform(new java.awt.Point(bd.x,bd.y)),
						transformer.inverseTransform(new java.awt.Point(bd.x+bd.width,bd.y+bd.height)) );
	}
}
