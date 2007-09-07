package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

// Begin with StartJApplet11.java

/*
 <applet code="HistStatsApplet.class" width=200 height=300>
 </applet>
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This program will run as an applet inside
 * an application frame.
 *
 * The applet uses the HistPanel to display contents of
 * an instance of Histogram. HistFormat used by HistPanel to
 * format the scale values.<br><br>
 *
 * Includes "Go" button to add random values from a Gaussian
 * distribution to the histogram. The number of values taken from
 * entry in a JTextField. "Clear"  button clears the histogram.
 * In standalone mode, the Exit button closes the program.
 * <br><br>
 */
public class HistStatsApplet extends JApplet
             implements ActionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// Use the HistPanel JPanel subclass here
  HistPanel fOutputPanel;

  HistogramStat fHistogram;
  int fNumDataPoints = 100;


  // A text field for input strings
  JTextField fTextField;

  // Flag for whether the applet is in a browser
  // or running via the main () below.
  boolean fInBrowser = true;

  //Buttons
  JButton fGoButton;
  JButton fStatsButton;
  JButton fClearButton;
  JButton fExitButton;

  /**
    * Create a User Interface with a histogram and a Go button
    * to initiate processing and a Clear button to clear the .
    * histogram. In application mode, the Exit button stops the
    * program. Add a stats button to open a frame window to show
    * statistical measures.
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

    fStatsButton = new JButton ("Stats");
    fStatsButton.addActionListener (this);

    fClearButton = new JButton ("Clear");
    fClearButton.addActionListener (this);

    fExitButton = new JButton ("Exit");
    fExitButton.addActionListener (this);

    JPanel fControlPanel = new JPanel ();

    fControlPanel.add (fTextField);
    fControlPanel.add (fGoButton);
    fControlPanel.add (fStatsButton);
    fControlPanel.add (fClearButton);
    fControlPanel.add (fExitButton);

    if (fInBrowser) fExitButton.setEnabled (false);

    panel.add (fControlPanel,"South");

    // Add text area with scrolling to the contentPane.
    content_pane.add (panel);

  } // init

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
    } else if (source == fStatsButton) {
        displayStats ();
    } else if (source == fClearButton) {
        fHistogram.clear ();
        repaint ();
    } else if (!fInBrowser)
        System.exit (0);
  } // actionPerformed

  /** Create a frame to display the distribution statistics. **/
  void displayStats () {
    JFrame frame =
        new JFrame ("Histogram Distributions Statistics");

    // Create a listener to close the frame
    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

    JTextArea area = new JTextArea ();

    double [] stats = fHistogram.getDataStats ();
    if (stats != null) {

      area.append ("Number entries = "+fHistogram.getTotal ()+"\n");

      String stat = PlotFormat.getFormatted (
                    stats[HistogramStat.I_MEAN],
                      1000.0,0.001,3);
      area.append ("Mean value = "+ stat +" ");

      stat = PlotFormat.getFormatted (
                    stats[HistogramStat.I_MEAN_ERROR],
                      1000.0,0.001,3);
      area.append (" +/- "+stat+"\n");

      stat = PlotFormat.getFormatted (
                    stats[HistogramStat.I_STD_DEV],
                      1000.0,0.001,3);
      area.append ("Std. Dev. = "+stat+"\n");

      stat = PlotFormat.getFormatted (
                    stats[HistogramStat.I_SKEWNESS],
                      1000.0,0.001,3);
      area.append ("Skewness = "+stat+"\n");

      stat = PlotFormat.getFormatted (
                    stats[HistogramStat.I_KURTOSIS],
                      1000.0,0.001,3);
      area.append ("Kurtosis = "+stat+"\n");
    } else {
      area.setText ("No statistical information available");
    }

    frame.getContentPane ().add (area);
    frame.setSize (200,200);
    frame.setVisible (true);;
  } // displayStats

  void makeHist () {
    // Create an instance of the Random class for
    // producing our random values.
    java.util.Random r = new java.util.Random ();

    // Them method nextGaussian in the class Randomproduces a value
    // centered at 0.0 and a standarde deviation
    // of 1.0.

    // Create an instance of our basic histogram class.
    // Make it wide enough enough to include most of the
    // gaussian values.
    if (fHistogram == null)
        fHistogram =
          new HistogramStat ("Gaussian Distribution with Statistics",
                            "random values",
                            20,-3.0,3.0);

    // Fill histogram with Gaussian distribution
    for (int i=0; i < fNumDataPoints; i++) {
        double val = r.nextGaussian ();
        fHistogram.add (val);
    }
  } // makeHist

  public static void main (String[] args) {
    int frame_width=450;
    int frame_height=300;

    //  Create the applet
    HistStatsApplet applet = new HistStatsApplet ();
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

} // class HistStatsApplet
