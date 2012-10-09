package venn.db;

import java.io.Serializable;

public class ElementProperties implements Serializable{

	public ElementProperties(String name, DiffExprValue diffExprValue,
			String shortName) {
		super();
		this.name = name;
		this.diffExprValue = diffExprValue;
		this.shortName = shortName;
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
	}

	String name;
	DiffExprValue diffExprValue;
	String shortName;

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
