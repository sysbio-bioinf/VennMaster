package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003

import java.awt.*;
import javax.swing.*;

/**
  *  This abstract class extends JPanel and provides methods
  *  to display a plot of data points, a histogram, etc. The
  *  subclass will provide the overriding methods to draw the
  *  actual plot within a frame. <br>
  *
  *  For this class, the words <br>
  *    frame, title, label, scale  <br>
  *  are used in variable and method names, and comments and are
  *  defined as follows: <br><br>
  *
  *  frame  - The border line box drawn around the plot. <br>
  *  title  - A text title drawn along the top of the frame. <br>
  *  label  - Text along the horizontal axis of the plot. <br>
  *  scale  - The numbers drawn along each axes. <br> <br>
  *
  *  This class only provides for the display of scales along the
  *  left side vertical and bottom horizontal axes. A label is
  *  provided for the bottom horizontal axis but not for the
  *  vertical axis.<br>
  *
  *  The size and position of the frame, title, etc. within
  *  the panel is fixed by a group of constants, each of which
  *  specifies what fraction of the panel's dimensions a given element
  *  can use. <br>
  *
  *  Note that in the comments, "data" refers to what is being
  *  plotted and "pixels" refers to the drawing coordinates and
  *  dimensions.
  *
 **/
public abstract class PlotPanel extends JPanel {

  // Abstract methods for the subclass to override.

  // Provide a title for the top of the frame
  abstract String getTitle ();
  // Provide a label for the bottom horizontal axis.
  abstract String getXLabel ();
  // Create arrays of scale values to plot along the axes.
  abstract void getScaling ();
  // Draw within the the frame the actual plot of interest.
  abstract void paintContents (Graphics g);

  // Variables and constants.

  // Colors for the plot components.
  protected Color fFrameLineColor  = Color.BLACK;
  protected Color fFrameFillColor  = Color.LIGHT_GRAY;
  protected Color fAxesNumColor    = Color.GRAY;
  protected Color fTitleColor      = Color.BLACK;
  protected Color fBgColor         = Color.WHITE;

  // Dimension and position variables in pixel values. (Note that
  // position is indicated by the top corner x,y coords.)
  protected int fPanelWidth, fPanelHeight; // Panel size
  protected int fFrameWidth, fFrameHeight; // Frame size
  protected int fFrameX,fFrameY;           // Frame position
  protected int fTitleX,fTitleY;           // Title position
  protected int fTitleWidth,fTitleHeight;  // Title size

  // Position and size of the area where the vertical scale
  // numbers will be drawn in pixel values.
  protected int fVertScaleX,fVertScaleY,
                fVertScaleWidth,fVertScaleHeight;

  // Position and size of the area where the horizontal scale
  // numbers will be drawn in pixel values.
  protected int fHorzScaleX, fHorzScaleY,
                fHorzScaleWidth,fHorzScaleHeight;

  // Position and size of the area where the horzontal label
  // will be drawn in pixel values.
  protected int fHorzLabelX,fHorzLabelY,
                fHorzLabelWidth,fHorzLabelHeight;


  // Upper limits: if the data values are below these limits then
  // the x, y scale values are put in decimal format. If the data
  // values are above these limits, then the values are put into
  // scientific format.
  double fYDecimalSci = 100.0;
  double fXDecimalSci = 1000.0;

  // Lower limits: if the data values are above these limits then
  // the x, y scale values are put in decimal format. If the data
  // values are below these limits, then the values are put into
  // scientific format.
  double fYLoDecimalSci = 0.01;
  double fXLoDecimalSci = 0.01;


  // The drawText() method first defines a rectangle for the area
  // in which it will draw the given text. The following constants
  // indicate how to align the text within the rectangle.
  final static int CENTER = 0;
  final static int LEFT   = 1;
  final static int RIGHT  = 2;


  // The following constants determine the sizes of the plot
  // elements relative to the panel. These are used in the
  // method getPositions().

