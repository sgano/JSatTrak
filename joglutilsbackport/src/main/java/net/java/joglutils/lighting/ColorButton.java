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
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF ERIK
 * TOLLERUD OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *   
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package net.java.joglutils.lighting;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Erik J. Tollerud
 */
public class ColorButton extends JButton implements ActionListener {
    
    /** Creates a new instance of GlobalAmbientButton */
    public ColorButton() {
        super();
        this.addActionListener(this);
    }
    public ColorButton(Action a) {
        super(a);
        this.addActionListener(this);
    }
    public ColorButton(Icon icon) {
        super(icon);
        this.addActionListener(this);
    }
    public ColorButton(String text) {
        super(text);
        this.addActionListener(this);
    }
    public ColorButton(String text, Icon icon) {
        super(text, icon);
        this.addActionListener(this);
    }
    public Color getColor() {
        return this.getForeground();
    }
    public void setColor(Color c) {
        this.setForeground(c);
    }

    public void actionPerformed(ActionEvent e) {
        JButton src = (JButton)e.getSource();
        Color c = JColorChooser.showDialog(this,"",src.getForeground());
        if (c != null)
            src.setForeground(c);
    }
    
    
}
