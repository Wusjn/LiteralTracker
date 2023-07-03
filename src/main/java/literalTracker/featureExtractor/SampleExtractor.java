package literalTracker.featureExtractor;

import literalTracker.Settings;
import literalTracker.featureExtractor.metrics.Metrics;
import literalTracker.lpGraph.Snapshot;
import literalTracker.utils.Counter;
import literalTracker.utils.DataManager;

import java.util.*;

import static literalTracker.ORPTracker.loadConfigNames;

public class SampleExtractor {
    public static void main(String[] args) throws Exception {
        Settings.DataPaths dataPaths = Settings.getDataPaths();

        Snapshot snapshot = DataManager.loadSnapshot(dataPaths.getSnapshotPath());
        List<TreeSample> literalTreeSamples = SampleFactory.fromLPGraph(snapshot.getLpGraph());
        List<TreeSample> optionTreeSamples = SampleFactory.fromOPGraph(snapshot.getOpGraph());


        List<TreeSample> optionSamplesWithConfigNames = new ArrayList<>();
        Set<String> configKeys = new HashSet<>(loadConfigNames(dataPaths.getConfigNamesPath()));
        for (TreeSample treeSample : optionTreeSamples){
            for (String configName : treeSample.samples.get(0).configNames){
                if (configKeys.contains(configName)){
                    optionSamplesWithConfigNames.add(treeSample);
                    break;
                }
            }
        }

        DataManager.saveJson(dataPaths.getLiteralSamplePath(), literalTreeSamples);
        DataManager.saveJson(dataPaths.getORPSamplePath(), optionSamplesWithConfigNames);
        DataManager.saveJson(dataPaths.getORPSamplePath().replace("ORPSamplePath.json","configs.json"), CollectConfigNames.collect(optionSamplesWithConfigNames));


        Metrics.getMetrics(optionTreeSamples);
        Metrics.getMetrics(literalTreeSamples);
        //DataManager.saveTreeSamples(literalTreeSamples, dataPaths.getLiteralSamplePath().replace(".json", ""));
        //DataManager.saveTreeSamples(optionSamplesWithConfigNames, dataPaths.getORPSamplePath().replace(".json", ""));

        Map<String,List<List<List<String>>>> literalCodeMap = SampleEncoder.encode(literalTreeSamples);
        Map<String,List<List<List<String>>>> orpCodeMap = SampleEncoder.encode(optionSamplesWithConfigNames);

        boolean shallFilterOutEmptyCode = true;
        if (shallFilterOutEmptyCode){
            literalCodeMap = filterOutEmptyCode(literalCodeMap);
            orpCodeMap = filterOutEmptyCode(orpCodeMap);
        }


        String orpSamplePath = dataPaths.getLiteralSamplePath();
        orpSamplePath.replace("LiteralSamplePath.json", "literalCodeList.json");

        DataManager.saveJson(orpSamplePath.replace("LiteralSamplePath.json", "literalCodeList.json"), literalCodeMap);
        DataManager.saveJson(orpSamplePath.replace("LiteralSamplePath.json", "orpCodeList.json"), orpCodeMap);
    }

    public static Map<String,List<List<List<String>>>> filterOutEmptyCode(Map<String,List<List<List<String>>>> codesMap){
        Map<String,List<List<List<String>>>> newCodesMap = new HashMap<>();
        for (String project : codesMap.keySet()){
            newCodesMap.put(project, new ArrayList<>());
            for (List<List<String>> codes : codesMap.get(project)){
                List<List<String>> newCodes = new ArrayList<>();
                for (List<String> code : codes){
                    if (code.size() != 0){
                        newCodes.add(code);
                    }
                }
                if (newCodes.size() != 0){
                    newCodesMap.get(project).add(newCodes);
                }
            }
        }
        return newCodesMap;
    }
}