  // Fraction of panel for the plot frame
  final static double FRAME_HT = 0.60;// 60% of panel height for frame
  final static double FRAME_WD = 0.75;// 75% of window width for frame
  final static double FRAME_X  = 0.20;// put top left corner at 20% of width
  final static double FRAME_Y  = 0.15;// put top left corner at 15% of height

  // Fraction of vertical area for the title
  protected double TITLE_HT = 0.10;

  // Fractions of the panel for left scale values
  final static double VSCALE_X = 0.100;
  final static double VSCALE_HT= 0.075;
  final static double VSCALE_WD= 0.18;

  // Fraction of vertical for the horizontal scale values
  protected double HSCALE_HT= 0.07;

  /**
    * Determine the positions of the title, label and scales
    * relative to the frame. <br>
    *
    * Use 60% of vertical space for the graph,<br>
    *  "  15%  "    "      "     "  top title area <br>
    *  "  25%  "    "      "     "  bot scale & label area <br>
    * Use 75% of horizonal space for the graph,<br>
    *   " 18%  "    "      "     "   vertical scale values <br>
    *   "  5%  "    "      "     "   right margin <br>
    *
   **/
  public void getPositions () {
    // First obtain the panel dimensions
    fPanelWidth  = getSize ().width;
    fPanelHeight = getSize ().height;

    // Then assign the frame dimensions.
    fFrameWidth =  (int) (FRAME_WD * fPanelWidth);
    fFrameHeight=  (int) (FRAME_HT * fPanelHeight);

    // Locate the frame by assigning its top left corner coords.
    fFrameX =  (int)(FRAME_X * fPanelWidth);
    fFrameY =  (int)(FRAME_Y * fPanelHeight);

    // Coordinates for the title
    fTitleX = fFrameX ;
    fTitleHeight =  (int)(TITLE_HT * fPanelHeight);
    fTitleY = fFrameY - fTitleHeight;
    fTitleWidth = fFrameWidth;

    // Coordinates for the vertical scale along the left
    // side axis.

    // Use all the horizontal room from the left side
    // up to the left edge of the frame.
    fVertScaleX      = 0; // Left most position
    // Use the VSCALE_WD fraction of the panel's width.
    fVertScaleWidth  = (int)(VSCALE_WD * fPanelWidth);
    // Use the VSCALE_HT fraction of the panel's height.
    fVertScaleHeight = (int)(VSCALE_HT * fPanelHeight);


    // Coordinates for the  horizontal scale along the
    // bottom axis.

    // Use the HSCALE_HT fraction of the panel's height
    fHorzScaleHeight =  (int)(HSCALE_HT * fPanelHeight);
    fHorzScaleY = fFrameY + fFrameHeight;

    // Coordinates for the horizontal label
    fHorzLabelY      = fHorzScaleY  + fHorzScaleHeight;
    fHorzLabelHeight = fPanelHeight - fHorzLabelY;
    fHorzLabelWidth  = fFrameWidth;
    fHorzLabelX      = fFrameX;

  } // getPositions


  /**
    *  The user can assign different colors for the frame elements.
    *  @param line color of frame line.
    *  @param fill color of frame background
    *  @param title color for the title above the frame
    *  @param background the color outside of the plot frame.
    *
   **/
  public void setFrameColors (Color line, Color fill,
                              Color numbers,
                              Color title,
                              Color background) {

    if (line       != null) fFrameLineColor = line;
    if (fill       != null) fFrameFillColor = fill;
    if (numbers    != null) fAxesNumColor   = numbers;
    if (title      != null) fTitleColor     = title;
    if (background != null) fBgColor        = background;

  } // setFrameColors


