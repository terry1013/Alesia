/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package dev.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;

/**
 *
 * @author Eneko
 */
public class JCTextField extends JTextField implements Serializable {

    private  Color shadowColor1 = new Color(200, 200, 200);
    private  Color shadowColor2 = new Color(230, 230, 230);
    private  Color borderColor = new Color(120, 120, 120);
    private boolean drawShadow = true;
    private int roundness = 5;


    public JCTextField() {
        super();
        setUI(new BasicTextFieldUI());
        super.setOpaque(false);
        setBackground(new Color(250, 250, 250));
        setCaretColor(Color.GRAY);
        setForeground(new Color(70, 70, 70));
        setSelectionColor(Color.GRAY);
        setBorder(new EmptyBorder(0, 3, 0, 3));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2.setColor(this.getBackground());
        g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, roundness,
                                                                roundness);
        if (drawShadow) {
            g2.setColor(shadowColor1);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 2, roundness,
                                                                    roundness);

            g2.setColor(shadowColor2);
            g2.drawRoundRect(1, 2, getWidth() - 3, getHeight() - 4, roundness,
                                                                    roundness);
        }
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, roundness + 2,
                                                                roundness + 2);
        super.paintComponent(g);
    }

    /**
     * Sets the border color.
     *
     * @param borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Shadow is emulated drawing two lines with light colors below the
     * top border.
     *
     * @param shadowColor1 darkest color, to draw the line closest to the border
     * @param shadowColor2 lighter color to draw the following line
     */
    public void setShadowColors(Color shadowColor1, Color shadowColor2) {
        this.shadowColor1 = shadowColor1;
        this.shadowColor2 = shadowColor2;
    }

    public void setDrawBorderShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
    }

    /**
     * The roundness is the width and height in pixels of the arc in this
     * component's corners.
     *
     * @param roundness
     */
    public void setRoundness(int roundness) {
        this.roundness = roundness;
        int margin = (int) Math.ceil((2 * roundness / 5f) + 1);
        margin = margin >= 3 ? margin : 3;
        setBorder(new EmptyBorder(0, margin, 0, margin));
    }
    
    /**
     * This funtion does nothing and should not be used.
     * JTextField is allways non opaque.
     */
    @Override
    public void setOpaque(boolean isOpaque) {
        
    }

    /**
     * This funtion should not be used.
     * You can change the border color with
     * setBorderColor(), and shadows with setShadowColors();
     */
    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
    }

    @Override
    public void paint(Graphics g) {
        paintComponent(g);
        paintChildren(g);
    }

}
