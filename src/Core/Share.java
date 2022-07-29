/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

/**
 *
 * @author panda
 */
public interface Share {

    int FFTNo = 1024;
    int PATT_SIZE = 6;
    int NDIST = 4;
    boolean DEBUG = false;
    
    enum STATE {

        START, ENDING, MATCH, NORMAL, RECORDING, STOP, CLOSE, OPEN, READY, EMPTY, TRAINING
    }

}
