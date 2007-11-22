/*
 * Created on 16.02.2006
 *
 */
package venn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import venn.AllParameters;
import venn.Constants;
import venn.db.AbstractGOCategoryProperties;
import venn.db.GOCategoryProperties1p;
import venn.db.GOCategoryProperties1p1fdr;
import venn.db.GOCategoryProperties3p;
import venn.db.GOCategoryProperties3p3fdr;
import venn.db.GODistanceFilter;
import venn.db.GoTree;
import venn.db.IVennDataModel;
import venn.db.VennFilteredDataModel;
import venn.db.GODistanceFilter.Parameters.FilterBy;
import venn.event.IFilterChainSucc;
import venn.utility.SystemUtility;


/**
 * A plug-in panel for the VennMaster main application for online filtering
 * a previously loaded dataset.
 * This is view/controller 
 * 
 * @author muellera
 *
 */
public class FilterPanel extends JPanel
//implements java.awt.event.ActionListener, java.awt.event.KeyListener, PropertyChangeListener, ChangeListener
		implements java.awt.event.ActionListener, java.awt.event.KeyListener,
		PropertyChangeListener, IFilterChainSucc, ChangeListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private LinkedList<ActionListener> actionListeners;
    
    private JComboBox maxPFDRComboBox;
    
    private JSpinner  	maxPFDR,
    					minTotal,
    					maxTotal,
    					minDistance;
    
    private SpinnerModel 	maxPFDRSpinnerModel,
    						minTotalSpinnerModel,
    						maxTotalSpinnerModel,
    						minDistanceSpinnerModel;
    
    private JFormattedTextField numCategories,
    							numElements,
    							numFilteredCategories,
    							numFilteredElements;

    private JButton resetButton;
    
    private JLabel 	minTotalLabel,
    				maxTotalLabel,
    				numElementsLabel,
    				numFilteredElementsLabel;

