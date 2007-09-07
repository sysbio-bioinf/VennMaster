/*
 * Created on 16.02.2006
 *
 */
package venn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import venn.db.GODistanceFilter;
import venn.db.VennFilteredDataModel;


/**
 * A plug-in panel for the VennMaster main application for online filtering
 * a previously loaded dataset.
 * This is view/controller 
 * 
 * @author muellera
 *
 */
public class FilterPanel extends JPanel
implements java.awt.event.ActionListener, java.awt.event.KeyListener, PropertyChangeListener, ChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private LinkedList<ActionListener> actionListeners;
    
    private JFormattedTextField maxPValue,
                                maxFDR,
                                maxTotal,
                                minTotal,
                                numCategories,
                                numElements,
                                numFilteredCategories,
                                numFilteredElements,
                                minDistance,
                                maxGroups;
    
    private LinkedList<JComponent>          fields;
    
    private VennFilteredDataModel   dataModel;


    private boolean checking;

    private int updating;

	private boolean pValueMode;

	private JLabel maxFDRlabel;
    
    
    
    public FilterPanel()
    {
        //super(new GridLayout(2,1));
        super( new BorderLayout() );
        
        JPanel panel = new JPanel();
        
        panel.setLayout(new GridLayout(11,2));
        
        updating = 0;
        checking = false;
        fields = new LinkedList<JComponent>();
        actionListeners = new LinkedList<ActionListener>();
        
        DecimalFormat floatFormat = new DecimalFormat("0.0000"),
                        intFormat = new DecimalFormat("0");        
        
        //setMinimumSize(new Dimension(getWidth(),200));
        
        panel.add(new JLabel("max p-Value"));
        maxPValue = new JFormattedTextField(floatFormat);
        fields.add(maxPValue);
        panel.add(maxPValue);
        
        panel.add(maxFDRlabel = new JLabel("max FDR"));
        maxFDR = new JFormattedTextField(floatFormat);
        fields.add(maxFDR);
        panel.add(maxFDR);

        panel.add(new JLabel("min total"));
        minTotal = new JFormattedTextField(intFormat);
        fields.add(minTotal);
        panel.add(minTotal);

        panel.add(new JLabel("max total"));
        maxTotal = new JFormattedTextField(intFormat);
        fields.add(maxTotal);
        panel.add(maxTotal);
        
        
        panel.add(new JLabel("min distance"));
        minDistance = new JFormattedTextField(intFormat);
        fields.add(minDistance);
        panel.add(minDistance);
        // minDistance.setVisible( false ); // TODO
        
        panel.add(new JLabel("max groups"));
        maxGroups = new JFormattedTextField(intFormat);
        fields.add(maxGroups);
        panel.add(maxGroups);        
        
    
        panel.add(new JLabel("#categories"));
        numCategories = new JFormattedTextField(intFormat);
        numCategories.setEditable(false);
        fields.add(numCategories);
        panel.add(numCategories);
        
        
        panel.add(new JLabel("#elements"));
        numElements = new JFormattedTextField(intFormat);
        numElements.setEditable(false);
        fields.add(numElements);
        panel.add(numElements);
        

        panel.add(new JLabel("#filtered categories"));
        numFilteredCategories = new JFormattedTextField(intFormat);
        numFilteredCategories.setEditable(false);
        fields.add(numFilteredCategories);
        panel.add(numFilteredCategories);
        
        
        panel.add(new JLabel("#filtered elements"));
        numFilteredElements = new JFormattedTextField(intFormat);
        numFilteredElements.setEditable(false);
        fields.add(numFilteredElements);
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

        
        button = new JButton("reset");
        button.setActionCommand("reset");
        button.addActionListener(this);
        panel.add(button);
        
        add( panel, BorderLayout.SOUTH );
        
        pValueMode = false;
        updateControls();

        //
        addListeners();
    }
   
    
    public void setPValueMode( boolean active )
    {
    	if( pValueMode != active )
    	{
    		pValueMode = active;
    		updateControls();
    	}
    }

    private void addListeners()
    {
        Iterator iter = fields.iterator();
        
        while(iter.hasNext())
        {
            JComponent comp = (JComponent)iter.next();
            comp.addKeyListener(this);
            comp.addPropertyChangeListener(this);   
        }
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
    
    public void check()
    {
        if( checking )
            return;
        
        checking = true;
        GODistanceFilter.Parameters params = getParameters();
        params.check();
        setParameters(params);
        checking = false;
    }
    
    
    
    public void setValues( int nCategories, int nElements, int nFilteredCategories, int nFilteredElements )
    {
        numCategories.setValue(new Integer(nCategories));
        numElements.setValue(new Integer(nElements));
        numFilteredCategories.setValue(new Integer(nFilteredCategories));
        numFilteredElements.setValue(new Integer(nFilteredElements));
    }
    
    public void setParameters(GODistanceFilter.Parameters param)
    {
        maxPValue.setValue(new Double(param.maxPValue));
        maxFDR.setValue(new Double(param.maxFDR));
        maxTotal.setValue(new Integer(param.maxTotal));
        minTotal.setValue(new Integer(param.minTotal));
        minDistance.setValue( new Integer(param.minDistance) );
        maxGroups.setValue( new Integer(param.maxGroups) );
    }    
    
    public GODistanceFilter.Parameters getParameters()
    {
        GODistanceFilter.Parameters param = new GODistanceFilter.Parameters();
        
        if( maxPValue.getValue() != null )
            param.maxPValue = ((Number)maxPValue.getValue()).doubleValue();
      
        if( maxFDR.getValue() != null )
            param.maxFDR = ((Number)maxFDR.getValue()).doubleValue();

        if( maxTotal.getValue() != null )
            param.maxTotal = ((Number)maxTotal.getValue()).intValue();
             
        if( minTotal.getValue() != null )
            param.minTotal = ((Number)minTotal.getValue()).intValue();
        
        if( minDistance.getValue() != null )
        	param.minDistance = ((Number)minDistance.getValue()).intValue();
        
        if( maxGroups.getValue() != null )
        	param.maxGroups = ((Number)maxGroups.getValue()).intValue();
        
        return param;
    }
    
    protected void commitFields()
    {
        // commit all text fields
        Iterator iter = fields.iterator();      
        while(iter.hasNext())
        {
            Object obj = iter.next();
            if(obj instanceof JFormattedTextField)
            {
                JFormattedTextField text = (JFormattedTextField)obj;
                if(text.isEditValid()) 
                {
                    try
                    {
                        text.commitEdit();
                    }
                    catch(ParseException e)
                    {
                    }
                }
            }
        }    
    }
    
    private void update()
    {
        if( updating > 0 )
            return;
        
        ++updating;
        
        commitFields();
        check();
        
        if( dataModel != null )
        {
            GODistanceFilter filter = (GODistanceFilter)dataModel.getFilter();
            if( filter != null )
            {
                filter.setParameters( getParameters() );
            }
        }
        
        --updating;
    }
    
    /**
     * React on buttons/ GUI controls
     */
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        
        if( cmd.equalsIgnoreCase("filter") )
        {
            update();
            // fireActionPerformed(new ActionEvent(this,0,"filter"));
            return;
        }
        
        if( cmd.equalsIgnoreCase("update") )
        {
            fireActionPerformed( new ActionEvent(this,0,"update") );
            return;
        }
        
        fireActionPerformed(new ActionEvent(this,0,cmd));
        
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
        update();
    }

    public void setDataModel( VennFilteredDataModel sourceDataModel )
    {
        if( dataModel != null )
        {
            dataModel.removeChangeListener( this );
            dataModel = null;
        }
        if( sourceDataModel == null )
            return;
        
        dataModel = sourceDataModel;
        dataModel.addChangeListener( this );
        
        updateControls();
    }

    /**
     * Update display with number of categories/elements ...
     *
     */
    private void updateControls()
    {
        if( updating > 1 )
            return;
        
        ++updating;
        
    		maxFDR.setVisible(!pValueMode);
    		maxFDRlabel.setVisible(!pValueMode);
        
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
            numFilteredElements.setValue( new Integer(dataModel.getNumElements()) );
            
            if( dataModel.getNumGroups() > venn.VennMaster.MAX_NUM_GROUPS )
            {
                numFilteredCategories.setBackground( Color.ORANGE );
                numFilteredCategories.setToolTipText("Warning: more than "+venn.VennMaster.MAX_NUM_GROUPS +
                            " are not recommended!");
            }
            else
            {
                numFilteredCategories.setBackground( getBackground() );
                numFilteredCategories.setToolTipText(null);
            }
            
            
            if( dataModel.getParentDataModel() != null )
            {
                numCategories.setValue( new Integer(dataModel.getParentDataModel().getNumGroups()));
                numElements.setValue( new Integer(dataModel.getParentDataModel().getNumElements()));                
            }
        } 

        --updating;
    }
    
    public void stateChanged(ChangeEvent e) 
    {
        if( e.getSource() == dataModel )
        {
            updateControls();
            return;
        }
    }
}
