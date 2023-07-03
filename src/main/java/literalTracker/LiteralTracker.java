package literalTracker;

import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.parser.Parser;
import literalTracker.parser.RepoMetaData;
import literalTracker.lpGraph.graphFactory.NodesByCategory;
import literalTracker.lpGraph.Snapshot;
import literalTracker.utils.CypherWriter;
import literalTracker.utils.DataManager;


public class LiteralTracker {
    public static void main(String[] args) throws LPGraphException {

        Settings.DataPaths dataPaths = Settings.getDataPaths();

        long startTime =  System.currentTimeMillis();
        System.out.println("-----------Running----------");

        RepoMetaData repoMetaData = new RepoMetaData(dataPaths.getMetaDataPath());
        Parser parser = new Parser(repoMetaData.getJavaSources());
        GraphFactory graphFactory = new GraphFactory(repoMetaData, parser);

        Snapshot snapshot = loadSnapshot(dataPaths, graphFactory);

        if (snapshot.getOpGraph() == null || Settings.shallCreateORPGraphAgain()){
            LPGraph opGraph = ORPTracker.trackORP(repoMetaData, dataPaths.getConfigNamesPath(), parser, graphFactory.lpGraph);
            snapshot.setOpGraph(opGraph);
            DataManager.saveSnapshot(snapshot, dataPaths.getSnapshotPath());
        }

        if (Settings.shallWriteCypher()){
            CypherWriter.writeGraph2Cypher(snapshot.getLpGraph(), "lpgraph");
            CypherWriter.writeGraph2Cypher(snapshot.getOpGraph(), "opgraph");
        }


        System.out.println("-----------Finish----------");
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("Time used: " + usedTime + " s");
    }

    public static Snapshot loadSnapshot(Settings.DataPaths dataPaths, GraphFactory graphFactory) throws LPGraphException{
        //read data from break point
        Snapshot snapshot = DataManager.loadSnapshot(dataPaths.getSnapshotPath());
        if (snapshot == null){
            //nothing saved, do it from the beginning
            snapshot = new Snapshot(null, null, null);
            NodesByCategory initialTrackedNodes = graphFactory.getInitialTrackedNodesFromLiterals();
            snapshot.setLpGraph(graphFactory.lpGraph);
            snapshot.setTrackedNodes(initialTrackedNodes);
            DataManager.saveSnapshot(snapshot, dataPaths.getSnapshotPath());

            graphFactory.createGraph(initialTrackedNodes);
            //graphFactory.lpGraph.deletePropagationOfNonFinalValue();
            graphFactory.lpGraph.propagateValue();
            snapshot.setLpGraph(graphFactory.lpGraph);
            snapshot.setTrackedNodes(null);
            DataManager.saveSnapshot(snapshot, dataPaths.getSnapshotPath());
        }else {
            if (snapshot.getTrackedNodes() == null) {
                //the lpGraph had been created, just load it
                graphFactory.lpGraph = snapshot.getLpGraph();
                //ORPTracker.trackORP(repoMetaData, parser, graphFactory.getLpGraph());
            } else {
                //only scanned the literals, continue with expression traversal
                graphFactory.lpGraph = snapshot.getLpGraph();
                graphFactory.createGraph(snapshot.getTrackedNodes());
                //graphFactory.lpGraph.deletePropagationOfNonFinalValue();
                graphFactory.lpGraph.propagateValue();
                snapshot.setLpGraph(graphFactory.lpGraph);
                snapshot.setTrackedNodes(null);
                DataManager.saveSnapshot(snapshot, dataPaths.getSnapshotPath());
            }
        }

        return snapshot;
    }

}
