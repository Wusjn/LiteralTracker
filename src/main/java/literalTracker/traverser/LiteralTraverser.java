package literalTracker.traverser;

import javafx.util.Pair;
import literalTracker.RepoMetaData;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.parser.Parser;
import literalTracker.utils.DirExplorer;
import literalTracker.visitor.LiteralVisitor;

import java.io.File;
import java.util.List;

public class LiteralTraverser {

    public static Pair<List<SimpleLiteralNode>, NodesByCategory> collectLiterals(GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser){
        LiteralVisitor visitor = new LiteralVisitor();
        LiteralVisitor.Arg arg = new LiteralVisitor.Arg(graphFactory);
        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java"),
                (level, path, file) -> {
                    try {
                        arg.path = path;
                        arg.fileName = file.getName();
                        parser.parse(file, visitor, arg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );
        for (String targetDir : repoMetaData.getJavaSources()){
            dirExplorer.explore(new File(targetDir));
        }
        return new Pair<>(arg.simpleLiteralNodes, NodesByCategory.categorizeNodes(arg.newlyCreatedNodes));
    }
}
