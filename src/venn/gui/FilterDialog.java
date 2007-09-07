/*
import java.sql.*;

public class DemoDriver
{
  public static void main(String[] args)
  {
    try
    {
      // load the driver into memory
      Class.forName("org.relique.jdbc.csv.CsvDriver");

      // create a connection. The first command line parameter is assumed to
      //  be the directory in which the .csv files are held
      Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + args[0] );

      // create a Statement object to execute the query with
      Statement stmt = conn.createStatement();

      // Select the ID and NAME columns from sample.csv
      ResultSet results = stmt.executeQuery("SELECT ID,NAME FROM sample");

      // dump out the results
      while (results.next())
      {
        System.out.println("ID= " + results.getString("ID") + "   NAME= " + results.getString("NAME"));
      }

      // clean up
      results.close();
      stmt.close();
      conn.close();
    }
    catch(Exception e)
    {
      System.out.println("Oops-> " + e);
    }
  }
}
 */
package venn.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.*;
import java.util.*;

import javax.swing.*;

import venn.db.EasyGeneFilter;


/**
 * @author muellera
 */
public class FilterDialog extends JDialog
implements java.awt.event.ActionListener, KeyListener, PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    public static final int INVALID = 0,
                            OK_OPTION = 1,
                            CANCEL_OPTION = 2;
    private int state;

    private JFormattedTextField minChange,
                                maxChange,
                                maxPValue;
    
    private boolean			pValueMode; // true: pValue, false:
    
    private LinkedList<JComponent>          fields;
    
    private boolean             initialized;
    private boolean checking;

    public FilterDialog(Frame owner)
    {
        super(owner,"Category Filter",true);
        initialized = false;
        state = INVALID;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        getContentPane().setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        
        panel.setLayout(new GridLayout(3,2));
        
        fields = new LinkedList<JComponent>();
        
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
        pValueMode = false;
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
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //FilterDialog frame = new FilterDialog(null);
        
        //frame.setVisible(true);
        
        try
        {
          // load the driver into memory
          Class.forName("org.relique.jdbc.csv.CsvDriver");

          // create a connection. The first command line parameter is assumed to
          //  be the directory in which the .csv files are held
          
          Properties props = new java.util.Properties();

          props.put("separator","\t");              // separator is a bar
          props.put("suppressHeaders","true");     // first line contains data
          props.put("fileExtension",".gce");       // file extension is .txt
          // props.put("charset","ISO-8859-2");
          
          Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + "S:\\data\\vennm",props);
          
         // => FilteredRowSet

          // conn.setTypeMap
          
          // HT GoMiner file format
          // category gene nTotal nChange enrichment pValue nCat CRMean FDR
          // String String Integer Integer Float Float Integer Float Float
          // GO:0006955_immune_response CD163   261 15  3.754789    -5.597408   1   0.0 0.000000
         
          // create a Statement object to execute the query with
          Statement stmt = conn.createStatement();

                 // Select the ID and NAME columns from sample.csv
          String table = "HT P11 change"; // "P11changed.txt.change";
          ResultSet results = stmt.executeQuery("SELECT * FROM "+table);
          
          
          
          ResultSetMetaData dat = results.getMetaData();
          
          
          // dat.getAttributes(table,)
          System.out.println("columns = "+dat.getColumnCount());
          
          for(int i=1; i<=dat.getColumnCount(); ++i )
          {
              System.out.println(i + ":" +
                             dat.getColumnName(i)+" "+
                             dat.getColumnTypeName(i));
          }

          // dump out the results
          while (results.next())
          {
              //System.out.println(results.getObject(1));
            //System.out.println("ID= " + results.getString("ID") + "   NAME= " + results.getString("NAME"));
          }

          // clean up
          results.close();
       
          stmt.close();
          
          
          conn.close();
        }
        catch(Exception e)
        {
          System.out.println("Oops-> " + e);
        }

        
        
        Enumeration E = DriverManager.getDrivers();
        System.out.println("JDBC Drivers");
        while( E.hasMoreElements() )
        {
            Driver driver = (Driver)E.nextElement();
            System.out.println(":" + driver.getClass().getName());
        }
        
    }
}
