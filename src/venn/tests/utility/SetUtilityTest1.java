/*
 * Created on 13.06.2005
 *
 */
package venn.tests.utility;

import java.util.BitSet;

import venn.utility.SetUtility;

import junit.framework.TestCase;

/**
 * @author muellera
 *
 */
public class SetUtilityTest1 extends TestCase 
{
	public static final int NUM_OF_SETS = 10;
	public static final int NUM_OF_ELEMENTS = 200;
	private BitSet[] sets, result;
	
	/**
	 * 
	 */
	public SetUtilityTest1(String name)
	{
		super(name);
	}
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		sets = new BitSet[6];
		
		for( int i=0; i<sets.length; ++i )
		{
		    sets[i] = new BitSet();
		}
		
		sets[0].set(0);
		sets[0].set(1);
		
		sets[1].set(1);
		sets[1].set(2);
		sets[1].set(3);
		
		sets[2].set(3);
		sets[2].set(4);
		
		sets[3].set(3);
		sets[3].set(4);		
		
		sets[4].set(5);
		sets[4].set(6);
		
		sets[5].set(5);
		sets[5].set(6);
		
		result = new BitSet[2];
		for( int i=0; i<result.length; ++i )
		{
		    result[i] = new BitSet();
		}		
		
		result[0].set(0);
		result[0].set(1);
		result[0].set(2);
		result[0].set(3);
		
		result[1].set(4);
		result[1].set(5);
	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testSetUtilityPartition()
	{
		BitSet[] partition = SetUtility.partition(sets);
		
		assertNotNull(partition);
		assertTrue(partition.length > 0);
		
		assertEquals( partition.length, result.length );
		for(int i=0; i<partition.length; ++i )
		{
		    partition[i].equals( result[i] );
		}
		
		// cumulate
		BitSet[] sum = new BitSet[partition.length];
		for( int i=0; i<partition.length; ++i )
		{
			for( int j=0, idx=0; j<partition[i].cardinality(); ++j, ++idx)
			{
				idx = partition[i].nextSetBit(idx);
				if( j == 0 )
				{
					sum[i] = (BitSet)sets[idx].clone();
				}
				else
				{
					sum[i].or(sets[idx]);
				}
			}
		}
		
		// test disjunctness
		for(int i=0; i<partition.length-1; ++i)
		{
			for(int j=i+1; j<partition.length; ++j)
			{
				assertFalse( sum[i].intersects(sum[j]) );
			}
		}
		
		// test completeness
		BitSet total = new BitSet();
		int csum = 0;
		for( int i=0; i<sum.length; ++i )
		{
			total.or(sum[i]);
			csum += sum[i].cardinality();
		}
		
		BitSet total2 = new BitSet();
		for( int i=0; i<sets.length; ++i )
		{
			total2.or(sets[i]);
		}
		assertTrue(total.cardinality() == total2.cardinality() );
		assertTrue(total2.equals(total));
	}
	
}
