package venn.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.*;
import java.util.*;

import javax.swing.*;

import venn.db.EasyGeneFilter;


/*
 * VennMaster//ParameterDialog.java
 * 
 * Created on 01.07.2004
 * 
 */

/**
 * @author muellera
 */
public class ImportDialog extends JDialog
implements java.awt.event.ActionListener, KeyListener, PropertyChangeListener
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    public static final int	INVALID = 0,
							OK_OPTION = 1,
							CANCEL_OPTION = 2;
	private int state;

	private JFormattedTextField minChange,
								maxChange,
								maxPValue;
	
	private LinkedList			fields;
	
	private boolean 			initialized;
	private boolean	checking;

	public ImportDialog(Frame owner)
	{
		super(owner,"Import GoMiner",true);
		initialized = false;
		state = INVALID;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridLayout(3,2));
		
		fields = new LinkedList();
		
		panel.add(new JLabel("mininum total"));
		minChange = new JFormattedTextField(new DecimalFormat("0"));
		minChange.addKeyListener(this);
		minChange.addPropertyChangeListener(this);
		fields.add(minChange);
		panel.add(minChange);
		
		panel.add(new JLabel("maximum total"));
		maxChange = new JFormattedTextField(new DecimalFormat("0"));
		maxChange.addKeyListener(this);
		maxChange.addPropertyChangeListener(this);
		fields.add(maxChange);
		panel.add(maxChange);
		
		
		panel.add(new JLabel("max p-Value"));
		maxPValue = new JFormattedTextField(new DecimalFormat("0.000"));
		maxPValue.addKeyListener(this);
		maxPValue.addPropertyChangeListener(this);
		fields.add(maxPValue);
		panel.add(maxPValue);

		getContentPane().add(panel,BorderLayout.CENTER);
		
		// add buttons
		panel = new JPanel();
		JButton button = new JButton("OK");
		button.addActionListener(this);
		button.setActionCommand("ok");
		panel.add(button);

		
		button = new JButton("Cancel");
		button.addActionListener(this);
		button.setActionCommand("cancel"); 		
		panel.add(button);
		
		getContentPane().add(panel,BorderLayout.SOUTH); 
		
		setSize(200,130);
		initialized = true;
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(200,130);
	}
	
	public int getState()
	{
		return state;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		if( cmd.equalsIgnoreCase("ok"))
		{
			processOkAction();
			return;
		}
		
		if( cmd.equalsIgnoreCase("cancel"))
		{
			processCancelAction();
			return;
		}
	}
	
	protected void processOkAction()
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
		check();
		state = OK_OPTION;
		dispose();		
	}
	
	protected void processCancelAction()
	{
		state = CANCEL_OPTION;
		dispose();		
	}
	
	public void check()
	{
		if( ! initialized && ! checking )
			return;
		
		checking = true;
		EasyGeneFilter.Parameters params = getParameters();
		params.check();
		setParameters(params);
		checking = false;
	}
	
	public void setParameters(EasyGeneFilter.Parameters parameters)
	{
		minChange.setValue(new Integer(parameters.minTotal));
		maxChange.setValue(new Integer(parameters.maxTotal));
		maxPValue.setValue(new Double(parameters.maxPValue));		
	}
	
	public EasyGeneFilter.Parameters getParameters()
	{
		EasyGeneFilter.Parameters param = new EasyGeneFilter.Parameters();
		
		if( minChange.getValue() != null )
			param.minTotal = ((Number)minChange.getValue()).intValue();
		if( maxChange.getValue() != null )
			param.maxTotal = ((Number)maxChange.getValue()).intValue();
		if( maxPValue.getValue() != null )
			param.maxPValue = ((Number)maxPValue.getValue()).doubleValue();
		
		return param;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		switch( e.getKeyCode() )
		{
			case KeyEvent.VK_ENTER:
				processOkAction();
				break;
				
			case KeyEvent.VK_ESCAPE:
				processCancelAction();
				break;
				
			default:
				// nothing
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e)
	{
		
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0)
	{
		check();
	}
}
