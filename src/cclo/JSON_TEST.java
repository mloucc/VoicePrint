
import Core.*;
import com.google.gson.Gson;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class JSON_TEST {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Lattice lattice = new Lattice("lat");
        Category cat = new Category("Snore");
        Chain chain = new Chain("Snore");
        int[] nd = {1, 2, 3, 4, 5};
        Node node = new Node(nd);
        chain.addNode(node);
        int[] nd2 = {6, 7, 8, 9, 10};
        node = new Node(nd2);
        chain.addNode(node);
        lattice.addCat(cat);

        Gson gson = new Gson();
        String json = gson.toJson(lattice);
        System.out.println(json);

    }         // TODO code application logic here
}
