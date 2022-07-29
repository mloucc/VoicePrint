package Core;

import java.util.ArrayList;

public class Category implements Share {

    //  --- Category Name
    public String name;
    //  --- One chain in the category
    public Chain chain;
    //  --- number of training node, max of 10000
    public int catCnt = 0;

    //  --- new a category also new a chain
    public Category(String name_) {
        name = name_;
        chain = new Chain(name);
    }

    //  --- add Node in Category will reject similar node
    public boolean addNode(Node newN) {
        //  --- calculate prob only before catCnt reach 10000
        if (catCnt < 10000) {
            catCnt++;
        }
        boolean newPattern = true;
        for (Node nd : chain.nodes) {
            if (nd.similar(newN)) {
                //  --- a similar node already in cat
                nd.cnt++;
                newPattern = false;
                break;
            }
        }
        if (newPattern) {
            //  --- no similar node in the cat
            chain.addNode(newN);
            newN.cnt++;
        }
        //  --- if the node really added?
        return newPattern;
    }

    //  --- match and give a score to a chain
    public int matchScore_I(Chain other) {
        int score = 0;
        //  --- check every node in the matched chain
        for (Node nd : other.nodes) {
            score += inCat(nd);
        }
        return score;
    }

    //  --- match and give a score to a chain
    public double matchScore_D(Chain other) {
        double score = 0.0;
        //  --- check every node in the matched chain
        for (Node nd : other.nodes) {
            //  --- add score of every node in the matched chain
            score += nodeScore(nd);
        }
        return score;
    }

    //  --- check if a node is in the category
    public int inCat(Node other) {
        for (Node nd : chain.nodes) {
            if (nd.similar(other)) {
                if (nd.common) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        return 0;
    }

    //  --- give a score to a single node
    public double nodeScore(Node other) {
        for (Node nd : chain.nodes) {
            if (nd.similar(other)) {
                //  --- the node is in the catigory return its prob.
                return nd.prob;
            }
        }
        //  --- node not in the category
        return 0.0;
    }

    //  --- trimming non representative node in the category
    public void trimNode() {
        //  --- collect node to remove in a list
        ArrayList<Node> removeList = new ArrayList<>();
        //  --- sum up the number of samples in all nodes
        int sumCnt = 0;
        for (Node nd : chain.nodes) {
            sumCnt += nd.cnt;
        }
        //  --- cutting threshold = sample No/1000
        int cutNo = sumCnt / 2000;
        for (Node nd : chain.nodes) {
            if (nd.cnt < cutNo) {
                removeList.add(nd);
            }
        }
        for (Node nd : removeList) {
            chain.nodes.remove(nd);
        }
        for (Node nd : chain.nodes) {
            nd.cnt = 0;
        }
    }

    //  --- recompute the weight of each node
    public void newProb() {
        if (catCnt < 10000) {
            for (Node nd : chain.nodes) {
                nd.prob = (double) nd.cnt / (double) catCnt;
            }
        }
    }

    public void showRouph() {
        show("Category: " + name);
        show("  Chain Size: " + chain.nodes.size());
    }

    public void show(String str) {
        System.out.println(str);
    }

}
