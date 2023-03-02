package literalTracker.featureExtractor;

import literalTracker.Settings;
import literalTracker.featureExtractor.metrics.Metrics;
import literalTracker.lpGraph.Snapshot;
import literalTracker.utils.Counter;
import literalTracker.utils.DataManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Metrics.getMetrics(optionTreeSamples);
        Metrics.getMetrics(literalTreeSamples);
        //DataManager.saveTreeSamples(literalTreeSamples, dataPaths.getLiteralSamplePath().replace(".json", ""));
        //DataManager.saveTreeSamples(optionSamplesWithConfigNames, dataPaths.getORPSamplePath().replace(".json", ""));

        List<List<List<String>>> literalCodeList = SampleEncoder.encode(literalTreeSamples);
        List<List<List<String>>> orpCodeList = SampleEncoder.encode(optionSamplesWithConfigNames);

        boolean shallFilterOutEmptyCode = true;
        if (shallFilterOutEmptyCode){
            literalCodeList = filterOutEmptyCode(literalCodeList);
            orpCodeList = filterOutEmptyCode(orpCodeList);
        }


        String orpSamplePath = dataPaths.getLiteralSamplePath();
        orpSamplePath.replace("LiteralSamplePath.json", "literalCodeList.json");

        DataManager.saveJson(orpSamplePath.replace("LiteralSamplePath.json", "literalCodeList.json"), literalCodeList);
        DataManager.saveJson(orpSamplePath.replace("LiteralSamplePath.json", "orpCodeList.json"), orpCodeList);
    }

    public static List<List<List<String>>> filterOutEmptyCode(List<List<List<String>>> codesList){
        List<List<List<String>>> newCodesList = new ArrayList<>();
        for (List<List<String>> codes : codesList){
            List<List<String>> newCodes = new ArrayList<>();
            for (List<String> code : codes){
                if (code.size() != 0){
                    newCodes.add(code);
                }
            }
            if (newCodes.size() != 0){
                newCodesList.add(newCodes);
            }
        }
        return newCodesList;
    }
}
