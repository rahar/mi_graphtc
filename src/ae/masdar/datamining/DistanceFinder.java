package ae.masdar.datamining;


import ae.masdar.datamining.db.DatabaseManager;
import ae.masdar.datamining.db.Relationship;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;
import org.neo4j.graphalgo.impl.util.MatrixUtil;
import org.neo4j.graphdb.Path;
import weka.core.Attribute;
import weka.core.Instance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.*;

public class DistanceFinder {
    private DatabaseManager dbManager;

    public DistanceFinder(DatabaseManager dbManager) {
        this.dbManager = dbManager;

    }


    public float getDistance(String fromWord, String toWord) {
        long ts = System.currentTimeMillis();
        try {
            Integer dst = dbManager.get(fromWord, toWord);
            if (dst != null)
                return dst;

            Path path = dbManager.findShortestPath(fromWord, toWord, Relationship.SYNONIM_NOUN);
            if (path == null) {
                // do something, lets say retVal += 100 ?
                System.out.println("WARNING: Path for " + fromWord + " -> " + toWord +" not found. Time:" + (System.currentTimeMillis()-ts)+" ms");
                dbManager.add(fromWord, toWord, new Integer(0));
                return 0;
            } else {
                dbManager.add(fromWord, toWord, path.length());
                return (float)path.length();
            }
        } finally {
            //System.out.println("getDistance "+fromWord+" -> "+toWord + " took " + (System.currentTimeMillis()-ts));
        }
    }


    private double getSim(Document fromDoc, Document toDoc) {
        double sim = 0;
        for(String testAttrib : fromDoc.getFreqs().keySet()) {
            double totalDist = 0;
            Iterator<String> keys = toDoc.getFreqs().keySet().iterator();
            Iterator<Double> values = toDoc.getFreqs().values().iterator();
            while(keys.hasNext()) {
                String trainAttrib = keys.next();
                double dist = getDistance(testAttrib, trainAttrib);
                if(dist != 0) {
                    dist /= 1;
                    dist *= values.next() / toDoc.getTotalFreq();
                    totalDist +=dist;
                }

            }
            sim += totalDist*fromDoc.getFreqs().get(testAttrib)/fromDoc.getTotalFreq();
        }

        return sim;
    }


    public ArrayList<Document> getSimsForDoc(Document testDoc, ArrayList<Document> trainDocs) {
        HashMap<String, Double> sims = new HashMap<String, Double>();
        ArrayList<Document> docsims = new ArrayList<Document>();
        int i=0;
        for(Document currDoc : trainDocs) {
            long ts = System.currentTimeMillis();
            Document doc = new Document(currDoc.getObjClass(), getSim(testDoc, currDoc));
            docsims.add(doc);
            System.out.println("Op took: "+ (System.currentTimeMillis() - ts));
            dbManager.commit();
        }
        long ts = System.currentTimeMillis();

        Collections.sort(docsims);
        return docsims;
    }

}
