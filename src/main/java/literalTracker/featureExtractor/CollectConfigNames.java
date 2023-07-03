package literalTracker.featureExtractor;

import java.util.*;

public class CollectConfigNames {
    public static Map<String, Set<String>> collect(List<TreeSample> treeSamples) {
        Map<String, Set<String>> configsByProject = new HashMap<>();
        for (TreeSample sample : treeSamples){
            String project = sample.project;
            if (!project.equals("none")){
                configsByProject.putIfAbsent(project,new HashSet<>());
                configsByProject.get(project).addAll(sample.samples.get(0).configNames);
            }
        }


        return configsByProject;
    }
}
