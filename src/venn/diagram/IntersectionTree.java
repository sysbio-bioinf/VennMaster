/*
 * VennMaster/geometry/IntersectionTree.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

import java.util.BitSet;
import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;
import venn.geometry.FPoint;

/**
 * 
 * Encapsulates an geometrical arrangement.
 * 
 * @author muellera
 */
public class IntersectionTree
implements ChangeListener
{
	public static final long MEMORY_LOWER_BOUND = 64*1024L;  
	
	private IntersectionTreeNode root;
 
	private boolean valid;				// tree is in a valid state
	private int maxIntersections;
	private boolean memoryCheck;

    private VennArrangement arrangement;
	
	public static class MemoryLowException extends RuntimeException 
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public MemoryLowException()
		{
			
		}
	}
	
	/**
	 * Throws a MemoryLowException if the runtime memory is too low.
	 *
	 */
	void checkMemory()
	{
		if( ! memoryCheck )
			return;
		
		Runtime rt = Runtime.getRuntime();
		if( rt.freeMemory() < MEMORY_LOWER_BOUND )
		{
			throw new MemoryLowException(); 
		}
	}
	
	/**
	 * Enable memory checks.
	 * @param check
	 */
	
	public void enableMemoryChecks(boolean check)
	{
		memoryCheck = check;
	}
	
	/**
	 * Creates a new intersection tree with the given polygon set and the
	 * bit sets sets. The tree will only be built up to maxIntersections.
	 * E.g. maxIntersections=2 intersects only the two sets at most.
     * 
	 * @param maxLevel maximum number of intersections (>=2)
     * 
	 */
	public IntersectionTree( int maxLevel )
	{        
        if( maxLevel < 2 )
            throw new IllegalArgumentException("maxLevel must be >= 2!");
				
		this.root = null;
		this.valid = false;
		this.maxIntersections = maxLevel;
		this.memoryCheck = true;
        this.arrangement = null;
	}
    	
	/**
	 * 
	 * @param idx
	 * @return A pointer to the source node (idx times left and 1 times right)
	 */
	public IntersectionTreeNode getSourceNode(int idx)
	{
		if( idx < 0 || idx >= getNumOfSets() )
			return null;
		
		if( root == null )
			throw new IllegalStateException("tree must be initialized for this operation");
		
		IntersectionTreeNode node = root;
		for(int i=0; i<idx; ++i)
		{
			Assert.assertNotNull( node );
			node = node.leftChild;
		}
		Assert.assertNotNull( node );
		return node.rightChild;
	}

	
	/**
	 * Updates the tree.
	 * @param level
	 * @param node
	 */
	private void internalBuildTree(int level, IntersectionTreeNode node)
	{
		if( node == null || level >= getNumOfSets() )
			return;
		checkMemory();
        
        Assert.assertNotNull( node.vennObject );

        IVennObject[] vennObjects = arrangement.getVennObjects();
		
		IVennObject p = null;
        if( node.nRight == 0 )
        {   // "intersect" the whole plane with the current polygon ... 
            // so its identical to the parent polygon
            p = vennObjects[level];
        } else
        {
			// intersect vennObjects
			p = node.vennObject.intersect( vennObjects[level] );            
		}
			
		// append right child if there is a nonempty polygonal intersection
		// or if the cardinality is > 0
		if( node.rightChild == null )
		{
			if( node.nRight < maxIntersections )
			{
                /*
				BitSet s = (BitSet)node.set.clone();
				s.and(sets[level]);
				int card = s.cardinality();
                */
                
                IVennObject is = vennObjects[level].intersect( node.vennObject );
                int card = is.cardinality();
	
				if( (p!=null) || (card>0) )
				{
					node.rightChild  = new IntersectionTreeNode();
					node.rightChild.setIndex = -1;
					node.rightChild.copy = false;
					node.rightChild.parent = node;
					node.rightChild.vennObject = is;
					node.rightChild.card = card;
					node.rightChild.nLeft = node.nLeft;
					node.rightChild.nRight = node.nRight+1;
					node.rightChild.path = (BitSet)node.path.clone();
					node.rightChild.path.set(level);
				}
			}
		}
		else
		{	
			if( (node.nRight>=maxIntersections) || (node.rightChild.card == 0 && p.isEmpty()) )
			{ // cutoff 
				node.rightChild = null;
			}
		}

		if( node.rightChild != null )
		{ // assign polygonal intersection and descend
			node.rightChild.copy = false;
			node.rightChild.setIndex = -1;
			node.rightChild.vennObject = p;
			if( p != null )
			{
				node.rightChild.area = p.area();
				if( p.equals(vennObjects[level]) )
				{ // this node is a new level or is identically to the level
					// so its only an alias if nRight > 1
					node.rightChild.setIndex = level;
				}
				else
				{
					if( p.equals(node.vennObject) )
					{ // this node is fully contained in its parent node					
						node.rightChild.setIndex = node.setIndex;
					}
				}
			}
			else
			{
				node.rightChild.area = 0.0;
			}
			internalBuildTree(level+1,node.rightChild);
		}
		
		// left child: WITHOUT polygon[level]
		if( level+1 >= getNumOfSets() )
		{
			node.leftChild = null;
		}
		else
		{
			if( node.leftChild == null )
				node.leftChild  = new IntersectionTreeNode();
				
			node.leftChild.vennObject = node.vennObject; 	// link to parent polygon
			node.leftChild.area = node.area;
			node.leftChild.card = node.card;
			node.leftChild.copy = true; // mark this polygon as copy
			node.leftChild.parent = node;
			node.leftChild.nLeft = node.nLeft+1;
			node.leftChild.nRight = node.nRight;
			node.leftChild.path = node.path;
			node.leftChild.setIndex = node.setIndex;	
			
			internalBuildTree(level+1,node.leftChild);
		}
	}
		
	/**
	 * Rebuilds/updates the whole tree.
	 *
	 */
	public void buildTree()
	{
		root = null;  // TODO: remove this??
        if( arrangement == null || arrangement.getNumOfSets() == 0 )
            return;
        
        IVennObject[] vennObjects = arrangement.getVennObjects();
        
		if( root == null )
		{
			root = new IntersectionTreeNode();
			//root.contains = new BitSet(vennObjects.length);
			root.path = new BitSet(vennObjects.length);
			root.parent = null;
			root.nLeft = 0;
			root.nRight = 0;
			root.copy = false;
            
            BitSet set = new BitSet();
            for( int i=0; i<vennObjects.length; ++i )
            {
                set.or( vennObjects[i].getElements() );
            }
            root.vennObject = new VennPolygonObject(null,set,0.0); 
			root.card = root.vennObject.cardinality();
		}
		else
		{
			root.invalidate();
		}
		internalBuildTree(0,root);
	}
	
	/**
	 * Recursively applies for each node the given visitor.
	 * 
	 * @param visitor
	 * @see IIntersectionTreeVisitor
	 */
	public void accept(IIntersectionTreeVisitor visitor)
	{
		if( getRoot() != null )
		{
            getRoot().accept(0,visitor);
		}
	}
	
	/**
	 * 
	 * @return The cardinality of the whole tree (how many elements are there in the sets?).
	 */
	public int getCardinality()
	{	
		if( getRoot() != null )
			return getRoot().card;
        
		return 0;
	}

	/**
	 * @return number of setes
	 */
	public int getNumOfSets()
	{
        if( arrangement != null )
            return arrangement.getNumOfSets();
        
        return 0;
	}
	
    /**
     * A path is a BitSet with the following properties:
     * The i-th bit corresponds to the i-th group. E.g. to get the intersection node
     * for the sets 0,1 and 3 use the BitSet with the bits {0,1,3} set.
     * @param path
     * @return The intersection tree node
     */
	public IntersectionTreeNode getByPath(BitSet path)
	{
        validate();
		if( path == null )
			throw new IllegalArgumentException("path must not be null");
		
		IntersectionTreeNode node = root;
		
		int len = path.length();
		for( int i=0; (i<len) && (node!=null); ++i )
		{
			if( path.get(i) )
			{
				node = node.rightChild;
			}
			else
			{
				node = node.leftChild;
			}
		}
		return node;
	}

	/**
	 * @return the root node
	 */
	public IntersectionTreeNode getRoot()
	{
        validate();
		return root;
	}

    /**
     * 
     * @param q
     * 
     * @return The node
     */
    protected IntersectionTreeNode findDeepestNode( FPoint q )
    {   
        validate();
        TreeQuery query = new TreeQuery(this);
        return query.findPolygonNode(q);
    }

    /**
     * 
     * @param q
     * @return A list of nodes.
     */
    protected LinkedList findAllNodes( FPoint q )
    {
        validate();
        TreeQuery query = new TreeQuery(this);
        return query.findAllNodes(q);
    }

    public void stateChanged(ChangeEvent e) 
    {
       // System.out.println("IntersectionTree.stateChanged()");
        if( e.getSource() == arrangement )
        {
            invalidate();
        }
    }    
    
    public void invalidate()
    {
        valid = false;
    }
    
    public void validate()
    {
        if( ! valid )
        {
            buildTree();
            valid = true;
        }
    }

    public void setArrangement(VennArrangement arrangement) 
    {
        if( this.arrangement != null )
            this.arrangement.removeChangeListener(this);
        
        this.arrangement = arrangement;
        if( arrangement != null )
            arrangement.addChangeListener(this);
        
        invalidate();
    }
    
    public VennArrangement getArrangement()
    {
        return arrangement;
    }
}