  /**
    * This method draws the frame, title, and the horizontal axis label.
    * It then invokes paintContents(), which the subclass overrides
    * to draw inside the frame. The paintConents() method is also
    * responsible for invoking drawAxesNumbers() to draw the axes scale
    * numbers. <br>
    *
    * The method first draws the panel and then invokes getPositions()
    * to obtain the coordinates for the frame, the title, labels, etc.
    * It then draws the title and x axis label using the drawText()
    * method and then draws the frame with paintFrame(). At the end it
    * invokes the paintContents () method.
    *
   **/
  public void paintComponent (Graphics g){
    // Set panel background color.
    setBackground (fBgColor);

    // Draw the panel.
    super.paintComponent (g);

    // Get the positions for the titles, labels, etc.
    getPositions ();

    // Draw the top title
    g.setColor (fTitleColor);
    drawText (g, getTitle (),
              fTitleX, fTitleY, fTitleWidth, fTitleHeight,0,
              CENTER);

    // Draw the bottom label
    drawText (g, getXLabel (),
              fHorzLabelX, fHorzLabelY,
              fHorzLabelWidth, fHorzLabelHeight,0,
              CENTER);


    // Draw the plot frame.
    paintFrame (g);

    // Finally, draw the the plot within in the frame
    // with paintConents(). It will also be responsible
    // for drawing the axes scale values.
    // This method must be overriden.
    paintContents (g);

  } // paintComponent


  /** Draw the plot frame. **/
  public void paintFrame (Graphics g) {
    Color old_color = g.getColor ();
    g.setColor (fFrameLineColor);
    g.drawRect (fFrameX, fFrameY, fFrameWidth, fFrameHeight);
    g.setColor (old_color);
  }

  /**
    * This method draws scale numbers along the left vertical and bottom
    * horizontal axes. It uses PlotFormat to format the numbers. The subclass
    * can use its getScaling() method to determine the two data
    * scale arrays. <br>
    *
    * @param g graphics context
    * @param xValue array of data values for horizontal scale axis.
    * @param yValue array of data values for vertical scale axis.
   **/
  void drawAxesNumbers (Graphics g,
                        double [] x_value,
                        double [] y_value) {

    // Check that both axes have scale values to draw.
    if (x_value.length == 0 || y_value.length == 0) return;

    String str_value;
    int type_size = 0;

    // Set the color for the scale numbers.
    g.setColor (fAxesNumColor);

    // Draw the horizontal axis scale numbers.

    // First determine the data scale range.
    double x_range = x_value[x_value.length-1] - x_value[0];
    if (x_range <= 0.0 ) return;

    // Next determine the spacing between numbers along the axis.
    double del_horz = (double)fFrameWidth/(double) (x_value.length);
    int horz;

    // Loop over the horizontal scale value array and draw
    // each value along the axis. Start from the rightmost value
    // and count down.
    for (int i = x_value.length-1; i >= 0 ; i--) {

        // Scale the data value to a pixel coordinate along the x axis.
        horz =  (int)(fFrameWidth * ((x_value[i] - x_value[0])/x_range)
                - del_horz/2.0) + fFrameX;

        // Don't let string go too far to the right. Use no more than
        // the space available.
        if ((horz + del_horz) > fPanelWidth) del_horz = fPanelWidth - horz;

        // Format the x data value into a string
        str_value = PlotFormat.getFormatted (x_value[i],
                                            fXDecimalSci,
                                            fXLoDecimalSci,
                                            2);

        // Now draw the number on the axis within the given box defined
        // by the horz, fHorzScaleY, del_horz, and fHorzScaleHeight values.
        //
        // Use the first value to get the size of the font as selected by
        // drawText() so that the number would fit into the area available.
        if (i == x_value.length-1)
            type_size = drawText (g, str_value,
                                  horz, fHorzScaleY,
                                  (int)del_horz, fHorzScaleHeight,
                                  0, CENTER);
        else
             drawText (g, str_value,
                       horz, fHorzScaleY,
                       (int)del_horz, fHorzScaleHeight,
                       type_size, CENTER);
    }

    // Draw the vertical axis scale numbers.

    // First determine the vertical data scale range.
    double y_range = y_value[y_value.length-1] - y_value[0];
    if (y_range <= 0.0) return;

    // Loop over the vertical scale value array and draw
    // each value along the axis. Start from the topmost value
    // and count down.
    for (int i = y_value.length-1; i >= 0 ; i--) {

        // Convert the y value to a string
        str_value = PlotFormat.getFormatted (y_value[i],
                                            fYDecimalSci, fYLoDecimalSci,
                                            2);

        // Scale the data value to a pixel coordinate along the y axis.
        fVertScaleY = fFrameY + fFrameHeight -
                     (int)(fFrameHeight * ((y_value[i]-y_value[0])/y_range))
                     - fVertScaleHeight;


        // Now draw the number on the axis within the given box defined
        // by the fVertScaleX, fVertScaleY, fVertScaleWidth, and
        // fVertScaleHeight values.
        //
        // Use the first value to get the size of the font as selected by
        // drawText() so that the number would fit into the area available.
        if (i == y_value.length-1)
            type_size = drawText (g, str_value,
                                 fVertScaleX, fVertScaleY,
                                 fVertScaleWidth, fVertScaleHeight,
                                 0, RIGHT);
        else
            drawText (g, str_value,
                     fVertScaleX, fVertScaleY,
                     fVertScaleWidth, fVertScaleHeight,
                     type_size, RIGHT);
    }

  } // drawAxesNumbers


