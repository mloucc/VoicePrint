package Core;

public class Node implements Share {
    public boolean common =false;
    public int cnt = 0;
    public double prob = 0.0;
    public int[] peaks = new int[PATT_SIZE];

    public Node(int[] peaks_) {
        System.arraycopy(peaks_, 0, peaks, 0, peaks_.length);
    }

    public boolean similar(Node other) {
        int[] a = peaks;
        int[] b = other.peaks;
        int diff = 0;
        if (a[0] == -1 && b[0] == -1) {
            return true;
        } else if (a[0] == -1 || b[0] == -1) {
            return false;
        }

        diff += minDist(a[PATT_SIZE - 1], b);
        diff += minDist(a[PATT_SIZE - 2], b);
        // diff += minDist(a[PATT_SIZE - 3], b);

        diff += minDist(b[PATT_SIZE - 1], a);
        diff += minDist(b[PATT_SIZE - 2], a);
        // diff += minDist(b[PATT_SIZE - 3], a);
        // show("Diff: " + diff);

        if (diff < 5) {
            return true;
        } else {
            return false;
        }
    }

    public int minDist(int a, int[] b) {
        int len = b.length;
        int minD = 10000;
        int dist;
        for (int i = 0; i < len; i++) {
            dist = Math.abs(a - b[i]);
            if (dist < minD) {
                minD = dist;
            }
        }
        return minD;
    }
}
