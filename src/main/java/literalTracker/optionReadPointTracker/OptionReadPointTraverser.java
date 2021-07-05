package literalTracker.optionReadPointTracker;

import javafx.util.Pair;
import literalTracker.RepoMetaData;
import literalTracker.parser.Parser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.utils.DirExplorer;

import java.io.File;
import java.util.List;
import java.util.Map;

public class OptionReadPointTraverser {

    public static Pair<Map<String, List<OptionReadPointNode>>, NodesByCategory> collectOptionReadPoints(RepoMetaData repoMetaData, Parser parser, List<String> configKeys){
        MethodVisitor visitor = new MethodVisitor();
        MethodVisitor.Arg arg = new MethodVisitor.Arg(configKeys);
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
        for (String targetDir : repoMetaData.javaSources){
            dirExplorer.explore(new File(targetDir));
        }
        return new Pair<>(arg.optionReadPointsByConfigKey, NodesByCategory.categorizeNodes(arg.newlyCreatedNodes));
    }
}
