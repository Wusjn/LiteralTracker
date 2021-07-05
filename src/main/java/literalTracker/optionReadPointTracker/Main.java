package literalTracker.optionReadPointTracker;

import javafx.util.Pair;
import literalTracker.RepoMetaData;
import literalTracker.parser.Parser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.traverser.Traverser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        RepoMetaData repoMetaData = new RepoMetaData("./data/sources.json");
        Parser parser = new Parser(repoMetaData.javaSources);

        //TODO: read config keys from file
        List<String> configKeys = new ArrayList<>();

        NodesByCategory initialTrackedNodes = getInitialTrackedNodes(repoMetaData, parser, configKeys);
        Traverser.traversal(repoMetaData, parser, initialTrackedNodes);

    }

    public static NodesByCategory getInitialTrackedNodes(RepoMetaData repoMetaData, Parser parser, List<String> configKeys){

        Pair<Map<String, List<OptionReadPointNode>>, NodesByCategory> pair = OptionReadPointTraverser.collectOptionReadPoints(repoMetaData, parser, configKeys);
        Map<String, List<OptionReadPointNode>> optionReadPointsByConfigKey = pair.getKey();
        NodesByCategory nodesByCategory = pair.getValue();

        return nodesByCategory;
    }
}
