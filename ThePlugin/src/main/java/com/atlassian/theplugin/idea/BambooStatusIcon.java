package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:54:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusIcon extends JLabel {

    Icon iconRed, iconGreen, iconGrey;

    BambooStatusIcon() {
        iconRed = IconLoader.getIcon("/icons/red-16.png");
        iconGreen = IconLoader.getIcon("/icons/green-16.png");
        iconGrey = IconLoader.getIcon("/icons/grey-16.png");

    }

    public void updateBambooStatus(BuildStatus status, String fullInfo) {
        setText("");
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
        final JComponent t = new JFileChooser();

        t.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                t.setVisible(false);
            }

            public void mousePressed(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void mouseReleased(MouseEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void mouseEntered(MouseEvent e) {
                System.out.println("mouse enter");
            }

            public void mouseExited(MouseEvent e) {


                if (e.getX() < 0 || e.getY() < 0 || e.getX() > t.getWidth() || e.getY() > t.getHeight()) {
                    //JOptionPane.showMessageDialog(null,"mouse exit ");

                    t.setVisible(false);
                    t.getParent().setVisible(false);
                    t.getParent().getParent().setVisible(true);
                    getParent().repaint();
                    getParent().setVisible(true);
                    //t.repaint();
                    //bambooLabel.repaint();
                    System.out.println("mouse exit");
                }


            }
        });

        //System.out.println("rendering");
        return new PluginStatusBarToolTip(this, t, true);
    }

}
