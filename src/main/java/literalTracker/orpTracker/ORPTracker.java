package literalTracker.orpTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import literalTracker.RepoMetaData;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.LPGraph;
import literalTracker.parser.Parser;
import literalTracker.traverser.NodesByCategory;
import literalTracker.traverser.Traverser;
import literalTracker.utils.DataManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ORPTracker {
    public static LPGraph trackORP(RepoMetaData repoMetaData, Parser parser, LPGraph lpGraph) {
        GraphFactory graphFactory = new GraphFactory();

        List<String> configKeys = loadConfigNames();

        NodesByCategory initialTrackedNodes = getInitialTrackedNodes(graphFactory, repoMetaData, parser, configKeys, lpGraph);
        Traverser.traversal(graphFactory , repoMetaData, parser, initialTrackedNodes);
        graphFactory.getLpGraph().deletePropagationOfNonFinalValue();

        return graphFactory.getLpGraph();
    }

    public static NodesByCategory getInitialTrackedNodes(
            GraphFactory graphFactory, RepoMetaData repoMetaData, Parser parser, List<String> configKeys, LPGraph lpGraph
    ){

        Pair<List<ORPNode>, NodesByCategory> pair =
                OptionReadPointTraverser.collectOptionReadPoints(graphFactory, repoMetaData, parser, configKeys, lpGraph);
        List<ORPNode> optionReadPointsByConfigKey = pair.getKey();
        NodesByCategory nodesByCategory = pair.getValue();

        return nodesByCategory;
    }

    //read config keys from file
    public static List<String> loadConfigNames(){
        Map<String, List<String>> configsByProject = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            configsByProject = objectMapper.readValue(
                    new File("./data/configNames.json"),
                    objectMapper.getTypeFactory().constructParametricType(
                            HashMap.class,
                            String.class,
                            ArrayList.class
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> configs = new ArrayList<>();
        for (String project : configsByProject.keySet()){
            List<String> configList = configsByProject.get(project);
            configs.addAll(configList);
        }

        return configs;
    }
}
