package ae.masdar.datamining.db;

import org.neo4j.graphdb.RelationshipType;

public enum Relationship implements RelationshipType {
    SYNONYM,
    SYNONIM_NOUN,
    SYNONIM_VERB

}
