/**
 * 
 */
package venn.gui;

import java.util.Arrays;

import javax.swing.SpinnerNumberModel;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * A SpinnerModel for double arrays.
 * Differences to SpinnerNumberModel:<p>
 * -it is possible to set values that are not in the array<p>
 * -<code>setArray</code> doesn't influence <code>getValue</code><p>
 * -<code>setArray</code> never fires a change event<p>
 * -<code>setValue</code> always fires a change event even if the same value is set twice
 */
public class DoubleArraySpinnerModel
extends SpinnerNumberModel { // extends SpinnerNumberModel so that JSpinner.NumberEditor can be used
	private double[] vals;
	private double val = -9999.0;
	
	/**
	 * 
	 */
	public DoubleArraySpinnerModel() {
		vals = new double[0];
	}

	public DoubleArraySpinnerModel(double[] values) {
		setArray(values);
	}
	
	/**
	 * the return value of <code>getValue</code> is not changed,
	 * doesn't fire a change event
	 * @param unsorted array with unique values
	 */
	public void setArray(double[] values) {
		if (values == null) {
			vals = new double[0];
			return;
		}

		vals = values.clone();
		Arrays.sort(vals);
		for (int i = 0; i < vals.length - 1; i++) {
			if (vals[i] == vals[i + 1]) {
				throw new IllegalArgumentException("elements must be unique");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SpinnerModel#getNextValue()
	 */
	@Override
	public Object getNextValue() {
		int ind = Arrays.binarySearch(vals, val);
		if (ind >= 0) {
			ind++;
		} else {
			ind = -ind - 1;
		}

		if (ind < vals.length) {
			val = vals[ind];
		} else {
			if (vals.length == 0 || val > vals[vals.length - 1]) {
				return val;
			}
			val = vals[vals.length - 1];
		}

		return val;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SpinnerModel#getPreviousValue()
	 */
	@Override
	public Object getPreviousValue() {
		int ind = Arrays.binarySearch(vals, val);
		if (ind >= 0) {
			ind--;
		} else {
			ind = -ind - 1;
			ind--;
		}

		if (ind >= 0) {
			val = vals[ind];
		} else {
			if (vals.length == 0 || val < vals[0]) {
				return val;
			}
			val = vals[0];
		}

		return val;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SpinnerModel#getValue()
	 */
	@Override
	public Object getValue() {
		return val;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SpinnerModel#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		if (!(value instanceof Double)) {
			throw new IllegalArgumentException("Double expected");
		}
		if (((Double) value).doubleValue() != val) {
			val = (Double) value;
		}
		// fires always a change event
		fireStateChanged();
	}
	
	
	@Override
	public Comparable getMinimum() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public Comparable getMaximum() {
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * not implemented
	 */
	@Override
	public Number getNumber() {
		throw new NotImplementedException();
	}

	/**
	 * not implemented
	 */
	@Override
	public Number getStepSize() {
		throw new NotImplementedException();
	}

	/**
	 * not implemented
	 */
	@Override
	public void setMinimum(Comparable minimum) {
		throw new NotImplementedException();
	}
	
	/**
	 * not implemented
	 */
	@Override
	public void setMaximum(Comparable maximum) {
		throw new NotImplementedException();
	}

	/**
	 * not implemented
	 */
	@Override
	public void setStepSize(Number stepSize) {
		throw new NotImplementedException();
	}
	
}
