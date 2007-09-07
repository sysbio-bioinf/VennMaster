// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003

package venn.graph;

import java.awt.*;

/**
  *  Drawing points on the PlotPanel. Allow for the option
  *  of different symbols as well as error bars.
 **/
public class DrawPoints extends DrawFunction
{
  double [] fXData;
  double [] fYData;

  double [] fXErr;
  double [] fYErr;

  // Symbol types for plotting
  public final static int RECT     = 0;
  public final static int RECTFILL = 1;
  public final static int OVAL     = 2;
  public final static int OVALFILL = 3;
  int fSymbolType = 3;// default filled oval

  Color fSymbolColor = Color.BLUE;
  Color fErrorColor  = Color.RED;

  /**
    *  Draw a straight line  onto the PlotPanel.
    *
    *  @param g graphics context
    *  @param frame_width display area width in pixels.
    *  @param frame_height display area height in pixels.
    *  @param frame_start_x horizontal point on display where
    *    drawing starts in pixel number.
    *  @param frame_start_y vertical point on display where
    *    drawing starts in pixel number.
    *  @param x_scale array holding lower and
    *    upper values of the function input scale range.
    *  @param y_scale array holding lower and
    *    upper values of the function output scale range.
   **/
  public void draw (Graphics g,
                    int frame_start_x, int frame_start_y,
                    int frame_width, int frame_height,
                    double [] x_scale, double [] y_scale){

    // Check if ready to draw the line
    if (fXData == null || fYData == null ) return;

    Color save_color = g.getColor ();

    // Get the number of horizontal scale values.
    int num_x_points = x_scale.length;
    int num_y_points = y_scale.length;

    // Get conversion factor from data scale to frame pixels
    double y_scale_factor = frame_height/(y_scale[num_y_points-1] - y_scale[0]);
    double x_scale_factor = frame_width/(x_scale[num_x_points-1] - x_scale[0]);

    // Plot points
    for (int i=0; i < fXData.length; i++) {

        // Convert to pixel nunber
        int y = frame_height - (int)((fYData[i] - y_scale[0]) * y_scale_factor)
                      + frame_start_y;
        int x =  (int)((fXData[i] - x_scale[0]) * x_scale_factor) + frame_start_x;

        // For data symbols get size relative to the frame.
        int sym_dim =  (int)(frame_width *.01);

        // Draw data point symbols
        g.setColor (fSymbolColor);

        // Now draw the desired symbol.
        switch  (fSymbolType) {
          case RECT :
            g.drawRect (x-sym_dim,y-sym_dim, 2*sym_dim, 2*sym_dim);
            break;

          case RECTFILL :
            g.fillRect (x-sym_dim,y-sym_dim, 2*sym_dim+1, 2*sym_dim+1);
            break;

          case OVAL :
            g.drawOval (x-sym_dim,y-sym_dim, 2*sym_dim, 2*sym_dim);
            break;

          case OVALFILL :
          default :
            g.fillOval (x-sym_dim,y-sym_dim, 2*sym_dim+1, 2*sym_dim+1);
            break;
        }

        g.setColor (fErrorColor);

        // Use error array references as flags for drawing errors
        if (fXErr != null) {
            int x_bar =  (int) (fXErr[i]*x_scale_factor/2.0);
            // Draw x error bar
            g.drawLine (x-x_bar,y,x+x_bar,y);
        }
        if (fYErr != null) {
            int y_bar =  (int) (fYErr[i]*y_scale_factor/2.0);
            // Draw y error bar
            g.drawLine (x,y-y_bar,x,y+y_bar);
        }
    }

    g.setColor (save_color);

  } // draw

  /**  Pass coords of the points to draw. **/
  public void setParameters (double [] param, double [][] data) {
    fYData = data[0];
    fXData = data[1];
    fYErr  = data[2];
    fXErr  = data[3];
  }

  /** Set the type of symbol to use for the points. **/
  public void setSymbolType (int type) {
    fSymbolType = type;
  }

} // class DrawPoints
