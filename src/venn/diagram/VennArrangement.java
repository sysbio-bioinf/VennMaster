/*
 * Created on 07.03.2006
 *
 */
package venn.diagram;

import venn.AllParameters;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import venn.db.IVennDataModel;
import venn.db.VennFilteredDataModel;
import venn.event.IFilterChainSucc;

/**
 * Encapsulates an Venn arrangement.
 * 
 * @author muellera
 *
 */
public class VennArrangement
implements Cloneable, Serializable, IFilterChainSucc
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private IVennObject[]       vennObjects;
    private boolean             valid;
    
    
    private transient LinkedList          listeners; 
    private transient IVennDataModel      model;
    private transient IVennObjectFactory  vennObjectFactory;

    private AllParameters params;
    
    /**
     * Copy constructor
     * 
     * @param source
     */
    public VennArrangement( VennArrangement source )
    {
        if( source == null )
            throw new IllegalArgumentException("source must not be null");
        
        listeners = new LinkedList();
        
        model = source.getDataModel();
        
        vennObjectFactory = null;
        
        IVennObject[] t = source.getVennObjects();
       
        vennObjects = new IVennObject[t.length];
                
        for( int i=0; i<t.length; ++i )
        {
            vennObjects[i] = t[i].duplicate();
        }
        valid = true;
        //observe();
        params = source.params;
    }
    
    
    public VennArrangement( IVennDataModel model, IVennObjectFactory vennObjectFactory, AllParameters ap )
    {
        listeners = new LinkedList();
        
        this.model = null;
        this.vennObjects = null;
        this.vennObjectFactory = vennObjectFactory;
        this.valid = false;
        
        this.params=ap;
        
        setDataModel( model );
    }
    
    /*
    private void observe()
    {
        if( vennObjects == null )
            return;        
        for( int i=0; i<vennObjects.length; ++i )
        {
            if( vennObjects[i] != null )
                vennObjects[i].addChangeListener(this);
        }
    }
    
    private void unobserve()
    {
        if( vennObjects == null )
            return;
        for( int i=0; i<vennObjects.length; ++i )
        {
            if( vennObjects[i] != null )
                vennObjects[i].removeChangeListener(this);
        }
    }
    */
    
    private void validate()
    {
        if( valid )
            return;
        
        //unobserve();
        
        if( model == null )
        {
            vennObjects = null;
            valid = true;
            return;
        }

        if( vennObjectFactory == null )
            throw new IllegalStateException("this is not possible for copied arrangements");
        
        // creates a new Venn object for every group
        vennObjects = new IVennObject[model.getNumGroups()];
        for( int gid=0; gid<model.getNumGroups(); ++gid )
        {
            int Lgid = gid;
            if( model instanceof VennFilteredDataModel )
            {
                Lgid = ((VennFilteredDataModel)model).localToGlobalGroupID(gid);
            }
            vennObjects[gid] = vennObjectFactory.create( Lgid, model.getGroupElements(gid), model.getNumGroups(), params );
        }
        
        //observe();
        
        valid = true;
    }
    
    /**
     * Source model changed
     *
     */
    public void invalidate()
    {
        if( valid )
        {
            valid = false;
            fireChangeEvent();
        }
    }
    
    /**
     * 
     * @return A set of IVennObjects
     */
    public IVennObject[] getVennObjects() 
    {
        validate();
        return vennObjects;
    }
    
    /**
     * 
     * @param model
     */
    public void setDataModel(IVennDataModel model) 
    {
        if( this.model != null ) {
        	this.model.setSucc(null);
        }
        this.model = model;
        
        if( model != null ) {
        	model.setSucc(this); // => predChanged
        }
        
        invalidate();
    }

    /* (non-Javadoc)
     * @see venn.IVennDiagramView#getDataModel()
     */
    public IVennDataModel getDataModel() 
    {
        return model;
    }


    
    public synchronized void addChangeListener( ChangeListener obj )
    {
        if( obj != null )
            listeners.add(obj);
    }
    
    public synchronized void removeChangeListener( ChangeListener obj )
    {
        if( obj != null )
            listeners.remove(obj);
    }
    
    public void fireChangeEvent()
    {
        synchronized( listeners )
        {
            ChangeEvent ev = new ChangeEvent(this);
            Iterator iter = listeners.iterator();
            while(iter.hasNext())
            {
                ((ChangeListener)iter.next()).stateChanged(ev);
            }
        }
    }

//    @Override
    public void predChanged() {
    	invalidate();
    }
    
    public int getNumOfSets() 
    {
        if( model != null )
            return model.getNumGroups();
        
        return 0;
    }
    
    /**
     * Sets all positions and scales of this arrangement to those
     * of the <tt>source</tt> arrangement.
     * <tt>source</tt> has to be compatible with this arrangement 
     * (that is: it must have the same data source).
     * Layout information (colors etc.) will be untouched.
     * 
     * @param source
     */
    public void assignState( VennArrangement source )
    {
        if( source == null )
            throw new IllegalArgumentException("source must be not null");
        
        //if( getDataModel() != source.getDataModel() )
        //    throw new IllegalArgumentException("incompatible source!");
        
        if( getNumOfSets() != source.getNumOfSets() )
            throw new IllegalArgumentException("incompatible source!");
        
        for( int i=0; i<getNumOfSets(); ++i )
        {
            getVennObjects()[i].assignState( source.getVennObjects()[i] );
        }
        fireChangeEvent();
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        
        
        for( int i=0; i<getNumOfSets(); ++i )
        {
            buf.append(i+":"+getVennObjects()[i].toString()+"\n");
        }
        return buf.toString();
    }
    
	public void setParameters(AllParameters params)
	{		
		this.params = params;
	}
	
	public AllParameters getParameters()
	{
		return params;
	}
}
