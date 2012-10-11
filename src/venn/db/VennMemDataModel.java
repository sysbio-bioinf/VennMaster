/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.util.BitSet;

/**
 * A IVennDataModel implementation which resides in memory.
 * 
 * @author muellera
 * 
 */
public class VennMemDataModel extends AbstractVennDataModel {
	BitSet groupElements[];
	String groupNames[], elementNames[];
	Object groupProperties[];
	ElementProperties elementProperties[];
	int numOfGroups, numOfElements;

	public VennMemDataModel() {
		super();
	}

	public VennMemDataModel(int numOfGroups, int numOfElements) {
		setSize(numOfGroups, numOfElements);
	}

	public void clear() {
		groupElements = null;
		groupNames = null;
		groupProperties = null;
		elementNames = null;
		elementProperties = null;
		numOfGroups = 0;
		numOfElements = 0;
	}

	public boolean isEmpty() {
		return (numOfGroups == 0);
	}

	public void setSize(int numOfGroups, int numOfElements) {
		this.numOfGroups = numOfGroups;
		this.numOfElements = numOfElements;

		groupElements = new BitSet[numOfGroups];
		groupNames = new String[numOfGroups];
		groupProperties = new Object[numOfGroups];
		elementProperties = new ElementProperties[numOfElements];
		elementNames = new String[numOfElements];

		// fireChangeEvent();
		notifySucc();
	}

	public void setGroupElements(int gid, BitSet elements) {
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		if (elements.length() > getNumElements())
			throw new IndexOutOfBoundsException("element id out of bounds");

		groupElements[gid] = (BitSet) elements.clone();
		// fireChangeEvent();
		notifySucc();
	}

	public void setGroupName(int gid, String name) {
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		groupNames[gid] = name;
		// fireChangeEvent();
		notifySucc();
	}

	public void setElementName(int eid, String name) {
		if (eid < 0 || eid >= getNumElements())
			throw new IndexOutOfBoundsException("element id out of bounds");

		elementNames[eid] = name;
		elementProperties[eid].setName(name);
		// fireChangeEvent();
		notifySucc();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.VennDataModelInterface#getNumGroups()
	 */
	public int getNumGroups() {
		return numOfGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.VennDataModelInterface#getNumElements()
	 */
	public int getNumElements() {
		return numOfElements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.VennDataModelInterface#getGroupElements(int)
	 */
	public BitSet getGroupElements(int gid) {
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		return groupElements[gid];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.VennDataModelInterface#getGroupName(int)
	 */
	public String getGroupName(int gid) {
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		return groupNames[gid];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.VennDataModelInterface#getElementName(int)
	 */
	public String getElementName(int eid) {
		if (eid < 0 || eid >= getNumGroups())
			throw new IndexOutOfBoundsException("element id out of bounds");

		return elementNames[eid];
	}

	public Object getGroupProperties(int gid) {
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		return groupProperties[gid];
	}

	public void setGroupProperties(int gid, Object property) {
		// TODO Auto-generated method stub
		if (gid < 0 || gid >= getNumGroups())
			throw new IndexOutOfBoundsException("group id out of bounds");

		groupProperties[gid] = property;
	}

	public void setElementProperties(int eid,ElementProperties eProps) {
		if (eid < 0 || eid >= getNumGroups()) {
			throw new IndexOutOfBoundsException("element id out of bounds");
		}

		elementProperties[eid]=eProps;

	}

	@Override
	public ElementProperties getElementProperties(int elementID) {
		return elementProperties[elementID];
	}

}
