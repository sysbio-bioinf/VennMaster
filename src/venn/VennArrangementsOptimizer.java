/**
 * 
 */
package venn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import junit.framework.Assert;
import venn.diagram.VennArrangement;
import venn.diagram.VennErrorFunction;
import venn.event.IsSimulatingListener;
import venn.event.ResultAvailableListener;
import venn.optim.EvolutionaryOptimizer;
import venn.optim.EvolutionaryOptimizerV1;
import venn.optim.IOptimizer;
import venn.optim.IOptimizerObserver;
import venn.optim.OptimizerWorker;
import venn.optim.ParallelSwarmOptimizer;
import venn.optim.SwarmOptimizer;

public class VennArrangementsOptimizer implements IOptimizerObserver, ActionListener {
    private static final Random  random = new Random();
    private VennArrangement[] vennArrangement;
    private VennErrorFunction[] errFunc;
    private IOptimizer[]    optim;
    private OptimizerWorker worker;
	private AllParameters   params;
	private LinkedList observers;
	private volatile boolean restart = false;
	private final List<IsSimulatingListener> isSimulatingListeners;
	private final List<ResultAvailableListener> resultAvailableListeners;
	private JProgressBar progressBar;
	private long seed;
	
    public VennArrangementsOptimizer() {
        observers = new LinkedList();
        isSimulatingListeners = new LinkedList<IsSimulatingListener>();
        resultAvailableListeners = new LinkedList<ResultAvailableListener>();
    }
    
    public void setArrangements(VennArrangement[] VennArrangements) {
    	this.vennArrangement = VennArrangements;
    }
    
    public synchronized void addIsSimulatingListener(IsSimulatingListener listener) {
    	if (isSimulatingListeners.contains(listener)) {
    		throw new IllegalStateException();
    	}
    	isSimulatingListeners.add(listener);
    }
    
    public synchronized void removeIsSimulatingListener(IsSimulatingListener listener) {
    	if (! isSimulatingListeners.contains(listener)) {
    		throw new IllegalArgumentException();
    	}
    	isSimulatingListeners.remove(listener);
    }
    
    private synchronized void fireIsSimulating(boolean isSimulating) {
    	for (IsSimulatingListener listener : isSimulatingListeners) {
    		listener.isSimulating(isSimulating);
    	}
    }

    public synchronized void addResultAvailableListener(ResultAvailableListener listener) {
    	if (resultAvailableListeners.contains(listener)) {
    		throw new IllegalStateException();
    	}
    	resultAvailableListeners.add(listener);
    }
    
    public synchronized void removeResultAvailableListener(ResultAvailableListener listener) {
    	if (! resultAvailableListeners.contains(listener)) {
    		throw new IllegalStateException();
    	}
    	resultAvailableListeners.remove(listener);
    }
    
    private synchronized void notifyResultAvailable(boolean isFinalResult) {
    	assert ! workerOff();
    	
    	if (restart) {
    		return;
    	}
    	for (ResultAvailableListener listener : resultAvailableListeners) {
    		listener.resultAvailable(isFinalResult);
    	}
    }
    
    /**
     * sets optional JProgressBar
     * @param progressBar
     */
    public void setProgressBar(JProgressBar progressBar) {
    	this.progressBar = progressBar;
    }
    
    public IOptimizer[] getOptim() {
    	return optim;
    }
    
    public VennErrorFunction[] getErrFunc() {
    	updateErrFuncToOptimum(); // necessary?
    	return errFunc;
    }
    
    public OptimizerWorker getWorker() {
    	return worker;
    }
    
    public void setParameters(AllParameters params) {
    	this.params = params;
    }
    
    public String getInfo() {
    	return String.valueOf("seed: " + seed + "\n");
    }
    
    /**
	 * Starts the optimization process.
	 *
	 */
	public void optimize()
	{
		Assert.assertTrue( workerOff() );
        Assert.assertNotNull( random );
                        
        fireIsSimulating(true);
        
		// set random seed
		if( params.randomSeed>0 )
		{
			seed = params.randomSeed;
		}
		else
		{
			seed = System.currentTimeMillis();
		}
		random.setSeed(seed);
		
		Assert.assertNotNull(vennArrangement);
		
		errFunc = new VennErrorFunction[vennArrangement.length];
		optim = new IOptimizer[vennArrangement.length];
		
		for( int i=0; i<vennArrangement.length; ++i )
		{
				// the following might be replaced with a factory method
		    errFunc[i] = new VennErrorFunction( new VennArrangement(vennArrangement[i]), params.errorFunction );
		    switch( params.optimizer )
		    {
		        case EvolutionaryOptimizerV1.Parameters.ID:
		            optim[i] = new EvolutionaryOptimizerV1(random,errFunc[i], params.optEvo );                    
		            break;
		            
		        case EvolutionaryOptimizer.Parameters.ID:
		            optim[i] = new EvolutionaryOptimizer(random,errFunc[i], params.optEvo2 );                    
		            break;

		        case SwarmOptimizer.Parameters.ID:
		            optim[i] = new SwarmOptimizer(random,errFunc[i], params.optSwarm );
		            break;

		        case ParallelSwarmOptimizer.Parameters.ID:
		            optim[i] = new ParallelSwarmOptimizer(random,errFunc[i], params.optPSwarm);
		            break;
		            
		        default:
		            Assert.fail("illegal optimizer");
		    }
		    
		    optim[i].setID( i );
		    optim[i].addObserver( this ); // IOptimizerObserver (notifyOptimization, finished, close)
		    
		    Iterator iter = observers.iterator();
		    while( iter.hasNext() )
		    {
		    		optim[i].addObserver( (IOptimizerObserver)iter.next() );
		    }
		    
		}
        
        newWorker().start();
	}

