package venn.geometry;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Extending the Drag label to support multiple lines. Makes use of basic html
 * capabilities of {@link javax.swing.JLabel}
 * 
 * @author behrens
 * 
 */
public class MultilineDragLabel extends DragLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3518475580584503762L;
	private String origText;
	private String[] lines;

	public MultilineDragLabel(ITransformer transformer, String text, BitSet path) {

		super(transformer, generateHtmlString(text), path);
		this.origText = text;
		this.lines = text.split("\\r?\\n");
		setBackground(new Color(0xff, 0x55, 0x55, 0xa0));
		updateBounds(getGraphics());

	}

	private static String generateHtmlString(String text) {
		// convert a text string with newline characters into a basic html
		// string.

		String[] lines = text.split("\n");
		StringBuffer htmlString = new StringBuffer("<html>");
		htmlString.append(StringUtils.join(lines, "<br>"));
		htmlString.append("<html/>");

		return htmlString.toString();
	}

	@Override
	protected void updateBounds(Graphics g) {
		java.awt.Rectangle bounds = getBounds();
		String text = getText();

		if (text == null || origText == null) {
			bounds.width = 50;
			bounds.height = 10;
		} else {

			bounds.width = getMultilineWidth(g);
			bounds.height = getMultilineHeight(g);

		}
		java.awt.Point p = transformer.transform(position);
		if (p != null) {
			bounds.x = p.x;
			bounds.y = p.y;
		}
		setBounds(bounds);
	}

	private int getMultilineHeight(Graphics g) {

		FontMetrics metrics = getFontMetrics(getFont());
		int myHeigth = 0;
		for (String line : lines) {
			Rectangle2D textBounds = metrics.getStringBounds(line, g);
			myHeigth += (int) Math.round(textBounds.getHeight());
		}
		return myHeigth + 6;
	}

	private int getMultilineWidth(Graphics g) {

		FontMetrics metrics = getFontMetrics(getFont());
		int maxWidth = 0;
		// System.out.println(getText());
		// System.out.println(origText);
		// System.out.println(lines);
		for (String line : lines) {
			Rectangle2D textBounds = metrics.getStringBounds(line, g);
			int w = (int) Math.round(textBounds.getWidth());
			maxWidth = (w > maxWidth) ? w : maxWidth;
		}
		return maxWidth + 20;

	}
}
