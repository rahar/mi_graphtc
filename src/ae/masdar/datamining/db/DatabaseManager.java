package ae.masdar.datamining.db;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class DatabaseManager {
    private static final String DB_PATH = "/srv/db";

    private GraphDatabaseService db;
    RecordManager recman;
    HTree hashtable;
    String fruit;
    String color;

    private IndexManager indexManager;
    private Index<Node> wordsIndex;
    PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
            Traversal.expanderForTypes(Relationship.SYNONIM_NOUN, Direction.OUTGOING), 10);

    public DatabaseManager() {

        db = new EmbeddedGraphDatabase(DB_PATH);

        registerShutdownHook(db);

        indexManager = db.index();
        wordsIndex = indexManager.forNodes("words");

        Properties props = new Properties();
        try {
            recman = RecordManagerFactory.createRecordManager("words", props);
            long recid = recman.getNamedObject("distances");
            if (recid != 0) {
                hashtable = HTree.load(recman, recid);
            } else {
                hashtable = HTree.createInstance(recman);
                recman.setNamedObject("distances", hashtable.getRecid());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> addWord(String word, JSONObject jsonObj) {
        ArrayList<String> retval = new ArrayList<String>();
        if (jsonObj == null)
            return retval;
        Transaction tx = null;
        Node sourceWord;

        try {

            JSONArray synNoun = null;
            JSONArray synVerb = null;
            if (jsonObj.has("noun")) {
                synNoun = jsonObj.getJSONObject("noun").getJSONArray("syn");
            }
            if (jsonObj.has("verb")) {
                synVerb = jsonObj.getJSONObject("verb").getJSONArray("syn");
            }

            // no checks for null needed. If object not found an exception is thrown

            tx = db.beginTx();
            sourceWord = getWordNode(word);
            retval.addAll(addArrayToDb(synNoun, sourceWord, Relationship.SYNONIM_NOUN));
            retval.addAll(addArrayToDb(synVerb, sourceWord, Relationship.SYNONIM_VERB));
            tx.success();
        } catch (JSONException e) {
            e.printStackTrace();
            if (tx != null)
                tx.failure();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (tx != null)
                tx.failure();
        } finally {
            if (tx != null)
                tx.finish();
        }
        return retval;
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    private Node getWordNode(String word) {
        Node node = getWordNodeInIndex(word);
        if (node == null) {
            node = db.createNode();
            node.setProperty(WordNode.WORD_PROPERTY, word);
            wordsIndex.add(node, WordNode.WORD_PROPERTY, word);
        }
        return node;
    }

    private Node getWordNodeInIndex(String word) {
        Node node = null;
        IndexHits<Node> hits = wordsIndex.get(WordNode.WORD_PROPERTY, word);
        if (hits.size() > 0) {
            node = hits.getSingle();
        }
        return node;
    }

    private List<String> addArrayToDb(JSONArray arr, Node sourceNode, Relationship rel) {
        ArrayList<String> retval = new ArrayList<String>();
        if (arr == null)
            return retval;
        for (int i = 0; i < arr.length(); i++) {
            String newWord = null;
            try {
                newWord = arr.getString(i);
            } catch (JSONException e) {
                continue;
            }
            System.out.println(newWord);
            retval.add(newWord);
            Node newWordNode = getWordNode(newWord);
            sourceNode.createRelationshipTo(newWordNode, rel);
        }
        return retval;
    }

    public Path findShortestPath(String source, String dest, Relationship rel) {
        Path retVal = null;
        Node srcNode = getWordNodeInIndex(source);
        Node dstNode = getWordNodeInIndex(dest);
        if (srcNode != null && dstNode != null) {
            /*for (Path path : finder.findAllPaths(srcNode, dstNode)) {
                retVal = path;
            }*/
            retVal = finder.findSinglePath(srcNode, dstNode);
        }
        return retVal;

    }

    public void commit() {
        try {
            recman.commit();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void shutdown() {
        try {
            recman.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(String fw, String sw, Integer dist) {
        try {
            hashtable.put(fw + ", " + sw, dist);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Integer get(String fw, String sw) {
        Object el = null;
        try {
            el = hashtable.get(fw + ", " + sw);

            if (el == null) {
                el = hashtable.get(sw + ", " + fw);
                if (el == null)
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return (Integer) el;
    }
}
