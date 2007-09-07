// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003

package venn.graph;

import java.awt.*;

/** Drawing straight line onto the PlotPanel. **/
public class DrawLine extends DrawFunction
{

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
    *  @param x_scale 2 dimensional array holding lower and
    *    upper values of the function input scale range.
    *  @param y_scale 2 dimensional array holding lower and
    *    upper values of the function output scale range.
   **/
  public void draw (Graphics g,
                   int frame_start_x, int frame_start_y,
                   int frame_width, int frame_height,
                   double [] x_scale, double [] y_scale) {

    Color save_color = g.getColor ();

    g.setColor (fColor);

    // Check if ready to draw the line
    if (fParameters == null) return;

    // Get the number of horizontal scale values.
    int num_x_points = x_scale.length;
    int num_y_points = y_scale.length;


    // Get conversion factor from data scale to frame pixels
    double y_scaleFactor = frame_height/(y_scale[num_y_points-1] - y_scale[0]);


    // Get the vertical coord vs the first and last points on
    // the horizontal axis.
    double y0 = fParameters[0] + fParameters[1] * x_scale[0];
    double y1 = fParameters[0] + fParameters[1] * x_scale[num_x_points-1];

    // Convert to pixel nunber
    int y_0_frame = frame_height -  (int)(y0 * y_scaleFactor) + frame_start_y;
    int y_1_frame = frame_height -  (int)(y1 * y_scaleFactor) + frame_start_y;

    // Don't draw outside of the frame.
    g.setClip (frame_start_x, frame_start_y, frame_width, frame_height);

    // Draw the straight line.
    g.drawLine (frame_start_x, y_0_frame, frame_start_x+frame_width, y_1_frame);

    g.setColor (save_color);

  } // draw

} // class DrawLine
