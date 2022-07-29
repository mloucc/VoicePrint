package Core;

import cclo.*;

public class Trainer implements Share {

    private Lattice lattice;
    private Category trainCat;

    ControlPan controlPan;

    public Trainer(ControlPan cp_) {
        controlPan = cp_;
    }

    // Train the given lattice based on a vector of input vectors
    public void setTraining(Lattice lattice_, Category trainCat_,
            ControlPan latticeRenderer) {
        lattice = lattice_;
        trainCat = trainCat_;
    }

    public void start() {
        if (lattice != null) {
            run();
        }
    }

    public void run() {
        //  --- if the lattice is empty, add the new training category
        if (lattice.cats.isEmpty()) {
            if (!trainCat.chain.nodes.isEmpty()) {
                String newCatName = trainCat.chain.name;
                Category newCat = new Category(newCatName);
                lattice.addCat(newCat);
            }
        }
        //  --- lattice is not empty

        // --- get the name of training data
        String chainName = trainCat.chain.name;
        //  --- check if training category in lattice
        boolean catInLattice = false;
        //  --- search the train category  in lattice
        for (Category lcat : lattice.cats) {
            // ---- if the training category is in lattice (lcat)
            if (lcat.name.equals(chainName)) {
                catInLattice = true;
                //  --- add all node in trainCat.chain to (lcat)
                for (Node nd : trainCat.chain.nodes) {
                    lcat.addNode(nd);
                }
            }
        }
        //  --- training data not in lattice
        if (!catInLattice) {
            Category newCat = new Category(chainName);
            for (Node nd : trainCat.chain.nodes) {
                newCat.addNode(nd);
            }
            lattice.addCat(newCat);
        }
        //  --- reconstruct the lattice
        for (Category lcat: lattice.cats) {
            lcat.trimNode();
        }
        
        lattice.doCorrelation();
        //  --- debug: showing the category after training
        lattice.showRouph();
        controlPan.latState = STATE.READY;
    }

    public void debug(int no_, String str_) {
        if (GUI.ON) {
            ((MsgPan) Main.msgPan).setMsg(no_, str_);
        }
    }

    public void show(String str) {
        System.out.println(str);
    }

    public void stop() {
    }
}
