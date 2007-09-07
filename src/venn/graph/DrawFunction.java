// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003

package venn.graph;
import java.awt.*;

/**
  *  Abstract base class for drawing functions onto
  *  the PlotPanel subclasses.
  *
 **/
public abstract class DrawFunction
{
  double [] fParameters;
  double [][] fData;
  Color fColor = Color.BLACK;

  /**
    *  Abstract base class for drawing functions onto
    *  the PlotPanel subclasses.
    *
    *  @param g graphics context
    *  @param frame_width display area width in pixels.
    *  @param frame_height display area height in pixels.
    *  @param frame_start_x horizontal point on display where
    *  drawing starts in pixel number.
    *  @param frame_start_y vertical point on display where
    *  drawing starts in pixel number.
    *  @param x_scale 2 dimensional array holding lower and
    *  upper values of the function input scale range.
    *  @param y_scale 2 dimensional array holding lower and
    *  upper values of the function output scale range.
   **/
  public abstract void draw (Graphics g,
                   int frame_start_x, int frame_start_y,
                   int frame_width, int frame_height,
                   double [] x_scale, double [] y_scale);

  /**
    *  Parameters and data for the function.
   **/
  public void setParameters (double [] parameters,
                             double[][] data) {
     fParameters = parameters;
     fData = data;
  }

  public void setColor (Color color){
    fColor = color;
  }
} // class DrawFunction
