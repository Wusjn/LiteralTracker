package literalTracker.lpGraph.graphFactory;

import literalTracker.lpGraph.nodeFactory.NodeFactory;
import literalTracker.parser.RepoMetaData;
import literalTracker.lpGraph.LPGraph;
import literalTracker.parser.Parser;

import java.util.List;


public class GraphFactory {
    public LPGraph lpGraph = new LPGraph();
    public RepoMetaData repoMetaData;
    public Parser parser;
    public NodeFactory nodeFactory;

    public GraphFactory(RepoMetaData repoMetaData, Parser parser){
        this.repoMetaData = repoMetaData;
        this.parser = parser;
        this.nodeFactory = new NodeFactory(this);
    }

    public NodesByCategory getInitialTrackedNodesFromLiterals(){
        return Traversal.literalTraversal(this);
    }

    public NodesByCategory getInitialTrackedNodesFromORPs(List<String> configKeys, LPGraph lpGraph){
        return Traversal.orpTraversal(this, configKeys, lpGraph);
    }

    public void createGraph(NodesByCategory initialTrackingNodes){
        NodesByCategory trackingNodes = initialTrackingNodes;
        while (!trackingNodes.noTrackedNodes()){
            NodesByCategory newTrackingNodes = new NodesByCategory();
            newTrackingNodes.merge(Traversal.expressionTraversal(this ,trackingNodes));
            newTrackingNodes.merge(Traversal.methodCallTraversal(this ,trackingNodes));

            trackingNodes = newTrackingNodes;
        }
    }

}
