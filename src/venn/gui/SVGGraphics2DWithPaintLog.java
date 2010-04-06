package venn.gui;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMTreeManager;
import org.apache.batik.svggen.ExtensionHandler;
import org.apache.batik.svggen.GenericImageHandler;
import org.apache.batik.svggen.ImageHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.svggen.SVGShape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGGraphics2DWithPaintLog extends Graphics2D {
	private SVGGraphics2D gr;
	public List<String> paintLog;


	public SVGGraphics2DWithPaintLog() {
	}
	
	public SVGGraphics2DWithPaintLog(Document arg0) {
		gr = new SVGGraphics2D(arg0);
		paintLog = new ArrayList<String>();
	}


	public void addRenderingHints(Map arg0) {
		gr.addRenderingHints(arg0);
	}

	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		gr.clearRect(arg0, arg1, arg2, arg3);
	}

	public void clip(Shape arg0) {
		gr.clip(arg0);
	}

	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		gr.clipRect(arg0, arg1, arg2, arg3);
	}

	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		gr.copyArea(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public Graphics create() {
		 SVGGraphics2D g = (SVGGraphics2D)gr.create();
		 SVGGraphics2DWithPaintLog res = new SVGGraphics2DWithPaintLog();
		 res.gr = g;
		 res.paintLog = paintLog;
		 return res;
	}

	public Graphics create(int x, int y, int width, int height) {
		SVGGraphics2D g = (SVGGraphics2D)gr.create(x, y, width, height);
		SVGGraphics2DWithPaintLog res = new SVGGraphics2DWithPaintLog();
		res.gr = g;
		res.paintLog = paintLog;
		return res;
	}

	public void dispose() {
		gr.dispose();
	}

	public void draw(Shape arg0) {
		gr.draw(arg0);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		gr.draw3DRect(x, y, width, height, raised);
	}

	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		gr.drawArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		gr.drawBytes(data, offset, length, x, y);
	}

	public void drawChars(char[] data, int offset, int length, int x, int y) {
		gr.drawChars(data, offset, length, x, y);
	}

	public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
		gr.drawGlyphVector(arg0, arg1, arg2);
	}

	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2,
			int arg3) {
		gr.drawImage(arg0, arg1, arg2, arg3);
	}

	public boolean drawImage(Image arg0, AffineTransform arg1,
			ImageObserver arg2) {
		return gr.drawImage(arg0, arg1, arg2);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		return gr.drawImage(arg0, arg1, arg2, arg3, arg4);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		return gr.drawImage(arg0, arg1, arg2, arg3);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		return gr.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		return gr.drawImage(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		return gr.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
				arg8, arg9, arg10);
	}

	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		return gr.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
				arg8, arg9);
	}

	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		gr.drawLine(arg0, arg1, arg2, arg3);
	}

	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		gr.drawOval(arg0, arg1, arg2, arg3);
	}

	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		gr.drawPolygon(arg0, arg1, arg2);
	}

	public void drawPolygon(Polygon p) {
		gr.drawPolygon(p);
	}

	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		gr.drawPolyline(arg0, arg1, arg2);
	}

	public void drawRect(int arg0, int arg1, int arg2, int arg3) {
		gr.drawRect(arg0, arg1, arg2, arg3);
	}

	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
		gr.drawRenderableImage(arg0, arg1);
	}

	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
		gr.drawRenderedImage(arg0, arg1);
	}

	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		gr.drawRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void drawString(AttributedCharacterIterator arg0, float arg1,
			float arg2) {
		gr.drawString(arg0, arg1, arg2);
	}

	public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
		gr.drawString(arg0, arg1, arg2);
	}

	public void drawString(String arg0, float arg1, float arg2) {
		gr.drawString(arg0, arg1, arg2);
	}

	public void drawString(String arg0, int arg1, int arg2) {
		gr.drawString(arg0, arg1, arg2);
	}

//	public boolean equals(Object obj) {
//		return gr.equals(obj);
//	}

	public void fill(Shape arg0) {
		gr.fill(arg0);
	}

	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		gr.fill3DRect(x, y, width, height, raised);
	}

	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		gr.fillArc(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		gr.fillOval(arg0, arg1, arg2, arg3);
	}

	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		gr.fillPolygon(arg0, arg1, arg2);
	}

	public void fillPolygon(Polygon p) {
		gr.fillPolygon(p);
	}

	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		gr.fillRect(arg0, arg1, arg2, arg3);
	}

	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		gr.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
	}

