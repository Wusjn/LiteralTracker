package literalTracker.featureExtractor.metrics;

import javafx.util.Pair;
import literalTracker.featureExtractor.Sample;
import literalTracker.featureExtractor.TreeSample;
import literalTracker.utils.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Metrics {
    public static void main(String[] args) {

    }

    public static void getMetrics(List<TreeSample> treeSamples){
        List<Integer> pathNums = new ArrayList<>();
        List<Integer> pathLen = new ArrayList<>();

        for (TreeSample treeSample : treeSamples){
            pathNums.add(treeSample.samples.size());
            for (Sample sample : treeSample.samples){
                pathLen.add(sample.names.size());
            }
        }

        System.out.println("path nums :");
        printFrequency(getFrequency(pathNums));
        System.out.println("path len :");
        printFrequency(getFrequency(pathLen));
    }

    public static void printFrequency(List<Pair<Integer,Double>> freq){
        double remain = 1;
        for (Pair<Integer,Double> pair : freq){
            System.out.println("value " + pair.getKey() + "  freq " + pair.getValue());
            remain -= pair.getValue();
        }
        System.out.println("value remain  freq " + remain);
    }

    public static List<Pair<Integer,Double>> getFrequency(List<Integer> nums){
        HashMap<Integer,Integer> map = new HashMap<>();
        for (int num : nums){
            map.put(num, map.getOrDefault(num,0) + 1);
        }
        List<Pair<Integer,Integer>> freqList = new ArrayList<>();
        double sum = 0;
        for (int key : map.keySet()){
            freqList.add(new Pair<>(key, map.get(key)));
            sum += map.get(key);
        }
        freqList.sort((x,y)->(y.getValue()-x.getValue()));

        List<Pair<Integer,Double>> res = new ArrayList<>();
        int resLen = freqList.size();
        if (freqList.size() >= 5){
            resLen = 5;
        }

        for (int i=0;i<resLen;i++){
            Pair<Integer,Integer> pair = freqList.get(i);
            res.add(new Pair<>(pair.getKey(), pair.getValue() / sum));
        }

        return res;
    }
}
