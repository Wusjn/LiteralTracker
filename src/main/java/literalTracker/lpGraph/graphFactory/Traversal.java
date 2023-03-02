package literalTracker.lpGraph.graphFactory;

import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.graphFactory.visitor.*;

import literalTracker.utils.DirExplorer;

import java.io.File;
import java.util.List;

public class Traversal {

    public static NodesByCategory literalTraversal(GraphFactory graphFactory){
        LiteralVisitor literalVisitor = new LiteralVisitor();
        LiteralVisitor.Arg arg = new LiteralVisitor.Arg(graphFactory);

        return dirTraversal(literalVisitor, arg);
    }

    public static NodesByCategory orpTraversal(
            GraphFactory graphFactory, List<String> configKeys, LPGraph lpGraph
    ){
        ORPVisitor ORPVisitor = new ORPVisitor();
        ORPVisitor.Arg arg = new ORPVisitor.Arg(graphFactory, configKeys, lpGraph);

        return dirTraversal(ORPVisitor, arg);
    }


    public static NodesByCategory expressionTraversal(GraphFactory graphFactory, NodesByCategory trackingNodes){
        ExpressionVisitor expressionVisitor = new ExpressionVisitor();
        ExpressionVisitor.Arg arg = new ExpressionVisitor.Arg(graphFactory, trackingNodes);

        if (arg.noTrackingNodes()){
            return new NodesByCategory();
        }else {
            return dirTraversal(expressionVisitor, arg);
        }

    }
    public static NodesByCategory methodCallTraversal(GraphFactory graphFactory, NodesByCategory trackingNodes){
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor();
        MethodCallVisitor.Arg arg = new MethodCallVisitor.Arg(graphFactory, trackingNodes);

        if (arg.noTrackingNodes()){
            return new NodesByCategory();
        }else {
            return dirTraversal(methodCallVisitor, arg);
        }

    }


    public static NodesByCategory dirTraversal(BaseVisitor<? extends BaseVisitor.Arg> visitor, BaseVisitor.Arg arg){
        GraphFactory graphFactory = arg.graphFactory;

        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java"),
                (level, path, file) -> {
                    try {
                        arg.path = path;
                        arg.fileName = file.getName();
                        graphFactory.parser.parse(file, visitor, arg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );
        for (String targetDir : graphFactory.repoMetaData.getJavaSources()) {
            dirExplorer.explore(new File(targetDir));
        }

        return NodesByCategory.categorizeNodes(arg.newlyTrackedNodes);
    }
}
