/*
 * Created on 30.05.2005
 *
 */
package venn.diagram;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;
import venn.Constants;
import venn.db.AbstractGOCategoryProperties;
import venn.geometry.AffineTransformer;
import venn.geometry.ConsistencyChecker;
import venn.geometry.DragLabel;
import venn.geometry.FPoint;
import venn.geometry.FRectangle;
import venn.geometry.FSegment;
import venn.geometry.ITransformer;
import venn.gui.HasLabelsListener;


/**
 * This is the main interface to generate a Venn diagram (without using a panel).
 * 
 * @author muellera
 *
 */
public class VennDiagramView extends JPanel
implements IVennDiagramView, ChangeListener, MouseListener, MouseMotionListener, ComponentListener, ActionListener, FocusListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int        DRAG_DELTA  = 10;
    
    private LinkedList          changeListeners;
   
    private VennArrangement     arrangement;

    private AffineTransformer   transformer;
    private IntersectionTree    tree;    

    private boolean             dragging;

    private Point               lastMousePosition;
    private ArrayList            selectedNodes;     // multiple selection
    private IntersectionTreeNode currentNode,
                                highlightNode;
    private String              infoText;
    private DragLabel           lastLabel;
    private Point               popupPosition;
    private BufferedImage       paintBuffer;
    private boolean viewChanged;
    private List<HasLabelsListener> hasLabelsListeners;
    private boolean logCardinalities;
    
    public VennDiagramView( VennArrangement arrangement, int maxLevel, boolean logCardinalities )
    {
    	this.logCardinalities = logCardinalities;
    	
        paintBuffer = null;
        viewChanged = true;
        
        changeListeners = new LinkedList();
        hasLabelsListeners = new LinkedList<HasLabelsListener>();
        this.arrangement = null;
        transformer = new AffineTransformer();
        tree = new IntersectionTree(maxLevel, logCardinalities);
        
        selectedNodes = new ArrayList();
        currentNode = null;
        highlightNode = null;
                
        setLayout(null);
        setToolTipText("");
        setFocusable(true);
        setEnabled(true);
        
        setBorder( new LineBorder( Color.GRAY, 1 ) );
        
        infoText = "cost";
        /*
        infoLabel = new JLabel("cost");
        infoLabel.setBounds(1,1,200,20);
        infoLabel.setBackground(Color.ORANGE);
        infoLabel.setVisible(true);
        
        add(infoLabel);
        */
        

        setBackground(Color.WHITE);
        
        setArrangement(arrangement);
        
        setDoubleBuffered(true);
        setVisible(true);
               
        addMouseMotionListener(this);
        addMouseListener( this );
        addComponentListener( this );
        addFocusListener( this );

        invalidate();
    }
    
    public void addChangeListener( ChangeListener obj )
    {
        if( obj != null )
            changeListeners.add( obj );
    }
    
    public void removeChangeListener( ChangeListener obj )
    {
        if( obj != null )
            changeListeners.remove( obj );
    }
    
    private void updateTransformer()
    {
        if( transformer == null )
        {
            transformer = new AffineTransformer();
        }

        int minExt = Math.min(getWidth(),getHeight());
        
        //transformer.setOffset( new FPoint(getX(),getY()) );
        transformer.setOffset( new FPoint(0.0,0.0) );
        transformer.setScale( new FPoint(minExt,minExt) );
        
    }
    
    public ITransformer getTransformer()
    {
        return transformer;
    }
    
    public void labelsSetTransformerAndListeners() {
    	Component[] comps = getComponents();
    	for (Component comp : comps) {
    		if (comp instanceof DragLabel) {
    			DragLabel label = (DragLabel) comp;
    			label.setTransformer(getTransformer());
    			assert label.getMouseListeners().length == 0;
    			label.addMouseListener(this);
    			assert label.getMouseMotionListeners().length == 0;
    			label.addMouseMotionListener(this);
    		}
    	}
    }
    
    public void removeLabelListeners() {
    	Component[] comps = getComponents();
    	for (Component comp : comps) {
    		if (comp instanceof DragLabel) {
    			DragLabel label = (DragLabel) comp;
                label.removeMouseListener(this);
                label.removeMouseMotionListener(this);
    		}
    	}
    }
    
    /**
     * Fast paint for drag operations.
     * 
     * @param g
     * @param itrans
     * 
     */
    public void draftPaint( Graphics g, ITransformer itrans )
    {
        if( arrangement == null  )
            return;

        IVennObject[] objs = (IVennObject[])arrangement.getVennObjects().clone();
        if( objs == null )
            return;
        
        // compute z-Order (larger categories are painted first so that
        // small categories are visible)
        /*
        Arrays.sort(objs, 
                new Comparator<IVennObject>()
                {

                    public int compare(IVennObject o1, IVennObject o2) {
                        int t1 = o1.cardinality(),
                            t2 = o2.cardinality();
                        
                        if( t1 < t2 )
                            return +1;
                        else
                            if( t1 > t2 )
                                return -1;
                        return 0;
                    }            
                });
         */
        
        Arrays.sort(objs, 
                new Comparator()
                {

                    public int compare(Object o1, Object o2) {
                        int t1 = ((IVennObject)o1).cardinality(),
                            t2 = ((IVennObject)o2).cardinality();
                        
                        if( t1 < t2 )
                            return +1;
                        else
                            if( t1 > t2 )
                                return -1;
                        return 0;
                    }            
                });        

        for( int i=0; i<objs.length; ++i )
        {
            objs[i].directPaint(g,itrans);
        }        
    }

    /**
     * 
     * @param g
     * @param itrans
     */
    public void directPaint( Graphics g, ITransformer itrans )
    {
        // draw polygons
        draftPaint( g, itrans );
        
        if( tree == null )
            return;
        
        // draw illegal overlaps in gray
        TreeTransformer trf = new TreeTransformer();
        tree.accept(trf);
        
        ArrayList levels = trf.getResult();
        for( int depth = 0; depth < levels.size(); ++depth )
        {
            Iterator nodeIter = ((LinkedList)levels.get(depth)).iterator();
            while( nodeIter.hasNext() )
            {
                IntersectionTreeNode node = (IntersectionTreeNode)nodeIter.next();
                if( node.vennObject != null )
                {
                    if( node.card == 0 )
                    {   // draw unwanted intersection in gray
                    	
                        if(arrangement.getParameters().colormode)
                        {
                        	node.vennObject.setFillColor( Color.GRAY );
                        }
                        else
                        {
                        	node.vennObject.setFillColor( Color.BLACK );
                        }
                    	node.vennObject.directPaint( g, itrans );
                    }
                }
            }
        }
        
        // draw selected nodes 
        Iterator iter = selectedNodes.iterator();
        while( iter.hasNext() )
        {
            IntersectionTreeNode node = (IntersectionTreeNode)iter.next();
            
            if( node != null && node.vennObject != null )
            {
                node.vennObject.directPaint( g, getTransformer() );
            }
        }
        
        // draw current node
        if( currentNode != null )
        {
            IVennObject obj = currentNode.vennObject; // arrangement.getVennObjects()[selectedOffset];
            Color col = obj.getFillColor();
            
            /*
            if( currentNode.nRight > 1 )
            {
                obj.setFillColor( new Color(1.0f,0.7f,0.0f,0.8f) );
            } else
            {
                float[] cmp = col.brighter().getComponents(null);
                obj.setFillColor( new Color(cmp[0],cmp[1],cmp[2],0.8f));
            }
            */

            if(arrangement.getParameters().colormode)
            {            
            	obj.setFillColor( new Color(1.0f,0.7f,0.0f,0.8f) );
            }
            else
            {
            	obj.setFillColor( new Color(1.0f,1.0f,1.0f,0.8f) );
            }
            obj.directPaint(g, getTransformer());
            obj.setFillColor( col );
        }
        
        // draw highlight node
        if( highlightNode != null )
        {
            IVennObject obj = highlightNode.vennObject; // arrangement.getVennObjects()[selectedOffset];
            Color col = obj.getFillColor();

            if(arrangement.getParameters().colormode)
            {
            	obj.setFillColor( new Color(1.0f,0.8f,0.1f,0.8f) );
            }
            else
            {
                obj.setFillColor( new Color(1.0f,1.0f,1.0f,0.8f));
            }
            obj.directPaint(g, getTransformer());
            obj.setFillColor( col );
        }
    }
    
    /**
     * Draws line connectors between polygons and Labels.
     * 
     * @param g
     */
    protected void drawLineConnectors( Graphics g )
    {
        Component[] comp = getComponents();
        for( int i=0; i<comp.length; ++i )
        {
            if( comp[i] instanceof DragLabel )
            {
                DragLabel label = (DragLabel)comp[i];
                
                if( label.getWithConnector() )
                { // draw connection line
                    IntersectionTreeNode node = tree.getByPath(label.getPath());
                    if( node != null && node.vennObject != null && !node.vennObject.isEmpty() )
                    {
                        FRectangle rect = label.getBoundaries(g);
                        Assert.assertNotNull( rect );
                        FSegment seg = new FSegment(node.vennObject.getCenter(),rect.center());
                        Assert.assertNotNull( seg );
                        
                        // Polygon intersection point
                        FPoint a, b;
                        a = ((VennPolygonObject)node.vennObject).getPolygon().intersect(seg);
                        b = rect.toPolygon().intersect(seg);
                        
                        if( (a != null) && (b != null) )
                        {
                            java.awt.Point  ta = getTransformer().transform(a),
                                            tb = getTransformer().transform(b);
                            g.setColor(Color.BLUE);
                            g.drawLine(ta.x,ta.y,tb.x,tb.y);
                        }
                        
                    }
                }
            }
        }
    }
        
    public void paintComponent( Graphics graphics, ITransformer itrans )
    {
        if( !hasViewChanged() && isDoubleBuffered() )
        {
            graphics.drawImage(paintBuffer,0,0,Color.WHITE,null);
            return;
        }
        
        Graphics g = graphics;
        if( !dragging && isDoubleBuffered() )
        {   // only use double buffering when not dragging
            if( paintBuffer == null )
                paintBuffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            
            g = paintBuffer.getGraphics();
        }
        
        super.paintComponent(g);    // show background
        
        if( dragging )
        {
            draftPaint(g,itrans);
        }
        else
        {   // full paint
            directPaint(g,itrans);  // show venn objects
            drawLineConnectors(g);
            paintChildren(g);               // show labels
        }
        
        if( infoText != null )
        {
            g.setColor(Color.BLUE);
            g.drawString(infoText,10,20);
        }
        if( g != graphics )
        {
            graphics.drawImage(paintBuffer,0,0,Color.WHITE,null);
        }
        
        viewChanged = false;
    }

    public void paintComponent(Graphics graphics) {
    	paintComponent(graphics, getTransformer());
    }
    
    public boolean hasViewChanged() 
    {
        if( paintBuffer == null || viewChanged )
            return true;
        return false;
    }
    
    public void invalidateView()
    {
        if( paintBuffer != null )
        {
            if( getWidth() != paintBuffer.getWidth() || getHeight() != paintBuffer.getHeight() )
            {   // discard paint buffer
                paintBuffer = null;
            }
        }
        viewChanged = true;
    }

    /* (non-Javadoc)
     * @see venn.IVennDiagramView#findGroups(java.awt.Point, java.awt.Rectangle)
     */
    public BitSet findGroups( Point p ) 
    {
        Assert.assertNotNull( arrangement );
        Assert.assertNotNull( arrangement.getVennObjects() );
        BitSet b = new BitSet();
        
        FPoint	q = getTransformer().inverseTransform(p);
        IVennObject[] objs = arrangement.getVennObjects();
        
        for( int gid = 0; gid < objs.length; ++gid )
        {
            if( objs[gid].contains(q) )
            {
                b.set(gid);
            }
        }
        
        return b;
    }
    
    
    public void clearSelection()
    {
        currentNode = null;
        selectedNodes.clear();
    }
    
    
    /* (non-Javadoc)
     * @see venn.IVennDiagramView#selectGroups(java.util.BitSet)
     */
    public void selectGroups( BitSet groups ) 
    {
        clearSelection();
        if( groups == null )
            groups = new BitSet();
        
        if( groups.cardinality() > 0 )
        {
            currentNode = tree.getByPath( groups );
        }
        if( currentNode == null )
        {
            for( int i=groups.nextSetBit(0); i>=0; i=groups.nextSetBit(i+1))
            {
                selectedNodes.add( tree.getSourceNode(i) );
            }
        }
        
        invalidateView();
        repaint();
    }

    /* (non-Javadoc)
     * @see venn.IVennDiagramView#highlightGroups(java.util.BitSet)
     */
    public void highlightGroups(BitSet groups) 
    {
    }
    
  

    /* (non-Javadoc)
     * @see venn.diagram.IVennDiagramView#getSelectedGroups()
     */
    public BitSet getSelectedGroups()
    {
        BitSet groups = new BitSet();
        Iterator iter = selectedNodes.iterator();
        while( iter.hasNext() )
        {
            IntersectionTreeNode node = (IntersectionTreeNode)iter.next();
            groups.or(node.path);
        }
        if( currentNode != null )
            groups.or( currentNode.path );
        
        return groups;
    }

    /* (non-Javadoc)
     * @see venn.diagram.IVennDiagramView#getHighlightedGroups()
     */
    public BitSet getHighlightedGroups() 
    {
        // TODO
        return null;
    }


    public void setArrangement(VennArrangement arrangement) 
    {
        if( this.arrangement != null )
        {
            this.arrangement.removeChangeListener(this);
        }
        
        this.arrangement = arrangement;
        
        removeAll();
        
        if( arrangement != null )
        {
            arrangement.addChangeListener(this);
        }
        
        tree.setArrangement( arrangement );
        
        invalidateView();
        invalidate();
    }


    public VennArrangement getArrangement()
    {
        return arrangement;
    }


  
    /*
     * Selects the object under the given x,y position.
     * @param point
     * @param append If true the found object will be added to the selection list.
     * @param sourceNode If true the sourceNode behind the object will be added to the selection list.
     */
    protected void selectObject(java.awt.Point point, boolean append, boolean sourceNode, boolean chooseSingle )
    {
        boolean changed = false;
        if( highlightNode != null )
        {
            highlightNode = null;
            changed = true;
        }
        
        FPoint q = getTransformer().inverseTransform( point );
        TreeQuery query = new TreeQuery(tree);

        if( ! append || chooseSingle )
        {
            if( selectedNodes.size() > 0 )
            {
                changed = true;
                selectedNodes.clear();
            }
        }
        
        if( sourceNode || chooseSingle )
        {   // finds only source nodes
            LinkedList result = query.findAllNodes(q);
            
            if( result != null )
            {
                if( ! result.contains(currentNode) )
                    currentNode = null;
                
                if( chooseSingle && currentNode != null )
                { // cycle through full polygons
                    int idx = result.indexOf(currentNode);
                    Assert.assertTrue( idx >= 0 );
                    for( int i=0; i < result.size(); ++i )
                    {
                        IntersectionTreeNode node = (IntersectionTreeNode)result.get((idx + i + 1 ) % result.size());
                        if( node.nRight == 1 )
                        {
                            if( node != currentNode )
                            {
                                currentNode = node;
                                invalidateView();
                                repaint();
                                fireChangeEvent();
                            }
                            return;
                        }
                    }
                    Assert.fail();
                }
                
                Iterator iter = result.iterator();
                while( iter.hasNext() )
                {
                    IntersectionTreeNode node = (IntersectionTreeNode)iter.next();
                    if( node.nRight == 1 )
                    {
                        if( chooseSingle )
                        {
                            if( currentNode == null )
                            {
                                currentNode = node;
                                invalidateView();
                                repaint();
                                fireChangeEvent();
                                return;
                            }
                        }
                        else
                        {
                            if( ! selectedNodes.contains(node) )
                                selectedNodes.add(node);
                            else
                                selectedNodes.remove(node);
                            changed = true;
                        }
                    }
                }
            }
            if( currentNode != null )
            {
                currentNode = null;
                changed = true;
            }
        }
        else
        { // finds deepest node(s)
            currentNode = query.findPolygonNode(q);
            if( currentNode != null && currentNode.card == 0 )
            { // do not allow to select unwanted regions
                currentNode = null; 
            }   
            changed = true;
        }
        if( changed )
        {
            invalidateView();
            repaint();
            fireChangeEvent();
        }
    }

  

    private void setDragging(boolean dragging)
    {
        if( this.dragging != dragging )
        {
            this.dragging = dragging;
            if( dragging )
            { // TODO: set mouse cursor to something ...
                
            }
            else
            { // TODO: set mouse cursor back
                lastMousePosition = null;
                tree.invalidate();
                invalidateView();
                repaint();
                fireChangeEvent();
            }
        }
    }
    

    private boolean isDragging()
    {
        if( lastMousePosition != null && dragging )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void fireChangeEvent()
    {
        ChangeEvent ev = new ChangeEvent(this);
        Iterator iter = changeListeners.iterator();
        while( iter.hasNext() )
        {
            ChangeListener obj = (ChangeListener)iter.next();
            if( obj != null )
                obj.stateChanged( ev );
        }
    }
    
    public void stateChanged(ChangeEvent e) 
    {
        invalidate();
    }


    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e) 
    {  
    }
    
    public void mouseExited(MouseEvent e) 
    {
    }


    public void mouseMoved(MouseEvent e)
    {
    }
    
    
    public void mouseDragged(MouseEvent e)
    {
        //requestFocus();
        
        if( e.getSource() instanceof VennDiagramView )
        { // drag VennObjects
            
            if( lastMousePosition != null && currentNode != null && currentNode.nRight == 1 )
            {
                if( ! isDragging() )
                {   // check when to start dragging
                    if( Math.max(Math.abs(lastMousePosition.x - e.getPoint().x),
                                 Math.abs(lastMousePosition.y - e.getPoint().y)) >= DRAG_DELTA )
                    {
                        // TODO: check if the venn object is locked
                        setDragging(true);
                    }
                }
                                
                if( isDragging() )
                {   
                    java.awt.Point p = new java.awt.Point(  e.getPoint().x-lastMousePosition.x,
                                                            e.getPoint().y-lastMousePosition.y);

                    lastMousePosition = e.getPoint();
                    FPoint delta = transformer.inverseTransform(p);
                    
                    IVennObject obj = currentNode.vennObject;
                    
                    FPoint newP = obj.getOffset().add(delta);
                    
                    double  x = newP.getX(),
                            y = newP.getY();
                    
                    if( obj.getBoundingBox().getMinX() + delta.getX() < 0.0 )
                        x = 0.5*obj.getBoundingBox().getWidth();
                    else
                        if( obj.getBoundingBox().getMaxX() + delta.getX() > 1.0 )
                            x = 1.0-0.5*obj.getBoundingBox().getWidth();
                    
                    if( obj.getBoundingBox().getMinY() + delta.getY() < 0.0 )
                        y = 0.5*obj.getBoundingBox().getHeight();
                    else
                        if( obj.getBoundingBox().getMaxY() + delta.getY() > 1.0 )
                            y = 1.0-0.5*obj.getBoundingBox().getHeight();
                    
                    obj.setOffset( new FPoint(x,y) );
                    tree.invalidate();
                    invalidateView();
                    repaint();
                }
            }
            else
            {
                setDragging(false);
            }
            return;
        }
        

        if( e.getSource() instanceof Component )
        { // DRAG LABELS 
            java.awt.Point p    = e.getPoint();
            if( lastMousePosition == null )
            { // remember start position
                lastMousePosition = p;
            }
            else
            {
                JComponent comp = (JComponent)e.getSource();
                java.awt.Point loc  = comp.getLocation();
                java.awt.Point newLoc = new java.awt.Point(loc.x + p.x - lastMousePosition.x,loc.y + p.y - lastMousePosition.y );
                if( newLoc.x < 0 )
                {
                    newLoc.x = 0;
                }
                else
                {
                    if( newLoc.x + comp.getWidth() >= getWidth() )
                    {
                        newLoc.x = getWidth()-comp.getWidth()-1;
                    }
                }
                
                if( newLoc.y < 0 )
                {
                    newLoc.y = 0;
                }
                else
                {
                    if( newLoc.y + comp.getHeight() >= getHeight() )
                    {
                        newLoc.y = getHeight()-comp.getHeight()-1;
                    }
                }
                comp.setLocation(newLoc);
                invalidateView();
                repaint();
            }
            return;
        }
    }
    
    public void mousePressed(MouseEvent event)
    {
        if( checkPopupMenu(event) )
            return;
        
        //requestFocus();

        if( event.getButton() == MouseEvent.BUTTON1 )
        {
            lastMousePosition = event.getPoint();
        }
        if( event.getButton() == MouseEvent.BUTTON3 && currentNode != null )
        {   // do not unselect a previously selected node if the right mouse
            // button is pressed (context menu)
            return;
        }
        
        if( event.getSource() instanceof VennDiagramView )
        {
            VennDiagramView v = (VennDiagramView)event.getSource();
            v.selectObject(event.getPoint(),event.isControlDown(),event.isShiftDown(),event.isAltDown());
assert v == this;
            return;
        }
        
        
        
        /*
        if( (event.getButton() & MouseEvent.BUTTON3_DOWN_MASK) > 0 )
        {
            VennPanel venn = (VennPanel)event.getSource();
            venn.selectObject(event.getPoint());            
            lastMousePosition = event.getPoint();
            return;         
        }
        */
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent event)
    {
        setDragging(false);
        checkPopupMenu(event);
    }

    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void componentResized(ComponentEvent e) 
    {
        updateTransformer();
        
        tree.invalidate();  // TODO: is this really necessary
        invalidateView();
        repaint();
    }

    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void setInfoText(String info) 
    {
        //infoLabel.setText(info);
        infoText = info;
        invalidate();
    }
    
    protected String mapGroupSet( BitSet set )
    {
        if( set == null )
            return "";
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        for(int i=set.nextSetBit(0); i>=0; i=set.nextSetBit(i+1)) 
        {
            buf.append( getArrangement().getDataModel().getGroupName(i) );
            if( i+1 < set.length() )
                buf.append(" , ");
            
        }
        buf.append("}");
        return buf.toString();
    }
    
    /**
     * 
     * @return A string concerning all unmet conditions.
     */
    public String getInconsistencies()
    {        
        StringBuffer buf = new StringBuffer();
       
        ConsistencyChecker checker = new ConsistencyChecker();
        tree.accept( checker );
        
        LinkedList result = checker.getResult();
        if( result != null )
        {
            if( result.size() > 0 )
                buf.append("missing overlaps:\n");
            
            Iterator iter = result.iterator();
            while( iter.hasNext() )
            {
                IntersectionTreeNode node = (IntersectionTreeNode)iter.next();
                
                buf.append( mapGroupSet(node.path) + " : "+getCardString(node)+"\n");
            }
        }
        
        return buf.toString();
    }
    
    private int getCard(IntersectionTreeNode node) {
    	if (logCardinalities) {
    		return AbstractGOCategoryProperties.log(node.card);
    	}
    	return node.card;
    }
    
    private String getCardString(IntersectionTreeNode node) {
    	if (logCardinalities) {
    		return "" + getCard(node) + "(log" + Constants.WHICH_NELEMENTS_LOG + ")";
    	} else {
    		return String.valueOf(getCard(node));
    	}
    }
    
    public String getSelectedNodeInfo()
    {
        if( currentNode == null )
            return "{}";
        return mapGroupSet(currentNode.path) + " : " + getCardString(currentNode);
    }
    
    
    /**
     * Custom tooltip texts
     */
    @Override
	public String getToolTipText(MouseEvent e) 
    {   
        if( tree == null )
            return null;
        
        IntersectionTreeNode node = currentNode;

        if( node == null )
        {
            FPoint q = getTransformer().inverseTransform(e.getPoint());
            
            TreeQuery query = new TreeQuery(tree);
            node = query.findPolygonNode(q);
            
            if( node != null )
            {
                if( node.card == 0 || node.nRight < 1 )
                    node = null;
            }
            
            if( highlightNode != node )
            {
                highlightNode = node;
                invalidateView();
                repaint();
                fireChangeEvent();
            }                           
        }
        
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        
        if( node == null )
            return null;
        
//      return mapGroupSet(node.path) + " : "+getCardString(node)+" : " + nf.format(node.area);
      String str = mapGroupSet(node.path) + " : "+getCardString(node)+" : " + nf.format(node.area);
//        System.err.println(str);
        return str;
    }

    public String getSelectionInfo()
    {
        StringBuffer buf = new StringBuffer();
        
        if( currentNode != null )
        {
            BitSet el = currentNode.vennObject.getElements();

            buf.append( "#groups=" );
            buf.append( currentNode.path.cardinality() );
            buf.append("  ");
            
            buf.append( "#elements=" );
//            buf.append( el.cardinality() );
            buf.append(getCardString(currentNode));
            buf.append("\n");

            buf.append( mapGroupSet(currentNode.path) );
            buf.append("\n");
            
            
            
            for( int i=el.nextSetBit(0); i>=0; i = el.nextSetBit(i+1) )
            {
                buf.append( arrangement.getDataModel().getElementName(i) );
                buf.append("\n");
            }
        }
        
        
        return buf.toString();
    }

    private boolean checkPopupMenu(MouseEvent event)
    {
        if( event.isPopupTrigger() )
        {
            if( event.getSource() == this )
            {
                if( currentNode == null )
                    return false;
                
                JPopupMenu popup = new JPopupMenu();
                JMenuItem item;
                item = new JMenuItem("Place Label");
                item.addActionListener(this);
                popup.add(item);

                /*
                boolean locked = false; 
                
                item = new JMenuItem("Lock");
                item.addActionListener(this);
                item.setEnabled(!locked);
                popup.add(item);
                
                item = new JMenuItem("Unlock");
                item.addActionListener(this);
                item.setEnabled(locked);
                popup.add(item);
                */

                popupPosition = new java.awt.Point(event.getX(),event.getY());                
                popup.show(event.getComponent(),event.getX(),event.getY());

                return true;
            }
            
            if( event.getSource() instanceof DragLabel )
            { // label popup menu
                lastLabel = (DragLabel)event.getSource();
                
                JPopupMenu popup = new JPopupMenu();
                JMenuItem item;
                
                item = new JMenuItem("Remove label");
                item.addActionListener(this);
                popup.add(item);
                
                JCheckBoxMenuItem cbitem = new JCheckBoxMenuItem("Line connector");
                cbitem.addActionListener(this);
                cbitem.setSelected(lastLabel.getWithConnector());
                popup.add(cbitem);

                popup.show(event.getComponent(),event.getX(),event.getY());
                
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    public void actionPerformed(ActionEvent event)
    {
        String cmd = event.getActionCommand();
        
        if( cmd.equalsIgnoreCase("lock") )
        {
            // generation[selectedGeneration].getLock().set(selectedOffset);
            invalidateView();
            repaint();
            return;
        }
        
        if( cmd.equalsIgnoreCase("unlock") )
        {
            // getLock().clear(selectedOffset);
            invalidateView();
            repaint();
            return;
        }
        
        if( cmd.equalsIgnoreCase("place label") )
        {
            if( currentNode == null )
                return;

            String text = getSelectedNodeInfo();
            if( text != null )
            {                    
                DragLabel label = new DragLabel(transformer,text,currentNode.path );                 
                if( popupPosition != null )
                {
                    label.setLocation(popupPosition.x,popupPosition.y);
                }
                else
                {
                    FPoint p = null;
                    if( currentNode.vennObject != null )
                    {
                        p = currentNode.vennObject.getOffset();
                    }
                    else
                    {
                        p = new FPoint(0.5,0.5);
                    }
                    label.setRelativePosition(p);
                }
                
                label.setVisible(true);
                label.addMouseListener(this);
                label.addMouseMotionListener(this);
                add(label);
                notifyHasLabelsChanged();
                invalidateView();
                repaint();
            }           
            return;
        }
        
        if( cmd.equalsIgnoreCase("remove label") )
        {
            if( lastLabel != null )
            {
                remove(lastLabel);
                lastLabel = null;
                notifyHasLabelsChanged();
                invalidateView();
                repaint();
            }
            
            return;
        }
        
        if( cmd.equalsIgnoreCase("line connector") )
        {
            if( lastLabel != null )
            {
                lastLabel.setWithConnector(!lastLabel.getWithConnector());
                invalidateView();
                repaint();
            }
        }
        
        System.out.println("VennPanel.actionPerformed : unhandled command : "+cmd);
    }

    public IntersectionTree getTree() 
    {
        return tree;
    }

    public void removeLabels() 
    {
        removeAll();
        notifyHasLabelsChanged();
        invalidateView();
        repaint();
    }

    public void focusGained(FocusEvent e) {
        
    }

    public void focusLost(FocusEvent e) 
    {
        /*
        if( getSelectedGroups().cardinality() > 0 )
        {
            selectGroups(null);
            repaint();
            fireChangeEvent();
        }
        */
    }
    
    public boolean hasLabels() {
    	for (Component comp : getComponents()) {
    		if (comp instanceof DragLabel) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public synchronized void addHasLabelsListener(HasLabelsListener listener) {
    	if (hasLabelsListeners.contains(listener)) {
    		throw new IllegalStateException();
    	}
    	hasLabelsListeners.add(listener);
    }
    
    public synchronized void removeHasLabelsListener(HasLabelsListener listener) {
    	if (! hasLabelsListeners.contains(listener)) {
    		throw new IllegalStateException();
    	}
    	hasLabelsListeners.remove(listener);
    }
    
    private synchronized void notifyHasLabelsChanged() {
    	for (HasLabelsListener listener : hasLabelsListeners) {
    		listener.hasLabelsChanged();
    	}
    }
    
}
