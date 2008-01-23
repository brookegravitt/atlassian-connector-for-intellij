package com.atlassian.theplugin.idea;


import prefuse.util.ui.JFastLabel;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:54:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusIcon extends JLabel {

    Icon iconRed, iconGreen, iconGrey;
    private static org.apache.log4j.Logger log = Logger.getLogger(BambooStatusIcon.class);


    BambooStatusIcon() {
    /*Action toolTipAction = this.getActionMap().get("postTip");
    if (toolTipAction != null)
    {

	ActionEvent postTip = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
	toolTipAction.actionPerformed( postTip );
    } */




        iconRed = IconLoader.getIcon("/icons/red-16.png");
        iconGreen = IconLoader.getIcon("/icons/green-16.png");
        iconGrey = IconLoader.getIcon("/icons/grey-16.png");

    }

    public void updateBambooStatus(BuildStatus status, String fullInfo) {
        setToolTipText(fullInfo);

        switch (status) {
            case ERROR:
                setIcon(iconGrey);
                break;
            case FAILED:
                setIcon(iconRed);
                break;
            case SUCCESS:
                setIcon(iconGreen);
                break;
        }
    }

    public JToolTip createToolTip() {
        JPanel panel = new JPanel();
        JEditorPane  pane = new JEditorPane();
        pane.setPreferredSize(new Dimension(490,290));
        pane.setContentType("HTML");
        pane.setEditable(false);
        pane.setText("<html><b>Ala</b><a href=http://www.onet.pl>onet</a></html>");
        panel.setPreferredSize(new Dimension(500,300));
        panel.add(pane);
        



        //t.setText("dfssssssssssssssssssssssdfsss\nssssssssssssssssssssdfssssssssssssssssssssss\nsdfsssssssssssssssssssssssdfssss\nsssssssssssssssssssdfssssssssssss\nsssssssssssdfsssssssssssssssssssssssdfssssssssssssssssssss\nsssdfsssssssssssssssssssssssdfsssssssssssssssssssssssd\nfsssssssssssssssssssssssdfsssssssssssssssssssssssdf\nsssssssssssssssssssssssdfsssssssssssssssssssssssdfssss\nsssssssssssssssssssdfssssssssssssssssssssssss");
        JToolTip toolTip = new JACustomTooltip  (this,panel, true);
              

       /* t.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void mouseReleased(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void mouseEntered(MouseEvent e) {
                System.out.println("mouse entered: ("+ e.getX() + "," + e.getY() + ")");
            }

            public void mouseExited(MouseEvent e) {


                if (e.getX() <= t.getX() ||
                    e.getY() <= t.getY() ||
                    e.getX() >= t.getX() + t.getWidth() ||
                    e.getY() >= t.getY() + t.getHeight()) {
                    //JOptionPane.showMessageDialog(null,"mouse exit ");


                    t.setVisible(false);
                   // t.repaint();
                    //t.getParent().setVisible(false);
                    //t.getParent().getParent().setVisible(true);
                    getParent().repaint();
                    getParent().setVisible(true);
                    
                    //t.repaint();
                    //bambooLabel.repaint();
                    System.out.println("mouse exit");


                //t.setVisible(false);
                System.out.println("Mouse (" + e.getX() + ", " + e.getY() + ")" +
                                   " FileChooser (" + t.getX() + ", " + t.getY() + ") bounds " + t.getBounds());

                



            }
        });     */

        //System.out.println("rendering");
        //return new PluginStatusBarToolTip(this, t, true);
        return toolTip;
    }         

}
