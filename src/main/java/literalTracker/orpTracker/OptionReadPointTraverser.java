package literalTracker.orpTracker;

import javafx.util.Pair;
import literalTracker.RepoMetaData;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.LPGraph;
import literalTracker.parser.Parser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.utils.DirExplorer;

import java.io.File;
import java.util.List;

public class OptionReadPointTraverser {

    public static Pair<List<ORPNode>, NodesByCategory> collectOptionReadPoints(
            GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser, List<String> configKeys, LPGraph lpGraph
    ){
        MethodVisitor visitor = new MethodVisitor();
        MethodVisitor.Arg arg = new MethodVisitor.Arg(graphFactory, configKeys, lpGraph);
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
        return new Pair<>(arg.optionReadPoints, NodesByCategory.categorizeNodes(arg.newlyCreatedNodes));
    }
}
