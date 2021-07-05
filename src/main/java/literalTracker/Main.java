package literalTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.parser.Parser;
import literalTracker.traverser.LiteralTraverser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.traverser.Traverser;
import literalTracker.utils.DataManager;

import java.io.*;
import java.util.HashMap;
import java.util.List;


public class Main {

    public static void main(String[] args) {

        long startTime =  System.currentTimeMillis();
        System.out.println("-----------Running----------");

        RepoMetaData repoMetaData = new RepoMetaData("./data/sources.json");
        Parser parser = new Parser(repoMetaData.javaSources);

        NodeFactory.lpGraph.nodeByLocation = (HashMap<String, BaseNode>) DataManager.readIntermediateData("./data/lpGraph");
        if (NodeFactory.lpGraph.nodeByLocation == null){
            NodeFactory.lpGraph.nodeByLocation = new HashMap<>();
            NodesByCategory initialTrackedNodes = getInitialTrackedNodes(repoMetaData, parser);
            Traverser.traversal(repoMetaData, parser, initialTrackedNodes);
            //NodeFactory.lpGraph.deletePropagationOfNonFinalValue();

            DataManager.writeIntermediateData("./data/lpGraph", NodeFactory.lpGraph.nodeByLocation);
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                        new File("./data/lpGraph.json"),
                        NodeFactory.lpGraph.nodeByLocation
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        System.out.println("-----------Finish----------");
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("Time used: " + usedTime + " s");
    }

    public static NodesByCategory getInitialTrackedNodes(RepoMetaData repoMetaData, Parser parser){

        Pair<List<SimpleLiteralNode>, NodesByCategory> pair = LiteralTraverser.collectLiterals(repoMetaData, parser);
        List<SimpleLiteralNode> literalNodes = pair.getKey();
        NodesByCategory nodesByCategory = pair.getValue();

        return nodesByCategory;
    }
}
