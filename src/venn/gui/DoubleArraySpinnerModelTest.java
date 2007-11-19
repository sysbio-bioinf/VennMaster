/**
 * 
 */
package venn.gui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.TestCase;

public class DoubleArraySpinnerModelTest extends TestCase implements ChangeListener {
	private DoubleArraySpinnerModel model;
	private ChangeEvent lastChangeEvent;
	private int stateChangeCount;
	
	/**
	 * @param name
	 */
	public DoubleArraySpinnerModelTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		stateChangeCount = 0;
		lastChangeEvent = null;
		double[] vals = {3.2, 2.0, 3.6, 6.0, 5.1};
		model = new DoubleArraySpinnerModel(vals);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

//	/**
//	 * Test method for {@link venn.gui.pValueSpinnerModel#pValueSpinnerModel()}.
//	 */
//	public void testPValueSpinnerModel() {
//	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setArray(double[])}.
	 */
	public void testSetArray1() {
		model = new DoubleArraySpinnerModel();
		double[] vals = {3.2, 2.0, 3.6, 6.0, 5.1};
		
		model.setArray(vals);
		model.setValue(5.9);
		assertEquals(6.0, model.getNextValue());
	}
	
	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setArray(double[])}.
	 */
	public void testSetArray2() {
		model = new DoubleArraySpinnerModel();
		// values not unique
		double[] vals = {3.2, 2.0, 3.6, 6.0, 3.2, 5.1};
		
		try {
			model.setArray(vals);
			fail();
		} catch (IllegalArgumentException expected) {
			assertTrue(true);
		}
	}
	
	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setArray(double[])}.
	 */
	public void testSetArray3() {
		model = new DoubleArraySpinnerModel();
		model.addChangeListener(this);
		double[] vals = {3.2, 2.0, 3.6, 6.0, 5.1};

		model.setArray(vals);
		assertEquals(0, stateChangeCount);
		model.setArray(vals);
		assertEquals(0, stateChangeCount);
	}
	
	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue1() {
		model.setValue(3.2);
		assertEquals(3.6, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue2() {
		model.setValue(3.5);
		assertEquals(3.6, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue3() {
		model.setValue(6.0);
		assertEquals(6.0, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue4() {
		model.setValue(6.5);
		assertEquals(6.5, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue5() {
		model.setValue(1.5);
		assertEquals(2.0, model.getNextValue());
		assertEquals(3.2, model.getNextValue());
		assertEquals(3.6, model.getNextValue());
		assertEquals(5.1, model.getNextValue());
		assertEquals(6.0, model.getNextValue());
		assertEquals(6.0, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getNextValue()}.
	 */
	public void testGetNextValue6() {
		model = new DoubleArraySpinnerModel();
		assertEquals(-9999.0, model.getNextValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue1() {
		model.setValue(2.0);
		assertEquals(2.0, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue2() {
		model.setValue(1.5);
		assertEquals(1.5, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue3() {
		model.setValue(5.3);
		assertEquals(5.1, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue4() {
		model.setValue(3.6);
		assertEquals(3.2, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue5() {
		model.setValue(6.5);
		assertEquals(6.0, model.getPreviousValue());
		assertEquals(5.1, model.getPreviousValue());
		assertEquals(3.6, model.getPreviousValue());
		assertEquals(3.2, model.getPreviousValue());
		assertEquals(2.0, model.getPreviousValue());
		assertEquals(2.0, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getPreviousValue()}.
	 */
	public void testGetPreviousValue6() {
		model = new DoubleArraySpinnerModel();
		assertEquals(-9999.0, model.getPreviousValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getValue()}.
	 */
	public void testGetValueNotSet() {
		model = new DoubleArraySpinnerModel(null);
		assertEquals(-9999.0, model.getValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#getValue()}.
	 */
	public void testGetValue() {
		model.setValue(123.0);
		double[] vals = {4.0, 2.0, 3.5};
		model.setArray(vals);
		assertEquals(123.0, model.getValue());
	}

//	/**
//	 * Test method for {@link venn.gui.pValueSpinnerModel#getValue()}.
//	 */
//	public void testGetValue() {
//	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setValue(java.lang.Object)}.
	 */
	public void testSetValueInArr() {
		model.setValue(2.0);
		assertEquals(2.0, model.getValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setValue(java.lang.Object)}.
	 */
	public void testSetValueNotInArr() {
		model.setValue(3.14);
		assertEquals(3.14, model.getValue());
	}

	/**
	 * Test method for {@link venn.gui.DoubleArraySpinnerModel#setValue(java.lang.Object)}.
	 */
	public void testSetValueWithChangeEvent() {
		model.addChangeListener(this);
		model.setValue(3.14);
		assertEquals(1, stateChangeCount);
		model.setValue(3.14);
		assertEquals(2, stateChangeCount);
		Object val = model.getNextValue();
		model.setValue(val);
		assertEquals(3, stateChangeCount);
		model.removeChangeListener(this);
		model.setValue(val);
		assertEquals(3, stateChangeCount);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		stateChangeCount++;
		lastChangeEvent = e;
	}

}