  /**
    *  Draw a string in the center of a given box. The method will
    *  reduce the font size if necessary to fit the string within
    *  the box. You can, however, use the the fixed_type_size_value
    *  parameter to set the text size to a specific. The method won't
    *  draw the string if it doesn't fit in the box. <br>
    *
    *  The position of the string within the box passed
    *  as LEFT, CENTER or RIGHT constant value. <br>
    *
    *  The method returns the value of the font size.<br>
    *
    *  @param g graphics context
    *  @param msg the text to draw
    *  @param x_box horizontal coordinate of box top left corner
    *  @param y_box vertical coordinate of box top left corner
    *  @param box_width horizontal size of the box
    *  @param box_height vertical size of the box
    *  @param fixed_type_size_value set the size of the text rather than
    *  letting method determine it.
    *  @param relative_position position within the box for the text.
   **/
  int drawText (Graphics g, String msg,
                int x_box, int y_box,
                int box_width, int box_height,
                int fixed_type_size_value,
                int relative_position) {

    boolean fixed_type_size = false;
    int type_size = 24;

      // Fixed to a particular type size.
    if (fixed_type_size_value > 0) {
        fixed_type_size = true;
        type_size = fixed_type_size_value;
    }

    int type_size_min = 8;
    int x = x_box,y = y_box;

    do  {
      // Create the font and pass it to the  Graphics context
      g.setFont (new Font ("Monospaced",Font.PLAIN,type_size));

      // Get measures needed to center the message
      FontMetrics fm = g.getFontMetrics ();

      // How many pixels wide is the string
      int msg_width = fm.stringWidth (msg);

      // How tall is the text?
      int msg_height = fm.getHeight ();

      // See if the text will fit in the allotted
      // vertical limits
      if (msg_height < box_height && msg_width < box_width) {
          y = y_box + box_height/2 + (msg_height/2);
          if (relative_position == CENTER)
              x = x_box + box_width/2 -  (msg_width/2);
          else if (relative_position == RIGHT)
            x = x_box + box_width - msg_width;
          else
            x = x_box;
          break;
      }

      // If fixedTypeSize and wouldn't fit, don't draw.
      if (fixed_type_size) return -1;

      // Try smaller type
      type_size -= 2;

    } while (type_size >= type_size_min);

    // Don't display the numbers if they did not fit
    if (type_size < type_size_min) return -1;

    // Otherwise, draw and return positive signal.
    g.drawString (msg,x,y);
    return type_size;
  } // drawText

} // class PlotPanel

