/**
 * @author mueller
 *
 */
package venn.tests.utility;

import java.util.BitSet;
import java.util.Random;

import venn.utility.SetUtility;

import junit.framework.*;

public class SetUtilityTest extends TestCase
{
	public static final int NUM_OF_SETS = 10;
	public static final int NUM_OF_ELEMENTS = 200;
	private BitSet[] sets;  
	
	/**
	 * 
	 */
	public SetUtilityTest(String name)
	{
		super(name);
	}
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		Random random = new Random();
		sets = new BitSet[NUM_OF_SETS];
		for(int i=0; i<sets.length; ++i)
		{
			sets[i] = new BitSet();
			int num = 1 + random.nextInt(NUM_OF_ELEMENTS/3);
			for(int j=0; j<num; ++j)
			{
				sets[i].set(random.nextInt(NUM_OF_ELEMENTS));
			}
		}		
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
