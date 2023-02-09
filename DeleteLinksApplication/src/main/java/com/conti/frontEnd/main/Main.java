package com.conti.frontEnd.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import com.conti.application.DeleteLinksApplication;
import com.conti.frontEnd.main.ComboCheckBox.CheckableItem;
import com.conti.pojo.ConfigDetailsPojo;

@SuppressWarnings("serial")
public class Main extends JFrame implements ActionListener {
	JButton b1, b2, b3;
	JRadioButton rb1, rb2;
	JComboBox<CheckableItem> comboBox;
	
	final JProgressBar jb;
	ButtonGroup group;
	JPanel newPanel, buttonPanel, radioButtonPanel;
	JLabel userLabel, passLabel, serverUrlLabel, statusLabel, inputFileNameLabel, attributeMappingLabel,
			baselineNameLabel, changeSetNameLabel, deliverChangeSetLabel;

	final JTextField textField1, textField2, textField3, textField4, textField5, textField7, textField8;

	public Main() {
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
		
		comboBox= new ComboCheckBox().makeUI();
		
	
		
		statusLabel = new JLabel();
		statusLabel.setText("State Of The Requirements :");
		

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

		attributeMappingLabel = new JLabel();
		attributeMappingLabel.setText("Links to be Deleted Input:");
		textField5 = new JTextField(150);
		textField5.setEditable(false);
		b3 = new JButton("Select Links Input File");
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField5.setText(inputFileAction());
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

		b1 = new JButton("Delete Links");
		b1.setBackground(new Color(255, 204, 153));

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttonPanel.setBackground(new Color(229, 255, 204));
		buttonPanel.setPreferredSize(new Dimension(100, 100));
		buttonPanel.setLayout(new GridBagLayout());

		radioButtonPanel = new JPanel();
		radioButtonPanel.add(rb1);
		radioButtonPanel.add(rb2);

		jb = new JProgressBar();
		jb.setString("Deleting links.....");
		jb.setStringPainted(true);
		jb.setFocusable(false);
		jb.setVisible(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.LAST_LINE_END;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		buttonPanel.add(b1, gbc);
		buttonPanel.add(jb, gbc2);

		newPanel = new JPanel(new GridLayout(10, 1));

		newPanel.add(userLabel); // set username label to panel
		newPanel.add(textField1); // set text field to panel
		newPanel.add(passLabel); // set password label to panel
		newPanel.add(textField2); // set text field to panel
		newPanel.add(serverUrlLabel); // set password label to panel
		newPanel.add(textField3);
		newPanel.add(statusLabel);
		newPanel.add(comboBox);
		newPanel.add(inputFileNameLabel); // set password label to panel
		newPanel.add(textField4);
		newPanel.add(attributeMappingLabel); // set password label to panel
		newPanel.add(textField5); // set text field to panel
		newPanel.add(baselineNameLabel); // set password label to panel
		newPanel.add(textField7); // set text field to panel
		newPanel.add(changeSetNameLabel); // set password label to panel
		newPanel.add(textField8);
		newPanel.add(deliverChangeSetLabel);
		newPanel.add(radioButtonPanel);// set text field to panel
		// newPanel.add(b1); //set button to panel
		newPanel.add(b2);
		newPanel.add(b3);

		gui.add(newPanel);
		gui.add(buttonPanel, BorderLayout.PAGE_END);

		this.setContentPane(gui);

		b1.addActionListener(this);

		setTitle("DELETE LINKS APPLICATION V1.0");
	}

	public void actionPerformed(ActionEvent e) {

		ConfigDetailsPojo configDetailsPojo = new ConfigDetailsPojo();
		String statesSelected= getDataStringRepresentation(comboBox.getModel());
		// TODO Auto-generated method stub
		if (textField1.getText().equals("") || textField2.getText().equals("") || textField3.getText().equals("")
				|| textField4.getText().equals("") || textField5.getText().equals("") || textField7.getText().equals("")
				|| textField8.getText().equals("") || statesSelected.isEmpty() || statesSelected==null) {
			JOptionPane.showMessageDialog(null, "Please enter all the fields");

		} else {
			b1.setVisible(false);
			b2.setEnabled(false);
			b3.setEnabled(false);
			jb.setVisible(true);
			jb.setIndeterminate(true);
			String userValue = textField1.getText(); // get user entered username from the textField1
			String passValue = textField2.getText();
			String serverUrl = textField3.getText();
			String inputFile = textField4.getText();
			String mappingFile = textField5.getText();
			String baseLineName = textField7.getText();
			
			String changeSetName = textField8.getText();// get user entered pasword from the textField2
			String deliverChangeSet = group.getSelection().getActionCommand();

			configDetailsPojo.setUserName(userValue);
			configDetailsPojo.setPassword(passValue);
			configDetailsPojo.setRepositoryUrl(serverUrl);
			configDetailsPojo.setInputFileName(inputFile);
			configDetailsPojo.setAttributeMappingFileName(mappingFile);
			configDetailsPojo.setBaselineName(baseLineName);
			configDetailsPojo.setChangeSetName(changeSetName);
			configDetailsPojo.setDeliverChangeSet(deliverChangeSet);
			configDetailsPojo.setStateSelected(statesSelected);

			DeleteLinksApplication.loadConfigProperties(configDetailsPojo);
			new Thread(new Runnable() {
				public void run() {

					DeleteLinksApplication.deleteLinks();

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {

						}
					});

				}
			}).start();

		}

	}
	
	   String getDataStringRepresentation(ListModel model) {
		    List<String> sl = new ArrayList<>();
		    for (int i = 0; i < model.getSize(); i++) {
		      Object o = model.getElementAt(i);
		      if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
		        sl.add(o.toString());
		      }
		    }
		    return sl.stream().sorted().collect(Collectors.joining(", "));
		  }
		

	public String inputFileAction() {
		JFileChooser inputFile = new JFileChooser();
		inputFile.showOpenDialog(this);
		return inputFile.getSelectedFile().getAbsolutePath();

	}

	
	public static void main(String[] args) {
		try {
			Main form = new Main();
			form.setBounds(350, 150, 450, 500); // set size of the frame
			form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			form.setVisible(true);
			form.setResizable(true);

		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}

}
