package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

// Begin from StartJApplet11.java

/*
 <applet code="RanDistTransformApplet.class" width=200 height=300>
 </applet>
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *  This program will run as an applet inside
 *  an application frame.<br> <br>
 *
 * The applet uses the HistPanel to display contents of
 *  an instance of Histogram. HistFormat used by HistPanel to
 *  format the scale values.
 *
 *  Includes "Go" button to add random values from a Gaussian
 *  distribution to the histogram. The number of values taken from
 *  entry in a JTextField. "Clear"  button clears the histogram.
 *  In standalone mode, the Exit button closes the program.
 *  <br><br>
 */
public class RanDistTransformApplet extends JApplet
             implements ActionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// Use the HistPanel JPanel subclass here
  HistPanel fOutputPanel;

  Histogram fHistogram ;
  int fNumDataPoints = 1000;

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

    JPanel control_panel = new JPanel ();

    control_panel.add (fTextField);
    control_panel.add (fGoButton);
    control_panel.add (fClearButton);
    control_panel.add (fExitButton);

    if (fInBrowser) fExitButton.setEnabled (false);

    panel.add (control_panel,"South");

    // Add text area with scrolling to the content_pane.
    content_pane.add (panel);

  } // init

  /** Respond to the buttons. **/
  public void actionPerformed (ActionEvent e) {
    Object source = e.getSource ();
    if (source == fGoButton || source == fTextField) {
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
    } else if (source == fClearButton) {
        fHistogram.clear ();
        repaint ();
    } else if (!fInBrowser)
        System.exit (0);
  } // actionPerformed


  /** Create a histogram and fill it with data using
    * transformation algorithm to simulate radioactive
    * decay lifetimes.
   **/
  void makeHist () {
    // Create an instance of the Random class for
    // producing our random values.
    java.util.Random r = new java.util.Random ();

    // Create an instance of our basic histogram class.
    // Make it wide enough enough to include most of the
    // gaussian values.
    if (fHistogram == null)
        fHistogram = new Histogram ("Radioactive Lifetimes",
                                "Arbitrary Units",
                                20,0.0,5.0);

    // Use the transformation method to generate a
    // radioactive decay distribution
    for (int i=0; i < fNumDataPoints; i++) {
        // y (x) = -ln (1-x/tau);
        // Generate ran FP between 0 & 1
        double x = r.nextDouble ();
        double val = - Math.log (1-x);
        fHistogram.add (val);
    }
  } // makeHist

  /** Use main() to run in standalone mode. **/
  public static void main (String[] args) {
    //
    int frame_width=450;
    int frame_height=300;

    // Create an instance of this applet an add to a frame.
    RanDistTransformApplet applet = new RanDistTransformApplet ();
    applet.fInBrowser = false;
    applet.init ();

    // Following anonymous class used to close window & exit program
    JFrame f = new JFrame ("Demo");
    f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Add applet to the frame
    f.getContentPane ().add ( applet);
    f.setSize (new Dimension (frame_width,frame_height));
    f.setVisible (true);
  } // main

} // class RanDistTransformApplet
