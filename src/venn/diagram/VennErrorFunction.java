 /*
 * VennMaster/geometry/ErrorFunction.java
 * 
 * Created on 29.06.2004
 * 
 */
package venn.diagram;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.BitSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;
import venn.Constants;
import venn.db.AbstractGOCategoryProperties;
import venn.geometry.FPoint;
import venn.geometry.FRectangle;
import venn.optim.IFunction;
import venn.utility.MathUtility;
import venn.utility.SystemUtility;

/**
 * Special error functional assessing the goodness of a VennArrangement.
 * 
 * @author muellera
 */
public class VennErrorFunction
implements IFunction, ChangeListener
{
	private Parameters             params;
	private IntersectionTree       tree;			// the intersection tree
	private ErrorFunctionVisitor   visitor;	// visitor for calculating the error
	
    private boolean                 valid;
    private double[]                lowerBounds, 
                                    upperBounds;
    private double                  cacheErrorFunction,     // partial cost cache
                                    cachePressureCost,
                                    cost;

    
    public VennErrorFunction( IntersectionTree tree, Parameters params )
    {
        this.params = params;
        this.tree = tree;
        
        initializeTransient();
    }
    
	/**
	 * Area should be the cardinality
	 * 
	 * @param arrangement
	 * @param params
	 */	
	public VennErrorFunction( VennArrangement arrangement, Parameters params )
	{
		this.params = params;
		tree = new IntersectionTree( params.maxIntersections, params.logCardinalities );
		tree.setArrangement( arrangement );
        
        initializeTransient();
	}
    
    
    @Override
	public IFunction copy() {
    	
    	return new VennErrorFunction(new VennArrangement(this.getArrangement()),params);
	}

	private void initializeTransient()
    {
        valid = false;  
        visitor = new ErrorFunctionVisitor();
        
        lowerBounds = new double[getNumInput()];
        upperBounds = new double[getNumInput()];
        
        // lower/upper bounds for x,y coordinates
        int idx = 0;
        for( int i=0; i<getNumOfSets(); ++i )
        {
            FRectangle bbox = tree.getArrangement().getVennObjects()[i].getBoundingBox();
            Assert.assertNotNull( bbox );
            
            // x boundaries
            lowerBounds[idx] = bbox.getWidth()/2;
            upperBounds[idx] = 1.0 - bbox.getWidth()/2;
            ++idx;
            
            // y boundaries
            lowerBounds[idx] = bbox.getHeight()/2;
            upperBounds[idx] = 1.0 - bbox.getHeight()/2;
            ++idx;
        }

        // lower/upper bounds for the scale parameters
        for( int i=0; i<getNumOfSets(); ++i )
        {
            lowerBounds[idx] = params.minScale;
            upperBounds[idx] = params.maxScale;
            ++idx;
        }           
    }
    
    
    public int getNumOfSets()
    {
        return tree.getNumOfSets();
    }
	
	
    /**
     * Writes the whole deviation profile to a (text) stream.
     * @param os
     * @throws IOException 
     */
    public void writeProfile(Writer os) throws IOException
    {
    	if (params.logCardinalities) {
    		os.write("Groups\tnGroups\tArea\tCardinality(log" + Constants.WHICH_NELEMENTS_LOG + ")\tErrorTerm\n");
    	} else {
    		os.write("Groups\tnGroups\tArea\tCardinality\tErrorTerm\n");
    	}
        visitor.setWriter(os);
        tree.accept(visitor);
    }
	
	/**
	 * 
	 * @return Size of the offsets vector
	 */
	public int getSize()
	{
		return tree.getNumOfSets();
	}
	
	public void setParameters(Parameters params)
	{
	    this.params = params;

        invalidate();
	}
	

	Parameters getParameters()
	{
		return params;
	}
	
	public IntersectionTree getTree()
	{
		return tree;
	}
    	
    /**
     * An IIntersectionTreeVisitor which summarizes an error value.
     * 
     * @author muellera
     *
     */
	class ErrorFunctionVisitor implements IIntersectionTreeVisitor
	{
		private double sum;   	// error sum
        Writer os;

        ErrorFunctionVisitor()
        {
            this( null );
        }

        /**
         * 
         * @param os An OuputStreamWriter object for writing all deviation information.
         */
        ErrorFunctionVisitor( OutputStreamWriter os )
		{
            this.os = os;
			reset();
		}
        
        public void setWriter( Writer os )
        {
            this.os = os;
        }
		
		void reset()
		{
			sum = 0.0;
		}
		
		double getValue()
		{
			return sum;
		}
        
        /**
         * Old error function
         * @param node
         * @return the partial error for this node
         */
        double errorFunc0( IntersectionTreeNode node )
        {
            // sum up square error (for  >= 2 intersections)
            // the errors for node.nRight == 1 are 0.0
            double  diff,           // area deviation 
                    part = 0.0;     // partial error
            
            int card = node.card;
            if (params.logCardinalities) {
            	card = AbstractGOCategoryProperties.log(card);
            }
            
            //diff = (node.area-(double)card)/((double)node.nRight-1);
            
            diff = (node.area -(double)card );
            
            // different cases
            if( node.nRight == 1 )
            {
                // 2005-10-18 AM single set deviation
                part = params.eta*diff*diff;
            }
            else
            {
                if( card == 0 )
                { // weight unwanted overlaps overproportional stronger
                    part = params.alpha*diff*diff /((double)(node.nRight-1));
                }
                else
                {   // there is a true intersection
                    
                    // diff /= (double)card;
                    if( node.area == 0.0 )
                    { // no graphical intersection at all! set strong weight
                        part = params.beta*diff*diff /((double)(node.nRight-1));
                    }
                    else
                    { // the touch is ok
                        part = params.gamma*diff*diff /((double)(node.nRight-1));
                    }
                }
            }
            return part;
        }

        /**
         * New error function
         * 
         * @param node
         * @return The partial error for this node
         */
        double errorFunc1( IntersectionTreeNode node )
        {
            // sum up square error (for  >= 2 intersections)
            // the errors for node.nRight == 1 are 0.0
            double  diff,           // area deviation 
                    part = 0.0;     // partial error
            
            double w;               // the weight
            int m = getNumOfSets(),
                k = node.nRight,    // number of involved sets
                min_card;
            
            // compute weight
            if(k==m)
                w = 1.0;
            else
                w = Math.pow(2.0,(double)(m-k-1))/MathUtility.choose(m,k);
            
            // smallest set
            min_card = -1;
            
            IVennObject[] objs = tree.getArrangement().getVennObjects();
            for(int i=0; i<node.path.length();++i)
            {
                if( node.path.get(i) )
                {
                    int card = objs[i].cardinality();
                    if (params.logCardinalities) card = AbstractGOCategoryProperties.log(card);
                    if( min_card < 0 ||  card < min_card )
                    {
                        min_card = card;
                    }
                }
            }
            
            int card = node.card;
            if (params.logCardinalities) card = AbstractGOCategoryProperties.log(card);
            diff = (node.area-(double)card);
                        
            // different cases
            if( node.nRight == 1 )
            {
                // 2005-10-18 AM single set deviation
                part = params.eta*diff*diff;
            }
            else
            {
                if( card == 0 )
                { // weight unwanted overlaps overproportional stronger
                    part = params.alpha*diff*diff;
                }
                else
                {   // there is a true intersection
                    if( diff <= 0 )  // area too small (this should be stronger weighted)
                        part = params.beta * diff * diff;
                    else    // area too large
                        part = params.gamma * diff * diff;
                }
            }
            
            return w*part/(double)min_card;
        }
        
        public void visit(int depth, IntersectionTreeNode node)
		{
        	int card = node.card;
            if (params.logCardinalities) card = AbstractGOCategoryProperties.log(card);

            if( !node.copy && node.nRight >= 1 && (node.area > 0.0 || card > 0))
			{
                double err = 0;
                switch( params.errorFunc )
                {
                    case 0: 
                        err = errorFunc0(node);
                        break;
                    case 1: 
                        err = errorFunc1(node);
                        break;
                    default:
                        Assert.fail("invalid error function index");
                }
                sum += err;
                if( os != null )
                {
                    StringBuffer buf = new StringBuffer();
                    buf.append(node.path.toString());
                    buf.append("\t");
                    buf.append(node.nRight);
                    buf.append("\t");
                    buf.append(node.area);
                    buf.append("\t");
                    buf.append(card);
                    buf.append("\t");
                    buf.append(err);
                    buf.append("\n");
                    try {
                        os.write(buf.toString());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        os = null; // disabling output
                    }
                }
			}
		}
	}
	
	

    
    /**
     * Returns all area deviations. This are the differences card-area.
     * So d[i][j] is (for i<>j) negative if the graphical intersection is greater
     * than the desired cardinality and positive if the cardinality is greater than
     * the graphical intersection.
     * 
     * @param d
     */
    public void getDeviations(double[][] d)
    {        
        BitSet path = new BitSet();
        Assert.assertEquals(d.length,getNumOfSets());

        // set diagonal to zero
        for( int i=0; i<d.length; ++i )
        {
            Assert.assertEquals(d.length,d[i].length);
            d[i][i] = 0.0;
        }
        
        for( int i=0; i<d.length-1; ++i )
        {
            for( int j=i+1; j<d.length; ++j )
            {
                path.clear();
                path.set(i);
                path.set(j);
                
                IntersectionTreeNode node = tree.getByPath(path);
                if( node != null )
                {
                	int card = node.card;
                    if (params.logCardinalities) card = AbstractGOCategoryProperties.log(card);
                    d[i][j] = (double)card - node.area;
                    d[j][i] = d[i][j];
                }
                else
                {
                    d[i][j] = 0.0;
                    d[j][i] = 0.0;
                }
            }
        }
    }
    
    /**
     * A.M.
     * @return pressure cost term (to force sets which are far away)
     */
    public double getPressureCost()
    {
        double cost = 0.0;

        int N = getNumOfSets();
        double[][] d = new double[N][N];
        IVennObject[] sets = tree.getArrangement().getVennObjects();
        
        getDeviations(d);
        
        for( int i=0; i<N-1; ++i )
        {
            for( int j=i+1; j<N; ++j )
            {
                cost += sets[j].getOffset().distance(sets[i].getOffset()) * Math.abs(d[i][j]);
            }
        }        
        return cost;
    }
    
    /**
     * H.K. pressure cost term
     * 
     * @return pressure cost term (to force sets together which are far away)
     */
    public double getPressureCost1()
    {
        double cost = 0.0;

        double  cx=0.0,
                cy=0.0;
        
        int N = getNumOfSets();
        IVennObject[] sets = tree.getArrangement().getVennObjects();
        
        for( int i=0; i<N; ++i )
        {
            cx += sets[i].getOffset().x;
            cy += sets[i].getOffset().y;
        }
        
        cx /= (double)N;
        cy /= (double)N;
        
        for( int i=0; i<N; ++i )
        {
            cost += Math.pow((sets[i].getOffset().x-cx),2.0) +
                    Math.pow((sets[i].getOffset().y-cy),2.0);
        }
        
        return cost;
    }    
    
    public void setInput(double[] input) 
    {
        Assert.assertEquals(input.length,getNumInput());
        
        int N = getNumOfSets();
        IVennObject[] sets = tree.getArrangement().getVennObjects();
                
        int idx = 0;
        for( int i=0; i<N; ++i )
            sets[i].setOffset( new FPoint(input[idx++],input[idx++]) );
        
        for( int i=0; i<N; ++i )
            sets[i].setScale( input[idx++] );
        
        tree.invalidate();
        
        valid = false;
    }
    
    public VennArrangement getArrangement()
    {
        return tree.getArrangement();
    }


    public double[] getLowerBounds()
    {
        return lowerBounds;
    }

    public double[] getUpperBounds()
    {
        return upperBounds;
    }

    public int getNumInput()
    {
        return 3 * tree.getNumOfSets();
    }

    public double getOutput()
    {
        if( !valid )
        {
            visitor.reset();
            tree.accept(visitor);
            
            cacheErrorFunction = visitor.getValue();
            cost = cacheErrorFunction;
            
            if( params.delta > 0.0 )
            {
                // TODO: which one is better????
                cachePressureCost = getPressureCost(); // A.M. function
//                cachePressureCost = getPressureCost1(); // H.K. function
                
                cost += params.delta * cachePressureCost;
            } else
            {
                cachePressureCost = 0.0;
            }
            
            valid = true;
        }
        return -cost;   // an optimizer maximizes a function
    }
    
    /**
     * 
     * @return Partial costs
     */
    public double[] getCost2()
    {        
        getOutput();  // update cost
        
        double[] E = {cacheErrorFunction,cachePressureCost};
        
        return E;
    }
    

    public void stateChanged(ChangeEvent e) 
    {
        invalidate();
    }

    private void invalidate() 
    {
        valid = false;
        tree.invalidate();
    }
    
    /**
     * Contains all parameteres concerning the ErrorFunction. 
     * @author muellera
     *
     */
    public static class Parameters implements Serializable
    {       
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        public int  errorFunc;          //!< error function index 0/1
        public double factor;           //!< area factor (area * factor = cardinality)
        public int maxIntersections;    //!< upper bound for intersections
        public double   alpha,          //!< weighting unwanted intersections 
                        beta,           //!< weighting missing intersections
                        gamma,          //!< weighting area deviations
                        eta,            //!< influence of area deviations for single sets
                        delta;              // gradient pressure
        public double   minScale,
                        maxScale;
        public boolean logCardinalities;
        
        public Parameters()
        {
            errorFunc = 1;
            factor = 0.001;
            maxIntersections = 6;
            alpha = 10.0;
            beta = 200.0;
            gamma = 5.0;
            eta = 1.0;
            delta = 400.0;
            minScale = 1.0;
            maxScale = 1.0;
        }
        
        public Object clone()
        {
            return SystemUtility.serialClone(this);
        }

        /**
         * 
         */
        public boolean check()
        {
        	int oldint;
        	double olddouble;
        	boolean changed = false;
        	
            oldint = errorFunc;
        	if (oldint != (errorFunc = MathUtility.restrict(errorFunc,0,1))) changed = true;
        	
        	oldint = maxIntersections;
            if (oldint != (maxIntersections = MathUtility.restrict(maxIntersections,2,20))) changed = true;
            
            olddouble = eta;
            if (olddouble != (eta = MathUtility.restrict(eta,0.0,10000.0))) changed = true;
            
            olddouble = alpha;
            if (olddouble != (alpha = MathUtility.restrict(alpha,0.0,10000.0))) changed = true;
            
            olddouble = beta;
            if (olddouble != (beta = MathUtility.restrict(beta,0.0,10000.0))) changed = true;
            
            olddouble = gamma;
            if (olddouble != (gamma = MathUtility.restrict(gamma,0.0,10000.0))) changed = true;
            
            olddouble = delta;
            if (olddouble != (delta = MathUtility.restrict(delta,0,10000.0))) changed = true;
            
            olddouble = minScale;
            if (olddouble != (minScale = MathUtility.restrict(minScale,0.5,1.0))) changed = true;
            
            olddouble = maxScale;
            if (olddouble != (maxScale = MathUtility.restrict(maxScale,1.0,1.5))) changed = true;
            
            return ! changed;
        }
    }    
}
