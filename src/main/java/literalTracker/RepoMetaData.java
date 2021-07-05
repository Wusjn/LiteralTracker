package literalTracker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoMetaData {

    public List<String> javaSources;
    public Map<String, List<String>> javaSourcesByProject;


    public void loadData(String sourcesMetadataPath){

        //read javaSourcesByProject from "./data/sources.json"
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            javaSourcesByProject = objectMapper.readValue(
                    new File(sourcesMetadataPath),
                    objectMapper.getTypeFactory().constructParametricType(
                            HashMap.class,
                            String.class,
                            ArrayList.class
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        //gather all sources, ignoring project
        javaSources = new ArrayList<>();
        for (String project : javaSourcesByProject.keySet()){
            for (String sourceRoot : javaSourcesByProject.get(project)){
                javaSources.add(sourceRoot);
            }
        }

    }

    public RepoMetaData(String sourcesMetadataPath){
        loadData(sourcesMetadataPath);
    }
}