//	public void finalize() {
//		gr.finalize();
//	}

	public Color getBackground() {
		return gr.getBackground();
	}

	public Shape getClip() {
		return gr.getClip();
	}

	public Rectangle getClipBounds() {
		return gr.getClipBounds();
	}

	public Rectangle getClipBounds(Rectangle r) {
		return gr.getClipBounds(r);
	}

	public Rectangle getClipRect() {
		return gr.getClipRect();
	}

	public Color getColor() {
		return gr.getColor();
	}

	public Composite getComposite() {
		return gr.getComposite();
	}

	public List getDefinitionSet() {
		return gr.getDefinitionSet();
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return gr.getDeviceConfiguration();
	}

	public final Document getDOMFactory() {
		return gr.getDOMFactory();
	}

	public final DOMTreeManager getDOMTreeManager() {
		return gr.getDOMTreeManager();
	}

	public final ExtensionHandler getExtensionHandler() {
		return gr.getExtensionHandler();
	}

	public Font getFont() {
		return gr.getFont();
	}

	public FontMetrics getFontMetrics() {
		return gr.getFontMetrics();
	}

	public FontMetrics getFontMetrics(Font arg0) {
		return gr.getFontMetrics(arg0);
	}

	public FontRenderContext getFontRenderContext() {
		return gr.getFontRenderContext();
	}

	public final SVGGeneratorContext getGeneratorContext() {
		return gr.getGeneratorContext();
	}

	public final GenericImageHandler getGenericImageHandler() {
		return gr.getGenericImageHandler();
	}

	public GraphicContext getGraphicContext() {
		return gr.getGraphicContext();
	}

	public final ImageHandler getImageHandler() {
		return gr.getImageHandler();
	}

	public Paint getPaint() {
		return gr.getPaint();
	}

	public Object getRenderingHint(Key arg0) {
		return gr.getRenderingHint(arg0);
	}

	public RenderingHints getRenderingHints() {
		return gr.getRenderingHints();
	}

	public Element getRoot() {
		return gr.getRoot();
	}

	public Element getRoot(Element arg0) {
		return gr.getRoot(arg0);
	}

	public final SVGShape getShapeConverter() {
		return gr.getShapeConverter();
	}

	public Stroke getStroke() {
		return gr.getStroke();
	}

	public final Dimension getSVGCanvasSize() {
		return gr.getSVGCanvasSize();
	}

	public Element getTopLevelGroup() {
		return gr.getTopLevelGroup();
	}

	public Element getTopLevelGroup(boolean arg0) {
		return gr.getTopLevelGroup(arg0);
	}

	public AffineTransform getTransform() {
		return gr.getTransform();
	}

	public int hashCode() {
		return gr.hashCode();
	}

	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
		return gr.hit(arg0, arg1, arg2);
	}

	public boolean hitClip(int x, int y, int width, int height) {
		return gr.hitClip(x, y, width, height);
	}

	public void rotate(double arg0, double arg1, double arg2) {
		gr.rotate(arg0, arg1, arg2);
	}

	public void rotate(double arg0) {
		gr.rotate(arg0);
	}

	public void scale(double arg0, double arg1) {
		gr.scale(arg0, arg1);
	}

	public void setBackground(Color arg0) {
		gr.setBackground(arg0);
	}

	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		gr.setClip(arg0, arg1, arg2, arg3);
	}

	public void setClip(Shape arg0) {
		gr.setClip(arg0);
	}

	public void setColor(Color arg0) {
		gr.setColor(arg0);
	}

	public void setComposite(Composite arg0) {
		gr.setComposite(arg0);
	}

	public final void setExtensionHandler(ExtensionHandler arg0) {
		gr.setExtensionHandler(arg0);
	}

	public void setFont(Font arg0) {
		gr.setFont(arg0);
	}

	public void setPaint(Paint arg0) {
		gr.setPaint(arg0);
	}

	public void setPaintMode() {
		gr.setPaintMode();
	}

	public void setRenderingHint(Key arg0, Object arg1) {
		gr.setRenderingHint(arg0, arg1);
	}

	public void setRenderingHints(Map arg0) {
		gr.setRenderingHints(arg0);
	}

	public void setStroke(Stroke arg0) {
		gr.setStroke(arg0);
	}

	public final void setSVGCanvasSize(Dimension arg0) {
		gr.setSVGCanvasSize(arg0);
	}

	public void setTopLevelGroup(Element arg0) {
		gr.setTopLevelGroup(arg0);
	}

	public void setTransform(AffineTransform arg0) {
		gr.setTransform(arg0);
	}

	public void setXORMode(Color arg0) {
		gr.setXORMode(arg0);
	}

	public void shear(double arg0, double arg1) {
		gr.shear(arg0, arg1);
	}

	public void stream(Element arg0, Writer arg1, boolean arg2)
			throws SVGGraphics2DIOException {
		gr.stream(arg0, arg1, arg2);
	}

	public void stream(Element arg0, Writer arg1)
			throws SVGGraphics2DIOException {
		gr.stream(arg0, arg1);
	}

	public void stream(String arg0, boolean arg1)
			throws SVGGraphics2DIOException {
		gr.stream(arg0, arg1);
	}

	public void stream(String arg0) throws SVGGraphics2DIOException {
		gr.stream(arg0);
	}

	public void stream(Writer arg0, boolean arg1)
			throws SVGGraphics2DIOException {
		gr.stream(arg0, arg1);
	}

	public void stream(Writer arg0) throws SVGGraphics2DIOException {
		gr.stream(arg0);
	}

	public String toString() {
		return gr.toString();
	}

	public void transform(AffineTransform arg0) {
		gr.transform(arg0);
	}

	public void translate(double arg0, double arg1) {
		gr.translate(arg0, arg1);
	}

	public void translate(int arg0, int arg1) {
		gr.translate(arg0, arg1);
	}


}
