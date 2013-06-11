package venn.db;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class holding properties additional properties of an element. The property
 * the field {@code name} is redundant with most implementations of the
 * {@link venn.db.IVennDataModel} which hold a seperate field for this property.
 * 
 * @author behrens
 * 
 */

public class ElementProperties implements Serializable {

	String name;
	DiffExprValue diffExprValue;
	String shortName;

	HashMap<String, HashMap<String, Number>> columns;

	public ElementProperties(String name, DiffExprValue diffExprValue,
			String shortName) {
		super();
		this.name = name;
		this.diffExprValue = diffExprValue;
		this.shortName = shortName;
		this.columns = new HashMap<String, HashMap<String, Number>>();

	}

	public ElementProperties(String name, String diffExprValue, String shortName) {

		super();
		// set differentialExpressionInformation
		DiffExprValue diffVal;
		if (diffExprValue.equalsIgnoreCase("under")) {
			diffVal = DiffExprValue.UNDER;
		} else if (diffExprValue.equalsIgnoreCase("over")) {
			diffVal = DiffExprValue.OVER;
		} else {
			diffVal = DiffExprValue.UNKNOWN;
		}

		this.name = name;
		this.diffExprValue = diffVal;
		this.shortName = shortName;
		this.columns = new HashMap<String, HashMap<String, Number>>();
	}

	
	/**
	 * set the Number corresponding to column name and group name for this Element
	 * 
	 * @param colName
	 * @param groupName
	 * @param value
	 */
	public void setColumn(String colName, String groupName, Number value) {
		HashMap<String, Number> colMap = columns.get(colName);
		if (colMap == null) {
			colMap = new HashMap<String, Number>();
			columns.put(colName, colMap);
		}
		colMap.put(groupName, value);
	}

	/**
	 * returns the number for this element given for the corresponding column
	 * name and group, or null if there is no Number stored for this column-group combination
	 * 
	 * @param colName
	 * @param groupName
	 * @return
	 */
	public Number getColNumber(String colName, String groupName) {
		HashMap<String, Number> colMap = columns.get(colName);
		if (colMap == null) {
			return null;
		}

		return colMap.get(groupName);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDiffExprValue(DiffExprValue diffExprValue) {
		this.diffExprValue = diffExprValue;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public DiffExprValue getDiffExprValue() {
		return diffExprValue;
	}

	public String getShortName() {
		return shortName;
	}

}
