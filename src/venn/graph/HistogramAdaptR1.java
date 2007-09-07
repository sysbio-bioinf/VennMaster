// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//
package venn.graph;

/**
  *  This class provides provides histograms that offer the option
  *  to adapt the range to keep all points within bin limits. This
  *  requires a data array to hold each individual entry.
  *  Requires an expandable data storage array to hold each data point.
  *
  *  This class derives from a combination of HistogramAdapt and
  *  HistogramMedian.
 **/
public class HistogramAdaptR1 extends HistogramStatR1
{
  // flag for adapting the limits to the data
  boolean fAdapt = true;

  // flag to indicate a value outside limits
  boolean fNeedToRebin = false;

  // data array.
  double [] fData ;

  // index pointing where next datum goes
  int fDataIndex = 0;

  /**
    * Constructor
    *
    * @param estmated number of data points.
    *   Negative to turn off adaptable binning.
    * @param numbins - number of bins for the histogram.
    * @param lo - lowest value of bin.
    * @param hi - highest value of bin.
    * @param numData - size of data array. Must be equal to or greater
    *        than the number of data points expected to be entered
    *        into the histogram.
   **/
  public HistogramAdaptR1 (int num_bins, double lo, double hi,
                        int num_data) {
    super (num_bins,lo,hi);
    if (num_data <= 0) fAdapt = false;
    else setData (num_data);
  } // ctor

  /**
    * Constructor with title and x axis label.
    *
    * @param title for histogram
    * @param estmated number of data points.
    *   Negative to turn off adaptable binning.
    * @param label for x axis of histogram.
    * @param inital number of bins for the histogram.
    * @param lowest value of bin.
    * @param highest value of bin.
    * @param size of data array. Must be equal to or greater
    *        than the number of data points expected to be entered
    *        into the histogram.
   **/
  public HistogramAdaptR1 (String title, String xLabel, int num_bins,
                       double lo, double hi, int num_data) {
    super (title, xLabel, num_bins,lo,hi);
    if (num_data <= 0 ) fAdapt = false;
    else setData (num_data);
  } // ctor

  /**
    *  Create a data array sufficient to include the data.
    *  @param number of data elements needed. If data already
    *  present in the current array, then the array will be big
    *  enough to hold the current data plus numData.
   **/
  void setData (int num_data) {
    // If previous data already present then
    // check if room for the new data. If not
    // then create a bigger data array.
    if (fDataIndex > 0) {
        int data_needed = fDataIndex + num_data + 1;
        if (data_needed > fData.length) {
            // Make a new data array
            double [] tmp = new double[data_needed];
            // Copy the old data into it
            System.arraycopy (fData,0,tmp,0,fData.length);
            // Move reference to new array
            fData = tmp;
        }
    }
    else // Create an initial or bigger data array if needed.
    if (fData == null || num_data > fData.length)
        fData = new double[num_data];
  } // setData

  /**
    * Clear the histogram and also reset the data array index
    * to the beginning.
   **/
  public void reset () {
    super.clear (); // Clear histogram arrys
    fDataIndex = 0; // reset data array pointer to beginning
  }

  /**
    *  Provide access to the data array. Returns
    *  null if the range adaptation turned off.
   **/
  double [] getData () {
    return fData;
  }

 /**
   * Can enable or disable the range adaption.
   *
   * @param boolean to turn on or off the extending of the limits
   *   when data values outside current limits occur.
  **/
  void setAdaptEnabled (boolean flag) {
    fAdapt = flag;
    if (!fAdapt) fData = null;
  }

  /**  Return index to data array element pointer. **/
  int getDataIndex () {
    return fDataIndex;
  }

  /**  Find if rebin request turned on. **/
  boolean getRebinFlag () {
    return fNeedToRebin;
  }


 /**
   * Add an entry to the histogram. Check if value outside current
   * limits of the histogram. If it is and the adaptation flag turned
   * on, then set rebin flag.<br><br>
   * Synchronize so that this method and rebin () method do not interfere.
   *
   * @param non-zero length array of int values.
  **/
  public synchronized void add (double x) {
    if (fAdapt) {
        // Add new data point to array
        if (fDataIndex < fData.length) {
            fData[fDataIndex++] = x;
            // If data point outside range, set rebin flag.
            if ( x < fLo || x > fHi) fNeedToRebin = true;
        } else
        // Could throw an exception, open a warning
        // dialog, or use setData () to create a bigger data array.
        // However, we just do a simple console print.
        System.out.println ("Data overflow");
    }
    super.add (x);
  } // add


