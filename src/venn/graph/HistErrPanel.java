package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003


import java.awt.*;

/**
  *  Display the histogram data. Subclass of PlotPanel that
  *  provides the option of drawing error bars.
 **/
public class HistErrPanel extends PlotPanel
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// Histogram access
  protected Histogram fHistogram;

  // Flags values to indicate type of bin display
  public final static int DRAW_BARS     = 0;
  public final static int DRAW_PTS      = 1;
  public final static int DRAW_PTS_ERRS = 2;
  int fDrawType = DRAW_BARS;

  // Color settings for the bin display
  Color fSymColor      = Color.BLUE;
  Color fErrorBarColor = Color.BLUE;

  protected Color fBarLineColor = Color.DARK_GRAY;
  protected Color fBarFillColor = Color.PINK;
  int gap = 2; // 2 pixels between bars

  // Include the option to draw a function on the panel
  DrawFunction [] fDrawFunctions = null;
  boolean fDrawFunctionFlag = false;

  // Fractional margin between highest bar and top frame
  double fTopMargin  = 0.05;
  // Fractional margnin between side bars and frame
  double fSideMargin = 0.01;

  // Arrays to hold the numbers for the axes scale.
  double [] fXScaleValue;
  double [] fYScaleValue;

  // Number of values to put on each axis.
  int fNumYScaleValues = 2;
  int fNumXScaleValues = 5;

  // Symbol types for plotting points
  public final static int RECT     = 0;
  public final static int RECTFILL = 1;
  public final static int OVAL     = 2;
  public final static int OVALFILL = 3;
  // Set default symbol to filled oval.
  int fSymbolType = OVALFILL;

  /**  Create the panel with the histogram. **/
  public HistErrPanel (Histogram histogram) {
    fHistogram = histogram;
    getScaling ();
  } // ctor

  /**
    * Create the panel with the histogram and pass array
    * of functions to draw over the histogram plot.
   **/
  public HistErrPanel (Histogram histogram,
                      DrawFunction [] fDrawFunctions) {
    fHistogram = histogram;
    getScaling ();

    // Option of drawing on top of the histogram.
    if ( fDrawFunctions != null){
        this.fDrawFunctions = fDrawFunctions;
        fDrawFunctionFlag = true;
    }
  } // ctor


  /** Switch to a new histogram. **/
  public void setHistogram (Histogram histogram) {
    fHistogram = histogram;
    getScaling ();
    repaint ();
  } // setHistogram

  /**
    * Get the values for putting scaling numbers on
    * the plot axes.
   **/
  void getScaling () {
    fYScaleValue = new double[fNumYScaleValues];
    // Use lowest value of 0;
    fYScaleValue[0] = 0.0;

    fXScaleValue = new double[fNumXScaleValues];
    // First get the low and high values;
    fXScaleValue[0] = fHistogram.getLo ();
    fXScaleValue[fNumXScaleValues-1] = fHistogram.getHi ();

    // Then calculate the difference between the values
    //  (assumes linear scale)
    double range = fXScaleValue[fNumXScaleValues-1] -
                        fXScaleValue[0];
    double dx = range/ (fNumXScaleValues-1);

    // Now set the intermediate scale values.
    for (int i=1; i <  (fNumXScaleValues-1); i++) {
        fXScaleValue[i] = i*dx + fXScaleValue[0];
    }
  } // getScaling

  /** Optional bar color settings. **/
  public void setBarColors (Color line, Color fill) {
    if ( line != null) fBarLineColor = line;
    if ( fill != null) fBarFillColor = fill;
  }

  /** Optional symbol color settings. **/
  public void setSymColors (Color color) {
    if ( color != null) fSymColor=color;
  }


  /**
    * Overrides the abstract method in PlotPanel superclass.
    * Draw the vertical bars that represent the bin contents.
    * Draw also the numbers for the scales on the axes.
   **/
  void paintContents (Graphics g) {
    // Get the histogram max value and bin data
    int maxDataValue = fHistogram.getMax ();

    // Ignore if no data in the histogram.
    // Assumes no negative contents.
    if (maxDataValue == 0) return;

    // Make vertical room for error bars
    if (fDrawType == DRAW_PTS_ERRS) {
        // Use estimate of the error bar for max value
        maxDataValue += Math.sqrt (maxDataValue);
    }

    // Get the histogram bin array.
    int [] bins = fHistogram.getBins ();

    // Remember foreground color for later
    Color oldColor = g.getColor ();

    // Choose to draw bins as either points with or without error bars
    // or vertical column bars.
    switch (fDrawType) {
      case DRAW_PTS:
      case DRAW_PTS_ERRS:
        drawPoints (g,bins,maxDataValue);
        break;

      case DRAW_BARS:
      default:
        drawBars (g,bins,maxDataValue);
        break;
    }

    // Draw the numbers along the axes
    drawAxesNumbers (g, fXScaleValue, fYScaleValue);

    // Draw the overlay functions
    if (fDrawFunctionFlag) {
        for (int i=0; i < fDrawFunctions.length; i++){
            fDrawFunctions[i].draw (
                          g,
                          fFrameX,     fFrameY,
                          fFrameWidth, fFrameHeight,
                          fXScaleValue,fYScaleValue);
        }
     }
    g.setColor (oldColor); //reset original color

  } // paintContents

  /**
    * Draw vertical bars to represent the histogram
    * bin contents.
    * @param g graphics context
    * @param bins histogram array
    * @param maxDataValue the large bin value. Used to set
    * the scale of the histogram.
   **/
  void drawBars (Graphics g, int [] bins, int max_data_value) {
    // Find the left margin width.
    int side_space =  (int)(fFrameWidth*fSideMargin);

    // Find the width of the frame within which to draw
    // the bars.
    // Leave room for gaps between frame and the sides of
    // the first and last bars.
    int draw_width= fFrameWidth - 2 * side_space -
                                  (bins.length-1) * gap;

    // Leave a gap between top of tallest bar and the frame.
    int draw_height=  (int)(fFrameHeight* (1.0 - fTopMargin));

    // Calculate step size between adjacent bars.
    // To avoid build up of roundoff errors, the bar
    // positions will be calculated from a FP value.
    float step_width= (float)draw_width/(float)bins.length;
    int bar_width = Math.round (step_width);
    step_width += gap;

    // Scale the bars to the maximum bin value.
    float scale_factor= (float)draw_height/max_data_value;

    // Starting horizontal point on left hand side.
    int start_x = fFrameX + side_space;
    // Starting vertical point on bottom left of frame
    int start_y = fFrameY + fFrameHeight;

    for (int i=0; i < bins.length; i++) {
        int bar_height =  (int) (bins[i] * scale_factor);

        int bar_x =  (int)(i*step_width) + start_x;

        // Bar drawn from top left corner
        int bar_y= start_y - bar_height;

        g.setColor (fBarLineColor);
        g.drawRect (bar_x, bar_y, bar_width, bar_height);

        g.setColor (fBarFillColor);
        g.fillRect (bar_x+1, bar_y+1, bar_width-2, bar_height-1);
    }

    // Find the scale value of the full frame height.
    fYScaleValue[fYScaleValue.length-1] =
      (double)(fFrameHeight/scale_factor);

  } // drawBars


  /**
    *  Draw points instead of column bars for the histogram contents.
    *  Option to include error bars.
   **/
  void drawPoints (Graphics g, int [] bins, int max_data_value) {
    // If error bars on the points requested, then
    // obtain the error array.
    double [] err_y = null;
    if (fDrawType == DRAW_PTS_ERRS) {
      // Get the histogram error array. If the histogram is
      // not the HistogramStatR1 subclass, an exception will occur
      // here.
      if (fHistogram instanceof HistogramStatR1){
          err_y =  ( (HistogramStatR1)fHistogram).getBinErrors ();
          if (err_y == null) return;
      } else
          return;
    }

    // Find the left margin width.
    int side_space =  (int) (fFrameWidth*fSideMargin);

    // For data symbols get size relative to the frame.
    int sym_dim =  (int) (fFrameWidth *.01);

    // Leave a gap between top of tallest bar and the frame.
    int draw_height=  (int) (fFrameHeight* (1.0 - fTopMargin));

    // Scale the data points to the maximum bin value.
    float scale_factor= (float)draw_height/max_data_value;

    // Calculate step size between adjacent points.
    // To avoid build up of roundoff errors, the point
    // positions will be calculated from a FP value.
    float step_width= (float)fFrameWidth/ (float)bins.length;

    // Calculate starting point horizontally. Put points
    // in middle of bin.
    int start_x = Math.round (step_width/2) + fFrameX;
    // Starting vertical point on bottom left of frame
    int start_y = fFrameY + fFrameHeight;

    // Use half the width of a bin as the x error bar.
    int x_error_bar = fFrameWidth/(2 * bins.length);

    for (int i=0; i < bins.length; i++) {
      int x =  (int) (i * step_width)+start_x;
      int y = start_y -  (int) (bins[i] * scale_factor);

      // Draw data point symbols
      g.setColor (fSymColor);
      switch  (fSymbolType) {
        case RECT :
            g.drawRect (x-sym_dim, y-sym_dim, 2*sym_dim, 2*sym_dim);
            break;

        case RECTFILL :
            g.fillRect (x-sym_dim, y-sym_dim, 2*sym_dim+1, 2*sym_dim+1);
            break;

        case OVAL :
            g.drawOval (x-sym_dim, y-sym_dim, 2*sym_dim, 2*sym_dim);
            break;

        case OVALFILL :
        default :
            g.fillOval (x-sym_dim, y-sym_dim, 2*sym_dim+1, 2*sym_dim+1);
            break;
      }

      g.setColor (fErrorBarColor);

      if (fDrawType == DRAW_PTS_ERRS) {
          // Draw x error bar as bin width.
          g.drawLine (x-x_error_bar,y,x+x_error_bar,y);

          if (err_y != null) {
              int y_bar =  (int)(err_y[i] * scale_factor/2.0);
              // Draw y error bars but keep within the
              // frame.
              int dy_up = y - y_bar;
              if (dy_up < fFrameY) dy_up = fFrameY;
              int dy_down = y + y_bar;
              if (dy_down > start_y) dy_down = start_y;
              g.drawLine (x, dy_up, x, dy_down);
          }
       }
    }
    // Find the scale value of the full frame height.
    fYScaleValue[fYScaleValue.length-1] =
       (double)(fFrameHeight/scale_factor);

  } // drawPoints


  // Methods overriding those in PlotPanel
  String getTitle ()
  {  return fHistogram.getTitle ();}

  String getXLabel ()
  {  return fHistogram.getXLabel ();}

  /**
    *  Set type of symbol to use for a point in the plot.
    *  @param type set to one of the class constants.
   **/
  void setSymbolType (int type) {
    if (type < 0 || type > 3) return;
    fSymbolType = type;
  }

  /** Set flag for drawing the error bars or not. **/
  void setDrawType (int drawType) {
     fDrawType = drawType;
  }

  /**
    *  Pass object to array of functions to draw over
    *  the histogram distributions.
   **/
  void setDrawFunction ( DrawFunction [] functions) {
     fDrawFunctions = functions;
  }

  /** Pass flag to draw a function overlay. **/
  void setDrawFunctionEnabled (boolean flag) {
     fDrawFunctionFlag = flag;
     // Don't enable if no function available.
     // Could throw an exception or a warning dialog here.
     //if ( drawFunction = null) fDrawFunctionFlag = false;

  } // setDrawFunctionEnabled

} // class HistErrPanel

