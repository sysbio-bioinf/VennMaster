/*
 * venn/VennMaster.java
 * 
 * Created on 25.06.2004
 * 
 * Main Application
 * 
 */
package venn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import junit.framework.Assert;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import venn.db.AbstractDataFilter;
import venn.db.GOCategoryProperties;
import venn.db.GODistanceFilter;
import venn.db.GeneOntologyReaderModel;
import venn.db.GoTree;
import venn.db.HTGeneOntologyReaderModel;
import venn.db.IDataFilter;
import venn.db.IGeneFilter;
import venn.db.IVennDataModel;
import venn.db.ListReaderModel;
import venn.db.VennFilteredDataModel;
import venn.diagram.IVennDiagramView;
import venn.diagram.VennArrangement;
import venn.diagram.VennErrorFunction;
import venn.geometry.FileFormatException;
import venn.gui.CommonFileFilter;
import venn.gui.ExcelAdapter;
import venn.gui.FilterPanel;
import venn.gui.InfoDialog;
import venn.gui.ParameterDialog;
import venn.gui.VennPanel;
import venn.optim.EvolutionaryOptimizer;
import venn.optim.EvolutionaryOptimizerV1;
import venn.optim.IOptimizer;
import venn.optim.IOptimizerObserver;
import venn.optim.OptimizerObserver;
import venn.optim.OptimizerWorker;
import venn.optim.StateObserver;
import venn.optim.SwarmOptimizer;
import venn.utility.SystemUtility;
import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.StringHolder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.thoughtworks.xstream.XStream;


/**
 * The main (stand alone) application.
 * @author muellera
 */
public class VennMaster 
extends javax.swing.JFrame
implements ActionListener, KeyListener, ChangeListener, ComponentListener, ListSelectionListener, IOptimizerObserver
{	
    private static final long serialVersionUID = 1L;
    public static final int MAX_NUM_GROUPS = 10;       // maximum number of groups
    
    public static final int VERSION_MAJOR = 0;          // version number
    public static final int VERSION_MINOR = 36;
    public static final int VERSION_SUB   = 0;
    public static final String VERSION_DATE = "2007-09-04";
    
    
    public static final int MODE_LIST = 0;
    public static final int MODE_GOMINER = 1;
    
    private int             dataMode;   // choose one of the two constants above
    
    
    private VennPanel 		venn;
	private AllParameters   params;
    private OptimizerWorker worker;
    private IOptimizer[]    optim;
	
	private JScrollPane 	scroller;
    private JPanel          progressPanel;
	private JProgressBar 	progressBar;
	private JMenu			menuFile;
	private JMenuItem 		menuItemOptimize,
                            menuItemResume,
							menuItemStop,
							menuItemOptions,
							menuItemRemove;
	
	private JSplitPane		splitter;
    
    // Panels on the bottom
	private JTabbedPane		infoPane;
    private FilterPanel     filterPanel;            // filter settings
	private JTextArea		inconsistencyInfo,      // show inconsistencies (not fulfilled intersections)
							globalInfo;
    private JList           selectionInfo;          // show infos about the current mouse selection
	//private EasyGeneFilter		currentGOfilter;
	private CategoryTableData		categoryTableModel;     // GO categories
	private JTable			categoryTable;
	private ExcelAdapter	excelAdapter;           // <- don't touch this!! This is a decorator! It will get a compiler warning!
	
	private String 			fileName;
	private String			lastWorkingPath;
	//private EasyGeneFilter	geneFilter;
    private IDataFilter     currentFilter;              // filter settings for the current Venn diagram
	private JComboBox 		zoomChooser;
	private int 			zoomLevel;
    private IVennDataModel  sourceDataModel;                // original data set
    private VennFilteredDataModel   filteredDataModel;           // manual filter 
    private Random  random;
    private VennErrorFunction[] errFunc;
    private boolean isUpdating;
    private boolean isTableChanging;
    private Writer reporterWriter;
	private LinkedList observers;
	private GoTree goTree;

	public VennMaster()
	{
		super("VennMaster "+VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_SUB);
		
		goTree = new GoTree();
        
        random = new Random();
        	
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addComponentListener(this);
		addKeyListener(this);
        		
		//geneFilter = new EasyGeneFilter();
        currentFilter = null;
        observers = new LinkedList();
		
		// load icon
		Image image = getImageResource("images/icon.png");
		if(image != null)
		{
			setIconImage(image);
		}
		
		// add menu bar
		JMenuBar menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createOptionsMenu());
		menubar.add(createHelpMenu());
		setJMenuBar(menubar);

		JPanel topPanel = new JPanel(new BorderLayout());
		
		// add panels
		//JPanel panel =  new JPanel();
		//panel.setLayout(new GridLayout(1,1));
        params = null;
		venn = new VennPanel();      
        
		venn.addChangeListener(this);
		venn.addActionListeners(this);
		venn.addKeyListener(this);		

		//panel.add(venn);
		scroller = 
			new JScrollPane(venn, // panel
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setAutoscrolls(true);
		topPanel.add(scroller,BorderLayout.CENTER);
		
		// add progress bars
		progressBar = new JProgressBar(0, 1000);
		progressBar.setValue(0);
	  	progressBar.setStringPainted(true);
	  	
        progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar,BorderLayout.CENTER);
        JButton button = new JButton("cancel");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        progressPanel.add(button,BorderLayout.EAST);
        
        progressPanel.setVisible(false);
	  	topPanel.add(progressPanel,BorderLayout.SOUTH);
	  	
	  	// status line
	  	String[] list = new String[6];
	  	list[0] = "400%";
	  	list[1] = "200%";
		list[2] = "150%";
		list[3] = "100%";
		list[4] = "75%";
		list[5] = "50%";
		
	  	zoomChooser = new JComboBox(list);
	  	zoomChooser.setSelectedIndex(3);
	  	zoomChooser.addActionListener(this);
	  	zoomChooser.addKeyListener(this);
	  	topPanel.add(zoomChooser,BorderLayout.NORTH);

	  	// Tab
	  	infoPane = new JTabbedPane();
        int numPanels = 0;
        
        filterPanel = new FilterPanel();
        filterPanel.addActionListener(this);
        filterPanel.setParameters(new GODistanceFilter.Parameters());
        filterPanel.setValues(0,0,0,0);
        
        infoPane.addTab("Filter", new JScrollPane(filterPanel));
        infoPane.setToolTipTextAt(numPanels++,"Filter GoMiner categories");
	  	
		globalInfo = new JTextArea();
		globalInfo.setEnabled(false);
		globalInfo.setDisabledTextColor(new Color(0,0,0));
		
		infoPane.addTab("Data",new JScrollPane(globalInfo));
        infoPane.setToolTipTextAt(numPanels++,"Data info");
		
        
	  	//selectionInfo = new JTextArea();
        selectionInfo = new JList(new DefaultListModel());
	  	selectionInfo.setEnabled(true);
        //selectionInfo.setEditable(false);
	  	//selectionInfo.setDisabledTextColor(new Color(0,0,0));
        selectionInfo.setToolTipText("Double click for link to genecard");
		//infoPane.addTab("Selection",new JScrollPane(selectionInfo));
        //infoPane.setToolTipTextAt(numPanels++,"Selected groups/genes");
        
        selectionInfo.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if( e.getClickCount() < 2 )
                            return;
                        JList list = (JList)e.getSource();
                        Object obj = list.getSelectedValue();
                        if( obj != null )
                        {
                            String str = obj.toString();
                            if( isGeneName(str) )
                                BrowserControl.displayURL( createGenecardURL( str ) );
                        }
                    }
                }
                );        
		
	  	inconsistencyInfo = new JTextArea();
	  	inconsistencyInfo.setEnabled(false);
	  	inconsistencyInfo.setDisabledTextColor(new Color(0,0,0));
		infoPane.addTab("Inconsistencies",new JScrollPane(inconsistencyInfo));
        infoPane.setToolTipTextAt(numPanels++,"Show missing overlaps: groups/cardinalities");
        

		// setup category table
		categoryTableModel = new CategoryTableData(venn);

		DefaultTableColumnModel cm = new DefaultTableColumnModel();

		TableColumn col;
                
		col = new TableColumn(0,10);
        col.setHeaderValue("#");
        cm.addColumn(col);
        
        col = new TableColumn(1,18);
		col.setHeaderValue("Active");
		col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
		col.setCellRenderer(new CheckboxRenderer(venn));
		cm.addColumn(col);
		
		col = new TableColumn(2,50);
		col.setHeaderValue("TermID");
		cm.addColumn(col);
        
        
        col = new TableColumn(3,195);
        col.setHeaderValue("Category");
        cm.addColumn(col);
        
        
		col = new TableColumn(4,50);
		col.setHeaderValue("nChanged");
		cm.addColumn(col);
		
        col = new TableColumn(5,50);
        col.setHeaderValue("nTotal");
        cm.addColumn(col);
        
        col = new TableColumn(6,50);
        col.setHeaderValue("pValue");
        cm.addColumn(col);
        
        col = new TableColumn(7,50);
        col.setHeaderValue("FDR");
        cm.addColumn(col);
        
