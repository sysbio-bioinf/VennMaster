package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

// Begun from StartJApplet11

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  *  This program will run as an applet inside
  *  an application frame.
  *
  * The applet uses the HistPanel to display contents of
  *  an instance of Histogram. HistFormat used by HistPanel to
  *  format the scale values.
  *
  *  Includes "Go" button to add random values from a Gaussian
  *  distribution to the histogram. The number of values taken from
  *  entry in a JTextField. "Clear"  button clears the histogram.
  *  In standalone mode, the Exit button closes the program.
  *
 **/
public class UIDrawHistApplet extends JApplet
             implements ActionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// Use the HistPanel JPanel subclass here
  HistPanel fOutputPanel = null;

  Histogram fHistogram;
  int fNumDataPoints   = 100;

  // A text field for input strings
  JTextField fTextField = null;

  // Flag for whether the applet is in a browser
  // or running via the main () below.
  boolean fInBrowser = true;

  //Buttons
  JButton fGoButton;
  JButton fClearButton;
  JButton fExitButton;

  /**
    * Create a User Interface with a textarea with sroll bars
    * and a Go button to initiate processing and a Clear button
    * to clear the textarea.
   **/
  public void init () {

    Container content_pane = getContentPane ();

    JPanel panel = new JPanel (new BorderLayout ());

    // Create a histogram with Gaussian distribution.
    makeHist ();

    // JPanel subclass here.
    fOutputPanel = new HistPanel (fHistogram);

    panel.add (fOutputPanel,"Center");

    // Use a textfield for an input parameter.
    fTextField =
      new JTextField (Integer.toString (fNumDataPoints), 10);

    // If return hit after entering text, the
    // actionPerformed will be invoked.
    fTextField.addActionListener (this);

    fGoButton = new JButton ("Go");
    fGoButton.addActionListener (this);

    fClearButton = new JButton ("Clear");
    fClearButton.addActionListener (this);

    fExitButton = new JButton ("Exit");
    fExitButton.addActionListener (this);

    JPanel control_panel = new JPanel ();

    control_panel.add (fTextField);
    control_panel.add (fGoButton);
    control_panel.add (fClearButton);
    control_panel.add (fExitButton);

    if (fInBrowser) fExitButton.setEnabled (false);

    panel.add (control_panel,"South");

    // Add text area with scrolling to the contentPane.
    content_pane.add (panel);

  } // init

  /** Respond to buttons. **/
  public void actionPerformed (ActionEvent e){
    Object source = e.getSource ();
    if (source == fGoButton || source == fTextField) {
        String strNumDataPoints = fTextField.getText ();

        try {
            fNumDataPoints = Integer.parseInt (strNumDataPoints);
        } catch (NumberFormatException ex) {
          // Could open an error dialog here but just
          // display a message on the browser status line.
          showStatus ("Bad input value");
          return;
        }
        makeHist ();
        repaint ();

    } else if (source == fClearButton  ) {
        fHistogram.clear ();
        repaint ();

    } else if (!fInBrowser)
        System.exit (0);

  } // actionPerformed

  /** Create a histogram if it doesn't yet exit. Fill it
    * with Gaussian random distribution.
   **/
  void makeHist () {
    // Create an instance of the Random class for
    // producing our random values.
    java.util.Random r = new java.util.Random ();

    // Them method nextGaussian in the class Random produces
    // a value centered at 0.0 and a standard deviation of 1.0.

    // Create an instance of our histogram class. Set the range
    // sot that it includes most of the distribution.
    if (fHistogram == null)
        fHistogram = new Histogram ("Gaussian Distribution",
                                  "random values",
                                  20,-3.0,3.0);

    // Fill histogram with Gaussian distribution
    for (int i=0; i < fNumDataPoints; i++) {
        double val = r.nextGaussian ();
        fHistogram.add (val);
    }
  } // makeHist

  /** Create a frame and add the applet to it. **/
  public static void main (String[] args) {
    // Dimensions for our frame
    int frame_width  = 450;
    int frame_height = 300;

    // Create an instance of the applet to add to the frame.
    UIDrawHistApplet applet = new UIDrawHistApplet ();
    applet.fInBrowser = false;
    applet.init ();

    // Create the frame with the title
    JFrame f = new JFrame ("Histogram with Gaussian");
    f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Add applet to the frame and display the frame.
    f.getContentPane ().add ( applet);
    f.setSize (new Dimension (frame_width,frame_height));
    f.setVisible (true);

  } // main

} // class UIDrawHistApplet
