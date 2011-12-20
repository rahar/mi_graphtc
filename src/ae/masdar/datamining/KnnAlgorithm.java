package ae.masdar.datamining;

import java.util.*;

public class KnnAlgorithm {

    public static String getCategory(Map<Document, Integer> distances) {
        TreeMap<Document, Integer> sorted = new TreeMap<Document, Integer>(new ValueComparator(distances));
        sorted.putAll(distances);
        for(Document doc : sorted.keySet()) {
            // first n neighbors get herev
        }
        return "";
    }
}
