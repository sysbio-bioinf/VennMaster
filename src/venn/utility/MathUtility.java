/*
 * VennMaster/geometry/MathUtility.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.utility;

import junit.framework.Assert;

/**
 * @author muellera
 */
public class MathUtility
{
	public static int sign(double x)
	{
		if( x < 0.0 )
			return -1;
		if( x > 0.0 )
			return +1;
		return 0;
	}
	
	public static double square(double x)
	{
		return x*x;
	}

	/**
	 * Restricts the given value <var>value</var> to the interval
	 * [lower,upper].
	 * @param value
	 * @param lower
	 * @param upper
	 * @return The restricted value.
	 */
	public static double restrict(double value, double lower, double upper)
	{
		if( lower > upper )
			throw new IllegalArgumentException("lower must be smaller than upper");
		if( value < lower )
		{
			return lower;
		}
		else
		{
			if( value > upper )
			{
				return upper;
			}
		}
		return value;
	}
	
	/**
	 * Restricts the given value <var>value</var> to the interval
	 * [lower,upper].
	 * @param value
	 * @param lower
	 * @param upper
	 * @return the restricted value
	 */
	public static int restrict(int value, int lower, int upper)
	{
		if( lower > upper )
			throw new IllegalArgumentException("lower must be smaller than upper");
		if( value < lower )
		{
			return lower;
		}
		else
		{
			if( value > upper )
			{
				return upper;
			}
		}
		return value;
	}

	/*
	public static int maxInt( Iterable container )
	{
	    Iterator iter = container.iterator();
	    Integer		obj;
	    boolean first = true;
	    int mx = 0;
	    
	    while( iter.hasNext() )
	    {
	        obj = (Integer)iter.next();
	        if( first || obj.intValue() > mx )
	        {
	            first = false;
	            mx = obj.intValue();
	        }
	    }
	    return mx;
	}

    public static int minInt( Iterable container ) 
    {
	    Iterator iter = container.iterator();
	    Integer		obj;
	    boolean first = true;
	    int mx = 0;
	    
	    while( iter.hasNext() )
	    {
	        obj = (Integer)iter.next();
	        if( first || obj.intValue() < mx )
	        {
	            first = false;
	            mx = obj.intValue();
	        }
	    }
	    return mx;
    }
    */
    
    public static double factorial( int n )
    {
        double f = 1;
        for( int i=2; i<=n; ++i )
        {
            f *= (double)i;
        }
        return f;
    }
    
    public static double prod( int i, int j )
    {
        double f = 1;
        for(int k = i; k<=j; ++k)
            f *= (double)k;
        return f;
    }
    
    public static double choose( int n, int k )
    {
        return prod(n-k+1,n)/factorial(k);
    }
    
    /**
     * Computes a floating point modulo operation of
     * the value x to m such that the result is in the
     * range [0,m)
     * @param x
     * @param m
     * @return modulo
     */
    public static double fmod(double x, double m) 
    {
        return x - m * Math.floor(x/m);
    }
    
    /**
     * Multivariate normal distribution.
     * Uses a cholesky defactorization.
     * 
     * @param mu    
     * @param cov covariance matrix
     * @return a vector from the pdf exp(-0.5 x' * C^{-1} * x)/(sqrt(2*Pi)^n det C)
     */
    public static double[] mvrnorm( double[] mu, double[][] cov )
    {
        /*
        // from the R-package mnormt
        rmnorm <- function(n=1, mean=rep(0,d), varcov)
        {
         d <- if(is.matrix(varcov)) ncol(varcov) else 1
         z <- matrix(rnorm(n*d),n,d) %*% chol(varcov)
         y <- t(mean+t(z))
         return(y)
        }
        */
        Assert.fail("unimplemented!");
        return null;
    }
    
    public static void matrixProduct( double[][] x, double[][] y, double[][] z )
    {
        Assert.assertEquals( x[0].length, y.length );   // ncols(x) == nrows(y) 
        Assert.assertEquals( x.length, z.length );      // nrows(x) == nrows(z)
        Assert.assertEquals( y[0].length, z[0].length );// ncols(y) == ncols(z)
        
        for( int i=0; i<x.length; ++i )
        {
            for( int j=0; j<y[0].length; ++j )
            {
                double sum = 0.0;
                for( int k=0; k<y.length; ++k )
                {
                    sum += x[i][k] * y[k][j];
                }
                z[i][j] = sum;
            }
        }
    }
    
    public static void matrixCopy( double[][] src, double[][] dst )
    {
        Assert.assertEquals( src.length, dst.length );
        Assert.assertEquals( src[0].length, dst[0].length );
        for( int i=0; i<src.length; ++i )
        {
            for( int j=0; j<src[i].length; ++j )
            {
                dst[i][j] = src[i][j];
            }
        }
    }
    

    public static double[][] createMatrix(int nrows, int ncols) 
    {
        return new double[nrows][ncols];
    }

    public static void fillMatrix( double[][] a, double val ) 
    {
        for( int i=0; i<a.length; ++i )
        {
            for( int j=0; j<a[i].length ; ++j )
            {
                a[i][j] = val;
            }
        }        
    }

    public static void unitMatrix( double[][] a ) 
    {
        for( int i=0; i<a.length; ++i )
        {
            for( int j=0; j<a[i].length ; ++j )
            {
                if( i == j )
                    a[i][j] = 1.0;
                else
                    a[i][j] = 0.0;
            }
        }        
    }    
    
    
    public static void main(String[] args)
    {
        //System.out.println(" "+choose(20,17));
        System.out.println(fmod(1.3,1.0));
        System.out.println(fmod(-0.3,1.0));
    }


}
