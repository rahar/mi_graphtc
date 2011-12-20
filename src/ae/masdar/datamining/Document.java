package ae.masdar.datamining;


import weka.core.Attribute;

import java.util.HashMap;
import java.util.Map;

public class Document implements Cloneable, Comparable{
    String objClass;
    Map<String, Double> freqs = new HashMap<String, Double>();
    Double sim;
    Double totalFreq = null;
    public Document() {
    }

    public Document(String objClass, Double similarity) {
        this.objClass = objClass;
        sim = similarity;
    }

    public Document(Double sim) {
        this.sim = sim;
    }

    public Double getSim() {
        return sim;
    }

    public void setSim(Double sim) {
        this.sim = sim;
    }

    public Map<String, Double> getFreqs() {
        return freqs;
    }

    public void setFreqs(Map<String, Double> freqs) {
        this.freqs = freqs;
    }

    public String getObjClass() {
        return objClass;
    }



    public void setObjClass(String objClass) {
        this.objClass = objClass;
    }

    public Double getTotalFreq() {
        if (totalFreq == null) {
            totalFreq = 0d;
            for(Double d : getFreqs().values())
                totalFreq += d;
        }
        return totalFreq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        if (!freqs.equals(document.freqs)) return false;
        if (objClass != null ? !objClass.equals(document.objClass) : document.objClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objClass != null ? objClass.hashCode() : 0;
        result = 31 * result + freqs.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Document{" +
                "objClass=" + objClass +
                ", freqs=" + freqs +
                '}';
    }


    @Override
    public int compareTo(Object o) {
        return sim.compareTo(((Document)o).getSim());  //To change body of implemented methods use File | Settings | File Templates.
    }
}
