package Core;

import java.util.ArrayList;

public class Lattice {

    public String name;
    public ArrayList<Category> cats = new ArrayList<>();

    public Lattice(String name_) {
        name = name_;
    }

    public void addCat(Category cat_) {
        cats.add(cat_);
    }

    public void doCorrelation() {
        int corNo = 0;
        int catNo = cats.size();
        for (int i = 0; i<catNo; i++) {
            for (int j=i+1; j <catNo; j++) {
                for (Node nd1: cats.get(i).chain.nodes) {
                    for (Node nd2: cats.get(j).chain.nodes) {
                        if (nd1.similar(nd2)) {
                            nd1.common = true;
                            nd2.common = true;
                            corNo++;
                        }
                    }
                }
            }
        }
        System.out.println("Correlated No: " + corNo);
    }

    public void showRouph() {
        show("---------------------");
        show("Lattice: " + name);
        for (Category cat : cats) {
            cat.showRouph();
        }
    }

    public void show(String str) {
        System.out.println(str);
    }
}
