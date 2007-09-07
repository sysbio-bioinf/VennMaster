/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;

import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * Implements a GOMiner data filter with minTotal, maxTotal, maxPValue, and maxFDR.
 * @author muellera
 *
 */
public class GODistanceFilter 
extends AbstractDataFilter implements Serializable, ChangeListener
{   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static class Parameters implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        public int      minTotal,       // min number of changes 
                        maxTotal;
        public double   maxPValue,      // max p-Value
                        maxFDR;         // max FDR

		public int maxGroups;

		public int minDistance;
        
        public Parameters()
        {
            minTotal    = 40;
            maxTotal    = 140;
            maxPValue   = 0.05;
            maxFDR      = 0.05;
            maxGroups   = 10;
            minDistance = 1;
        }
        
        public String toString()
        {
            StringBuffer buf = new StringBuffer();
            buf.append("minTotal = " + minTotal+"\n");
            buf.append("maxTotal = " + maxTotal+"\n");
            buf.append("maxPValue = " + maxPValue+"\n");
            buf.append("maxFDR = " + maxFDR+"\n");
            buf.append("maxGroups = " + maxGroups +"\n");
            buf.append("minDistance = " + minDistance +"\n");            
            return buf.toString();
        }
        
        public void check()
        {
            if( minTotal < 0 )
                minTotal = 0;
            
            if( maxTotal < minTotal )
                maxTotal = minTotal;
            
            maxPValue = MathUtility.restrict(maxPValue,0.0,1.0);
            maxFDR = MathUtility.restrict(maxFDR,0.0,1.0);
            maxGroups = MathUtility.restrict(maxGroups,2,100);
            minDistance = MathUtility.restrict(minDistance,1,100);
        }
    }    
    
    private Parameters params;
    private IVennDataModel dataModel;
    private BitSet groups;
    private boolean valid;
    private GoTree goTree;
   
    
    public GODistanceFilter()
    {
        params = new Parameters();
        groups = new BitSet();
        valid = false;
        goTree = null;
    }
    
    public GODistanceFilter( GoTree goTree )
    {
    	this();
        this.goTree = goTree;
    }
    
    public void setGoTree( GoTree goTree )
    {
    	this.goTree = goTree;
    }
    
    public boolean accepts( int nTotal, int nChange, double pValue, double FDR )
    {
        if( nTotal < params.minTotal || nTotal > params.maxTotal )
            return false;
            
        if( pValue > params.maxPValue )
            return false;
        
        if( FDR > params.maxFDR )
            return false;
            
        return true;
    }
        
    private void update() 
    {
    	// does all the filtering work for the cached data model
    	if( groups == null )
    	{
    		groups = new BitSet();
    	}
    	groups.clear();
    	if( dataModel == null )
    	{
    		return;
    	}
    	int n = dataModel.getNumGroups();
    	if( n <= 0 )
    	{
    		return;
    	}
    	
    	// update standard filtering criteria
    	for( int i=0; i<n; ++i )
    	{
    		GOCategoryProperties props = (GOCategoryProperties)dataModel.getGroupProperties(i);
    		if( props != null )
    		{
    			props.meanDist = -1;
    			if( accepts(props.nTotal,props.nChange,props.pValue,props.FDR) )
    			{
    				groups.set(i);
    			}
    		}
    	}
    	
    	// distances
    	if( goTree == null )
    	{
    		System.out.println("no goTree available");
    		return;
    	}
    	
    	int[][] dist = computeDistances();
    	
    	if( dist == null )
    	{
    		return;
    	}
    	BitSet set = findMaxDistSubset( dist, params.minDistance );
    	
    	// eliminate marked sets
    	int ii = 0;
    	BitSet groupsUpdated = (BitSet)groups.clone();
    	for( int i = groups.nextSetBit(0); i >= 0; i = groups.nextSetBit(i+1) )
    	{
    		if( ! set.get(ii) )
    		{
    			groupsUpdated.clear(i);
    		}
    			
    		++ii;
    	}
    	
    	groups = groupsUpdated;
    	
    	// update minDist
    	dist = computeDistances();
    	ii = 0;
    	for( int i = groups.nextSetBit(0); i >= 0; i = groups.nextSetBit(i+1) )
    	{
    		GOCategoryProperties props = (GOCategoryProperties)dataModel.getGroupProperties(i);
    		if( props != null )
    		{
    			props.meanDist = 0.0;
    			
    			for( int jj=0; jj<dist.length; ++jj )
    			{
    				props.meanDist += dist[ii][jj];
    			}
    			props.meanDist /= (double)(dist.length-1);
    		}
    		++ii;
    	}
    }
    
