package com.conti.application.frontEnd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.conti.application.main.DeleteAttributeApplication;
import com.conti.pojo.AttributeDetailsPojo;
import com.conti.pojo.ConfigDetailsPojo;


@SuppressWarnings("serial")
public class DeleteAttributeGUI extends JFrame implements ActionListener {

	JButton b1, b2, nextButton1, nextButton2, updatewf, updateboth;
	JRadioButton rb1, rb2, rb3, rb4;
	final JProgressBar jb, jb1,jb2;
	ButtonGroup group, group1;
	JPanel newPanel, buttonPanel, radioButtonPanel1, attributePanel;
	JLabel userLabel, passLabel, serverUrlLabel, inputFileNameLabel, baselineNameLabel, changeSetNameLabel,
			deliverChangeSetLabel, artifactTypeDetailsLabel;
	DefaultTableModel model;
	JTable attributeTable, workflowTable;
	JTabbedPane tabbedPane;
	final static String Config_PANEL = "Config Details";
	final static String Attribute_PANEL = "Attribute Details";
	final static String Workflow_PANEL = "Workflow Details";

	final JTextField textField1, textField2, textField3, textField4, textField7, textField8;
	JTextArea textField9;

	public DeleteAttributeGUI() {
		tabbedPane = new JTabbedPane();
		JPanel gui = new JPanel(new BorderLayout(5, 5));
		gui.setBorder(new EmptyBorder(4, 4, 4, 4));
		gui.setBackground(new Color(204, 204, 255));

		userLabel = new JLabel();
		userLabel.setText("Username:");
		textField1 = new JTextField(15);

		passLabel = new JLabel();
		passLabel.setText("Password:");
		textField2 = new JPasswordField(15);

		serverUrlLabel = new JLabel();
		serverUrlLabel.setText("ServerURL:");
		textField3 = new JTextField(150);

		inputFileNameLabel = new JLabel();
		inputFileNameLabel.setText("Project Details Input:");
		textField4 = new JTextField(150);
		textField4.setEditable(false);
		// textField4.setVisible(false);
		b2 = new JButton("Select Project Input File");
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField4.setText(inputFileAction());
			}
		});

		nextButton1 = new JButton("Next >>");
		nextButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
			}
		});

		nextButton2 = new JButton("Next >>");
		nextButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
			}
		});

		baselineNameLabel = new JLabel();
		baselineNameLabel.setText("BaselineName:");
		textField7 = new JTextField(150);

		changeSetNameLabel = new JLabel();
		changeSetNameLabel.setText("ChangeSetName:");
		textField8 = new JTextField(150);

		deliverChangeSetLabel = new JLabel();
		deliverChangeSetLabel.setText("Deliver ChangeSet:");
		rb1 = new JRadioButton("True");
		rb1.setActionCommand("True");
		rb2 = new JRadioButton("False");
		rb2.setActionCommand("False");

		group = new ButtonGroup();
		group.add(rb1);
		group.add(rb2);
		group.setSelected(rb2.getModel(), true);

		b1 = new JButton("Delete Attributes");

		updatewf = new JButton("Update Workflow");
		updateboth = new JButton("Delete Attributes & Update Workflow");
		// b1.setBackground(new Color(255, 204, 153));

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		buttonPanel.setBackground(new Color(229, 255, 204));
		// buttonPanel.setPreferredSize(new Dimension(100, 100));
		// buttonPanel.setLayout(new GridBagLayout());

		radioButtonPanel1 = new JPanel();
		radioButtonPanel1.add(rb1);
		radioButtonPanel1.add(rb2);

		jb = new JProgressBar();
		jb.setString("Deleting Attributes.....");
		jb.setStringPainted(true);
		jb.setFocusable(false);
		jb.setVisible(false);
		
		jb1 = new JProgressBar();
		jb1.setString("Updating Workflow.....");
		jb1.setStringPainted(true);
		jb1.setFocusable(false);
		jb1.setVisible(false);
		
		jb2 = new JProgressBar();
		jb2.setString("Deleting Attributes & Updating Workflow.....");
		jb2.setStringPainted(true);
		jb2.setFocusable(false);
		jb2.setVisible(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.LAST_LINE_END;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		newPanel = new JPanel(new GridLayout(7, 1));
		newPanel.add(userLabel); // set username label to panel
		newPanel.add(textField1); // set text field to panel
		newPanel.add(passLabel); // set password label to panel
		newPanel.add(textField2); // set text field to panel
		newPanel.add(serverUrlLabel); // set password label to panel
		newPanel.add(textField3);
		newPanel.add(inputFileNameLabel); // set password label to panel
		newPanel.add(textField4);
		newPanel.add(baselineNameLabel); // set password label to panel
		newPanel.add(textField7); // set text field to panel
		newPanel.add(changeSetNameLabel); // set password label to panel
		newPanel.add(textField8);
		newPanel.add(deliverChangeSetLabel);
		newPanel.add(radioButtonPanel1);// set text field to panel
		// newPanel.add(b2);

		buttonPanel.add(b2, gbc2);
		buttonPanel.add(nextButton1, gbc);

		JFrame attributeFrame = setupAttributeDetailsPanel();
		JFrame Workflowframe = setupWorkflowPanel();

		gui.add(newPanel);
		gui.add(buttonPanel, BorderLayout.PAGE_END);

		tabbedPane.addTab(Config_PANEL, gui);
		tabbedPane.addTab(Attribute_PANEL, attributeFrame.getContentPane());
		tabbedPane.add(Workflow_PANEL, Workflowframe.getContentPane());

		this.setContentPane(tabbedPane);

		b1.addActionListener(this);
		updatewf.addActionListener(this);
		updateboth.addActionListener(this);

		setTitle("DELETE ATTRIBUTES & UPDATE WORKFLOW APPLICATION");

	}

	public JFrame setupWorkflowPanel() {
		JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());
		JPanel btnPnl = new JPanel(new BorderLayout());
		JPanel tablePnl = new JPanel();

		JPanel bottombtnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JTable table = createWorkFlowTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 38, 40, 5);
		tablePnl.add(scrollPane);
		tablePnl.setPreferredSize(new Dimension(50, 345));

		bottombtnPnl.add(updatewf);
		bottombtnPnl.add(jb1);
		bottombtnPnl.add(updateboth);
		bottombtnPnl.add(jb2);
		bottombtnPnl.setBackground(new Color(229, 255, 204));

		btnPnl.add(bottombtnPnl, BorderLayout.CENTER);
		btnPnl.setBackground(new Color(229, 255, 204));
		// btnPnl.setPreferredSize(new Dimension(100, 100));
		btnPnl.setLayout(new GridBagLayout());

		// frame.add(table.getTableHeader(), BorderLayout.NORTH);
		frame.add(tablePnl, BorderLayout.NORTH);

		frame.add(btnPnl, BorderLayout.SOUTH);
		return frame;

	}

	public JFrame setupAttributeDetailsPanel() {
		JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());
		JPanel btnPnl = new JPanel(new BorderLayout());
		JPanel tablePnl = new JPanel();
		JPanel artifactTypePnl = new JPanel();
		JPanel bottombtnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JTable table = createAttributeTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 38, 40, 5);
		tablePnl.add(scrollPane);
		tablePnl.setPreferredSize(new Dimension(50, 200));
		artifactTypeDetailsLabel = new JLabel();
		artifactTypeDetailsLabel.setText("Enter the comma separated Artifact Types:");
		textField9 = new JTextArea(5, 30);
		textField9.setLineWrap(true);

		JScrollPane jsp = new JScrollPane(textField9);

		artifactTypePnl.add(artifactTypeDetailsLabel);
		artifactTypePnl.add(jsp);
		artifactTypePnl.setPreferredSize(new Dimension(50, 200));
		// artifactTypePnl.setBorder(new EmptyBorder(4, 4, 4, 4));
		bottombtnPnl.add(b1);
		bottombtnPnl.add(jb);
		bottombtnPnl.add(nextButton2);
		bottombtnPnl.setBackground(new Color(229, 255, 204));

		btnPnl.add(artifactTypePnl);
		btnPnl.add(bottombtnPnl, BorderLayout.CENTER);
		btnPnl.setBackground(new Color(229, 255, 204));
		// btnPnl.setPreferredSize(new Dimension(100, 100));
		btnPnl.setLayout(new GridBagLayout());

		// frame.add(table.getTableHeader(), BorderLayout.NORTH);
		frame.add(tablePnl, BorderLayout.NORTH);
		frame.add(artifactTypePnl, BorderLayout.CENTER);
		frame.add(btnPnl, BorderLayout.SOUTH);
		return frame;
	}

	public JTable createAttributeTable() {

		String[] actionValues = new String[] { "Delete attribute completely", "Remove from artifact type only" };
		JComboBox<?> cb = new JComboBox<Object>(actionValues);

		String[] cols = { "Attribute Name", "Action" };
		model = new DefaultTableModel(cols, 10);
		attributeTable = new JTable(model);
		// table.setBounds(30,40,200,300);
		TableColumn actionColumn = attributeTable.getColumnModel().getColumn(1);
		actionColumn.setCellEditor(new DefaultCellEditor(cb));

		return attributeTable;

	}

	public JTable createWorkFlowTable() {

		//String[] actionValues = new String[] { "Workflow1", "workflow2" };
		//JComboBox<?> cb = new JComboBox<Object>(actionValues);

		String[] cols = { "Artifact Type Name", "Workflow" };
		model = new DefaultTableModel(cols, 20);
		workflowTable = new JTable(model);
		// table.setBounds(30,40,200,300);
		//TableColumn actionColumn = workflowTable.getColumnModel().getColumn(1);
		//actionColumn.setCellEditor(new DefaultCellEditor(cb));

		return workflowTable;

	}

	public String inputFileAction() {
		JFileChooser inputFile = new JFileChooser();
		inputFile.showOpenDialog(this);
		return inputFile.getSelectedFile().getAbsolutePath();

	}

	public AttributeDetailsPojo readAttributePaneDetails() {
		AttributeDetailsPojo attributeDetailsPojo = new AttributeDetailsPojo();

		String artifactTypes = textField9.getText();
		HashMap<String, String> attributeDetailsMap = new HashMap<>();
		ArrayList<String> artifactTypeList = new ArrayList<>();
		String[] artifactTypesArr = artifactTypes.split(",");
		artifactTypeList.addAll(Arrays.asList(artifactTypesArr));

		for (int count = 0; count < attributeTable.getModel().getRowCount(); count++) {
			// String attributeName= attributeTable.getModel().getValueAt(count, 0);
			if (attributeTable.getModel().getValueAt(count, 0) != null) {

				attributeDetailsMap.put(attributeTable.getModel().getValueAt(count, 0).toString(),
						attributeTable.getModel().getValueAt(count, 1).toString());
			} else {

				continue;
			}

		}

		attributeDetailsPojo.setArtifactTypeList(artifactTypeList);
		attributeDetailsPojo.setAttributeDeletionMap(attributeDetailsMap);
		return attributeDetailsPojo;
	}
	
	public AttributeDetailsPojo readWorkFlowPanelDetails()
	{
		AttributeDetailsPojo  attributeDetailsPojo= new AttributeDetailsPojo();
		HashMap<String, String> workFlowDetailsMap = new HashMap<>();
		for (int count = 0; count < workflowTable.getModel().getRowCount(); count++) {
			// String attributeName= attributeTable.getModel().getValueAt(count, 0);
			if (workflowTable.getModel().getValueAt(count, 0) != null) {

				workFlowDetailsMap.put(workflowTable.getModel().getValueAt(count, 0).toString(),
						workflowTable.getModel().getValueAt(count, 1).toString());
			} else {

				continue;
			}

		}
		
		attributeDetailsPojo.setWorkflowDetailsMap(workFlowDetailsMap);
		return attributeDetailsPojo;
	}


	public void actionPerformed(ActionEvent e) {
		
		if (textField1.getText().equals("") || textField2.getText().equals("") || textField3.getText().equals("")
				|| textField4.getText().equals("") || textField7.getText().equals("")
				|| textField8.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Please enter all the Config Details");

		}
		
			
		else if (e.getSource()==updatewf)
         {
			 	
			 AttributeDetailsPojo attributeDetailsPojo = readWorkFlowPanelDetails();
			 
			 if (attributeDetailsPojo.getWorkflowDetailsMap().size() < 1) {
					JOptionPane.showMessageDialog(null, "Please enter the artifact types & workflow that needs to be updated!!");
				}
			 else
			 {
				 	updatewf.setVisible(false);
					updateboth.setVisible(false);
					jb1.setVisible(true);
					jb1.setIndeterminate(true);
					tabbedPane.setEnabledAt(0, false);
				    tabbedPane.setEnabledAt(1, false);
				    workflowTable.setEnabled(false);
					ConfigDetailsPojo configDetailsPojo = setConfigDetails();
					 DeleteAttributeApplication.loadConfigProperties(configDetailsPojo, attributeDetailsPojo, "Update");
						new Thread(new Runnable() {
							@Override
							public void run() {

								final Boolean worklfowCompleted = DeleteAttributeApplication.DeleteAttribute_UpdateWorkflowApplication();

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {

										if (worklfowCompleted) {
											jb1.setIndeterminate(false);
											tabbedPane.setEnabledAt(0, true);
										    tabbedPane.setEnabledAt(1, true);
											jb1.setString("Updating Workflows Completed");
											JOptionPane.showMessageDialog(null, "Updating Workflows Completed");
											
										}

									}
								});

							}
						}).start();
			 }
			
         }
		
		
		else if(e.getSource()==updateboth)
		{
			AttributeDetailsPojo attributeDetailsPojo = readAttributePaneDetails();
			AttributeDetailsPojo attributeDetailsPojo2 = readWorkFlowPanelDetails();
			attributeDetailsPojo.setWorkflowDetailsMap(attributeDetailsPojo2.getWorkflowDetailsMap());
			ConfigDetailsPojo configDetailsPojo = setConfigDetails();
			
			
			 if (attributeDetailsPojo.getAttributeDeletionMap().size() < 1) {
					JOptionPane.showMessageDialog(null, "Please enter the attributes that needs to be deleted!!");
				}

				else if (textField9.getText().equals("")) {
					JOptionPane.showMessageDialog(null,
							"Please enter the artifact types from where attributes needs to be deleted!!");
				}
			 
				else if (attributeDetailsPojo.getWorkflowDetailsMap().size() < 1) {
					JOptionPane.showMessageDialog(null, "Please enter the artifact types & workflow that needs to be updated!!");
				}
			 
				else
				{
					updatewf.setVisible(false);
					updateboth.setVisible(false);
					jb2.setVisible(true);
					jb2.setIndeterminate(true);
					tabbedPane.setEnabledAt(0, false);
				    tabbedPane.setEnabledAt(1, false);
				    workflowTable.setEnabled(false);
					DeleteAttributeApplication.loadConfigProperties(configDetailsPojo, attributeDetailsPojo , "Delete_Update");
					new Thread(new Runnable() {
						@Override
						public void run() {

							final Boolean deleteCompleted = DeleteAttributeApplication.DeleteAttribute_UpdateWorkflowApplication();

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {

									if (deleteCompleted) {
										jb.setIndeterminate(false);
										tabbedPane.setEnabledAt(0, true);
									    tabbedPane.setEnabledAt(2, true);
										jb.setString("Deleting Attributes Completed");
										JOptionPane.showMessageDialog(null, "Deleting attributes completed");
										
										
									}

								}
							});

						}
					}).start();
				}
		}
		 
		else if (e.getSource()==b1) {
			AttributeDetailsPojo attributeDetailsPojo = readAttributePaneDetails();
			ConfigDetailsPojo configDetailsPojo = setConfigDetails();
			
			
			 if (attributeDetailsPojo.getAttributeDeletionMap().size() < 1) {
				JOptionPane.showMessageDialog(null, "Please enter the attributes that needs to be deleted!!");
			}

			else if (textField9.getText().equals("")) {
				JOptionPane.showMessageDialog(null,
						"Please enter the artifact types from where attributes needs to be deleted!!");
			}
			 
			else
			{
				b1.setVisible(false);
				b2.setEnabled(false);
				nextButton2.setVisible(false);
				jb.setVisible(true);
				jb.setIndeterminate(true);
				tabbedPane.setEnabledAt(0, false);
			    tabbedPane.setEnabledAt(2, false);
			    
			    //ConfigDetailsPojo configDetailsPojo = setConfigDetails();
				DeleteAttributeApplication.loadConfigProperties(configDetailsPojo, attributeDetailsPojo, "Delete");
				new Thread(new Runnable() {
					@Override
					public void run() {

						final Boolean deleteCompleted = DeleteAttributeApplication.DeleteAttribute_UpdateWorkflowApplication();

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {

								if (deleteCompleted) {
									jb2.setIndeterminate(false);
									tabbedPane.setEnabledAt(0, true);
								    tabbedPane.setEnabledAt(2, true);
									jb2.setString("Deletion & Updation Completed");
									JOptionPane.showMessageDialog(null, "Deleting attributes & Updating Workflows completed");
									
								}

							}
						});

					}
				}).start();

			}
			}
			
	}
	
	public ConfigDetailsPojo setConfigDetails()
	{
		ConfigDetailsPojo configDetailsPojo= new ConfigDetailsPojo();
		String userValue = textField1.getText(); // get user entered username from the textField1
		String passValue = textField2.getText();
		String serverUrl = textField3.getText();
		String inputFile = textField4.getText();
		String baseLineName = textField7.getText();
		String changeSetName = textField8.getText();// get user entered pasword from the textField2
		String deliverChangeSet = group.getSelection().getActionCommand();

		configDetailsPojo.setUserName(userValue);
		configDetailsPojo.setPassword(passValue);
		configDetailsPojo.setRepositoryUrl(serverUrl);
		configDetailsPojo.setInputFileName(inputFile);
		configDetailsPojo.setBaselineName(baseLineName);
		configDetailsPojo.setChangeSetName(changeSetName);
		configDetailsPojo.setDeliverChangeSet(deliverChangeSet);
		
		return configDetailsPojo;
	}

	public static void main(String[] args) {
		try {
			DeleteAttributeGUI form = new DeleteAttributeGUI();
			form.setBounds(350, 150, 550, 450); // set size of the frame
			form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			form.setVisible(true);
			form.setResizable(false);

		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}
}
