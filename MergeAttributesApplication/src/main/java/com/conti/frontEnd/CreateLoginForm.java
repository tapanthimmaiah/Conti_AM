package com.conti.frontEnd;

import javax.swing.*;

import com.conti.pojo.ConfigDetailsPojo;
import com.conti.application.MergeAttributesApplication;

import java.awt.*;  
import java.awt.event.*;  


@SuppressWarnings("serial")
public class CreateLoginForm extends JFrame implements ActionListener  
{  
    //initialize button, panel, label, and text field  
    JButton b1,b2;  
    JPanel newPanel;  
    JLabel userLabel, passLabel ,serverUrlLabel ,inputFileNameLabel,sourceAttributeLabel, targetAttributeLabel , baselineNameLabel,changeSetNameLabel;  
    final JTextField  textField1, textField2 , textField3,textField4,textField5,textField6,textField7,textField8;  
      
    //calling constructor  
    public CreateLoginForm()  
    {     
          
        //create label for username   
        userLabel = new JLabel();  
        userLabel.setText("Username:");      //set label value for textField1  
          
        //create text field to get username from the user  
        textField1 = new JTextField(15);    //set length of the text  
  
        //create label for password  
        passLabel = new JLabel();  
        passLabel.setText("Password:");      //set label value for textField2  
          
        //create text field to get password from the user  
        textField2 = new JPasswordField(15);    //set length for the password  
        
        //create label for username   
        serverUrlLabel = new JLabel();  
        serverUrlLabel.setText("ServerURL:");      //set label value for textField1  
          
        //create text field to get username from the user  
        textField3 = new JTextField(150);    //set length of the text  
        
        //create label for username   
        //inputFileNameLabel = new JLabel();  
        
        inputFileNameLabel = new JLabel();
        inputFileNameLabel.setText("Project Details Input:");
        textField4=new JTextField(150); 
        textField4.setEditable(false);
        b2= new JButton("Select Input File");
        b2.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
            	textField4.setText(inputFileAction());
            } 
        });
        
     
        sourceAttributeLabel = new JLabel();  
        sourceAttributeLabel.setText("SourceAttribute:");      //set label value for textField1  
          
        //create text field to get username from the user  
        textField5 = new JTextField(150);    //set length of the text  
        
        //create label for username   
        targetAttributeLabel = new JLabel();  
        targetAttributeLabel.setText("TargetAttribute:");      //set label value for textField1  
        
       
          
        //create text field to get username from the user  
        textField6 = new JTextField(150);    //set length of the text  
        
        String data[][]={ {"101","Amit"},    
                {"102","Jai"},    
                {"101","Sachin"}};    
        String column[]={"SourceAttribute","TargetAttribute"};         
        JTable jt=new JTable(data,column);    
        jt.setBounds(30,40,200,300);          
        JScrollPane sp=new JScrollPane(jt); 
        
        //create label for username   
        baselineNameLabel = new JLabel();  
        baselineNameLabel.setText("BaselineName:");      //set label value for textField1  
          
        //create text field to get username from the user  
        textField7 = new JTextField(150);    //set length of the text  
        
        //create label for username   
        changeSetNameLabel = new JLabel();  
        changeSetNameLabel.setText("ChangeSetName:");      //set label value for textField1  
          
        //create text field to get username from the user  
        textField8 = new JTextField(150);    //set length of the text  
          
        //create submit button  
        b1 = new JButton("Merge Attributes"); //set label to button  
          
        //create panel to put form elements  
        newPanel = new JPanel(new GridLayout(9, 1));
        
       
        newPanel.add(userLabel);    //set username label to panel  
        newPanel.add(textField1);   //set text field to panel  
        newPanel.add(passLabel);    //set password label to panel  
        newPanel.add(textField2);   //set text field to panel  
        newPanel.add(serverUrlLabel);    //set password label to panel  
        newPanel.add(textField3);   //set text field to panel 
        newPanel.add(inputFileNameLabel);    //set password label to panel  
        newPanel.add(textField4);  
          //set text field to panel 
        newPanel.add(sourceAttributeLabel);    //set password label to panel  
        newPanel.add(textField5);   //set text field to panel 
        newPanel.add(targetAttributeLabel);    //set password label to panel  
        newPanel.add(textField6);   //set text field to panel 
        //newPanel.add(sp);
        newPanel.add(baselineNameLabel);    //set password label to panel  
        newPanel.add(textField7);   //set text field to panel 
        newPanel.add(changeSetNameLabel);    //set password label to panel  
        newPanel.add(textField8);   //set text field to panel 
        newPanel.add(b1);           //set button to panel  
        newPanel.add(b2); 
          
        //set border to panel   
        add(newPanel, BorderLayout.CENTER);  
        this.setContentPane(newPanel);
        
      //perform action on button click   
        b1.addActionListener(this);     //add action listener to button  
          
        setTitle("MERGE ATTRIBUTES APPLICATION");   
        setSize(400,500);
        setVisible(true);  
        
        
    }  
      
    
    public String inputFileAction()
    {
        JFileChooser inputFile = new JFileChooser();
        inputFile.showOpenDialog(this);
        return inputFile.getSelectedFile().getAbsolutePath();
       
    }
    //define abstract method actionPerformed() which will be called on button click   
    public void actionPerformed(ActionEvent ae)     //pass action listener as a parameter  
    {  
    	ConfigDetailsPojo configDetailsPojo= new ConfigDetailsPojo();
        String userValue = textField1.getText();        //get user entered username from the textField1  
        String passValue = textField2.getText();
        String serverUrl = textField3.getText();
        String inputFile = textField4.getText();
        String sourceAttribute = textField5.getText();
        String targetAttribute = textField6.getText();
        String baseLineName = textField7.getText();
        String changeSetName = textField8.getText();//get user entered pasword from the textField2  
          
        //check whether the credentials are authentic or not  
        if (userValue!=null && passValue!=null) {  //if authentic, navigate user to a new page  
            configDetailsPojo.setUserName(userValue);
            configDetailsPojo.setPassword(passValue);
            configDetailsPojo.setRepositoryUrl(serverUrl);
            configDetailsPojo.setInputFileName(inputFile);
            
            configDetailsPojo.setBaselineName(baseLineName);
            configDetailsPojo.setChangeSetName(changeSetName);
            configDetailsPojo.setDeliverChangeSet(changeSetName);
            
            MergeAttributesApplication.loadConfigProperties(configDetailsPojo);
        }  
        else{  
            //show error message  
            System.out.println("Please enter valid username and password");  
        }  
    }  
}  
