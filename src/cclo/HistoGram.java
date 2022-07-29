/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cclo;

import Core.Share;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class HistoGram extends JPanel implements Share {

    //++widith, height --- of panel //
    //++Row, Colum No ---  margin = 10 ----
    int rowNo = 200, colNo = FFTNo;

    //++Plotting array  1000 x colNo ----
    double array[][] = new double[rowNo][colNo];
    boolean isChar[] = new boolean[rowNo];

    //++Initial row for input
    int cidx = 0;

    //++ Constructor
    public HistoGram() {

        //++White Background  -------------------
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        this.setVisible(true);
    }

    //++ Paint
    @Override
    public void paintComponent(Graphics g) {

        //++ get panel area size
        Dimension dim = this.getSize();
        int pWidth = dim.width, pHeight = dim.height;

        //++Clear  clear the panel
        g.setColor(Color.WHITE);
        g.clearRect(0, 0, pWidth, pHeight);

        g.setColor(Color.BLACK);

        //++ column seperation
        int colSep = (pWidth - 40) / FFTNo;
        int hMargin = (pWidth - (colSep * FFTNo)) / 2;
        int vMargin = (pHeight - rowNo) / 2;

        //++ Current plotting row
        int cRow;

        //++ Printing Rows top down
        for (int i = 0; i < rowNo; i++) {
            cRow = (cidx + i) % rowNo;
            if (isChar[cRow]) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.RED);

            }
            for (int j = 0; j < colNo; j++) {
                if (array[cRow][j] > 1.0) {
                    g.drawOval(hMargin + j * colSep, vMargin + i, 2, 2);
                }
            }
        }
    }

    public void setChar(boolean val) {
        int row = cidx - 1;
        if (row == -1) {
            row = rowNo - 1;
        }
        isChar[row] = val;
    }

    public void updateArray(double[] value) {

        //++gather information form
        System.arraycopy(value, 0, array[cidx], 0, FFTNo);
        cidx = ++cidx % rowNo;
        repaint();
    }
}
