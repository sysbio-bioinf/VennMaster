package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

// Begun from StartJApplet1.java

import java.awt.Container;

import javax.swing.JApplet;

/**
  *  Create an instance of Histogram, add a Gaussian generated
  *  distribution of data to it and display it.
 **/
public class DrawHistApplet extends JApplet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
Histogram fHistogram = null;

  public void init ()  {

    Container content_pane = getContentPane ();

    // Create a histogram with Gaussian distribution.
    makeHist ();

    // Create an instance of a JPanel sub-class
    HistPanel hist_panel = new HistPanel (fHistogram);

    // And add one or more panels to the JApplet panel.
    content_pane.add (hist_panel);

  } // init

  /**
    *  Create the histogram and a set of data drawn
    *  from a Gaussian random number generator.
   **/
  void makeHist () {
    // Create an instance of the Random class for
    // producing our random values.
    java.util.Random r = new java.util.Random ();

    // Them method nextGaussian in the class Random produces
    // a value centered at 0.0 and a standard deviation
    // of 1.0.

    // Create an instance of our basic gHistogram class.
    // Make it wide enough enough to include most of the
    // gaussian values.
    fHistogram = new Histogram ("Gaussian Distribution", "random values",
                             10,-2.0,2.0);

    // Fill histogram with Gaussian distribution
    for (int i=0; i<100000; i++) {
         double val = r.nextGaussian ();
         fHistogram.add (val);
    }
  } // makeHist

} // class DrawHistApplet