  /**
    *  Rebin the histogram using the data array.
    *  Synchronize to avoid interference with new data
    *  entering during this method.
   **/
  public synchronized void rebin () {
    if (fDataIndex <= 1) return;

    // Find new limits from the out of range datum,
    for (int i=0; i < fDataIndex; i++) {
        if (fData[i] < fLo) fLo = fData[i];
        if (fData[i] > fHi) fHi = fData[i];
    }

    // Set new limits
    fLo = Math.floor (fLo);
    fHi = Math.ceil (fHi);
    fRange = fHi - fLo;

    // Clear the histogram entries
    clear ();

    // Refill the histogram according to the new range.
    for (int i=0; i < fDataIndex; i++) {
        super.add (fData[i]);
    }

    fNeedToRebin = false;
  } // rebin


  /**
    *  If adaptation turned on, get the statistical measures of
    *  the distribution directly from the data.  (Uses a two pass
    *  approach reduced numerical errors compared to using the
    *  moments.)
    *  Otherwise, use the overriden method in HistogramR1.
    *  @return  values in double array correspond to <br><br>
    *    1 - mean <br>
    *    2 - std. dev <br>
    *    3 - error on the mean <br>
    *    4 - skewness <br>
    *    5 - kurtosis <br>
    *    6 - median <br><br>
    *
    *  The median is calculated from the data.<br><br>
    *
   **/
  public double [] getStats () {
    if (!fAdapt) {
        return super.getStats ();
    }

    if (fDataIndex == 0) {
       fStats[I_MEAN]       =  0.0;
       fStats[I_STD_DEV]    =  0.0;
       fStats[I_MEAN_ERROR] =  0.0;
       fStats[I_SKEWNESS]   =  0.0;
       fStats[I_KURTOSIS]   =  0.0;
       fStats[I_MEDIAN]     =  0.0;
       return fStats;
    }

    double sum = 0;

    // First find the mean.
    for (int i=0; i < fDataIndex; i++) {
        sum += fData[i];
    }

    fStats[I_MEAN] = sum/fDataIndex;

    // Now calculate the moments.

    double sumDev2 = 0.0;
    double sumSkew = 0.0;
    double sumKurt = 0.0;

    // Now make a second pass through the data.
    for (int i=0; i < fDataIndex; i++) {
        double dev = fData[i] - fStats[I_MEAN];
        double dev2 = dev * dev;

        sumDev2 += dev2;
        sumSkew += dev*dev2;
        sumKurt += dev2*dev2;
    }

    double variance = sumDev2;
    if (fDataIndex > 1)  variance /= (fDataIndex-1);

    fStats[I_STD_DEV] = Math.sqrt (variance);

    // Error on mean = s / sqrt (n)
    fStats[I_MEAN_ERROR] = fStats[I_STD_DEV]/Math.sqrt (fDataIndex);

    if (variance != 0) {
        double sigmaCube = fStats[I_STD_DEV] * variance;

        fStats[I_SKEWNESS] = sumSkew/ (fDataIndex*sigmaCube);
        fStats[I_KURTOSIS] = sumKurt/ (fDataIndex*variance*variance) - 3.0;
    } else {
        fStats[I_SKEWNESS]   =  0.0;
        fStats[I_KURTOSIS]   =  0.0;
    }
    fStats[I_MEDIAN] = getMedian ();
    return fStats;

  } // getStats

  /**
    *  This method overrrides the  method in HistogramR1 so as to
    *  calculate the median from the data array. If adaptation
    *  turned off, the overriden method used.
   **/
  public double getMedian () {

    if (fAdapt) { // then use data to get median
        double median = 0.0;

        if (fDataIndex > 1) {
           java.util.Arrays.sort (fData, 0, fDataIndex);
           int mid = fDataIndex/2;
           // fDataIndex points to element above last entry.
           if ((fDataIndex % 2) == 0) {
              // even number of entries
              return  (fData[mid] + fData[mid-1])/2.0;
           } else  {// odd number of entries
              return fData[mid];
           }
        } else
           return fData[0];
    } else // Otherwise, use the bin contents.
       return super.getMedian ();

  } // getMedian

} // class HistogramAdaptR1

