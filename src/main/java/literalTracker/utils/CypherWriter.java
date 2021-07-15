package literalTracker.utils;

import com.github.javaparser.utils.StringEscapeUtils;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.node.BaseNode;
import org.neo4j.driver.*;

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
        sb.append(node.toCypher());
        for (BaseNode nextNode : node.getNextNodes()){
            counter.add(1);
            sb.append(nextNode.toCypher(counter));
            sb.append("\n");
        }
        return sb.toString();
    }


    private void writeGraph(LPGraph lpGraph){
        try(Session session = driver.session((SessionConfig.forDatabase( "lpgraph" )))){
            session.writeTransaction(tx -> {
                tx.run("CREATE INDEX\n" +
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


    public static void writeGraph2Cypher(LPGraph lpGraph) throws Exception {
        try(CypherWriter cypherWriter = new CypherWriter("bolt://localhost:7687", "wusj", "730115")){
            cypherWriter.writeGraph(lpGraph);
        }
    }
}
