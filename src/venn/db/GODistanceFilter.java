/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.TreeSet;

import junit.framework.Assert;
import venn.Constants;
import venn.db.GoTree.WhichNode;
import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * Implements a GOMiner data filter with minTotal, maxTotal, maxPValue, and maxFDR.
 * @author muellera
 *
 */
public class GODistanceFilter 
extends AbstractDataFilter implements Serializable
{   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static class Parameters implements Serializable
    {
    	public enum FilterBy {P_VALUE, FDR, P_UNDER, P_OVER, P_CHANGE, FDR_UNDER, FDR_OVER, FDR_CHANGE;
    		public static String string(FilterBy val) {
    			switch (val) {
    			case P_VALUE:
    				if (true) return "max p-Value";
    				break;
    			case FDR:
    				if (true) return "max FDR";
    				break;
    			case P_UNDER:
    				if (true) return "pUnder";
    				break;
    			case P_OVER:
    				if (true) return "pOver";
    				break;
    			case P_CHANGE:
    				if (true) return "pChange";
    				break;
    			case FDR_UNDER:
    				if (true) return "fdrUnder";
    				break;
    			case FDR_OVER:
    				if (true) return "fdrOver";
    				break;
    			case FDR_CHANGE:
    				if (true) return "fdrChange";
    				break;
    				default:
    					if (true) throw new IllegalArgumentException();
    				break;
    			}
    			throw new IllegalArgumentException();
    		}
    		
    		public String string() {
    			return string(this);
    		}
    	}
    	
        /**
         * 
         */
        private static final long serialVersionUID = 2L;
        
        public int      minTotal,       // min number of changes 
                        maxTotal;
//        public double   maxPValue,      // max p-Value
//                        maxFDR;         // max FDR
        public double maxPFDR; // max p or fdr
        
//		public int maxGroups;

		public int minDistance;
		
		public FilterBy filterBy = FilterBy.P_CHANGE;
		
        public Parameters()
        {
            minTotal    = 40;
            maxTotal    = 140;
//            maxPValue   = 0.05;
//            maxFDR      = 0.05;
            maxPFDR 	= 0.05;
//            maxGroups   = 10;
            minDistance = 1;
        }
        
        public boolean compare(Object obj) {
        	if (obj == null) {
        		return false;
        	}
        	if (! (obj instanceof Parameters)) {
        		return false;
        	}
        	final Parameters other = (Parameters) obj;
        	if  (minTotal != other.minTotal
        			|| maxTotal != other.maxTotal
//					|| maxPValue != other.maxPValue
        			|| maxPFDR != other.maxPFDR
        			|| filterBy != other.filterBy
//        			|| maxGroups != other.maxGroups
					|| minDistance != other.minDistance) {
        		return false;
        	}
        	return true;
        }
        
        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer();

            buf.append("minTotal = " + minTotal+"\n");
            buf.append("maxTotal = " + maxTotal+"\n");

            buf.append(filterBy.string() + " = " + maxPFDR+"\n");

//            buf.append("maxGroups = " + maxGroups +"\n");
            buf.append("minDistance = " + minDistance +"\n");            
            return buf.toString();
        }
        
        public void check()
        {
            if( minTotal < 0 )
                minTotal = 0;
            
            if( maxTotal < minTotal )
                maxTotal = minTotal;
            
//            maxPValue = MathUtility.restrict(maxPValue,0.0,1.0);
//            maxFDR = MathUtility.restrict(maxFDR,0.0,1.0);
            if (filterBy == FilterBy.P_VALUE
            		|| filterBy == FilterBy.P_UNDER
            		|| filterBy == FilterBy.P_OVER
            		|| filterBy == FilterBy.P_CHANGE) {
            	maxPFDR = MathUtility.restrict(maxPFDR,0.0,1.0);
            } else if (filterBy == FilterBy.FDR
            		|| filterBy == FilterBy.FDR_UNDER
            		|| filterBy == FilterBy.FDR_OVER
            		|| filterBy == FilterBy.FDR_CHANGE) {
            	maxPFDR = MathUtility.restrict(maxPFDR, 0.0, Double.MAX_VALUE);
            } else {
            	throw new IllegalStateException();
            }
//            maxGroups = MathUtility.restrict(maxGroups,2,100);
            minDistance = MathUtility.restrict(minDistance,1,100);
        }
    }    
    
    private Parameters params;
    private transient IVennDataModel dataModel;
    private transient BitSet groups;
