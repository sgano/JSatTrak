/*
 * Copyright (c) 2006 Erik Tollerud (erik.tollerud@gmail.com) All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *   
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *    
 * - Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *   
 * The names of Erik Tollerud, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *    
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ERIK TOLLERUD,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ERIK
 * TOLLERUD, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT 
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF BEN
 * CHAPPELL OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *   
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package net.java.joglutils;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class is a basic encapsulation of a JPanel in a dialog.
 * @author Erik J. Tollerud
 */
public class JPanelDialog extends JDialog implements ActionListener{
    private boolean result = false;
    private JButton acceptButton;
    private JButton rejectButton;
    private JPanel mainPanel;
    private int numOfPanels = 0;
    
    /** Creates a new instance of JPanelDialog */
    public JPanelDialog(JPanel inputPanel) {
        super();
        this.setModal(false);
        initLayout(inputPanel);
    }
    public JPanelDialog(String title, JPanel inputPanel) {
        super();
        this.setTitle(title);
        this.setModal(false);
        initLayout(inputPanel);
    }
    
    public JPanelDialog(Frame parent, JPanel inputPanel) {
        super(parent,false);
        initLayout(inputPanel);
    }
    public JPanelDialog(Frame parent, String title, JPanel inputPanel) {
        super(parent,title,false);
        initLayout(inputPanel);
    }
    /**
     * Shows this dialog as a modal dialog
     * @return true if aceept option is clicked
     */
    public boolean showAsModal() {
        boolean startModal = this.isModal();
        this.setModal(true);
        this.setVisible(true);
        this.setModal(startModal);
        return result;
    }
    /**
     * Checks if accept option was chosen
     * @return true if aceept option was clicked on last showing of dialog
     */
    public boolean isAccepted() {
        return result;
    }
    public void setButtonTexts(String acceptButtonText, String rejectButtonText) {
        acceptButton.setText(acceptButtonText);
        rejectButton.setText(rejectButtonText);
    }
    private void initLayout(JPanel inputPanel) {
        GridBagConstraints gbc;
        
        this.acceptButton = new javax.swing.JButton();
        this.rejectButton = new javax.swing.JButton();
        this.mainPanel = inputPanel;
        
        //using gridbaglayout
        getContentPane().setLayout(new java.awt.GridBagLayout());
        
        acceptButton.setText("Ok");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5,5,5,10);
        gbc.anchor = gbc.EAST;
        getContentPane().add(acceptButton, gbc);
        
        rejectButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5,10,5,5);
        gbc.anchor = gbc.WEST;
        getContentPane().add(rejectButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = gbc.CENTER;
        getContentPane().add(mainPanel, gbc);

        pack();
        
        acceptButton.addActionListener(this);
        rejectButton.addActionListener(this);
        
        //TODO: see if this works properly - hide might be better
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                result = false;
                dispose();
            }
        });
        this.numOfPanels = 1;
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == acceptButton) {
            //TODO: see if this works properly - hide might be better
            this.result = true;
            this.dispose();
            
        }
        else if(e.getSource() == rejectButton) {
            //TODO: see if this works properly - hide might be better
            this.result = false;
            this.dispose();            
        }
        else
            //Should never get here
            throw new RuntimeException("action was performed from a source that shouldn't exist");
            
        
    }
    public static boolean showModalDialog(JPanel panel) {
        JPanelDialog jpd = new JPanelDialog(panel);
        return jpd.showAsModal();
    }
    public static boolean showModalDialog(JPanel panel, String title) {
        JPanelDialog jpd = new JPanelDialog(title,panel);
        return jpd.showAsModal();
    }
    public static boolean showModalDialog(Frame parent, JPanel panel) {
        JPanelDialog jpd = new JPanelDialog(parent,panel);
        return jpd.showAsModal();
    }
    public static boolean showModalDialog(Frame parent, JPanel panel, String title) {
        JPanelDialog jpd = new JPanelDialog(parent,title,panel);
        return jpd.showAsModal();
    }
    public java.awt.Component add(java.awt.Component compToAdd) {
        GridBagConstraints gbc;
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = numOfPanels++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = gbc.CENTER;
        getContentPane().add(compToAdd, gbc);
        
        this.getContentPane().remove(acceptButton);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = numOfPanels;
        gbc.insets = new Insets(5,5,5,10);
        gbc.anchor = gbc.EAST;
        getContentPane().add(acceptButton, gbc);        
        
        this.getContentPane().remove(rejectButton);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = numOfPanels;
        gbc.insets = new Insets(5,10,5,5);
        gbc.anchor = gbc.WEST;
        getContentPane().add(rejectButton, gbc);
        
        pack();
        
        return compToAdd;
    }
    
}
