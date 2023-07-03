package com.conti.frontEnd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.conti.application.GCModuleExtractorApplication;

import com.conti.pojo.ConfigDetailsPojo;

@SuppressWarnings("serial")
public class GCModuleExtractorGUI  extends JFrame implements ActionListener {

	JButton b1;
	JProgressBar jb;
	JPanel newPanel,buttonPanel;
	JLabel userLabel, passLabel, GCUrlLabel;
	JTextField textField1, textField2, textField3;
	
	public GCModuleExtractorGUI()
	{
		JPanel gui = new JPanel(new BorderLayout(5, 5));
		gui.setBorder(new EmptyBorder(4, 4, 4, 4));
		gui.setBackground(new Color(204, 204, 255));
		
		userLabel = new JLabel();
		userLabel.setText("Username:");
		textField1 = new JTextField(15);

		passLabel = new JLabel();
		passLabel.setText("Password:");
		textField2 = new JPasswordField(15);

		GCUrlLabel = new JLabel();
		GCUrlLabel.setText("GC URL:");
		textField3 = new JTextField(150);
		
		b1 = new JButton("Extract RM module");
		b1.setBackground(new Color(255, 204, 153));
		
		jb=new JProgressBar(); 
		jb.setString("Exporting RM module details from this GC.....");
		jb.setStringPainted(true);
		jb.setFocusable(false); 
		jb.setVisible(false);
		
		newPanel = new JPanel(new GridLayout(3, 1));
		

		newPanel.add(userLabel); // set username label to panel
		newPanel.add(textField1); // set text field to panel
		newPanel.add(passLabel); // set password label to panel
		newPanel.add(textField2); // set text field to panel
		newPanel.add(GCUrlLabel); // set password label to panel
		newPanel.add(textField3);
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttonPanel.setPreferredSize(new Dimension(100, 100));
		buttonPanel.setLayout(new GridBagLayout());
				
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.LAST_LINE_END;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		buttonPanel.add(b1,gbc);
		buttonPanel.add(jb,gbc2);
		
		gui.add(newPanel);
		gui.add(buttonPanel, BorderLayout.PAGE_END);
		this.setContentPane(gui);
		
		b1.addActionListener(this);

		setTitle("GC Module Extractor Application V1.0");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		ConfigDetailsPojo configDetailsPojo = new ConfigDetailsPojo();
		// TODO Auto-generated method stub
		if (textField1.getText().equals("")|| textField2.getText().equals("") || textField3.getText().equals("") ) {
			JOptionPane.showMessageDialog(null, "Please enter all the fields");
		
		}
		else
		{
		b1.setVisible(false);
		jb.setVisible(true);
		jb.setIndeterminate(true);
		String userValue = textField1.getText(); // get user entered username from the textField1
		String passValue = textField2.getText();
		String GCUrl = textField3.getText();
		configDetailsPojo.setUserName(userValue);
		configDetailsPojo.setPassword(passValue);
		configDetailsPojo.setRepositoryUrl(GCUrl);
		GCModuleExtractorApplication.loadConfigProperties(configDetailsPojo);
		
		 new Thread(new Runnable() {
	            @Override
	            public void run() {
	             
	            	final Boolean iscompleted= GCModuleExtractorApplication.GCmoduleExtract();
	                
	                    SwingUtilities.invokeLater(new Runnable() {
	                        @Override
	                        public void run() {
	                        	
	            				if(iscompleted)
	            				{
	            					jb.setIndeterminate(false);
	            					jb.setVisible(false);
	            					b1.setVisible(true);
	            					JOptionPane.showMessageDialog(null, "GC RM module Details Exported");
	            				}
	            				else {
	            					
	            					jb.setVisible(false);
	            					b1.setVisible(true);
	            					JOptionPane.showMessageDialog(null, "GC RM module Details completed with errors. Please check the logs!!");
	            					
	            				}
	                           
	                        }
	                    });
	                   
	            }
	        }).start();
		}
		
	}
	
	public static void main(String[] args) {
		try
		{
			GCModuleExtractorGUI form = new GCModuleExtractorGUI();
		form.setBounds(350, 150, 450, 300); // set size of the frame
		form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		form.setVisible(true);
		form.setResizable(false);
		
		}
		catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	
	}

}
