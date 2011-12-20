package ae.masdar.datamining;

import ae.masdar.datamining.db.DatabaseManager;
import ae.masdar.datamining.db.Relationship;
import ae.masdar.datamining.db.WordNode;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import weka.classifiers.lazy.kstar.KStarWrapper;

import java.io.*;
import java.net.URLEncoder;
import java.sql.CallableStatement;
import java.util.*;

public class Starter {
    private static final String[] API_KEYS = {"2ea08600b3243f4da3617a44102d051a", "c659c5cc82c143c4754d22a866c5cfeb", "da87e9e1aac29aeb7d292596a0f15989", "829131d2bc9d95cfa627b9b743c464c3"};
    private int apikeyIndex = 0;
    DatabaseManager dbManager = new DatabaseManager();
    DistanceFinder finder;
    private ArrayList<String> processed = new ArrayList<String>();
        FileWriter fstream;
      BufferedWriter out;

    private List<String> doWord(String word) {

        String url = null;
        try {
            url = "http://words.bighugelabs.com/api/2/" + API_KEYS[apikeyIndex] + "/" + URLEncoder.encode(word, "UTF-8") + "/json";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject jsObj = null;
        try {
            jsObj = Crawler.getJson(url);
        } catch (LimitExceededException e) {
            apikeyIndex++;
            if (apikeyIndex > API_KEYS.length - 1) {
                System.out.println("PANIC!: Keys are over, please add more or wait 24 hours");
                System.exit(1);
            }
            System.out.println("WARNING: Key limit exceeded, taking key number " + apikeyIndex);
            doWord(word);
        }

        return dbManager.addWord(word, jsObj);
    }

    public void processList(List<String> wordList, int depth) {
        if (depth == 0)
            return;
        for (String word : wordList) {
            if (!processed.contains(word)) {
                processed.add(word);
                processList(doWord(word), depth - 1);
            }
        }
    }

    private void crawler() {
        List<String> words = doWord("oil");
        for (String word : words) {
            processList(doWord(word), 5);
        }
        System.out.println("Total words: " + processed.size());


        long ts = System.currentTimeMillis();
        Path path = dbManager.findShortestPath("work", "hair style", Relationship.SYNONIM_NOUN);
        if (path == null) {
            System.out.println("Can not find words in index");
            System.exit(1);
        }
        ts = System.currentTimeMillis() - ts;
        StringBuilder strBld = new StringBuilder();
        for (Node node : path.nodes()) {
            strBld.append(node.getProperty(WordNode.WORD_PROPERTY)).append("  ");
        }
        System.out.println(strBld.toString());
        System.out.println("Path length: " + path.length());
        System.out.println("Time: " + ts);
    }

    private void distance() {
        DistanceFinder finder = new DistanceFinder(dbManager);
        System.out.println(finder.getDistance("oil", "gas"));
    }

    private ArrayList<Document> getDocsInDir(String dir) {
        ArrayList<Document> docs = new ArrayList<Document>();
        File dirFile = new File(dir);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        };
        for (File docFile : dirFile.listFiles(filter)) {
            docs.addAll(new ArffFileParser(docFile).getDocumentsWithSortedAttrib());
        }
        return docs;
    }


    public String classify(Document test, ArrayList<Document> train, int k) {

        ArrayList<Document> arr = finder.getSimsForDoc(
                test,
                train);

        for (Document doc : arr) {
            System.out.println(doc.getObjClass() + " : " + doc.getSim());
        }
        openStream("k3.txt");
        write(test.getObjClass() + " " +classifyK(arr, 3)+ "\n");

        openStream("k5.txt");
        write(test.getObjClass() + " " +classifyK(arr, 5)+ "\n");

        openStream("k7.txt");
        write(test.getObjClass() + " " +classifyK(arr, 7)+ "\n");

        openStream("k9.txt");
        write(test.getObjClass() + " " +classifyK(arr, 9)+ "\n");

        openStream("k13.txt");
        write(test.getObjClass() + " " +classifyK(arr, 13)+ "\n");

        openStream("k15.txt");
        write(test.getObjClass() + " " +classifyK(arr, 15)+ "\n");

        openStream("tt.txt");
        return classifyK(arr, 3);
    }

    public String classifyK(ArrayList<Document> arr, int k) {

        HashMap<String, Integer> scores = new HashMap<String, Integer>();
        for (int i = arr.size() - 1; i >= arr.size() - 1 - k && i >= 0; i--) {
            if (!scores.containsKey(arr.get(i).getObjClass())) {
                scores.put(arr.get(i).getObjClass(), 1);
            } else {
                scores.put(arr.get(i).getObjClass(), scores.get(arr.get(i).getObjClass()) + 1);
            }
        }

        String maxscore = "";
        int max = Integer.MIN_VALUE;
        for (Map.Entry entry : scores.entrySet()) {
            if ((Integer) entry.getValue() > max) {
                max = (Integer) entry.getValue();
                maxscore = (String) entry.getKey();
            }
        }
        return maxscore;
    }

    private void write(String data) {
        try {
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void algo() {
        boolean crawl = false;


        ArrayList<Document> trainDocs = getDocsInDir("/home/rafael/workspaces/IdeaProjects/TextMiner/data/train");
        ArrayList<Document> testDocs = getDocsInDir("/home/rafael/workspaces/IdeaProjects/TextMiner/data/test");

        if (crawl) {
            ArrayList<String> words = new ArrayList<String>();
            for (Document doc : trainDocs) {
                words.add(doc.getObjClass());
            }
            for (Document doc : testDocs) {
                words.add(doc.getObjClass());
            }
            processList(words, 4);

        } else {
            for(Document testDoc : testDocs) {
                String newClass = classify(testDoc, trainDocs, 5);
                System.out.println(testDoc.getObjClass() + " " + newClass);
            }


            //System.out.println("Winner class: " + maxscore + " with score:" + max);
        }
    }

    private void openStream(String filename) {
        if (out != null)
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            fstream = new FileWriter(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out = new BufferedWriter(fstream);
    }

    public Starter() {

        finder = new DistanceFinder(dbManager);
        try {
            algo();
        } finally {
            dbManager.shutdown();
        }

/*
       ArrayList<String> lst = new ArrayList<String>();
       lst.add("acids");
       lst.add("africa");
       lst.add("agriculture");
       lst.add("affected");
       lst.add("war");
       lst.add("imports");
       lst.add("discounts");
       lst.add("accelerated");
       lst.add("intervention");
       processList(lst, 6);*/
        //doWord("crude");

    }


    public static void main(String[] args) {
        new Starter();
    }

}
