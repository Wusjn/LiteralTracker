package literalTracker.traverser;

import literalTracker.RepoMetaData;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.parser.Parser;
import literalTracker.utils.DirExplorer;
import literalTracker.visitor.ExpressionVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Traverser {

    public static NodesByCategory traversalForOnePass(GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser, NodesByCategory trackedNodes){
        ExpressionVisitor expressionVisitor = new ExpressionVisitor();
        ExpressionVisitor.Arg arg = new ExpressionVisitor.Arg(graphFactory, trackedNodes);


        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java"),
                (level, path, file) -> {
                    try {
                        arg.path = path;
                        arg.fileName = file.getName();

                        parser.parse(file, expressionVisitor, arg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );
        for (String targetDir : repoMetaData.getJavaSources()) {
            dirExplorer.explore(new File(targetDir));
        }

        return NodesByCategory.categorizeNodes(arg.newlyCreatedNodes);
    }

    public static void traversal(GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser, NodesByCategory trackedNodes){
        while (!trackedNodes.noTrackedNodes()){
            trackedNodes = traversalForOnePass(graphFactory, repoMetaData, parser, trackedNodes);
        }
    }
}
