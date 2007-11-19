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

import venn.diagram.IntersectionTree.MemoryLowException;

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
    private volatile BoundedRangeModel	model;			// progress indicator
	private LinkedList 			listeners, 		// listeners for ActionEvents
								optimizers;		// optimizers to be set
	private volatile boolean constructEnded;
	private volatile boolean finishedComplete;
	private volatile boolean constructStarted;
	private volatile boolean finishedStarted;
	private volatile boolean workerAborted;
	private volatile boolean off;
	
	public OptimizerWorker()
	{
		listeners 	= new LinkedList();
		optimizers 	= new LinkedList();
		model 		= new DefaultBoundedRangeModel();
	}
	
    public boolean off() {
    	if (off) {
    		return true;
    	}
    	// abortWorker may be called after construct and the event dispatch thread is waiting
    	// so finished cannot set off = true
    	if (workerAborted && constructEnded && ! finishedStarted) {
    		return true;
    	}
    	return false;
    }
    
    // if called by event dispatch thread abortWorker() must be called first
    // (deadlock because if the event dispatch thread waits here it cannot call finished())
    public synchronized void waitForOff() {
    	while (! off()) {
    		try {
    			wait();
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();
			}
    	}
    }
    
    public boolean finishedComplete() {
    	return finishedComplete;
    }
    
    /**
     * abort worker, finished() won't be called
     */
    public void abortWorker() {
    	assert ! workerAborted;
    	
    	if (finishedComplete) {
    		return;
    	}
    	workerAborted = true;
    	interrupt();
    }
    
	public synchronized void addOptimizer( IOptimizer opt )
	{
        Assert.assertFalse( constructStarted ); // race condition
	    optimizers.add(opt);
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
		
	public synchronized Object construct() {
		assert ! constructStarted;
		assert ! constructEnded;
		assert ! finishedComplete;
		assert ! off;
		
		Thread.currentThread()
		.setPriority((Thread.currentThread().getPriority() + Thread.MIN_PRIORITY) / 2);

		final String strAborted = "aborted";

		if (workerAborted) {
			optimizers = null;
			off = true;
			notifyAll();
			return strAborted;
		}
		
		constructStarted = true;
		Object res = _construct();
		constructEnded = true;
		notifyAll();

		if (workerAborted) {
			optimizers = null;
			off = true;
			notifyAll();
			return strAborted;
		}

		return res;
	}
	
	/**
	 * Triggers the optimization.
	 * 
	 * Returns "aborted", "finished", "interrupted" or "error"
	 * 
	 * If "error"
	 * @see SwingWorker#construct()
	 */
	private synchronized Object _construct()
	{
		Assert.assertNotNull( model );
		assert ! off;
		
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
			if (Thread.interrupted()) {
				optFinished();
				return "interrupted";
			}

			// find a non-terminated optimizer
			while( ((IOptimizer)optimizers.get(iround)).endCondition() )
			{
				iround = (iround + 1) % optimizers.size();
			}
			try {
				((IOptimizer)optimizers.get(iround)).optimize();
			} catch (MemoryLowException e) {
				e.printStackTrace();
				optFinished();
				return "error";
			}
			
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
		
		optFinished();
		return "finished";
	}
	
	private synchronized void optFinished() {
		for( int i=0; i<optimizers.size(); ++i )
		{
		    ((IOptimizer)optimizers.get(i)).finished(); 
		}
		
		// reset progress indicator to the end
	    model.setValue( model.getMaximum() );	
	}
	
	public synchronized void finished() {
		assert ! finishedComplete;
		assert constructEnded || ! constructStarted;
		
		if (workerAborted) {
			optimizers = null;
			off = true;
			notifyAll();
			return;
		}

		finishedStarted = true;
		_finished();
		finishedComplete = true;

		optimizers = null;

		off = true;
		notifyAll();
	}
	
	private synchronized void _finished()
	{		       
		assert ! off;
		String state = "";
		if( get() != null )
			state = (String)get();
        
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

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		assert listeners.size() == 0;
	}
}
