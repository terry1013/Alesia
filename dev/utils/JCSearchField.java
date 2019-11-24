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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.core.animation.timing.*;
import org.jdesktop.core.animation.timing.interpolators.*;
import org.jdesktop.swing.animation.timing.sources.*;

/**
 * @author Eneko
 */
public class JCSearchField extends JCTextField implements Serializable {

    private static BufferedImage searchIcon;
    private int expandedWidth = 100;
    private int expandDirection = SwingConstants.RIGHT;
    private boolean expandOnFocus = false;
    private boolean expanded = false;
    private Animator animator = null;
    private static final SwingTimerTimingSource timingSource =
                         new SwingTimerTimingSource(15, TimeUnit.MILLISECONDS);


    public JCSearchField() {
        super();
        try {
            searchIcon = ImageIO.read(JCSearchField.class.getResource("/resources/images"
                                                                 + "/magnifier.png"));
        } catch (IOException ex) {
            Logger.getLogger("jcool").log(Level.SEVERE, "Couldn't load "
                                                          + "resources.");
        }
        super.setBorder(new EmptyBorder(0, 25, 0, 8));
        super.setRoundness(10);
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if ((expandOnFocus)&&(!expanded))
                    expand();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ((expandOnFocus)&&(expanded))
                    contract();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(searchIcon, 4, (int) Math.ceil((getHeight() - 20) / 2d), 20,
                                                                       20, null);
        g2.dispose();

    }

    /**
     * Enables expand animation on focus. See setExpandDirection() and
     * setExpandedWidth().
     *
     * @see
     *
     * @param expand
     */
    public void setExpandOnFocus(boolean expand) {
        expandOnFocus = expand;
        if (expandOnFocus)
            initAnimator();
    }
    
    /**
     * Use SwingConstants.LEFT and SwingConstants.RIGHT to determine
     * the expand direction. It's setted to RIGHT by default.
     * 
     * @param direction
     */
    public void setExpandDirection(int direction) {
        expandDirection = direction;
        if (expandOnFocus)
            initAnimator();
    }

    /**
     * Sets the width of the search field in its expanded state (size to which
     * will grow when focus gained)
     * gained
     *
     * @param width
     */
    public void setExpandedWidth(int width) {
        expandedWidth = width;
        if (expandOnFocus)
            initAnimator();
    }

    private void initAnimator() {
        animator = new AnimatorBuilder(timingSource)
                   .setInterpolator(new AccelerationInterpolator(0.3, 0.2))
                   .setDuration(300, TimeUnit.MILLISECONDS)
                   .build();
        animator.addTarget(new TimingTargetAdapter() {
            @Override
            public void end(Animator source) {
                animationEnded();
            }
        });
        TimingTarget setter;
        if (expandDirection == SwingConstants.LEFT) {
             setter = PropertySetter.getTarget(this, "location", getLocation(),
                                               new Point(this.getX() - (expandedWidth
                                                         - this.getWidth()), getY()));
             animator.addTarget(setter);
        }
        setter = PropertySetter.getTarget(this, "size", getSize(),
                                          new Dimension(expandedWidth,
                                                          getHeight()));
        animator.addTarget(setter);
    }
    
    private void expand() {
        if (animator.isRunning()) {
            animator.cancel();
        }
        expanded = true;
        timingSource.init();
        animator.start();
    }

    private void contract() {
        if (animator.isRunning())
            animator.cancel();
        expanded = false;
        timingSource.init();
        animator.startReverse();
    }

    private void animationEnded() {
        timingSource.dispose();
    }

    /**
     * This funtion does nothing and should not be used. You can change the
     * border color with setBorderColor(), and shadows with setShadowColors();
     */
    @Override
    public void setBorder(Border border) {

    }

}