//        col = new TableColumn(8,50);
//        col.setHeaderValue("dist");
//        cm.addColumn(col);
       
		categoryTable = new JTable(categoryTableModel,cm);
		categoryTable.setSelectionBackground( Color.ORANGE );
		categoryTable.setSelectionForeground( Color.BLUE );

        categoryTable.setDefaultRenderer(Object.class, new CategoryTableRenderer( venn ) );
        categoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);		
        
        categoryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        categoryTable.setColumnSelectionAllowed(true);
        categoryTable.setRowSelectionAllowed(true);
        
        categoryTable.getSelectionModel().addListSelectionListener( this );
        categoryTable.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if( e.getClickCount() < 2 )
                            return;
                        JTable table = (JTable)e.getSource();
                        int row = table.rowAtPoint(e.getPoint()),
                            col = table.columnAtPoint(e.getPoint());
                        System.out.println(row + ":"+col);
                        if( row>=0 && col>=0 )
                        {
                            col = table.convertColumnIndexToModel(col);
                            if( col == 2 )
                            {
                                CategoryTableData model = (CategoryTableData)table.getModel();
                                GOCategoryProperties props = model.getGoProperties(row);
                                if( props != null )
                                {
                                    BrowserControl.displayURL( createGoURL( props.ID ) );
                                }
                            }
                        }
                    }
                }
                );
		
		excelAdapter = new ExcelAdapter(categoryTable);

        
        JSplitPane catSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,   
                                    new JScrollPane(categoryTable),
                                    new JScrollPane(selectionInfo) );
        catSplit.setDividerLocation(170);
		infoPane.addTab("Categories", catSplit);
		
		// topPanel.setMinimumSize(new Dimension(100,100));
		topPanel.setPreferredSize(new Dimension(400,400));
		
		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,topPanel,infoPane);
		//splitter.setDividerLocation(0.8);
        splitter.setDividerLocation(400);
		
		getContentPane().add(splitter);
		
	  	setZoomLevel(100);
	  	updateInfoArea();
        
        setDataMode( MODE_LIST );
    
        setParameters( new AllParameters() );
	}
	
	
    protected boolean isGeneName(String str) 
    {
        if( str.length() <= 2 )
            return false;
        
        str = str.toUpperCase();
        
        
        char C = str.charAt(0);
               
        return C>='A' && C<='Z';
    }


    /**
     * Creates a link to the godatabase.org website for the given GoID.
     * @param termID
     * @return URL
     */
	protected String createGoURL(long termID) 
    {
        DecimalFormat format = new DecimalFormat("0000000");
        
        return "http://godatabase.org/cgi-bin/go.cgi?action=replace_tree&query=GO:" + format.format(termID);
    }
    
    /**
     * Creates a link to the genecards.org website for the given gene.
     * @param gene
     * @return URL
     */
    protected String createGenecardURL( String gene )
    {
        return "http://www.genecards.org/cgi-bin/carddisp?"+gene.trim();
    }


    private JMenu createFileMenu()
	{
		JMenuItem item;
		
		// MENU: FILE
		menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
        
		// open go
		item = new JMenuItem("Open GoMiner file",'O');
		item.addActionListener(this);
		menuFile.add(item);

        // open ht go
        item = new JMenuItem("Open High-Throughput GoMiner file",'H');
        item.addActionListener(this);
        menuFile.add(item);
        
		
		// open list
		item = new JMenuItem("Open List",'l');
		item.addActionListener(this);
		menuFile.add(item);
				
		// save
		item = new JMenuItem("Save",'S');
		item.addActionListener(this);
		menuFile.add(item);
        
        // export profile
        item = new JMenuItem("Export Profile",'E');
        item.addActionListener(this);
        menuFile.add(item);
        

		// exit
		item = new JMenuItem("Exit",'X');
		item.addActionListener(this);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,KeyEvent.ALT_MASK));
		menuFile.add(item);
		
		// TODO: MENU: VIEW
		return menuFile;
	}
	
	private JMenu createOptionsMenu()
	{
		// MENU: FILE
		JMenu menu = new JMenu("Options");
		menu.setMnemonic('O');
					
		// restart
        /*
		menuItemRestart = new JMenuItem("Restart",'r');
		menuItemRestart.addActionListener(this);
		menu.add(menuItemRestart);
        */
		
		// update
		menuItemOptimize = new JMenuItem("Optimize",'e');
		menuItemOptimize.addActionListener(this);
		menu.add(menuItemOptimize);
        
        menuItemResume = new JMenuItem("Resume",'r');
        menuItemResume.addActionListener(this);
        menu.add(menuItemResume);
        
		
		menuItemStop= new JMenuItem("Stop",'e');
		menuItemStop.addActionListener(this);
		menuItemStop.setEnabled(false);
		menu.add(menuItemStop);
		
		
		// options
		menuItemOptions = new JMenuItem("Options",'o');
		menuItemOptions.addActionListener(this);
		menu.add(menuItemOptions );
		
        JMenuItem item;
        item = new JMenuItem("Save Options",'s');
        item.addActionListener(this);
        menu.add(item);
        
        item = new JMenuItem("Load Options",'l');
        item.addActionListener(this);
        menu.add(item);
        
		//
		menuItemRemove = new JMenuItem("Remove all labels",'a');
		menuItemRemove.addActionListener(this);
		menu.add(menuItemRemove);
		
		return menu;
	}	
	
	private JMenu createHelpMenu()
	{
		// MENU: FILE
		JMenu menu = new JMenu("Help");
		menu.setMnemonic('H');
					
		JMenuItem item = new JMenuItem("About VennMaster",'A');
		item.addActionListener(this);
		menu.add(item);
		
		return menu;
	}
	
	public void actionPerformed(ActionEvent event)
	{
	    Object src = event.getSource();
		String cmd = event.getActionCommand();
		
		if( worker != null && src == worker )
		{ // the simulation has stopped!
            if( ! isSimulating() )
            {
    			actionStopped();
    			updateInfoArea();
    		    
    		    progressPanel.setVisible(false);
            }
            else
            {
                System.out.println("warning: message from worker!");
            }
		    return;
		}

		if( cmd.equalsIgnoreCase("Open GoMiner file"))
		{
			actionFileOpenGo();
			return;
		}
        
        if( cmd.equalsIgnoreCase("Open High-Throughput GoMiner file"))
        {
            actionFileOpenHTGo();
            return;
        }        

		if( cmd.equalsIgnoreCase("open list"))
		{
			actionFileOpenList();
			return;
		}
		
		if( cmd.equalsIgnoreCase("save"))
		{
			actionFileSave();
			return;
		}
        
        if( cmd.equalsIgnoreCase("export profile"))
        {
            actionExportProfile();
            return;
        }
		
		if( cmd.equalsIgnoreCase("exit") )
		{
			actionFileExit();
			return;
		}

		
		if( cmd.equalsIgnoreCase("optimize") )
		{
			actionOptionsOptimize();
			return;
		}
		
        if( cmd.equalsIgnoreCase("resume") )
        {
            actionOptionsResume();
            return;
        }
        
		if( cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("cancel") )
		{
			actionStop();
			return;
		}
		
		if( cmd.equalsIgnoreCase("options") )
		{
			actionOptionsOptions();
			return;
		}
		
        if( cmd.equalsIgnoreCase("save options") )
        {
            actionOptionsSaveOptions();
            return;
        }
        
        if( cmd.equalsIgnoreCase("load options") )
        {
            actionOptionsLoadOptions();
            return;
        }
        
		if( cmd.equalsIgnoreCase("remove all labels") )
		{
			venn.removeLabels();
			venn.repaint();
			return;
		}
				
		if( cmd.equalsIgnoreCase("comboBoxChanged") )
		{
			// zoom changed
			switch( zoomChooser.getSelectedIndex() )
			{
				case 0:
					setZoomLevel(400);
					break;				
				case 1:
					setZoomLevel(200);
					break;
				case 2:
					setZoomLevel(150);
					break;
				case 3:
					setZoomLevel(100);
					break;
				case 4:
					setZoomLevel(75);
					break;
				case 5:
					setZoomLevel(50);
					break;
			}

			return;
		}
		
		if( cmd.equalsIgnoreCase("About VennMaster"))
		{
			InfoDialog dialog = new InfoDialog(this);
			dialog.setVisible(true);
			return;
		}
		
		if( event.getSource() == (Object)venn )
		{
			// events from the VennPanel
			actionStopped();
			updateInfoArea();
			if( cmd != null )
			{
				if( cmd.equalsIgnoreCase("simulation.error") )
				{
					JOptionPane.showMessageDialog(	this,
							"Simulation aborted due to an exception\n",
							"Error",
							JOptionPane.ERROR_MESSAGE);					
				}
				
			}
			return;
		}
        
        if( event.getSource() == filterPanel )
        {
            if( cmd.compareTo("update") == 0 )
            { // updates the Venn diagram
                //venn.setDataModel( filteredDataModel.createCopy() );
                // check number of categories and give out a warning
                if( filteredDataModel.getNumGroups() > MAX_NUM_GROUPS )
                {
                    int res = JOptionPane.showConfirmDialog(this,
                                            "There are "+filteredDataModel.getNumGroups()+" categories.\r\n"+
                                            "It is not recommended to use more than "+MAX_NUM_GROUPS+" categories!\r\n"+
                                            "Are you sure to continue?",
                                            "Warning",JOptionPane.YES_NO_CANCEL_OPTION);
                    if( res != JOptionPane.OK_OPTION )
                        return;        
                }
                
                if( filteredDataModel.getNumGroups() < 1 )
                {
                    JOptionPane.showMessageDialog(this,
                    							  "There are "+filteredDataModel.getNumGroups()+" categories.\r\n"+
                                                  "It is not possible to display less than 1 categorie!\r\n",
                                                  "Error",0);
                    return;   
                }
                
                if( filteredDataModel.getFilter() != null )
                {
                    // create a copy of the filter in the current data model
                    currentFilter = (IDataFilter)(((AbstractDataFilter)filteredDataModel.getFilter()).clone());
                    
                    // create a new filtered data model with the copied filter
                    venn.setDataModel( new VennFilteredDataModel(sourceDataModel,currentFilter) );
                } else
                {
                    currentFilter = null;
                    venn.setDataModel( sourceDataModel );
                }
                setZoomLevel(zoomLevel);
                // start optimization
                actionOptionsOptimize();
                
                return;
            } // update
            if( cmd.compareTo("reset") == 0 )
            { // sets back filter parameters
                filteredDataModel.setFilter( (IDataFilter)currentFilter.clone() );
                return;
            }
        }
		
		System.err.println("VennMaster.actionPerformed unhandled action : " + cmd);
	}
	
    /**
     * Sets the zoom level of the Venn diagram viewer.
     * 
     * @param level
     */
	public void setZoomLevel(int level)
	{
		Dimension dim = new Dimension((400*Math.max(venn.getNumViews(),1)*level)/100,(400*level)/100);

        zoomLevel = level;
        
        venn.setPreferredSize(dim);
        venn.setSize(dim);
		venn.invalidate();
	}
	
	// ++++++++++++++++++++ MENU ACTIONS +++++++++++++++++++++++
	
	/**
	 * Reads the gene ontology miner file format.
	 */
	protected void actionFileOpenGo()
	{
        /*
		ImportDialog importDialog = new ImportDialog(this);
		importDialog.setParameters(geneFilter.getParameters());
		importDialog.setVisible(true);
		if( importDialog.getState() != ImportDialog.OK_OPTION )
			return;
		
		geneFilter.setParameters(importDialog.getParameters());
        */
		
		//System.out.println(geneFilter.getParameters());
		
		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;

		String 	groupFile = null,
				groupName = null,
				geneFile = null,
				geneName = null;

		if( lastWorkingPath != null )
		{
			dialog.setCurrentDirectory(new File(lastWorkingPath));
		}
		
		// LOAD CATEGORIES
		dialog.setAcceptAllFileFilterUsed(false);		
		filter = new CommonFileFilter("Summary Export File (.se,.txt)");
		filter.addExtension("se");
        filter.addExtension("txt");
		dialog.addChoosableFileFilter(filter);

		if( dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			lastWorkingPath = file.getAbsolutePath();
			if( file.exists() )
			{
				groupFile = file.getAbsolutePath();
				groupName = file.getName();
			}
			else
			{
				JOptionPane.showMessageDialog(	this,
												"File does not exsist '"+ file.getName().toString() +"' ",
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return;				
			}
		}
		else
		{
			return;
		}

		// LOAD GENE LIST
		
		dialog = new JFileChooser();
		dialog.setCurrentDirectory(new File(lastWorkingPath));
		
		filter = new CommonFileFilter("Gene Category Export (.gce,.txt)");
        filter.addExtension("gce");
		filter.addExtension("txt");
		dialog.setFileFilter(filter);
		
		if( dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			lastWorkingPath = file.getAbsolutePath();
			if( file.exists() )
			{
				geneFile = file.getAbsolutePath();
				geneName = file.getName();				
			}
			else
			{
				JOptionPane.showMessageDialog( this,
										"File does not exsist '"+ file.getName().toString() +"' ",
										"Error",
										JOptionPane.ERROR_MESSAGE);
				return;				
			}
		}
		else
		{
			return;
		}
		
		if( groupFile != null && geneFile != null )
		{
			try
			{
                sourceDataModel = loadGOMiner(groupFile,geneFile,null);
				fileName = groupName + " : " + geneName;
				setTitle("VennMaster "+fileName);
                setDataMode(MODE_GOMINER);                                
                filteredDataModel = new VennFilteredDataModel(sourceDataModel, new GODistanceFilter(goTree) );
                filterPanel.setDataModel( filteredDataModel );
                filterPanel.setPValueMode(true);
                venn.setDataModel(null);
                infoPane.setSelectedIndex(0);
                setZoomLevel(zoomLevel);
			}
			catch( IOException e )
			{
				JOptionPane.showMessageDialog(	this,
												"IO error while reading files '"+ groupFile+"/"+ geneFile +"' "+e,						
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return;
			}				
			catch( venn.geometry.FileFormatException e)
			{
				JOptionPane.showMessageDialog(	this,
												"File format error reading file '"+ groupFile+"/"+ geneFile +"' "+e,
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
	
    
    /**
     * Reads the high throughput gene ontology miner file format.
     */
    protected void actionFileOpenHTGo()
    {
        /*
        ImportDialog importDialog = new ImportDialog(this);
        importDialog.setParameters(geneFilter.getParameters());
        importDialog.setVisible(true);
        if( importDialog.getState() != ImportDialog.OK_OPTION )
            return;
        
        geneFilter.setParameters(importDialog.getParameters());
        */
        
        //System.out.println(geneFilter.getParameters());
        
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;

        String  geneFile  = null,
                geneName = null;
        
        if( lastWorkingPath != null )
        {
            dialog.setCurrentDirectory(new File(lastWorkingPath));
        }
        
        // LOAD CATEGORIES
        dialog.setAcceptAllFileFilterUsed(false);
        
        // LOAD GENE LIST       
        filter = new CommonFileFilter("High-Throughput GoMiner .gce file (.gce,.txt)");
        filter.addExtension("gce");
        filter.addExtension("txt");
        dialog.setFileFilter(filter);       
        
        if( dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION )
        {
            File file = dialog.getSelectedFile();
            lastWorkingPath = file.getAbsolutePath();
            if( file.exists() )
            {
                geneFile = file.getAbsolutePath();
                geneName = file.getName();              
            }
            else
            {
                JOptionPane.showMessageDialog( this,
                                        "File does not exsist '"+ file.getName().toString() +"' ",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                return;             
            }
        }
        else
        {
            return;
        }
        
        if( geneFile != null )
        {
            try
            {
                sourceDataModel = loadHTGOMiner(geneFile,null);
                fileName = geneName;
                setTitle("VennMaster "+fileName);
                setDataMode(MODE_GOMINER);
                filteredDataModel = new VennFilteredDataModel(sourceDataModel, new GODistanceFilter(goTree) );                                
                filterPanel.setDataModel( filteredDataModel );
                filterPanel.setPValueMode(false);
                venn.setDataModel(null);
                infoPane.setSelectedIndex(0);
                setZoomLevel(zoomLevel);
            }
            catch( IOException e )
            {
                JOptionPane.showMessageDialog(  this,
                                                "IO error while reading files '"+ geneFile +"' "+e,                      
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                return;
            }               
            catch( venn.geometry.FileFormatException e)
            {
                JOptionPane.showMessageDialog(  this,
                                                "File format error reading file '"+ geneFile +"' "+e,
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
	/**
	 * Opens a single list file.
	 *
	 */
	protected void actionFileOpenList()
	{
		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;
		
		dialog.setAcceptAllFileFilterUsed(false);		
		filter = new CommonFileFilter("LIST Listfile (.list,.lst,.txt)");
		filter.addExtension("list");
		filter.addExtension("lst");
        filter.addExtension("txt");
		
		dialog.addChoosableFileFilter(filter);

		if( lastWorkingPath != null )
		{
			dialog.setCurrentDirectory(new File(lastWorkingPath));
		}
		
		if( dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			
			lastWorkingPath = file.getAbsolutePath();
				
			if( file.exists() )
			{
				try
				{
					fileName = file.getName();
					this.setTitle("Venn Master - "+fileName);
					
                    sourceDataModel = loadFromListFile( file.getAbsolutePath() );                    
                    setDataMode( MODE_LIST );
                    venn.setDataModel( sourceDataModel );
                    infoPane.setSelectedIndex( infoPane.getTabCount() - 1);
                    setZoomLevel(zoomLevel);
					actionOptionsOptimize();
				}
				catch( IOException e )
				{
					JOptionPane.showMessageDialog(	this,
													"IO error while reading file '"+ file.getName().toString() +"' "+e,							
													"Error",
													JOptionPane.ERROR_MESSAGE );
				}				
				catch( venn.geometry.FileFormatException e)
				{
					JOptionPane.showMessageDialog( 	this,
													"File format error reading file '"+ file.getName().toString() +"' "+e,
													"Error",
													JOptionPane.ERROR_MESSAGE );
				}
			}
		}
	}

    private void setDataMode(int mode)
    {
        if( filterPanel == null )
            return;
        
        switch( mode )
        {
            case MODE_LIST:
                filterPanel.setVisible(false);
                if( infoPane.getSelectedIndex() == 0 )
                    infoPane.setSelectedIndex(infoPane.getTabCount()-1);
                break;
                
            case MODE_GOMINER:
                filterPanel.setVisible(true);
                break;
                
            default:
                Assert.fail("illegal argument for VennMaster.setDataMode()");
        }
        dataMode = mode;
    }


    /**
     * Writes the current Venn diagram to an SVG file.
     * 
     * @param os
     * @throws UnsupportedEncodingException 
     * @throws SVGGraphics2DIOException 
     */
	public void writeSVGFile(OutputStream os,int width, int height) throws UnsupportedEncodingException, SVGGraphics2DIOException
    {
           // Get a DOMImplementation
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        int mx = Math.min(height,width);
        Dimension dim = new Dimension(mx*venn.getNumViews(),mx);
        
        svgGenerator.setSVGCanvasSize( dim );   

        // Ask the test to render into the SVG Graphics2D implementation
        venn.directPaint( svgGenerator, dim );

        // Finally, stream out SVG to the standard output using UTF-8
        // character to byte encoding
        boolean useCSS = true; // we want to use CSS style attribute
        Writer out;
        out = new OutputStreamWriter(os, "UTF-8");
        svgGenerator.stream(out, useCSS);
    }
    
    
	protected void actionFileSave()
	{
		JFileChooser dialog = new JFileChooser();
		CommonFileFilter filter;
		
		dialog.setAcceptAllFileFilterUsed(false);
		
		filter = new CommonFileFilter("JPEG Image (.jpg,.jpeg)");
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		dialog.addChoosableFileFilter(filter);
		
		filter = new CommonFileFilter("SVG Image (.svg)");
		filter.addExtension("svg");
		dialog.addChoosableFileFilter(filter);		
				
		if( dialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION )
		{
			File file = dialog.getSelectedFile();
			
			String ext = CommonFileFilter.getExtension(file);
			if( ext == null )
			{
				int idx=-1;
				for(int i=0; idx<dialog.getChoosableFileFilters().length; ++i)
				{
					if( dialog.getChoosableFileFilters()[i] == dialog.getFileFilter() )
					{
						idx = i;
						break;
					}
				}
				if( idx >= 0 )
				{
					switch(idx)
					{
						case 0:
							ext = "jpg";
							break;
						case 1:
							ext = "svg";
							break;
						default:
							ext = "jpg";
					}
					
				}
				else
				{
					ext = "jpg";
				}
				file = new File( file.getAbsolutePath() + "." + ext ); 
			}
			
			if( file.exists() )
			{
				// overwrite file??
				int res = JOptionPane.showConfirmDialog(this,"File '"+ file.getName().toString() +"'already exists! Do you want to replace the existing file?");
				if( res != JOptionPane.OK_OPTION )
					return;
			}
			
			// open output stream
			FileOutputStream os;
			String path = file.getAbsolutePath();
			try 
			{
				os = new FileOutputStream(path);
			}
			catch(FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(	this,
												"Cannot open file\r\n"+path,
												"Error",
												JOptionPane.ERROR_MESSAGE);
				return;
			}
			if( os == null )
			{
				JOptionPane.showMessageDialog(	this,
						"Cannot open file\r\n"+path,
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		
			if( ext.compareToIgnoreCase("jpg") == 0 || ext.compareToIgnoreCase("jpeg") == 0 )
			{ // save as JPEG
				BufferedImage image = new BufferedImage(venn.getWidth(),venn.getHeight(),
														BufferedImage.TYPE_3BYTE_BGR);
											
				Graphics g = image.getGraphics();
				g.setColor(Color.WHITE); 
				g.fillRect(0,0,image.getWidth(),image.getHeight());
		
				venn.directPaint( g, new Dimension(image.getWidth(),image.getHeight()) );
											
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
				
				try
				{
					JPEGEncodeParam jpegParam = JPEGCodec.getDefaultJPEGEncodeParam(image);
					jpegParam.setQuality(1.0f,false);
					encoder.encode(image,jpegParam);
					os.close();
				}
				catch(IOException e)
				{
					JOptionPane.showMessageDialog(	this, 
													"Error while writing file\r\n"+path,
													"Error",
													JOptionPane.ERROR_MESSAGE	);
					return;					
				}
			}
			else
			{ // write SVG file

                try {
                    //writeSVGFile(os,venn.getWidth(),venn.getHeight());
                    writeSVGFile(os,400,400);
                    os.close();
                }
                catch (UnsupportedEncodingException e) 
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(  this,
                            "Cannot write file \r\n"+path,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    
                    return;
                }                
                catch (SVGGraphics2DIOException e) 
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(  this,
                            "Error while creating SVG file\r\n"+path+"\r\n"+
                            e.getLocalizedMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);                 
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(  this,
                            "Error while creating SVG file\r\n"+path+"\r\n"+
                            e.getLocalizedMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);                                     
                }
			}
		}
	}
	
    /**
     * Exports the error function profile to an ASCII file
     *
     */
    protected void actionExportProfile()
    {
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;
        
        dialog.setAcceptAllFileFilterUsed(false);
        
        filter = new CommonFileFilter("Text File (.txt)");
        filter.addExtension("txt");
        
        dialog.addChoosableFileFilter(filter);
                        
        if( dialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION )
        {
            return;
        }
        
        File file = dialog.getSelectedFile();        
        if( file.exists() )
        {
            // overwrite file??
            int res = JOptionPane.showConfirmDialog(this,"File '"+ file.getName().toString() +"'already exists! Do you want to replace the existing file?");
            if( res != JOptionPane.OK_OPTION )
                return;
        }
            
        // open output stream
        FileWriter os = null;

        try 
        {
            os = new FileWriter(file);
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(  this,
                                            "Cannot open file\r\n"+file.getAbsolutePath(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  this,
                    "Cannot open file\r\n"+file.getAbsolutePath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
        }
        if( os == null )
        {
            JOptionPane.showMessageDialog(  this,
                    "Cannot open file\r\n"+file.getAbsolutePath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            writeProfile(os);
            os.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            JOptionPane.showConfirmDialog( this, "I/O error while saving file "+file.getAbsolutePath());
        }        
    }
    
    /**
     * Writes the profiles for all generations to a file.
     * @param os
     * @throws IOException 
     */
    public void writeProfile(OutputStreamWriter os) throws IOException
    {
        if( ! venn.hasData() )
            return ;
        
        Assert.assertNotNull( errFunc );
        Assert.assertNotNull( optim );
        
        IVennDiagramView[] views = venn.getViews();
       
        for( int i=0; i<errFunc.length; ++i )
        {
            os.write("SUB-PROBLEM "+i+"\n");
            
            // output group names
            IVennDataModel data = views[i].getArrangement().getDataModel();
            
            for( int j=0; j<data.getNumGroups(); ++j )
            {
                os.write("GROUP["+j+"] ");
                os.write(data.getGroupName(j));
                os.write("\n");
            }
            
            double[] opt = optim[i].getOptimum();
            Assert.assertNotNull( opt );
            
            os.write("cost = "+(-optim[i].getValue()));
            
            errFunc[i].setInput( opt );
            
            os.write("\n\nPROFILE:\n");
            errFunc[i].writeProfile( os );
            os.write("\n\n");
        }
    }
        
    
	protected void actionFileExit()
	{
		setVisible(false);
		dispose();		
	}
    
    private void  setSimulating( boolean sim )
    {
        if( isVisible() )
        {
            menuFile.setEnabled(!sim);
            menuItemOptimize.setEnabled(!sim);
            menuItemResume.setEnabled(!sim);
            menuItemStop.setEnabled(sim);
            menuItemOptions.setEnabled(!sim);
            
            if( progressBar != null )
            {
                progressBar.setBackground( Color.BLACK );
                progressPanel.setVisible( sim );
            }            
        }
    }
	
	protected void actionOptionsOptimize()
	{		
        if( isSimulating() )
            return;
		optimize();
	}

    protected void actionOptionsResume()
    {
        if( isSimulating() )
            return;
        
        resume();
    }
    
    /**
     * 
     * @return True if the optimizer is working
     */
    protected boolean isSimulating()
    {
        if( worker==null ) 
            return false;
        
        return worker.isRunning();
    }
    
    private synchronized OptimizerWorker newWorker()
    {
        Assert.assertFalse( isSimulating() );
        
        // remove old worker
        if( worker != null )
        {
            worker.removeActionListener( this );
            worker = null;
            if( progressBar != null )
                progressBar.setModel(null);
        }
       
        // create new worker
        worker = new OptimizerWorker();
        worker.addActionListener( this );
        
        if( progressBar != null )
        {
            progressBar.setModel( worker.getModel() );
        }
        
        // attach optimizers
        Assert.assertNotNull( optim );
        for( int i=0; i<optim.length; ++i )
        {
            worker.addOptimizer( optim[i] );
        }
        
        return worker;
    }
	
	/**
	 * Starts the optimization process.
	 *
	 */
	private void optimize()
	{
        Assert.assertFalse( isSimulating() );
        Assert.assertNotNull( random );
        
        if( !hasData() )
            return;
        
        categoryTable.invalidate();
        repaint();
        
        setSimulating(true);
        
        // set random seed
        if( params.randomSeed>0 )
        {
            random.setSeed( params.randomSeed );
        }
        else
        {
            random.setSeed( System.currentTimeMillis() );
        }
        
        VennArrangement[] arr = venn.getArrangements();
        Assert.assertNotNull(arr);
        
        errFunc = new VennErrorFunction[arr.length];
        optim = new IOptimizer[arr.length];
        
        for( int i=0; i<arr.length; ++i )
        {
        		// the following might be replaced with a factory method
            errFunc[i] = new VennErrorFunction( new VennArrangement(arr[i]), params.errorFunction );
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
                    
                default:
                    Assert.fail("illegal optimizer");
            }
            
            optim[i].setID( i );
            optim[i].addObserver( this );
            
            Iterator iter = observers.iterator();
            while( iter.hasNext() )
            {
            		optim[i].addObserver( (IOptimizerObserver)iter.next() );
            }
            
        }
        
        newWorker().start();
	}
    
    public boolean hasData() 
    {
        if( sourceDataModel == null || sourceDataModel.getNumGroups() == 0 )
            return false;
        
        return true;
    }


    private void resume()
    {
        if( errFunc == null || optim == null )
            return;
        
        if( !hasData() )
            return;
        
        setSimulating(true);
        for( int i=0; i<optim.length; ++i )
        {
            optim[i].reset();
        }
        
        newWorker().start();
    }
	
    /**
     * End of optimization (interrupted or finished)
     *
     */
	protected void actionStopped()
	{
        if( errFunc != null )
        {
            for( int i=0; i<errFunc.length; ++i )
            {
                double[] opt = optim[i].getOptimum();
                if( opt != null )
                {
                    errFunc[i].setInput( opt );
                    venn.getArrangements()[i].assignState( errFunc[i].getArrangement() );
                }
            }
            venn.invalidateView();
        }
        //
        
        setSimulating(false);
	}

	/**
	 * Abort the simulation process (if it is running)
	 *
	 */
	protected void actionStop()
	{
		if( worker != null )
		{
            progressBar.setBackground(Color.ORANGE);
			worker.interrupt();
		}
		else
		{
			actionStopped();
		}
	}
	
	protected void actionOptionsOptions()
	{
        if( isSimulating() )
            return;
        
		ParameterDialog dialog = new ParameterDialog(this);
		dialog.setParameters((AllParameters)params.clone());
		
		dialog.setVisible(true);
		if( dialog.getState() == ParameterDialog.OK_OPTION )
		{
			params = dialog.getParameters();
			//System.out.println(params.mutationParameters);
			setParameters(params);
			actionOptionsOptimize();
		}
	}
    
    protected void actionOptionsSaveOptions()
    {
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;
        
        dialog.setAcceptAllFileFilterUsed(false);
        
        filter = new CommonFileFilter("VennMaster Options (.xml)");
        filter.addExtension("xml");
        
        dialog.addChoosableFileFilter(filter);
                        
        if( dialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION )
        {
            return;
        }
        
        File file = dialog.getSelectedFile();
        String ext = CommonFileFilter.getExtension(file);
        if( ext == null || ext.length() == 0 )
        {
            file = new File(file.getAbsolutePath()+".xml");
        }
                
        if( file.exists() )
        {
            // overwrite file??
            int res = JOptionPane.showConfirmDialog(this,"File '"+ file.getName().toString() +"'already exists! Do you want to replace the existing file?");
            if( res != JOptionPane.OK_OPTION )
                return;
        }

        // open output stream

        FileWriter fs = null;
        try 
        {
            fs = new FileWriter(file);
            if( fs == null )
            {
                JOptionPane.showMessageDialog(  this,
                        "Cannot open file\r\n"+file.getAbsolutePath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(fs));
            //XMLEncoder os = new XMLEncoder(new BufferedOutputStream(fs));
            //           
            XStream xstream = new XStream();
            ObjectOutputStream os = xstream.createObjectOutputStream(fs,"doc");
            
            os.writeObject(venn.getParameters());
            
            os.close();
            fs.close();
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(  this,
                                            "Cannot open file\r\n"+file.getAbsolutePath(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            return;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  this,
                    "I/O error while opening file\r\n"+file.getAbsolutePath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);            
        }
    }
    
	/**
	 * Loads VennMaster options from a file. 
     *
	 */
    protected void actionOptionsLoadOptions()
    {
        JFileChooser dialog = new JFileChooser();
        CommonFileFilter filter;
        
        dialog.setAcceptAllFileFilterUsed(false);
        
        filter = new CommonFileFilter("VennMaster Options (.xml)");
        filter.addExtension("xml");
        
        dialog.addChoosableFileFilter(filter);
                        
        if( dialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION )
        {
            return;
        }
        
        File file = dialog.getSelectedFile(); 
        if( ! file.exists() )
        {
            JOptionPane.showConfirmDialog(this,"The chosen file does not exist!");
            return;
        }
        
        if( ! file.canRead() )
        {
            JOptionPane.showConfirmDialog(this,"The file '"+file.getPath()+"' has no read privileges!");
            return;
        }
                    
        // open input stream
        FileReader fs = null;

        try 
        {
            fs = new FileReader(file);
            if( fs == null )
            {
                JOptionPane.showMessageDialog(  this,
                        "Cannot open file\r\n"+file.getPath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // ObjectInputStream os = new ObjectInputStream(fs);
            //XMLDecoder os = new XMLDecoder(new BufferedInputStream(fs));
            
            
            AllParameters param = null;

            XStream xstream = new XStream();
            ObjectInputStream os = xstream.createObjectInputStream(fs);
            
            param = (AllParameters)os.readObject();
            
            os.close();
            fs.close();
            
            
            if( param != null )
            {
                setParameters(param);
            }
            else
            {
                JOptionPane.showMessageDialog(  this,
                        "Cannot read configuration from file\r\n"+file.getPath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            os.close();
            fs.close();
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(  this,
                                            "Cannot open file\r\n"+file.getPath(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            return;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  this,
                    "I/O error while opening file\r\n"+file.getPath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
        }
        catch (ClassNotFoundException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(  this,
                    "Wrong data format of file \r\n"+file.getPath(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
        }
    }
    
	private void setParameters(AllParameters param) 
    {
        this.params = param;
        
        if( venn != null )
            venn.setParameters( param );
    }


    /**
	 * Finds an image resource with the given path.
	 * @param name Path to the image resource.
	 * @return The image resource.
	 */
	public java.awt.Image getImageResource(String name)
	{
		Image img = null;
		try
		{
			java.net.URL url = getClass().getResource(name);
			if( url == null )
			{
				System.err.println("cannot find resource name '"+name+"'");
				return null;
			}
			img = getToolkit().createImage(url);
			if( img == null )
			{
				System.err.println("cannot create image with url '"+url.toString()+"'");
				return null;
			}
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img,0);
			try
			{
				mt.waitForAll();
			}
			catch(InterruptedException e)
			{
				//
			}
		}
		catch(Exception e)
		{
			System.err.println("MainApp.getImageResource '"+name+"' " + e);
		}
		return img;
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		switch( e.getKeyCode() )
		{
			case KeyEvent.VK_ESCAPE:
				actionStop();
				break;
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
	
    /**
     *  Updates the info area.
     *
     */
	private void updateInfoArea()
	{
        isUpdating = true;
        
        try {
    		// globalInfo
    		StringBuffer buffer = new StringBuffer();
           
    		buffer.append("memory = " + (Runtime.getRuntime().freeMemory()/1024)+" kB\n");
    		if( fileName != null )
    		{
    			buffer.append("["+fileName+"]\n");
    		}
    		
    		buffer.append(venn.getGlobalInfo());
            
            if( currentFilter != null )
            {
                buffer.append("\nFilter settings:\n");
                buffer.append(currentFilter.toString());
            }
    		globalInfo.setText(buffer.toString());
    		globalInfo.moveCaretPosition(0);
    	
    		//selectionInfo.setText( venn.getSelectionInfo() );
    		//selectionInfo.moveCaretPosition(0);
            //selectionInfo.select(0,0);
            DefaultListModel listModel = (DefaultListModel)selectionInfo.getModel();
            
            String[] str = venn.getSelectionInfo().split("\n");
            listModel.clear();
            for( int i=0; i<str.length; ++i )
            {
                listModel.addElement(str[i]);
            }
    		
    		inconsistencyInfo.setText(venn.getInconsistencies());
    		inconsistencyInfo.moveCaretPosition(0);
    		
            
            // update cost values
            IVennDiagramView[] views = venn.getViews();
            if( views != null )
            {
                for( int i=0; i<views.length; ++i )
                {
                    VennErrorFunction errf = new VennErrorFunction( views[i].getTree(), params.errorFunction );
                    
                    DecimalFormat format = new DecimalFormat("0.000");
                    views[i].setInfoText("cost = "+format.format(-errf.getOutput()));
                }
            }
        }
        finally {
            isUpdating = false;
        }
    }
    
    private void updateCategoryTableSelection()
    {       
        // After adding a listener to the table selection model a
        // valueChanged event is fired. So the flag "isTableChanging" is
        // removed in the valueChanged() method of VennMaster.
        isTableChanging = true;
        categoryTable.getSelectionModel().removeListSelectionListener(this);
        
        try {
            categoryTable.getSelectionModel().setValueIsAdjusting(true);            
            BitSet bsel = venn.getSelection();
            categoryTable.clearSelection();
            categoryTable.setColumnSelectionInterval(0,categoryTable.getColumnCount()-1);
            for( int i=bsel.nextSetBit(0); i>=0; i=bsel.nextSetBit(i+1) )
            {
                categoryTable.addRowSelectionInterval(i,i);
            }            
            categoryTable.getSelectionModel().setValueIsAdjusting(false);
        }
        finally {
            categoryTable.getSelectionModel().addListSelectionListener(this);
        }        
    }
	
	/**
	 * Updates the info area fields. Will be called if a view changed its selection.
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event)
	{
		if( event.getSource() == venn )
		{
			updateInfoArea();
            updateCategoryTableSelection();
            return;
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent arg0)
	{

	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent arg0)
	{

		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent arg0)
	{
		//splitter.setDividerLocation(0.8);
        splitter.setDividerLocation(getHeight()-350);
	}


    /**
     * Category table changed its selection.
     */
	public void valueChanged(ListSelectionEvent e) 
    {
        if( e.getSource() == categoryTable.getSelectionModel() )
        {
            if( isUpdating || isTableChanging || e.getValueIsAdjusting() )
            {
                isTableChanging = false;
                return;
            }
            BitSet set = new BitSet();
            int[] rows = categoryTable.getSelectedRows();
            for( int i=0; i<rows.length; ++i )
            {
                set.set( rows[i] );
            }
            venn.setSelection( set );
            updateInfoArea();
            return;
        }
    }
  
      
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent arg0)
	{
		
	}
	
   	    
  

    /**
     * Command line version of VennMaster.
     * 
     * @param args The command line arguments.
     */
    private void parserArguments(String[] args)
    {
        ArgParser parser = new ArgParser("java -jar venn.jar <arguments>");
        
        AllParameters params = null;
        IDataFilter  filter = null;
        
        BooleanHolder   versionOpt = new BooleanHolder();
        
        StringHolder    configFile = new StringHolder(),
                        outConfigFile = new StringHolder(),
                        listFile = new StringHolder(),
                        gceFile = new StringHolder(),
                        seFile = new StringHolder(),
                        htGceFile = new StringHolder(),
                        filterFile = new StringHolder(),
                        optStateFile = new StringHolder(),
                        outFilterFile = new StringHolder(),
                        svgFile = new StringHolder(),
                        simFile = new StringHolder(),
                        profFile = new StringHolder();
     
        // create the parser and specify the allowed options ...
        parser.addOption("--version,-v %v # show VennMaster version", versionOpt);
        parser.addOption("--cfg %s #input configuration file", configFile); 
        parser.addOption("--ocfg %s #output configuration file", outConfigFile);
        parser.addOption("--list %s #list input file", listFile);
        parser.addOption("--gce %s #GoMiner gene-category export file", gceFile);
        parser.addOption("--se %s #GoMiner summary export file", seFile);
        parser.addOption("--htgce %s #High-Throughput GoMiner gce file", htGceFile);
        parser.addOption("--filter %s #gene filter for GoMinor import.", filterFile);
        parser.addOption("--ofilter %s #output filter file", outFilterFile);
        parser.addOption("--optstate %s #output of the optimizer state",optStateFile);
        parser.addOption("--svg %s #generates an SVG file of the Venn diagram", svgFile);
        parser.addOption("--sim %s #output of the simulation profile",simFile);
        parser.addOption("--prof %s #output of the final error profile",profFile);
        
        parser.matchAllArgs(args);
        
        System.out.println( "VennMaster version "+
                            VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_SUB+"  ("+VERSION_DATE+")");
        if( versionOpt.value )
        {
            System.exit(0);
        }
        
        
        // LOAD CONFIG FILE
        if( configFile.value != null )
        {   // load parameters from file
            params = (AllParameters)SystemUtility.readXMLObject(new File(configFile.value));
            if( params == null )
                System.exit(-1);
            params.check();
        }
        else
        {   // default parameter set
            params = new AllParameters();
        }
        setParameters(params);
        
        if( outConfigFile.value != null )
        {
            SystemUtility.writeXMLObject(params,new File(outConfigFile.value));
        }
        
        
        // LOAD DATA       
        try
        {
            boolean flag = false;
            
//          LIST FILE IMPORT
            if( listFile.value != null )
            {   
                if( gceFile.value != null || seFile.value != null )
                {
                    System.err.println("--list option excludes the use of --gce and --se");
                    System.exit(-1);
                }
                if( filterFile.value != null )
                {
                    System.err.println("--filter cannot be used with --list");
                    System.exit(-1);
                }
                sourceDataModel = loadFromListFile(listFile.value);
                venn.setDataModel(sourceDataModel);
                
                flag = true;
            }
            
//          GOMINER FILE IMPORT
            if( !flag && (gceFile.value != null) && (seFile.value != null) )
            {   
                if( filterFile.value != null )
                {
                    filter = (IDataFilter)SystemUtility.readXMLObject(new File(filterFile.value));
                    if( filter == null )
                    {
                        System.err.println("Error while loading gene filter from file "+filterFile.value);
                        System.exit(-1);
                    }    
                    if( filter instanceof GODistanceFilter )
                    {
                    	((GODistanceFilter)filter).setGoTree( goTree );
                    }
                }
                else
                {
                    filter = new GODistanceFilter( goTree );
                }
                
                if( outFilterFile.value != null )
                {   // write gene filter to xml file
                    if( ! SystemUtility.writeXMLObject(filter, new File(outFilterFile.value)) )
                        System.err.println("Warning: cannot write output filter file "+outFilterFile.value);
                }
                sourceDataModel = loadGOMiner(seFile.value,gceFile.value,null);                
                filteredDataModel = new VennFilteredDataModel( sourceDataModel, filter );
                venn.setDataModel( filteredDataModel );
                flag = true;
            }
            
//          HIGH-THROUGHPUT GOMINER FILE IMPORT
            if( !flag && (htGceFile.value != null) )
            {   
                if( filterFile.value != null )
                {
                    filter = (IDataFilter)SystemUtility.readXMLObject(new File(filterFile.value));
                    if( filter == null )
                    {
                        System.err.println("Error while loading gene filter from file "+filterFile.value);
                        System.exit(-1);
                    }
                    if( filter instanceof GODistanceFilter )
                    {
                    	((GODistanceFilter)filter).setGoTree( goTree );
                    }                    
                }
                else
                {
                    filter = new GODistanceFilter(goTree);
                }
                
                if( outFilterFile.value != null )
                {   // write gene filter to xml file
                    if( ! SystemUtility.writeXMLObject(filter, new File(outFilterFile.value)) )
                        System.err.println("Warning: cannot write output filter file "+outFilterFile.value);
                }
                sourceDataModel = loadHTGOMiner(htGceFile.value,null);                
                filteredDataModel = new VennFilteredDataModel(sourceDataModel, filter );
                venn.setDataModel(filteredDataModel);
                
                flag = true;
            }
            
            
            
            if( !flag )
            {
                System.err.println("You have to specify input data with --gce and --se, --htgce, or --list");
                System.exit(-1);
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            System.err.println("I/O error while importing data set.");
            System.exit(-1);
        }
        
        clearObservers();
        
        if( simFile.value != null )
        {
             try {
                Writer writer = new FileWriter(simFile.value);
                addObserver( new OptimizerObserver(writer) );
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while opening file "+simFile.value);
                System.exit(-1);                
            }
        }
        
        if( optStateFile.value != null )
        {
            try {
                Writer writer = new FileWriter(optStateFile.value);
                addObserver( new StateObserver(writer) );
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while opening file "+optStateFile.value);
                System.exit(-1);                
            }
        }
        
        // START SIMULATION
        optimize();
        
        // wait until simulation stopped
        try 
        {
            worker.getThread().join();
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        // stop reporter
        clearObservers();
        
        // EXPORT PROFILE
        if( profFile.value != null )
        {
            try {
                FileWriter fs = new FileWriter(profFile.value);
                writeProfile(fs);
                fs.close();                
            }
            catch (IOException e) 
            {
                e.printStackTrace();
                System.err.println("I/O error while exporting profile.");
                System.exit(-1);
            } 
        }
                
        // WRITE SVG
        if( svgFile.value != null )
        {
            try {
                FileOutputStream fs = new FileOutputStream(svgFile.value);
                writeSVGFile(fs,400,400);
                fs.close();
            } 
            catch (FileNotFoundException e) 
            {
                e.printStackTrace();
                System.err.println("File not found error while exporting SVG.");
                System.exit(-1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.err.println("Unsupported encoding exception while exporting SVG.");
                System.exit(-1);                
            } catch (SVGGraphics2DIOException e) {
                e.printStackTrace();
                System.err.println("SVG Graphics 2D I/O exception while exporting SVG.");
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("I/O error while exporting SVG.");
                System.exit(-1);                
            }
        }
    }
    
    public void clearObservers()
    {
    		Iterator iter = observers.iterator();
    		while( iter.hasNext() )
    		{
    			IOptimizerObserver obs = (IOptimizerObserver)iter.next();
    			obs.close();
    		}
    		observers.clear();
    }
    
    public void addObserver( IOptimizerObserver observer ) 
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
     * 
     * @param fileName
     */
    public IVennDataModel loadFromListFile(String fileName)
        throws IOException, FileFormatException
    {
        ListReaderModel reader = new ListReaderModel();
        reader.loadFromFile(fileName);
        return reader;
    }
    

    /**
     * Imports a GoMiner data file pair.
     * @param groupFile
     * @param geneList
     * @param filter
     * @throws IOException
     * @throws FileFormatException
     */
    protected IVennDataModel loadGOMiner(String groupFile, String geneList, IGeneFilter filter )
    throws IOException, FileFormatException
    {
        GeneOntologyReaderModel gor = new GeneOntologyReaderModel();
        gor.loadFromFile(groupFile,geneList,filter);
        return gor;
    }
    
    /**
     * Load a high-throughput GOMiner file
     * @param groupFile
     * @param filter
     * @throws IOException
     * @throws FileFormatException
     */
    protected IVennDataModel loadHTGOMiner(String groupFile, IGeneFilter filter )
    throws IOException, FileFormatException
    {
        HTGeneOntologyReaderModel gor = new HTGeneOntologyReaderModel();
        gor.loadFromFile(groupFile,filter);
        return gor;
    }
    
    
   

    /**
     * Every optimizer calls this function. Updates the view from time to time.
     */
    public void notifyOptimization( IOptimizer source ) 
    {        
        if( !isVisible() )
            return;
        
        if( params.updateInterval <= 0 )
            return;
        
        if( source.getProgress() % params.updateInterval == 0 )
        {
            int id = source.getID();
            errFunc[id].setInput( source.getOptimum() );
            venn.getArrangements()[id].assignState( errFunc[id].getArrangement() );
            venn.invalidateView();
        }
    }

    // callback if an optimizer has ended.
    public void finished(IOptimizer source) 
    {
        
    }
   
    
    public static void setLookAndFeel()
    {        
        // switch to windows look and feel
        //String plaf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        try {
            // UIManager.setLookAndFeel(plaf);
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
            //SwingUtilities.updateComponentTreeUI(frame);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
 
    public static void createAndShowGUI()
    {
        VennMaster frame = new VennMaster();
        frame.loadGoDB();
        frame.setSize(400,650);
        frame.setVisible(true);
    }
    
    public static void main(String[] args)
    {
        // parse arguments
        if( args.length > 0 )
        {   // command line version
        		VennMaster venn = new VennMaster();
        		venn.loadGoDB();
        		venn.parserArguments(args);
        }
        else
        {   // start graphical interface
            setLookAndFeel();
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    createAndShowGUI();
                }
            });
        }
    }


	private void loadGoDB() 
	{
		// TODO Auto-generated method stub
		String name = "data/obo.out";
		InputStream stream = getClass().getResourceAsStream(name);
		/*
		if( stream == null )
		{
			name = "data/seq_gene.md.gz";
			stream = getClass().getResourceAsStream(name);
			if( stream != null )
			{
			    stream = new GZIPInputStream(stream);
			}
		}	
		*/
		if( stream != null )
		{
			try {
				goTree.read( new InputStreamReader(stream) );
				System.out.println("goTree loaded from file '"+name+"'");
			} catch (FileFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
		{
			System.err.println("error: cannot load GOTree");
		}
	}


	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