	public void stopAndRestartOptimization() {
		stopForRestart(); // stop if running
		restart();
	}
	
    private OptimizerWorker newWorker()
    {
        Assert.assertTrue( workerOff() );
        
        removeWorker();
        assert worker == null;
       
        // create new worker
        worker = new OptimizerWorker();
        worker.addActionListener( this );
        
        // attach optimizers
        Assert.assertNotNull( optim );
        for( int i=0; i<optim.length; ++i )
        {
            worker.addOptimizer( optim[i] );
        }
        
        if (progressBar != null) {
            progressBar.setModel(worker.getModel());
        }
        
        return worker;
    }

	/**
	 * remove old worker
	 */
	private void removeWorker() {
		if( worker != null )
        {
            worker.removeActionListener( this );
            if (progressBar != null) {
            	progressBar.setModel(null);
            }
            worker = null;
        }
	}
	
    private void resume_()
    {
        if( errFunc == null || optim == null )
            return;
                
        fireIsSimulating(true);
        for( int i=0; i<optim.length; ++i )
        {
            optim[i].reset();
        }
        
        newWorker().start();
    }

    
    /**
     * 
     * @return True if the optimizer is working
     */
    private boolean workerOff()
    {
        if( worker==null ) {
            return true;
        }
        
        return worker.off();
    }
    
    public boolean workerFinishedComplete() {
    	if (worker == null) {
    		return true;
    	}
    	
    	return worker.finishedComplete();
    }
    
    public synchronized void clearObservers()
    {
    		Iterator iter = observers.iterator();
    		while( iter.hasNext() )
    		{
    			IOptimizerObserver obs = (IOptimizerObserver)iter.next();
    			obs.close();
    		}
    		observers.clear();
    }
    
    public synchronized void addObserver( IOptimizerObserver observer ) 
    {
    		if( observer != null )
    		{
    			if( ! observers.contains(observer) )
    				observers.add(observer);
    		}
    	/*
        if( reporterWriter != null )
        {
            try {
                reporterWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            reporterWriter = null;
        }
        this.reporterWriter = reporter;
        */
    }

    
    
    /**
     * Every optimizer calls this function. Updates the view from time to time.
     */
    // IOptimizerObserver
    public synchronized void notifyOptimization( IOptimizer source ) 
    {        
    	assert ! SwingUtilities.isEventDispatchThread();
    	assert ! worker.off();
    	
        if( params.updateInterval <= 0 )
            return;
        
        if( source.getProgress() % params.updateInterval == 0 )
        {
            int id = source.getID();
            errFunc[id].setInput( source.getOptimum() );
            notifyResultAvailable(false);
        }

    }

    // callback if an optimizer has ended.
    // IOptimizerObserver
    public void finished(IOptimizer source) 
    {

    }

    // IOptimizerObserver
    public void close() {
    	// TODO Auto-generated method stub
    	
    }

    public void actionPerformed(ActionEvent e) {
    	assert SwingUtilities.isEventDispatchThread();
    	
	    Object src = e.getSource();
		String cmd = e.getActionCommand();

		assert src == worker;

		// the simulation has stopped! (interrupted, finished, or error, not called on abort)
		assert ! restart;
		assert ! worker.off();

		updateErrFuncToOptimum();

		fireIsSimulating(false);
		notifyResultAvailable(true);
    }

	/**
	 * 
	 */
	private void updateErrFuncToOptimum() {
		if (optim == null || optim.length == 0) {
			return;
		}
		assert optim.length == errFunc.length;
		
		for( int i=0; i<errFunc.length; ++i )
		{
			double[] opt = optim[i].getOptimum();
			if( opt != null )
			{
				errFunc[i].setInput( opt );
			}
		}
	}

    public void resume()
    {
        if( ! workerFinishedComplete() )
            return;
        
        resume_();
    }

	/**
	 * Stop the simulation process (if it is running)
	 * worker.finished() will be called
	 */
	public void stop()
	{
		if( worker != null )
		{
			worker.interrupt();
		}
	}

	/**
	 * stop worker (if running), worker results calculated so far are discarded
	 */
	// not synchronized, deadlock with notifyOptimization
    public void stopForRestart() {
    	assert ! restart;
        restart = true;
        if (worker != null) {
        	worker.abortWorker();
        	worker.waitForOff();
        }
        assert workerOff();
    }
    
    public void restart() {
    	assert workerOff();
    	if (! restart) {
    		throw new IllegalStateException();
    	}
    	
    	restart = false;
    	optimize();
    }
    

	public synchronized VennArrangement[] getOptArrangements() {
		updateErrFuncToOptimum(); // necessary?
		VennArrangement[] res = new VennArrangement[errFunc.length];
		for (int i = 0; i < errFunc.length; i++) {
			res[i] = errFunc[i].getArrangement();
		}
		return res;
	}

}
