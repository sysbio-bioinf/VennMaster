// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

package venn.graph;

/**
 * This class comes from a refactoring of Histogram,
 * HistogramStats, HistogramAdapt, and HistogramMedian.
 */
public class HistogramStatR1 extends Histogram
{
  protected double [] fBinErrors;

  protected boolean fDoDataStats = true;

  // These constants indicate for each element of the
  // array returned from the getStats () method the statistical
  // measure to which it corresponds.
  public final static int I_MEAN       = 0;
  public final static int I_STD_DEV    = 1;
  public final static int I_MEAN_ERROR = 2;
  public final static int I_SKEWNESS   = 3;
  public final static int I_KURTOSIS   = 4;
  public final static int I_MEDIAN     = 5;
  public final static int I_NUMSTATS   = 6;

  protected double [] fStats = new double[I_NUMSTATS];

  protected double [] fMoments= new double[5];

//--- Constructors -----------------------------------------
  /**
   * The constructor will create an array of a given
   * number of bins. The range of the histogram given
   * by the upper and lower limit values.
   *
   * @param numBins number of bins for the histogram.
   * @param lo lowest value of bin.
   * @param hi highest value of bin.
  **/
  public HistogramStatR1 (int num_bins, double lo, double hi) {
    super (num_bins,lo,hi);
  } // ctor

 /**
   * Constructor with title and x axis label.
   *
   * @param title histogram title
   * @param xLabel label for x axis of histogram.
   * @param numBins number of bins for the histogram.
   * @param lo lowest value of bin.
   * @param hi highest value of bin.
  **/
  public HistogramStatR1 (String title, String xLabel, int num_bins,
                         double lo, double hi) {
    super (title, xLabel, num_bins,lo,hi);
  } // ctor


  /** Get the number of entries in the smallest bin. **/
  public int getMin () {
    int min = getMax ();

    for (int i=0; i < fNumBins;i++)
        if (min > fBins[i]) min = fBins[i];

    return min;
  }


//--- Bin  modification ------------------------------------

  /**
    * Add an entry to a bin. Include if value is in the range
    *    lo <= x < hi
   **/
  public void add (double x) {
    if (x >= fHi)
        fOverflows++;
    else  if ( x < fLo)
        fUnderflows++;
    else  {
        double val = x - fLo;

        // Casting to int will round off to lower
        // integer value.
        int bin =  (int) (fNumBins *  (val/fRange) );

        // Increment the corresponding bin.
        fBins[bin]++;
    }

    // Calculate raw moments from each data value.
    fMoments[0] += 1;
    fMoments[1] += x;
    double x2 = x * x;
    fMoments[2] += x2;
    fMoments[3] += x2 * x;
    fMoments[4] += x2 * x2;
  }

  /** Clear the histogram bins and the over and under flows. **/
  public void clear () {
    // Clear the bins, over and under flosts
    super.clear ();

    // Clear the bin errors if they exist.
    int i;
    for (i=0; i < 5; i++) fMoments[i] = 0.0;
    if (fBinErrors != null)
       for (i=0; i < fBinErrors.length; i++) fBinErrors[i] = 0.0;

  }


//--- Statistics methods -----------------------------------

  /**
    * Get the average and standard deviation from the
    * distribution of the bins rather than from the
    * individual data inputs as in getStats () method.
    * Overflows and underflows ignored.<br><br>
    * If histogram empty, all values set to zero.
   **/
  public double [] getStats () {

    int total = getTotal ();

    if (total == 0) {
       fStats[I_MEAN]       =  0.0;
       fStats[I_STD_DEV]    =  0.0;
       fStats[I_MEAN_ERROR] =  0.0;
       fStats[I_SKEWNESS]   =  0.0;
       fStats[I_KURTOSIS]   =  0.0;
       fStats[I_MEDIAN]     =  0.0;
       return fStats;
    }

    double wt_total = 0;
    double bin_width = fRange/fNumBins;

    double bin_mid = 0.5 * bin_width + fLo;

    // First get the average using the
    // middle of the bin as the position
    // and weighted by the bin contents.
    for (int i=0; i < fNumBins;i++) {
        total += fBins[i];
        bin_mid += bin_width;
        wt_total  += fBins[i] * bin_mid;
    }

    fStats[I_MEAN] = wt_total/total;

    // Now calculate the moments.
    bin_mid = 0.5 * bin_width + fLo;

    double sum_dev2 = 0.0;
    double sum_skew = 0.0;
    double sum_kurt = 0.0;

    for (int i=0; i < fNumBins; i++) {
        double dev = bin_mid - fStats[I_MEAN];
        double dev2 = dev * dev;

        sum_dev2 += dev2;
        sum_skew += dev  * dev2;
        sum_kurt += dev2 * dev2;
    }

    double variance = sum_dev2;
    if (total > 1) variance /= (total-1);

    fStats[I_STD_DEV] = Math.sqrt (variance);
    // Error on mean = s / sqrt (n)
    fStats[I_MEAN_ERROR] = fStats[I_STD_DEV]/Math.sqrt (total);

    if (variance != 0) {
        double sigmaCube = fStats[I_STD_DEV]  * variance;
        fStats[I_SKEWNESS] = sum_skew/ (total * sigmaCube);
        fStats[I_KURTOSIS] = sum_kurt/ (total * variance*variance) - 3.0;
    } else {
        fStats[I_SKEWNESS]   =  0.0;
        fStats[I_KURTOSIS]   =  0.0;
    }

    fStats[I_MEDIAN] = getMedian ();

    return fStats;

  } // getStats

