package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003

import java.text.*;

/**
  * This class provides a static method getFormatted that
  * returns a double value as a string in decimal or scientific
  * notation.<br><br>
 **/
public class PlotFormat
{
  final static DecimalFormat DF1 = new DecimalFormat ("0.0");
  final static DecimalFormat DF2 = new DecimalFormat ("0.00");
  final static DecimalFormat DF3 = new DecimalFormat ("0.000");
  final static DecimalFormat DF4 = new DecimalFormat ("0.0000");

  final static DecimalFormat SF1 = new DecimalFormat ("0.0E0");
  final static DecimalFormat SF2 = new DecimalFormat ("0.00E0");
  final static DecimalFormat SF3 = new DecimalFormat ("0.000E0");
  final static DecimalFormat SF4 = new DecimalFormat ("0.0000E0");

 /**
  * The options include 1 to 3 decimal places. Values below
  * decimalLimit use decimal notation; above this use scientific
  * notation.
  * @param input value
  * @param upper limit before changing to scientific notation
  * @param lower limit before changing to scientific notation
  * @param number of decimal places in the output.
  */
  public static String getFormatted (double val,
                                     double decimal_hi_limit,
                                     double decimal_lo_limit,
                                     int decimal_places) {
  // If less than decimalLimit, or equal to zero, use decimal style
    if (val == 0.0 ||  (Math.abs (val) <= decimal_hi_limit &&
        Math.abs (val) > decimal_lo_limit)) {
        switch  (decimal_places) {
          case 1 : return DF1.format (val);
          case 2 : return DF2.format (val);
          case 3 : return DF3.format (val);
          case 4 : return DF4.format (val);
          default: return DF1.format (val);
        }
    } else {
      // Create the format for Scientific Notation with E
        switch  (decimal_places) {
          case 1 : return SF1.format (val);
          case 2 : return SF2.format (val);
          case 3 : return SF3.format (val);
          case 4 : return SF4.format (val);
          default: return SF1.format (val);
        }
    }
  } // getFormatted

} // class PlotFormat