package ae.masdar.datamining.db;


import org.neo4j.graphdb.Node;

public class WordNode {

    public static final String WORD_PROPERTY = "Label";
    public static final String TYPE_PROPERTY = "type";

    private Node underlyingNode;

    public WordNode(Node node) {
        this.underlyingNode = node;
    }

    public final String getWord() {
        return ( String ) underlyingNode.getProperty(WORD_PROPERTY);
    }

    public void setWord( final String word ) {
        underlyingNode.setProperty(WORD_PROPERTY, word);
    }



}
