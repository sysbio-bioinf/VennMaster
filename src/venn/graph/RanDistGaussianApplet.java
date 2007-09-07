package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

// Include the tag here for testing with the Appletviewer
/*
 <applet code="RanDistTransformApplet.class" width=200 height=300>
 </applet>
*/

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
 */
public class RanDistGaussianApplet extends JApplet
             implements ActionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// Use the HistPanel JPanel subclass here
  HistPanel fOutputPanel;

  Histogram fHistogram;
  int fNumDataPoints = 5000;

  // A text field for input strings
  JTextField fTextField;

  // Flag for whether the applet is in a browser
  // or running via the main () below.
  boolean fInBrowser = true;

  //Buttons
  JButton fGoButton;
  JButton fClearButton;
  JButton fExitButton;

  /**
   * Create a User Interface with histograms and buttons to
   * control the program. A textfield holds number of entries
   * to be generated for the histogram.
   */
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

    JPanel controlPanel = new JPanel ();

    controlPanel.add (fTextField);
    controlPanel.add (fGoButton);
    controlPanel.add (fClearButton);
    controlPanel.add (fExitButton);

    if (fInBrowser) fExitButton.setEnabled (false);

    panel.add (controlPanel,"South");

    // Add text area with scrolling to the content_pane.
    content_pane.add (panel);

  }

  /** Respond to the buttons. **/
  public void actionPerformed (ActionEvent e) {
    Object source = e.getSource ();
    if (source == fGoButton || source == fTextField)  {
        String strNumDataPoints = fTextField.getText ();
        try {
          fNumDataPoints = Integer.parseInt (strNumDataPoints);
        }
        catch (NumberFormatException ex) {
          // Could open an error dialog here but just
          // display a message on the browser status line.
          showStatus ("Bad input value");
          return;
        }
        makeHist ();
        repaint ();
    }
    else if (source == fClearButton) {
        fHistogram.clear ();
        repaint ();
    } else if (!fInBrowser)
        System.exit (0);
  } // actionPerformed


  /** Creat the histogram and use Guassian - Polar Transform
    * algorithm to fill it with random data.
   **/
  void makeHist () {
    // Create an instance of the Random class for
    // producing our random values.
    java.util.Random r = new java.util.Random ();

    // Create an instance of our basic histogram class.
    // Make it wide enough enough to include most of the
    // gaussian values.
    if (fHistogram == null)
        fHistogram = new Histogram ("Gaussian - Polar Transform",
                                "Arbitrary Units",
                                25,-4.0,4.0);

    // Use the transformation method to generate a
    // radioactive decay distribution
    for (int i=0; i < fNumDataPoints; i++) {
        // Generate random vals -1.0 to 1.0
        double v1 = 2.0 * r.nextDouble () - 1.0;
        double v2 = 2.0 * r.nextDouble () - 1.0;
        // Restrict them to inside a unit circle
        double R = v1*v1 + v2*v2;

        if (R < 1.0) {
            double val = v1 * Math.sqrt (-2.0* (Math.log (R))/R);
            fHistogram.add (val);
        }
    }
  } // makeHist

  /** Use main() to run in standalone mode. **/
  public static void main (String[] args) {
    //
    int frame_width=450;
    int frame_height=300;

    // Create an instance of this applet an add to a frame.
    RanDistGaussianApplet applet = new RanDistGaussianApplet ();
    applet.fInBrowser = false;
    applet.init ();

    // Following anonymous class used to close window & exit program
    JFrame f = new JFrame ("Demo");
    f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Add applet to the frame
    f.getContentPane ().add ( applet);
    f.setSize (new Dimension (frame_width, frame_height));
    f.setVisible (true);
  } // main

} // class RanDistGaussianAppelt
