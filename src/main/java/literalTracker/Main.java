package literalTracker;

import javafx.util.Pair;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.orpTracker.ORPTracker;
import literalTracker.parser.Parser;
import literalTracker.traverser.LiteralTraverser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.traverser.SnapShot;
import literalTracker.traverser.Traverser;
import literalTracker.utils.CypherWriter;
import literalTracker.utils.DataManager;
import lombok.Getter;

import java.util.List;


public class Main {

    @Getter
    public static class DataPaths{
        private String metaDataPath;
        private String snapShotPath;
        private String jsonPath;

        public DataPaths(String metaDataPath, String snapShotPath, String jsonPath){
            this.metaDataPath = metaDataPath;
            this.snapShotPath = snapShotPath;
            this.jsonPath = jsonPath;
        }
    }

    public static void main(String[] args) throws Exception {

        DataPaths dataPaths = new DataPaths(
                "./data/sources.json",
                "./data/results/snapshot",
                "./data/results/lpGraph.json"
                );
        DataPaths testDataPath = new DataPaths(
                "./data/sources_test.json",
                "./data/results/snapshot_test",
                "./data/results/lpGraph_test.json"
        );

        boolean isTestMode = false;
        if (isTestMode){
            dataPaths = testDataPath;
        }


        long startTime =  System.currentTimeMillis();
        System.out.println("-----------Running----------");

        RepoMetaData repoMetaData = new RepoMetaData(dataPaths.getMetaDataPath());
        Parser parser = new Parser(repoMetaData.getJavaSources());
        GraphFactory graphFactory = new GraphFactory();

        SnapShot snapShot = loadSnapShot(dataPaths.getSnapShotPath());
        if (snapShot == null){
            snapShot = new SnapShot(null, null, null);
            NodesByCategory initialTrackedNodes = getInitialTrackedNodes(graphFactory, repoMetaData, parser);
            snapShot.setLpGraph(graphFactory.getLpGraph());
            snapShot.setTrackedNodes(initialTrackedNodes);
            saveSnapShot(snapShot, dataPaths.getSnapShotPath());

            Traverser.traversal(graphFactory,repoMetaData, parser, initialTrackedNodes);
            graphFactory.getLpGraph().deletePropagationOfNonFinalValue();
            snapShot.setLpGraph(graphFactory.getLpGraph());
            snapShot.setTrackedNodes(null);
            saveSnapShot(snapShot, dataPaths.getSnapShotPath());
        }else {
            if (snapShot.getTrackedNodes() == null) {
                graphFactory.setLpGraph(snapShot.getLpGraph());
                //ORPTracker.trackORP(repoMetaData, parser, graphFactory.getLpGraph());
            } else {
                graphFactory.setLpGraph(snapShot.getLpGraph());
                Traverser.traversal(graphFactory, repoMetaData, parser, snapShot.getTrackedNodes());
                graphFactory.getLpGraph().deletePropagationOfNonFinalValue();
                snapShot.setLpGraph(graphFactory.getLpGraph());
                snapShot.setTrackedNodes(null);
                saveSnapShot(snapShot, dataPaths.getSnapShotPath());
            }
        }

        if (snapShot.getOpGraph() == null){
            LPGraph opGraph = ORPTracker.trackORP(repoMetaData, parser, graphFactory.getLpGraph());
            snapShot.setOpGraph(opGraph);
            saveSnapShot(snapShot, dataPaths.getSnapShotPath());
        }

        CypherWriter.writeGraph2Cypher(snapShot.getLpGraph());

        System.out.println("-----------Finish----------");
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("Time used: " + usedTime + " s");
    }

    public static NodesByCategory getInitialTrackedNodes(GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser){
        Pair<List<SimpleLiteralNode>, NodesByCategory> pair = LiteralTraverser.collectLiterals(graphFactory, repoMetaData, parser);
        List<SimpleLiteralNode> literalNodes = pair.getKey();
        NodesByCategory nodesByCategory = pair.getValue();

        return nodesByCategory;
    }

    public static SnapShot loadSnapShot(String path){
        SnapShot snapShot = (SnapShot) DataManager.readIntermediateData(path);
        return snapShot;
    }
    public static void saveSnapShot(SnapShot snapShot, String path){
        DataManager.writeIntermediateData(path, snapShot);
    }

}
