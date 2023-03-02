package literalTracker.utils;

import literalTracker.Settings;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.DatabaseException;

public class CypherWriter implements AutoCloseable {
    private final Driver driver;

    public CypherWriter(String uri, String user, String password){
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close(){
        driver.close();
    }

    private String cypherCreteNode(BaseNode node){
        StringBuilder sb = new StringBuilder();
        Counter counter = new Counter();
        //MERGE (n:Node {range:"1-1:2-2"}) MERGE (n) -[r:Propagate]-> (m:Node {range:"2-2:3-3"}) MERGE (n) -[r:Propagate]-> (p:Node {range:"3-3:4-4"})
        sb.append(node.toCypherLink());
        for (BaseNode nextNode : node.getNextNodes()){
            counter.add(1);
            sb.append(nextNode.toCypherLink(counter));
            sb.append("\n");
        }
        return sb.toString();
    }

    private void deleteDatabase(String databaseName){
        try(Session session = driver.session((SessionConfig.forDatabase( databaseName )))) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n)\n" +
                        "OPTIONAL MATCH (n)-[r]-()\n" +
                        "DELETE n,r");
                return 0;
            });
        }
    }


    private void writeGraph(LPGraph lpGraph, String databaseName){
        deleteDatabase(databaseName);
        try(Session session = driver.session((SessionConfig.forDatabase( databaseName )))){
            session.writeTransaction(tx -> {
                tx.run("CREATE INDEX hashkey2node\n" +
                        "FOR (n:Node)\n" +
                        "ON (n.hashKey)");
                return 0;
            });
            for (BaseNode node : lpGraph.getNodeByLocation().values()){
                final String cypherStatement = cypherCreteNode(node);

                session.writeTransaction(tx ->{
                    tx.run(cypherStatement);
                    return 0;
                });
            }
        }
    }


    public static void writeGraph2Cypher(LPGraph lpGraph, String databaseName) throws LPGraphException {
        try(CypherWriter cypherWriter = new CypherWriter(Settings.getNeo4jUri(), Settings.getNeo4jUserName(), Settings.getNeo4jPassword())){
            cypherWriter.writeGraph(lpGraph, databaseName);
        }
    }
}
