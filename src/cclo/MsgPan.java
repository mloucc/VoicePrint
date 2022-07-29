/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cclo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Melissa
 */
public class MsgPan extends JPanel {

    int width, height;
    static String[] msg = {"Hello", "Fine"};

    public MsgPan() {
        width = this.getWidth();
        height = this.getHeight();
    }

    public void setMsg(int no_, String msg_) {
        msg[no_] = msg_;
        repaint();
    }

    public void paintComponent(Graphics g) {
        //++ get panel area size
        Dimension dim = this.getSize();
        int pWidth = dim.width, pHeight = dim.height;

        //++Clear  clear the panel
        g.setColor(Color.WHITE);
        g.clearRect(0, 0, pWidth, pHeight);

        g.setColor(Color.BLACK);
        g.drawString(msg[0], 20, 40);
        g.drawString(msg[1], 20, 70);

    }
}