//    private LinkedList<JComponent>          fields;
    
    private VennFilteredDataModel   dataModel;
    private IVennDataModel   		origDataModel;

    private GODistanceFilter 	  filterToUse;
    private VennFilteredDataModel modelToUse;
    
    private GoTree goTree;
    
    private boolean listeningOff;
    
    private AllParameters params;
    
    private String floatFormatString;

    
    public FilterPanel(GoTree goTree)
    {
        //super(new GridLayout(2,1));
        super( new BorderLayout() );
        
        this.goTree = goTree;
        
        JPanel panel = new JPanel();
        
        GridLayout gridLayout = new GridLayout(8,2);
        panel.setLayout(gridLayout);
        
        actionListeners = new LinkedList<ActionListener>();
        
        floatFormatString = "0.0000";
        String intFormatString   = "0";
        
        //setMinimumSize(new Dimension(getWidth(),200));
        
        

        
        maxPFDRComboBox = new JComboBox();
        maxPFDRComboBox.setEditable(false);
        panel.add(maxPFDRComboBox);
        
        maxPFDRSpinnerModel = new DoubleArraySpinnerModel();
        maxPFDR = new JSpinner(maxPFDRSpinnerModel);
        maxPFDR.setEditor(new JSpinner.NumberEditor(maxPFDR, floatFormatString));
        maxPFDRSpinnerModel.addChangeListener(this);
        panel.add(maxPFDR);

        minTotalLabel = new JLabel();
//        panel.add(new JLabel("min total"));
        panel.add(minTotalLabel);
        minTotalSpinnerModel = new DoubleArraySpinnerModel();
        minTotal = new JSpinner(minTotalSpinnerModel);
        minTotal.setEditor(new JSpinner.NumberEditor(minTotal, intFormatString));
        minTotalSpinnerModel.addChangeListener(this);
        panel.add(minTotal);

        maxTotalLabel = new JLabel();
//        panel.add(new JLabel("max total"));
        panel.add(maxTotalLabel);
        maxTotalSpinnerModel = new DoubleArraySpinnerModel();
        maxTotal = new JSpinner(maxTotalSpinnerModel);
        maxTotal.setEditor(new JSpinner.NumberEditor(maxTotal, intFormatString));
        maxTotalSpinnerModel.addChangeListener(this);
        panel.add(maxTotal);
        
        
        panel.add(new JLabel("min distance"));
        minDistanceSpinnerModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        minDistance = new JSpinner(minDistanceSpinnerModel);
        minDistance.setEditor(new JSpinner.NumberEditor(minDistance, intFormatString));
        minDistance.addChangeListener(this);
        panel.add(minDistance);
                

        panel.add(new JLabel("#categories"));
        numCategories = new JFormattedTextField(new DecimalFormat(intFormatString));
        numCategories.setEditable(false);
        panel.add(numCategories);
        
        numElementsLabel = new JLabel();
//        panel.add(new JLabel("#elements"));
        panel.add(numElementsLabel);
        numElements = new JFormattedTextField(new DecimalFormat(intFormatString));
        numElements.setEditable(false);
        panel.add(numElements);
        

        panel.add(new JLabel("#filtered categories"));
        numFilteredCategories = new JFormattedTextField(new DecimalFormat(intFormatString));
        numFilteredCategories.setEditable(false);
        numFilteredCategories.setFont(numFilteredCategories.getFont().deriveFont(Font.BOLD));
        panel.add(numFilteredCategories);
        
        numFilteredElementsLabel = new JLabel();
//        panel.add(new JLabel("#filtered elements"));
        panel.add(numFilteredElementsLabel);
        numFilteredElements = new JFormattedTextField(new DecimalFormat(intFormatString));
        numFilteredElements.setEditable(false);
        numFilteredElements.setFont(numFilteredElements.getFont().deriveFont(Font.BOLD));
        panel.add(numFilteredElements);
        
        add(panel,BorderLayout.NORTH);
        
        
        // Buttons
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(1,3));
        
        
        JButton button;
        
        button = new JButton("filter");
        button.setActionCommand("filter");
        button.addActionListener(this);
        panel.add(button);
        
        
        button = new JButton("update");
        button.setActionCommand("update");
        button.addActionListener(this);
        panel.add(button);

        
        resetButton = new JButton("reset");
        resetButton.setActionCommand("reset");
        resetButton.addActionListener(this);
        panel.add(resetButton);
        
        add( panel, BorderLayout.SOUTH );
    }
   
    public void setParameters(AllParameters params) {
    	if (dataModel != null && params.logNumElements != this.params.logNumElements) {
    		GODistanceFilter.Parameters filterParameters = getParameters();
    		this.params = params;
            setParameters(filterParameters);
    		update();
    	}
    	this.params = params;

    	if (params.logNumElements) {
    		minTotalLabel.setText("min total (log" + Constants.WHICH_NELEMENTS_LOG + ")");
    		maxTotalLabel.setText("max total (log" + Constants.WHICH_NELEMENTS_LOG + ")");
    		numElementsLabel.setText("#elements (log" + Constants.WHICH_NELEMENTS_LOG + ")");
    		numFilteredElementsLabel.setText("#filtered elements (log" + Constants.WHICH_NELEMENTS_LOG + ")");
    	} else {
    		minTotalLabel.setText("min total");
    		maxTotalLabel.setText("max total");
    		numElementsLabel.setText("#elements");
    		numFilteredElementsLabel.setText("#filtered elements");
    	}
    	if (dataModel != null) {
    		updateMinMaxTotalSpinnerData();
    	}

    	updateControls(); // perhaps maxCategories changed
    }
    
    private void makeComboBox(boolean pValue, boolean fdr,
    		boolean pUnder, boolean pOver, boolean pChange,
    		boolean fdrUnder, boolean fdrOver, boolean fdrChange) {
    	
    	maxPFDRComboBox.removeActionListener(this);
    	maxPFDRComboBox.removeAllItems();
    	
    	if (pValue) {
    		if (pUnder || pOver || pChange || fdrUnder || fdrOver || fdrChange) {
    			throw new IllegalArgumentException();
    		}

    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.P_VALUE));
    	}

    	if (fdr) {
    		if (pUnder || pOver || pChange || fdrUnder || fdrOver || fdrChange) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.FDR));
    	}
    	
    	if (pUnder) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.P_UNDER));
    	}
    	
    	if (pOver) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.P_OVER));
    	}
    	
    	if (pChange) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.P_CHANGE));
    	}
    	
    	if (fdrUnder) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.FDR_UNDER));
    	}
    	
    	if (fdrOver) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.FDR_OVER));
    	}
    	
    	if (fdrChange) {
    		if (pValue || fdr) {
    			throw new IllegalArgumentException();
    		}
    		
    		maxPFDRComboBox.addItem(new FilterByEnumWrapper(FilterBy.FDR_CHANGE));
    	}
    	
    	maxPFDRComboBox.addActionListener(this);
    	
    }
    
    public void addActionListener( ActionListener listener )
    {
        if( listener != null )
            actionListeners.add(listener);
    }
    
    protected void fireActionPerformed( ActionEvent event )
    {
        
        Iterator iter = actionListeners.iterator();
        while(iter.hasNext())
        {
            ((ActionListener)iter.next()).actionPerformed(event);
        }
    }
    
    private void setParameters(GODistanceFilter.Parameters param)
    {
    	listeningOff = true;
    	maxPFDR.setValue(param.maxPFDR);
    	
    	// select correct combo box item for p/fdr
    	boolean found = false;
    	for (int i = 0; i < maxPFDRComboBox.getItemCount(); i++) {
			FilterByEnumWrapper item = (FilterByEnumWrapper) maxPFDRComboBox.getItemAt(i);
			if (item.getFilterBy() == param.filterBy) {
				assert ! found;
				maxPFDRComboBox.setSelectedIndex(i);
				found = true;
			}
		}
    	if (! found) {
    		throw new IllegalStateException();
    	}
    	
    	if (params.logNumElements) {
    		maxTotal.setValue(Double.valueOf(AbstractGOCategoryProperties.log(param.maxTotal)));
    		minTotal.setValue(Double.valueOf(AbstractGOCategoryProperties.log(param.minTotal)));
    	} else {
            maxTotal.setValue(Double.valueOf(param.maxTotal));
            minTotal.setValue(Double.valueOf(param.minTotal));
    	}

        minDistance.setValue( new Integer(param.minDistance) );
        listeningOff = false;
    }    
    
    private GODistanceFilter.Parameters getParameters()
    {
        GODistanceFilter.Parameters param = new GODistanceFilter.Parameters();
        
        if( maxPFDR.getValue() != null )
        	param.maxPFDR = ((Number)maxPFDR.getValue()).doubleValue();

        param.filterBy = ((FilterByEnumWrapper) maxPFDRComboBox.getSelectedItem()).getFilterBy();
        
        if( maxTotal.getValue() != null ) {
        	param.maxTotal = ((Number)maxTotal.getValue()).intValue();
        	if (params.logNumElements) {
        		param.maxTotal = AbstractGOCategoryProperties.pow(param.maxTotal);
        	}
        }
             
        if( minTotal.getValue() != null ) {
            param.minTotal = ((Number)minTotal.getValue()).intValue();
        	if (params.logNumElements) {
        		param.minTotal = AbstractGOCategoryProperties.pow(param.minTotal);
        	}
        }
        
        if( minDistance.getValue() != null )
        	param.minDistance = ((Number)minDistance.getValue()).intValue();
        
        return param;
    }
    
    private void update()
    {
        if( dataModel != null )
        {
            GODistanceFilter filter = (GODistanceFilter)dataModel.getFilter();
            if( filter != null )
            {
                filter.setParameters( getParameters() );
            }
        }
    }
    
    /**
     * React on buttons/ GUI controls
     */
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        
        if( cmd.equalsIgnoreCase("filter") )
        {
            update();
            return;
        }
        
        if( cmd.equalsIgnoreCase("update") )
        {
        	if (origDataModel == null) {
        		return;
        	}
        	assert dataModel != null;
        	

		    // check number of categories and give out a warning
        	if( dataModel.getNumGroups() > params.maxCategories )
    		{
    			int res = JOptionPane.showConfirmDialog(this,
				"Warning: a high number of categories may lead to increased running time.\n" +
				"Are you sure to continue?\n" +
				"(The warning threshold can be set in Options -> max Categories)",
				"Warning",JOptionPane.YES_NO_OPTION);
    			if( res != JOptionPane.OK_OPTION )
    				return;        
    		}

    		if( dataModel.getNumGroups() < 1 )
    		{
    			JOptionPane.showMessageDialog(this,
    					"There are "+dataModel.getNumGroups()+" categories.\r\n"+
    					"It is not possible to display less than 1 category!\r\n",
    					"Error",0);
    			return;   
    		}
        	

    		filterToUse = (GODistanceFilter)dataModel.getFilter().clone();
        	filterToUse.setUser(null);
        	origDataModel.setSucc(null);
        	modelToUse = new VennFilteredDataModel((IVennDataModel) SystemUtility.serialClone(origDataModel), filterToUse);
        	resetButton.setEnabled(false);
        	fireActionPerformed( new ActionEvent(this,0,"update") );
        	return;
        }
        
        if (cmd.equalsIgnoreCase("reset")) {
        	if (filterToUse != null && dataModel != null) {
        		final GODistanceFilter f = (GODistanceFilter) filterToUse.clone();
        		f.setUser(null);
        		dataModel.setFilter(f);
        	}
        	return;
        }
        
        if (e.getSource() == maxPFDRComboBox) {
        	if (listeningOff) {
        		return;
        	}
        	update();
        	updatePFDRSpinnerData();
        	return;
        }
        
        assert false;
    }

    public VennFilteredDataModel getFilteredDataModel() {
    	if (modelToUse == null) {
    		throw new IllegalStateException(); // action "update" is necessary first
    	}
    	return modelToUse;
    }

    public GODistanceFilter getFilterToUse() {
    	return filterToUse;
    }
    
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void propertyChange(PropertyChangeEvent evt) 
    {
    	Object src = evt.getSource();
    	assert src instanceof JFormattedTextField;

    	if (listeningOff) {
    		return;
    	}
        update();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
    	assert e.getSource() instanceof SpinnerModel || e.getSource() instanceof JSpinner;
    	
    	if (listeningOff) {
    		return;
    	}

    	update();
    }

    public void setDataModel( IVennDataModel sourceDataModel, GODistanceFilter filter )
    {
//    	filterToUse = null;

    	if( dataModel != null )
        {
//            dataModel.removeChangeListener( this );
        	dataModel.setSucc(null);
            dataModel = null;
        }

    	if (sourceDataModel == null) {
    		throw new IllegalArgumentException();
    	}
    	if (sourceDataModel.getNumGroups() <= 0) {
    		throw new IllegalStateException();
    	}
    	if (sourceDataModel.getSucc() != null) {
    		throw new IllegalStateException();
    	}
    	
    	origDataModel = sourceDataModel;
    	modelToUse = null;
    	
    	final GODistanceFilter filter0 = (filter == null ? new GODistanceFilter(goTree) : filter); 

    	// set default filterBy
    	if (sourceDataModel != null) {
    		if (sourceDataModel.getNumGroups() > 0) {
    			filter0.getParameters().filterBy =
    				((AbstractGOCategoryProperties) sourceDataModel.getGroupProperties(0)).getFilterBy();
    		}
    	}
    	
        // make combo box for p/fdr selection
    	GODistanceFilter.Parameters filterParameters = filter0.getParameters();
        final AbstractGOCategoryProperties prop = (AbstractGOCategoryProperties) sourceDataModel.getGroupProperties(0);
        if (prop.getClass().equals(GOCategoryProperties1p.class)) {
            makeComboBox(true, false, false, false, false, false, false, false);
        } else if (prop.getClass().equals(GOCategoryProperties1p1fdr.class)) {
            makeComboBox(true, true, false, false, false, false, false, false);
        } else if (prop.getClass().equals(GOCategoryProperties3p.class)) {
            makeComboBox(false, false, true, true, true, false, false, false);
        } else if (prop.getClass().equals(GOCategoryProperties3p3fdr.class)) {
        	makeComboBox(false, false, true, true, true, true, true, true);
        } else {
        	throw new IllegalArgumentException();
        }

        dataModel = new VennFilteredDataModel(sourceDataModel, filter0);
//        dataModel.addChangeListener( this );
        dataModel.setSucc(this);
        dataModel.notifySucc();
        
    	// set maxPFDR spinner data
        updatePFDRSpinnerData();
    	
    	//set minTotal and maxTotal spinner data
    	updateMinMaxTotalSpinnerData();
    }

	/**
	 * 
	 */
	private void updateMinMaxTotalSpinnerData() {
		final GODistanceFilter filt = (GODistanceFilter)dataModel.getFilter();
		int[] arr = filt.whichNTotalsOccur(params.logNumElements);
		double[] darr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			darr[i] = arr[i];
		}
		arr = null;

		((DoubleArraySpinnerModel) minTotal.getModel()).setArray(darr);

		((DoubleArraySpinnerModel) maxTotal.getModel()).setArray(darr);
		darr = null;
	}

    private void updatePFDRSpinnerData() {
    	// remove duplicate values
    	Set<Double> set = new HashSet<Double>();
    	final int numGroups = origDataModel.getNumGroups();
    	DecimalFormat format = new DecimalFormat(floatFormatString);
		for (int i = 0; i < numGroups; i++) {
			double val = ((AbstractGOCategoryProperties) origDataModel.getGroupProperties(i)).getPFDRValue();
			// set values with the same precision used in the JSpinner
			// because e.g. if the JSpinner gets a value with getNextValue(), limits its precision, and
			// sets it again, the new set value may be lower than the value got by getNextValue().
			// then the next call to getNextValue() returns the same value again.  
    		try {
				set.add(format.parse((format.format(val))).doubleValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}
    	Object[] objArr = set.toArray();
    	set = null;

    	double[] doubleArr = new double[objArr.length];
    	for (int i = 0; i < objArr.length; i++) {
    		doubleArr[i] = (Double) objArr[i];
    	}
    	objArr = null;

    	DoubleArraySpinnerModel spinnerModel = ((DoubleArraySpinnerModel) maxPFDR.getModel());
    	spinnerModel.setArray(doubleArr);


    	// check if all values can be reached by getNextValue() and getPreviousValue()
    	ChangeListener[] changeListeners = spinnerModel.getChangeListeners();
    	for (int i = 0; i < changeListeners.length; i++) {
    		spinnerModel.removeChangeListener(changeListeners[i]);
    	}
    	double oldValue = (Double) spinnerModel.getValue();

    	{
    		spinnerModel.setValue(-9999.0);
    		double val = -9999.0;
    		double old;
    		int count = 0;
    		do {
    			old = val;
    			val = (Double) spinnerModel.getNextValue();
    			count++;
    		} while (old != val);
    		if (count != doubleArr.length + 1) {
    			System.err.println("updatePFDRSpinnerData: check failed: " + count + " " + doubleArr.length);
    		}
    	}

    	{
    		spinnerModel.setValue(Double.MAX_VALUE);
    		double val = Double.MAX_VALUE;
    		double old;
    		int count = 0;
    		do {
    			old = val;
    			val = (Double) spinnerModel.getPreviousValue();
    			count++;
    		} while (old != val);
    		if (count != doubleArr.length + 1) {
    			System.err.println("updatePFDRSpinnerData: check2 failed: " + count + " " + doubleArr.length);
    		}
    	}
		doubleArr = null;


    	spinnerModel.setValue(oldValue);
    	for (int i = 0; i < changeListeners.length; i++) {
    		spinnerModel.addChangeListener(changeListeners[i]);
    	}
    }
    
    /**
     * Update display with the computed values from GODistanceFilter and the filter parameters
     * it has used
     */
    private void updateControls()
    {
        numCategories.setText("--");
        numElements.setText("--");
        numFilteredCategories.setText("--");
        numFilteredElements.setText("--");        
               
        if( dataModel != null  )
        {
            if( dataModel.getFilter() != null )
            {
                setParameters( ((GODistanceFilter)dataModel.getFilter()).getParameters() );
            }
            
            numFilteredCategories.setValue( new Integer(dataModel.getNumGroups()) );
            if (params.logNumElements) {
            	numFilteredElements.setValue( new Integer(AbstractGOCategoryProperties.log(dataModel.getNumElements())) );
            } else {
            	numFilteredElements.setValue( new Integer(dataModel.getNumElements()) );
            }
            
            if( dataModel.getNumGroups() > params.maxCategories )
            {
                numFilteredCategories.setBackground( Color.ORANGE );
                numFilteredCategories.setToolTipText("Warning: a high number of categories may lead to increased running time");
            } else if (dataModel.getNumGroups() < 1) {
                numFilteredCategories.setBackground( Color.RED );
                numFilteredCategories.setToolTipText("It is not possible to display less than 1 category!");
            } else
            {
                numFilteredCategories.setBackground( getBackground() );
                numFilteredCategories.setToolTipText(null);
            }
            
            
            if( dataModel.getParentDataModel() != null )
            {
                numCategories.setValue( new Integer(dataModel.getParentDataModel().getNumGroups()));
                if (params.logNumElements) {
                	numElements.setValue( new Integer(AbstractGOCategoryProperties.log(dataModel.getParentDataModel().getNumElements())));                
                } else {
                	numElements.setValue( new Integer(dataModel.getParentDataModel().getNumElements()));                
                }
            }
        } 

    	resetButton.setEnabled(dataModel != null && dataModel.getNumGroups() > 0
				&& filterToUse != null
				&& ! filterToUse.getParameters().compare(getParameters())
				&& ((AbstractGOCategoryProperties) dataModel
						.getGroupProperties(0)).canFilterBy(filterToUse
						.getParameters().filterBy));
    }
    
//    public void stateChanged(ChangeEvent e) 
//    {
//        if( e.getSource() == dataModel )
//        {
//            updateControls();
//            return;
//        }
//    }
    
//    @Override
    public void predChanged() {
    	updateControls();
    }
 
    /**
     * for combo box (maps toString() to string()) 
     */
    private static class FilterByEnumWrapper {
    	private final FilterBy filterBy;
    	
    	public FilterByEnumWrapper(FilterBy filterBy) {
    		this.filterBy = filterBy;
    	}
    	
    	/* (non-Javadoc)
    	 * @see java.lang.Object#toString()
    	 */
    	@Override
    	public String toString() {
    		return filterBy.string();
    	}

		public FilterBy getFilterBy() {
			return filterBy;
		}
    	
    }
    
}