  /**
    *  Get the statistical measures of the distribution calculated
    *  from the individual entry values  (except the median).
    *  @return  values in double array correspond to <br><br>
    *    1 - mean <br>
    *    2 - std. dev <br>
    *    3 - error on the mean <br>
    *    4 - skewness <br>
    *    5 - kurtosis <br>
    *    6 - median <br><br>
    *
    *  The median is calculated from the bin distribution.<br><br>
    *
   **/
  public double [] getDataStats () {

    double n = fMoments[0];

    // Average value = 1/n * sum[x]
    double mean = fMoments[1]/n;

    // Use running mean.
    fStats[I_MEAN] = mean;
    double mean_sq = mean*mean;

    // Check on minimum number of entries.
    if ( n < 2) return fStats;

    // Convert power sums to central moments
    double m2 = fMoments[2]/n;
    double cm2 = m2 - mean * mean;
    double m3 = fMoments[3]/n;
    double cm3 = 2.0 * mean * mean_sq - 3.0 * mean * m2 + m3;
    double m4 = fMoments[4]/n;
    double cm4 = -3.0 * mean_sq * mean_sq + 6.0 * mean_sq * m2
                     -4.0 * mean * m3 + m4;

    // variance = N/ (N-1) m2
    double variance = cm2 * (n/ (n-1.0));

    // Std. Deviation s = sqrt (variance)
    fStats[I_STD_DEV] = Math.sqrt (variance);

    // Error on mean = s / sqrt (N)
    fStats[I_MEAN_ERROR] = fStats[1]/Math.sqrt (n);

    // Skewness = n^2/ (n-1) (n-2) *  cm3/s^3
    fStats[I_SKEWNESS] =  ( n/ ( (n-1) * (n-2)) ) * n * cm3 /
                                     (variance*fStats[I_STD_DEV]);

    // Kurtosis = n (n+1)/ (n-1) (n-2) (n-3) * cm4/s^4 - 3 (n-1)^2 / (n-2) (n-3)
    double factor1 =  ( n * (n+1.0))/ (  (n-1.0) * (n-2.0) * (n-3.0) );
    double factor2 =  ( 3.0 * (n-1.0) * (n-1.0) )/ ( (n-2.0) * (n-3.0));
    fStats[I_KURTOSIS] = factor1 * cm4 * n/ (variance * variance) - factor2;

    fStats[I_MEDIAN]  = getMedian ();

    return fStats;

  } // getDataStats

  /**
    *  The median is calculated from the bin distribution.
    *  Overflows and underflows ignored.
   **/
  public double getMedian () {

    int half_total_entries = getTotal ()/2;
    int sum_bin_entries = 0;
    int sum = 0;

    double bin_width = fRange/fNumBins;
    double median = 0.0;

    // Sum bins up to half total of entries
    for ( int i=0; i < fNumBins; i++) {
      sum = sum_bin_entries + fBins[i];

      // Check if bin crosses halfTotal point
      if (sum >= half_total_entries ) {
          // Scale linearly across the bin
          int dif = half_total_entries - sum_bin_entries;
          double frac = 0.0;
          if (fBins[i] > 0) {
              frac =  ((double)dif)/(double)fBins[i];
          }
          median = (i + frac) * bin_width + fLo;
          // Finished
          break;
      }
      // Not reached median yet.
      sum_bin_entries = sum;
    }
    return median;
  } // getMedian

//--- Bin error methods -----------------------------------

  /**
    * Provide an array of error values for all bins.
    * @return array of double values.
   **/
  public double [] getBinErrors () {
    return fBinErrors;
  }

  /**
    * Return the error for a particular bin.
    * @param from 0 to highest bin value
   **/
  public double getBinError (int bin) {
    if (bin>= 0 && bin < fBins.length)
      return fBinErrors[bin];
    else
      return -1.0;
  } // getBinError

  /**
    * Calculate the error on each bin according to the
    * sqrt (bin contents) from std. dev. of Poisson distribution.
   **/
  public void makeBinErrors () {
    if (fBinErrors == null ||
        fBinErrors.length != fBins.length)
        fBinErrors = new double [fBins.length];

    for ( int i = 0; i < fNumBins; i++)
    {
      fBinErrors[i] = Math.sqrt (fBins[i]);
    }
  } // makeBinErrors

  /**
   * Pack the errors on each bin.
   * @param false if array size doesn't
   *        match number of bins.
   */
  public boolean packErrors (double [] errors) {
    if (errors.length != fNumBins) return false;
    if (fBinErrors == null || fBinErrors.length != fNumBins)  {
        fBinErrors = new double[errors.length];
    }

    for (int i = 0; i < fNumBins; i++)  {
        fBinErrors[i] = errors[i];
    }
    return true;
  } //packErrors

} // class HistogramStatR1