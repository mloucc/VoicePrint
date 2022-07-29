package Core;


import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Chain implements Share {

    public String name;
    public ArrayList<Node> nodes = new ArrayList<>();

    public Chain(String name_) {
        name = name_;
    }

    public void addNode(Node node_) {
        nodes.add(node_);
    }

    @Override
    public String toString() {
        String res = "[";
        for (Node nd : nodes) {
            res += "[";
            for (int i = 0; i < PATT_SIZE; i++) {
                res += nd.peaks[i] + ",";
            }
            res += "]";
        }
        res += "]";
        return res;
    }

    public void show(String str) {
        System.out.println(str);
    }

}
