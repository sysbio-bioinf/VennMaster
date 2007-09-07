/*
 * VennMaster/geometry/SetUtility.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.utility;

import java.util.*;

/**
 * Help functions for BitSets
 * @author muellera
 */
public class SetUtility
{
	
	/**
	 * Looks up only the marked group indices in <var>indices</var> and returns
	 * an array with size indices.cardinality().
	 * 
	 * @param objects
	 * @param indices
	 * @return A sub-family of sets
	 */
	public static BitSet[] lookup(BitSet[] objects, BitSet indices)
	{
		int			card = indices.cardinality();
		BitSet[]	result = new BitSet[card];
		
		for(int i=0, j=0; i<card; ++i, ++j)
		{
			j = indices.nextSetBit(j);
			result[i] = objects[j];
		}
		return result;
	}
	
	/**
	 * 
	 * @param sets
	 * @return The union over all sets
	 */
	public static BitSet union(BitSet[] sets)
	{
		// initialize bit set to the whole set
		int n = 0;
		for(int i=0; i<sets.length;++i)
		{
			if( sets[i].size() > n )
				n = sets[i].size();	
		}
		BitSet set = new BitSet(n);
		for(int i=0; i<sets.length; ++i )
		{
			set.or(sets[i]);
		}
		return set;
	}
	
	/**
	 * Partitions a number of bitsets in disjunct parts.
	 * 
	 * @param sets
	 * @return An array of BitSet[]'s representing which set is included (the set index
	 * of the array sets[] is now contained)
	 * 
	 * set[0] = { 0, 1, 2 }
	 * set[1] = { 2, 3 }
	 * set[2] = { 4, 5 }
	 * 
	 * => ( { 0, 1 }, { 2 } )
	 * 
	 */
	public static BitSet[] partition(BitSet[] sets)
	{
		LinkedList list = new LinkedList();
		BitSet marker = new BitSet(sets.length);	// which sets have already been observed
		BitSet which = new BitSet(sets.length);
		int remaining = sets.length;
		while( remaining > 0 )
		{ // while there are unassigned sets
			BitSet uset = null;
			int count = 0;
			which.clear();	// the current set of groups
			do
			{
				count = 0;
				for(int j=0, i=0; j<remaining; ++j,++i)
				{
					i = marker.nextClearBit(i);
					if( uset == null ) 
					{
						uset = (BitSet)sets[i].clone();
						which.set(i);
						marker.set(i);
						++count;
					}
					else
					{
						if( uset.intersects(sets[i]) )
						{ // merge results with the given set
							uset.or(sets[i]);
							which.set(i);
							marker.set(i);
							++count;
						}
					}
				} // for
				remaining -= count;
			}
			while( count > 0 && remaining > 0 );
			list.add(which.clone());
		}
		
		BitSet[] partition = new BitSet[list.size()];
		int idx = 0;
		Iterator iter = list.iterator();
		while( iter.hasNext() )
		{
			partition[idx] = (BitSet)iter.next();
			++idx;
		}
		
		return partition;
	}
	
	/**
	 * 
	 * @param sets
	 * @return An array of cardinalities.
	 */
	public static int[] cardinality(BitSet[] sets)
	{
		int[] card = new int[sets.length];
		for(int i=0; i<sets.length;++i)
		{
			card[i] = sets[i].cardinality();
		}
		return card;		
	}

	/**
	 * @param sets
	 * @return a square matrix with the intersection cardinalities of all pairs
	 */
	public static int[][] intersectionMatrix(BitSet[] sets)
	{
		int[][] matrix = new int[sets.length][sets.length];
			
		for(int i=0; i<sets.length; ++i)
		{
			matrix[i][i] = sets[i].cardinality();
			for(int j=i+1; j<sets.length; ++j)
			{
				BitSet tmp = (BitSet)sets[i].clone();
				tmp.and(sets[j]);
								
				matrix[i][j] = tmp.cardinality();
				matrix[j][i] = matrix[i][j];
			}
		}		
		return matrix;
	}
	
	public static void main(String[] args)
	{
		BitSet[]	sets = new BitSet[5];
		for( int i=0; i<sets.length; ++i )
			sets[i] = new BitSet();
		
		sets[0].set(0);
		sets[0].set(1);
		sets[0].set(2);
		
		sets[1].set(2);
		sets[1].set(3);
		sets[1].set(4);
		
		sets[2].set(0);
		sets[2].set(5);
		sets[2].set(6);
		
		sets[3].set(7);
		sets[3].set(8);
		
		sets[4].set(8);
		sets[4].set(9);
		
		
		BitSet[] part = partition(sets);
		
		for(int i=0; i<sets.length; ++i )
			System.out.println("A"+i+" = "+sets[i]);
		
		for(int i=0; i<part.length; ++i )
		{
			System.out.print(part[i]+" : ");
			BitSet[] result = lookup(sets,part[i]);
			for(int j=0; j<result.length; ++j )
				System.out.print(result[j]+" ");
			System.out.println();
		}
		
		System.out.println();
	}
}