//    private boolean valid;
    private transient GoTree goTree;
   
    
    private GODistanceFilter()
    {
        params = new Parameters();
        groups = new BitSet();
//        valid = false;
        goTree = null;
    }
    
    public GODistanceFilter( GoTree goTree )
    {
    	this();
        this.goTree = goTree;
        if (goTree == null) {
        	throw new IllegalArgumentException("goTree must not be null");
        }
    }
    
    public void setGoTree( GoTree goTree )
    {
    	this.goTree = goTree;
        if (goTree == null) {
        	throw new IllegalArgumentException("goTree must not be null");
        }

        if (dataModel != null) {
        	validate();
        	notifyUser();
        }
    }
    
//    private boolean accepts( int nTotal, int nChange, double pValue )
//    {
//    	params.useFdr = false; // TODO
//        if( nTotal < params.minTotal || nTotal > params.maxTotal )
//            return false;
//            
//        if( pValue > params.maxPValue )
//            return false;
//        
//        return true;
//    }
        
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
    		AbstractGOCategoryProperties props = (AbstractGOCategoryProperties)dataModel.getGroupProperties(i);
    		if( props != null )
    		{
    			props.setMeanDist(-1);

    			if (filterCategory(props)) {
    				groups.set(i);
    			}
    		}
    	}

    	// distances
    	if( goTree == null )
    	{
    		System.out.println("no goTree available");
    		throw new IllegalStateException("goTree must not be null");
//    		return;
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
    		AbstractGOCategoryProperties props = (AbstractGOCategoryProperties)dataModel.getGroupProperties(i);
    		if( props != null )
    		{
    			props.setMeanDist(0.0);

    			if (groups.cardinality() >= 2) {
    				for( int jj=0; jj<dist.length; ++jj )
    				{
    					props.setMeanDist(props.getMeanDist() + dist[ii][jj]);
    				}
    				props.setMeanDist(props.getMeanDist() / (double)(dist.length-1));
    			} else {
    				props.setMeanDist(1.0);
    			}
    		}
    		++ii;
    	}
    }
    
    private boolean filterCategory(AbstractGOCategoryProperties cat) {
    	cat.setFilterBy(params.filterBy);

    	if (! cat.checkRangeTotal(params.minTotal, params.maxTotal)) {
    		return false;
    	}
    	
    	if (cat.getPFDRValue() > params.maxPFDR) {
    		return false;
    	}
    	
    	return true;
    }
    
    public int[] whichNTotalsOccur(boolean log) {
    	TreeSet<Integer> nTotals = new TreeSet<Integer>();
    	final int numGroups = dataModel.getNumGroups();
    	int totalMax = 0;
    	for (int i = 0; i < numGroups; i++) {
    		AbstractGOCategoryProperties property = (AbstractGOCategoryProperties) dataModel.getGroupProperties(i);
    		
    		if (property.getNTotal() > totalMax) {
    			totalMax = property.getNTotal();
    		}

    		if (log) {
    			nTotals.add(property.getNTotalLog());
    		} else {
    			nTotals.add(property.getNTotal());
    		}
    	}
    	if (log
				&& Math.log(totalMax) / Math.log(Constants.WHICH_NTOTAL_LOG)
				> Math.floor(Math.log(totalMax) / Math.log(Constants.WHICH_NTOTAL_LOG))) {
			nTotals.add(AbstractGOCategoryProperties.log(totalMax) + 1);
		}

    	Object[] nTotalsArr = nTotals.toArray();
    	int[] res = new int[nTotalsArr.length];
    	for (int i = 0; i < nTotalsArr.length; i++) {
    		res[i] = (Integer) nTotalsArr[i];
    	}

    	return res;
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

    private BitSet findMaxDistSubset( int[][] dist, final int minDist ) {
    	return findMaxDistSubset(dist, minDist, groups);
    }
    
    
    private BitSet findMaxDistSubset( int[][] dist, final int minDist, BitSet groups0 ) 
    {
    	Assert.assertNotNull( dist );
    	Assert.assertTrue( minDist >= 1 );
    	BitSet set = new BitSet();
    	final int n = dist.length;
    	Assert.assertEquals(n, groups0.cardinality());
    	
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
        	if(i < minDist)
        	{
            	int ll = 0,
            	a=0,
            	b=0;
            	for( int l = groups0.nextSetBit(0); l >= 0; l = groups0.nextSetBit(l+1) )
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
            	assert ll > 0;
            	assert a != b;
            	
    			WhichNode eli = goHigherNode(a,b);

    			for( int j=0; j<n; ++j )
        		{
        			// node to eliminate is the one located higher in the tree (for distance = 1 this is parent node)
        			if(eli==WhichNode.NODE1)
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
        		if(eli==WhichNode.NODE1)
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
    	AbstractGOCategoryProperties props1 = (AbstractGOCategoryProperties)dataModel.getGroupProperties(i),
    						 props2 = (AbstractGOCategoryProperties)dataModel.getGroupProperties(j);
    	if( (props1 == null) || (props2 == null ) )
    	{
    		return -1;
    	}
    	long goID1 = props1.getID(),
    		 goID2 = props2.getID();
    	
    	// return goTree.findMinDistanceToSharedParent(goID1, goID2);
    	return goTree.findDistanceBetweenNodes(goID1, goID2);
    }
	
	public WhichNode goHigherNode(int i, int j)
	{
		AbstractGOCategoryProperties props1 = (AbstractGOCategoryProperties)dataModel.getGroupProperties(i),
		 					 props2 = (AbstractGOCategoryProperties)dataModel.getGroupProperties(j);
		if( (props1 == null) || (props2 == null ) )
		{
			return WhichNode.NONE;
		}
		long goID1 = props1.getID(),
		goID2 = props2.getID();

		return goTree.findLessDistantNodeToSharedParent(goID1, goID2);
	}
    
	private int[][] computeDistances() {
		return computeDistances(groups);
	}
	
    private int[][] computeDistances(BitSet groups0)
    {
    	if( groups0.cardinality() < 2 )
    	{
//    		System.err.println("updateDistances() not enough groups");
    		return null;
    	}
    	
    	int n = groups0.cardinality();
    	
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
    	for( int i = groups0.nextSetBit(0); i >= 0; i = groups0.nextSetBit(i+1) ) 
    	{
    		// int jj = 0; BUG! JKraus 15.05.2007
    		int jj = ii;
    		for( int j = groups0.nextSetBit(i); j >= 0; j = groups0.nextSetBit(j+1) ) 
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
    
    private void validate() {
    	update();
//    	valid = true;
    }
    
    public void setParameters(Parameters params)
    {
//    	valid = false;
    	this.params = params;
        this.params.check();

        if (dataModel != null) {
        	validate();
        	notifyUser();
        }
    }
    
    public Parameters getParameters()
    {
        return params;
    }
        
	// assumption: model has changed if this function is called
	// (either new object or same object and new state)
	// and if model has changed this function must be called
    //  @Override
    public void setDataModel(IVennDataModel model) {
    	dataModel = model;

    	validate();
    	notifyUser();
    }
    
    @Override
    public boolean accept(int groupID) {
//    	if( goTree == null )
//    		return true;

//    	if (! valid) {
//    		validate();
//    	}
    	return groups.get(groupID);
    }
    
//    @Override
//    public Object clone()
//    {
//        // SystemUtility.serialClone(this);
////    	System.out.println("clone");
//    	GODistanceFilter filter = new GODistanceFilter();
//    	filter.setParameters((Parameters) SystemUtility.serialClone(params));
//    	filter.dataModel = dataModel;
//    	filter.goTree = goTree;
//    	return filter;
//    }
    
    @Override
    public Object clone()
    {
        // SystemUtility.serialClone(this);
//    	System.out.println("clone");
    	GODistanceFilter filter = (GODistanceFilter) super.clone();
//    	filter.setParameters((Parameters) SystemUtility.serialClone(params));
    	filter.params = (GODistanceFilter.Parameters) SystemUtility.serialClone(params);
    	// ! dataModel not copied
    	filter.dataModel = dataModel;
    	filter.goTree = goTree;
    	filter.groups = (BitSet) groups.clone();
    	return filter;
    }
    
    @Override
    public String toString()
    {
        
        return "GODistanceFilter\n"+params.toString();
    }
    


//  only for junit tests
    
    public int[][] jutComputeDistances(BitSet groups0) {
		return computeDistances(groups0);
	}
	
    public BitSet jutFindMaxDistSubset( int[][] dist, final int m, BitSet groups0 ) {
    	return findMaxDistSubset(dist, m, groups0);
    }

}
