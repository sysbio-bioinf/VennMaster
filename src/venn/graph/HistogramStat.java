package venn.graph;
// "Java Tech"
//  Code provided with book for educational purposes only.
//  No warranty or guarantee implied.
//  This code freely available. No copyright claimed.
//  2003
//

/**
  * This class provides provides additional statistical
  * measures of the histogram distribution.
 **/
public class HistogramStat extends Histogram
{
  protected boolean fDoDataStats = true;
  protected double [] fBinErrors;

  protected double [] fMoments= new double[5];
  protected double fMean;

  // These constants indicate for each element of the
  // array returned from the getStats () method the statistical
  // measure to which it corresponds.
  public final static int I_MEAN       = 0;
  public final static int I_STD_DEV    = 1;
  public final static int I_MEAN_ERROR = 2;
  public final static int I_SKEWNESS   = 3;
  public final static int I_KURTOSIS   = 4;
  public final static int I_NUMSTATS   = 5;

 /**
  * Constructor
  * @param number of bins for the histogram.
  * @param lowest value of bin.
  * @param highest value of bin.
  */
  public HistogramStat (int num_bins, double lo, double hi) {
    super (num_bins,lo,hi);
  } // ctor

 /**
  * Constructor with title and x axis label.
  *
  * @param title for histogram
  * @param label for x axis of histogram.
  * @param number of bins for the histogram.
  * @param lowest value of bin.
  * @param highest value of bin.
  */
  public HistogramStat (String title, String xLabel, int num_bins,
                       double lo, double hi) {
    super (title, xLabel, num_bins,lo,hi);
  } // ctor

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
  } // getBin Error

 /**
   * Calculate the error on each bin according to the
   * sqrt (bin contents) from std. dev. of Poisson distribution.
  **/
  public void makeBinErrors () {
    for ( int i = 0; i < fNumBins; i++) {
      fBinErrors[i] = Math.sqrt (fBins[i]);
    }
  }

 /**
   * Pack the errors on each bin.
   * @param false if array size doesn't match number of bins.
  **/
  public boolean packErrors (double [] errors) {
    if (errors.length != fNumBins) return false;
    if (fBinErrors == null || fBinErrors.length != fNumBins) {
       fBinErrors = new double[errors.length];
    }

    for (int i = 0; i < fNumBins; i++) {
         fBinErrors[i] = errors[i];
    }
    return true;
  } // packErrors

 /**
   * Add an entry to the histogram, plus calculate the
   * power sums  (if flag set) for each entry rather than
   * from the bin values.
   *
   * @param non-zero length array of int values.
  **/
  public void add (double x) {
    // First add entry as usual.
    super.add (x);

    // Then do moments if flag set.
    if (!fDoDataStats)
        return;
    else {
        fMoments[0] += 1;
        fMoments[1] += x;
        double x2 = x * x;
        fMoments[2] += x2;
        fMoments[3] += x2 * x;
        fMoments[4] += x2 * x2;
    }
  } // add

  /** Clear the histogram bins and the moments. **/
  public void clear () {
    super.clear (); // Clear histogram arrys
    int i;
    for (i=0; i < 5; i++) fMoments[i] = 0.0;
    if (fBinErrors != null)
        for (i=0; i < fBinErrors.length; i++) fBinErrors[i]=0.0;
  } // clear

 /**
   * Turn on or off the accumulation and calculation of
   * the data statistics.
  **/
  public void setDataStats (boolean flag) {
    fDoDataStats = flag;
  }

  /**
    *  Get the statistical measures of the distribution calculated
    *  from the entry values.
    *  @return  values in double array correspond to
    *  mean,standard deviation, error on the mean, skewness, and
    *  kurtosis. If the number of entries is zero or the statistics
    *  accumulation is turned off  (see setStats () method), a null
    *  value will return
   **/
  public double [] getDataStats () {
    // If stats turned off or no entries, then give up
    if (!fDoDataStats || fMoments[0] == 0) return null;

    double [] stats = new double[I_NUMSTATS];

    double n = fMoments[0];

    // Average value = 1/n * sum[x]
    fMean = fMoments[1]/n;

    // Use running mean.
    stats[0] = fMean;
    double mean_sq = fMean * fMean;

    // Check on minimum number of entries.
    if (n < 2) return stats;

    // Convert power sums to central moments
    double m2 = fMoments[2]/n;
    double cm2 = m2 - fMean * fMean;
    double m3 = fMoments[3]/n;
    double cm3 = 2.0 * fMean * mean_sq - 3.0 * fMean * m2 + m3;
    double m4 = fMoments[4]/n;
    double cm4 = -3.0 * mean_sq * mean_sq + 6.0 * mean_sq * m2
                     -4.0 * fMean * m3 + m4;

    // variance = N/ (N-1) m2
    double variance = cm2 *  (n/ (n-1.0));

    // Std. Deviation s = sqrt (variance)
    stats[1] = Math.sqrt (variance);

    // Error on mean = s / sqrt (N)
    stats[2] = stats[1]/Math.sqrt (n);

    // Skewness = n^2/ (n-1) (n-2) *  cm3/s^3
    stats[3] =  ( n/ ( (n-1) * (n-2)) ) * n * cm3 /  (variance * stats[1]);

    // Kurtosis = n (n+1)/ (n-1) (n-2) (n-3) * cm4/s^4 - 3 (n-1)^2 / (n-2) (n-3)
    double factor1 =  ( n * (n+1.0))/ (  (n-1.0) * (n-2.0) * (n-3.0) );
    double factor2 =  ( 3.0 * (n-1.0) * (n-1.0) )/ ( (n-2.0) * (n-3.0));
    stats[4] = factor1 * cm4 * n/ (variance*variance) - factor2;

    return stats;
  } // getDataStats

} // class HistogramStat