//    private BitSet findMaxDistSubset( int[][] dist, int m ) 
//    {
//    	Assert.assertNotNull( dist );
//    	Assert.assertTrue( m >= 2 );
//    	BitSet set = new BitSet();
//    	int n = dist.length;
//    	
//    	// compute initial row sum
//    	int[] 	rowSum = rowSum( dist );
//    	
//    	set.set(0, n, true);
//    	
//    	// finds the row with the minimum distance
//    	while( set.cardinality() > m )
//    	{
//    		// find optimum index
//        	int i = findMinimumIndex( rowSum, set );
//        	
//        	// eliminate row/column i from the rowSum
//        	for( int j=0; j<n; ++j )
//        	{
//        		rowSum[j] -= dist[j][i];
//        	}
//        	set.clear(i);
//    	}
//    	return set;
//	}

    private BitSet findMaxDistSubset( int[][] dist, int m ) 
    {
    	Assert.assertNotNull( dist );
    	Assert.assertTrue( m >= 1 );
    	BitSet set = new BitSet();
    	int n = dist.length;
    	
    	// compute initial row sum
    	// int[] 	rowSum = rowSum( dist );
    	
    	set.set(0, n, true);
    	
    	// finds the row with the minimum distance
    	for( int k=0; k<n; ++k)
    	{
    		// find optimum index
        	int i = findMinimum( dist[k], set );
        	int idx = findMinimumIndex( dist[k], set );
        	
        	// eliminate one of nodes if distance < minDist
        	if(i < m)
        	{
            	int ll = 0,
            	a=0,
            	b=0;
            	for( int l = groups.nextSetBit(0); l >= 0; l = groups.nextSetBit(l+1) )
            	{
            		if(ll==k)
            		{
            			a = l;
            		}
            		if(ll==idx)
            		{
            			
            			b = l;
            		}
            		ll = ll+1;
            	}
    			int eli = goHigherNode(a,b);
        		for( int j=0; j<n; ++j )
        		{
        			// node to eliminate is located higher in the tree (for distance = 1 this is parent node)
        			if(eli==1)
        			{
        				dist[k][j] = Integer.MAX_VALUE;
        				dist[j][k] = Integer.MAX_VALUE;
        			}
        			else
        			{
        				dist[j][idx] = Integer.MAX_VALUE;
        				dist[idx][j] = Integer.MAX_VALUE;
        			}
        		}
        		if(eli==1)
        			set.clear(k);
        		else
        			set.clear(idx);
        	}
    	}
    	return set;
	}

    private int findMinimum( int[] array, BitSet set ) 
    {
    	int iMin = -1,
			min = Integer.MAX_VALUE;
    	
    	for( int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1) )
		{
			if( iMin < 0 || array[i] < min )
			{
				if( array[i] > 0)
				{
					min = array[i];
					iMin = i;
				}
			}
		}
		return min;
	}
   
    
    private int findMinimumIndex( int[] array, BitSet set ) 
    {
    	int iMin = -1,
			min = Integer.MAX_VALUE;
    	
    	for( int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1) )
		{
			if( iMin < 0 || array[i] < min )
			{
				if( array[i] > 0)
				{
					min = array[i];
					iMin = i;
				}
			}
		}
		return iMin;
	}

	/**
     * Builds the sum over each row of the matrix.
     *  
     * @param matrix
     * @return
     */
    public int[] rowSum( int[][] matrix )
    {
    	int n = matrix.length;
    	int[] sum = new int[n];
    	Arrays.fill(sum,0);
    	
    	for( int i=0; i<n; ++i )
    	{
    		for( int j=0; j<n; ++j )
    		{
    			sum[i] += matrix[i][j];
    		}
    	}
    	return sum;
    }

    /**
     * 
     * @param matrix
     * @return
     */
    public int[] colSum( int[][] matrix )
    {
    	int n = matrix.length;
    	int[] sum = new int[n];
    	Arrays.fill(sum,0);
    	
    	for( int i=0; i<n; ++i )
    	{
    		for( int j=0; j<n; ++j )
    		{
    			sum[i] += matrix[j][i];
    		}
    	}
    	return sum;
    }

	public int goDist( int i, int j )
    {
    	GOCategoryProperties props1 = (GOCategoryProperties)dataModel.getGroupProperties(i),
    						 props2 = (GOCategoryProperties)dataModel.getGroupProperties(j);
    	if( (props1 == null) || (props2 == null ) )
    	{
    		return -1;
    	}
    	long goID1 = props1.ID,
    		 goID2 = props2.ID;
    	
    	// return goTree.findMinDistanceToSharedParent(goID1, goID2);
    	return goTree.findDistanceBetweenNodes(goID1, goID2);
    }
	
	public int goHigherNode(int i, int j)
	{
		GOCategoryProperties props1 = (GOCategoryProperties)dataModel.getGroupProperties(i),
		 					 props2 = (GOCategoryProperties)dataModel.getGroupProperties(j);
		if( (props1 == null) || (props2 == null ) )
		{
			return -1;
		}
		long goID1 = props1.ID,
		goID2 = props2.ID;

		return goTree.findLessDistantNodeToSharedParent(goID1, goID2);
	}
    
    public int[][] computeDistances()
    {
    	if( groups.cardinality() < 2 )
    	{
    		System.err.println("updateDistances() not enough groups");
    		return null;
    	}
    	
    	int n = groups.cardinality();
    	
    	int[][] dist = new int[n][n];
    	for( int i=0; i<n; ++i )
    	{
    		for( int j=0; j<n; ++j )
    		{
    			dist[i][j] = 0;
    		}
    	}
    	
    	// Find pairwise minimum Distances 
    	int ii = 0;
    	for( int i = groups.nextSetBit(0); i >= 0; i = groups.nextSetBit(i+1) ) 
    	{
    		// int jj = 0; BUG! JKraus 15.05.2007
    		int jj = ii;
    		for( int j = groups.nextSetBit(i); j >= 0; j = groups.nextSetBit(j+1) ) 
			{
				if( i < j )
				{
					dist[ii][jj] = goDist( i, j );
					dist[jj][ii] = dist[ii][jj];
    			}
				++jj;
        	}
    		++ii;
    	}
    	return dist;
    }
    
    private void validate( IVennDataModel model )
    {
    	if( dataModel != model )
    	{
    		if( dataModel != null )
    		{
    			dataModel.removeChangeListener( this );
    		}
    		dataModel = model;
    		dataModel.addChangeListener( this );
    		valid = false;
    	}
    	if( !valid )
    	{
    		update();
    		valid = true;
    	}
    }
    
    private void invalidate()
    {
    	if( valid )
    	{
    		valid = false;
    		// fireChangeEvent();
    	}
    }
                
   
    public void setParameters(Parameters params)
    {
    	valid = false;
        this.params = params;
        this.params.check();
        fireChangeEvent();
    }
    
    public Parameters getParameters()
    {
        return params;
    }
    
    

    public boolean accept(IVennDataModel model, int groupID)
    {       
        validate( model );
        if( goTree == null )
        	return true;
        
        return groups.get(groupID);
    }
    
    public Object clone()
    {
        // SystemUtility.serialClone(this);
    	System.out.println("clone");
    	GODistanceFilter filter = new GODistanceFilter();
    	filter.setParameters((Parameters) SystemUtility.serialClone(params));
    	filter.dataModel = dataModel;
    	filter.goTree = goTree;
    	return filter;
    }
    
    public String toString()
    {
        
        return "GODistanceFilter\n"+params.toString();
    }

	public void stateChanged(ChangeEvent arg0) 
	{
		invalidate();
	}
}
