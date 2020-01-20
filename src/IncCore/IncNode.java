package IncCore;

import cclo.Share;
import java.util.ArrayList;

public class IncNode extends ArrayList<Integer> implements Share {

    public String name = "";
    public int id;

    public IncNode() {

    }

    public IncNode(int id_) {
        id = id;
    }

    public boolean isZero() {
        boolean isZero_ = true;
        for (int i = 0; i < PATT_SIZE; i++) {
            if (get(i) != -1) {
                isZero_ = false;
                break;
            }
        }
        return isZero_;
    }

    public int euclideanDist(IncNode node) {
        if (node.size() != size()) {
            show("Error computing distance ...");
            return Integer.MAX_VALUE;
        }

        int minDist, cDist, cIdx;
        int summation = 0, temp;
        int pSize = size();
        for (int x = 0; x < pSize; x++) {
            cIdx = this.get(x);
            if (cIdx == -1) {
                continue;
            }
            minDist = PATT_SIZE;
            for (int y = 0; y < pSize; y++) {
                if (node.get(y) == -1) {
                    cDist = PATT_SIZE;
                } else {
                    cDist = node.get(y) - cIdx;
                }
                if (cDist < 0) {
                    cDist = -cDist;
                }
                if (cDist < minDist) {
                    minDist = cDist;
                }
            }
            summation += minDist;
        }
        for (int x = 0; x < pSize; x++) {
            cIdx = node.get(x);
            if (cIdx == -1) {
                continue;
            }
            minDist = PATT_SIZE;
            for (int y = 0; y < pSize; y++) {
                if (this.get(y) == -1) {
                    cDist = PATT_SIZE;
                } else {
                    cDist = node.get(y) - cIdx;
                }
                if (cDist < 0) {
                    cDist = -cDist;
                }
                if (cDist < minDist) {
                    minDist = cDist;
                }
            }
            summation += minDist;
        }
        return summation;
    }

    public void show(String str) {
        System.out.println(str);
    }

    public int getDistance(IncNode cNode) {
        int dist = this.euclideanDist(cNode);
        return dist;
    }
}
