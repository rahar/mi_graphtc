package ae.masdar.datamining;


import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ArffFileParser {
    Instances data;

    public ArffFileParser(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArffFileParser(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Instance> getInstances(int count) {
        ArrayList<Instance> instances = new ArrayList<Instance>();
        Enumeration<Instance> inst = data.enumerateInstances();
        for (int i = 0; i < count && inst.hasMoreElements(); i++)
            instances.add(inst.nextElement());
        return instances;
    }


    public ArrayList<Attribute> getAttributes() {
        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        Enumeration<Attribute> attributeEnumeration = data.enumerateAttributes();
        while (attributeEnumeration.hasMoreElements())
            attributesList.add(attributeEnumeration.nextElement());
        return attributesList;
    }


    public ArrayList<Document> getDocumentsWithSortedAttrib() {
        ArrayList<Document> docs = new ArrayList<Document>();
        Enumeration<Instance> instanceEnumeration = data.enumerateInstances();
        while(instanceEnumeration.hasMoreElements()) {
            Instance inst = instanceEnumeration.nextElement();

            Document doc = new Document();
            for(int i=0; i<inst.numAttributes(); i++) {
                if (inst.attribute(i).name().equalsIgnoreCase("class"))
                    continue;
                doc.getFreqs().put(inst.attribute(i).name(), inst.value(i));
            }
            doc.setFreqs(sortHashMapByValuesD(doc.getFreqs()));
            doc.setObjClass(inst.stringValue(inst.numAttributes()-1));
            docs.add(doc);

        }
        return docs;
    }

    public static LinkedHashMap sortHashMapByValuesD(Map passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap =
            new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }

            }

        }
        return sortedMap;
    }
}
