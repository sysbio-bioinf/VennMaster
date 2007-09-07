/*
 * Created on 25.05.2005
 *
 */
package venn.optim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import junit.framework.Assert;


/**
 * Implements a SwingWorker class for background calculations.
 * Without interruption it will optimize all given IOptimizers.
 * Add new optimizers with <code>addOptimizer( IOptimizer )</code>.
 * The whole thing is started with <code>worker.start()</code>.
 * 
 * @author muellera
 */
public class OptimizerWorker extends SwingWorker
{
    private BoundedRangeModel	model;			// progress indicator
	private LinkedList 			listeners, 		// listeners for ActionEvents
								optimizers;		// optimizers to be set
	private Exception 			lastError;
	
	public OptimizerWorker()
	{
		listeners 	= new LinkedList();
		optimizers 	= new LinkedList();
		lastError 	= null;
		model 		= new DefaultBoundedRangeModel();
	}
	
    public boolean isRunning()
    {
        if( getThread() == null )
            return false;
        return( getThread().isAlive() );
    }
    
	public synchronized void addOptimizer( IOptimizer opt )
	{
        Assert.assertFalse( isRunning() );
	    optimizers.add(opt);
	}
	
	public synchronized void clearOptimizers()
	{
        Assert.assertFalse( isRunning() );
	    optimizers.clear();
	}
	
	/**
	 * Subscribe a progress bar with e.g.
	 * getModel().addChangeListener( progressBar )
	 * 
	 * @return A BoundedRangeModel which can be used e.g. with a progress bar.
	 */
	public BoundedRangeModel getModel()
	{
	    return model;
	}
		
	/**
	 * 
	 * @return The last exception (if an "error" message was sent)
	 */
	public Exception getLastError()
	{
		Exception error = lastError;
		lastError = null;
		return error;
	}
	
	/**
	 * Adds a listener for the following events:
	 * <ol>
	 * <li>"aborted"
	 * <li>"interrupted"
	 * <li>"error"
	 * <li>"finished"
	 * </ol>
	 * @param listener
	 */
	public synchronized void addActionListener(ActionListener listener)
	{
        if( listener != null )
            listeners.add( listener );
	}
    
    public synchronized void removeActionListener(ActionListener listener)
    {
        if( listener != null )
            listeners.remove( listener );
    }

	
	/**
	 * 
	 * @return Returns <code>true</code> if all optimizers met their end conditions.
	 */
	private boolean allFinished()
	{
	    Iterator iter = optimizers.iterator();
	    while( iter.hasNext() )
	    {
	        IOptimizer opt = (IOptimizer)iter.next();
	        if( ! opt.endCondition() )
	        {
	            return false;
	        }
	    }
	    return true;
	}
	
	/**
	 * Triggers the optimization.
	 * 
	 * Returns "aborted", "finished", "interrupted" or "error"
	 * 
	 * If "error"
	 * @see SwingWorker#construct()
	 */
	public Object construct()
	{
		Assert.assertNotNull( model );
		
		lastError = null;
		
		// compute total progress range and setup optimizers
		int nrange = 0;
		for( int i=0; i<optimizers.size(); ++i )
		{
		    IOptimizer opt = (IOptimizer)optimizers.get(i); 
		    nrange += opt.getMaxProgress();
		}
		    
		model.setRangeProperties(0,1,0,nrange,false);
		
		// optimize each generation (independently solvable subset) separately
		int iround = 0;
		while( ! allFinished() )
		{	
            if( Thread.interrupted() )
                return "interrupted";
            
            // find a non-terminated optimizer
		    while( ((IOptimizer)optimizers.get(iround)).endCondition() )
		    {
		        iround = (iround + 1) % optimizers.size();
		    }
		    ((IOptimizer)optimizers.get(iround)).optimize();
		    
            // update progress
            if( model != null )
            {
    		    int progress = 0;
    		    for( int i=0; i<optimizers.size(); ++i )
    		    {
    		        progress += ((IOptimizer)optimizers.get(i)).getProgress();
    		    }
    		    model.setValue(progress);
            }
            
		    // choose next optimizer
		    iround = (iround + 1) % optimizers.size();
		}
		return "finished";
	}
	
	public void finished()
	{		       
		String state = "";
		if( get() != null )
			state = (String)get();
        
        //System.out.println("OptimizerWorker "+state);
		
		for( int i=0; i<optimizers.size(); ++i )
		{
		    ((IOptimizer)optimizers.get(i)).finished(); 
		}
		
		// reset progress indicator to the end
	    model.setValue( model.getMaximum() );	
        
        // notify listeners
		fireActionEvent(state);
	}
	
	/**
	 * @param state
	 */
	private synchronized void fireActionEvent(String state)
	{
		ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,state);
		
        synchronized( listeners )
        {
    		Iterator iter = listeners.iterator();
    		while( iter.hasNext() )
    		{
    			((ActionListener)iter.next()).actionPerformed(event);
    		}
        }
	}
}
