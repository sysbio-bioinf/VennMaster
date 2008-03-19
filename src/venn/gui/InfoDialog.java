package venn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import venn.utility.SystemUtility;
/**
 * @author mueller
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InfoDialog extends JDialog
implements ActionListener
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Image image;

    public InfoDialog(Frame owner)
	{
		super(owner, "VennMaster Info",true);
        
        image = SystemUtility.getImageResource(this,"images/icon_high2.png");
        
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		JTextArea	area = new JTextArea() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent( Graphics g ) {
                g.setColor(Color.GRAY);
                g.fillRect(0,0,getWidth(),getHeight());
                if( image != null ) {
                    g.drawImage(image,(getWidth()-image.getWidth(null))/2,
                                    (getHeight()-image.getHeight(null))/2,this);
                }
                super.paintComponent(g);
                
            }		        
		};
        area.setOpaque(false);
		area.setEditable(false);
		area.setText("VennMaster "+venn.Constants.VERSION_MAJOR+"."+venn.Constants.VERSION_MINOR+"."+venn.Constants.VERSION_SUB+
                     "  ("+venn.Constants.VERSION_DATE+")\n" +
					 "Developed by Hans A. Kestler and Andre Mueller\n" +
					 "\n"+
					 "This product includes software developed by\n" +
					 "the  Apache Software Foundation  (http://www.apache.org/).\n" +
                     "This software is creative commons (see cc.html).\n" + 
                     "Some rights reserved.");
        area.setForeground( Color.ORANGE );
        area.setFont( area.getFont().deriveFont(Font.BOLD) );
        area.setAutoscrolls(true);
        area.setMargin(new Insets(10,10,10,10));
		cp.add( new JScrollPane(area), BorderLayout.CENTER);
		
		JButton button = new JButton("OK");
		button.addActionListener(this);
		
		cp.add(button,BorderLayout.SOUTH);
		
		setSize(400,200);
	}
    

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		
		if( cmd.equalsIgnoreCase("ok") )
		{
			setVisible(false);
			dispose();
		}
		
	}
}
