package literalTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.parser.RepoMetaData;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.LPGraph;
import literalTracker.parser.Parser;
import literalTracker.lpGraph.graphFactory.NodesByCategory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ORPTracker {
    public static LPGraph trackORP(RepoMetaData repoMetaData, String configNamesPath, Parser parser, LPGraph lpGraph) throws LPGraphException {
        GraphFactory graphFactory = new GraphFactory(repoMetaData, parser);

        List<String> configKeys = loadConfigNames(configNamesPath);

        NodesByCategory initialTrackedNodes = graphFactory.getInitialTrackedNodesFromORPs(configKeys, lpGraph);
        graphFactory.createGraph(initialTrackedNodes);
        //graphFactory.lpGraph.deletePropagationOfNonFinalValue();
        graphFactory.lpGraph.propagateValue();

        return graphFactory.lpGraph;
    }

    //read config keys from file
    public static List<String> loadConfigNames(String configNamesPath){
        List<String> configs = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            configs = objectMapper.readValue(
                    new File(configNamesPath),
                    objectMapper.getTypeFactory().constructParametricType(
                            List.class,
                            String.class
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configs;
    }
}
