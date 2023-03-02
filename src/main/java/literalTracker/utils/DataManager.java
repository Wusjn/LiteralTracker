package literalTracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import literalTracker.featureExtractor.TreeSample;
import literalTracker.lpGraph.Snapshot;

import java.io.*;
import java.util.List;

public class DataManager {
    public static Object readIntermediateData(String filePath){
        Object object = null;
        File file = new File(filePath);
        if (file.exists()){
            try {
                object = new ObjectInputStream(new FileInputStream(file)).readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static void writeIntermediateData(String filePath, Object object){
        File file = new File(filePath);
        try {
            new ObjectOutputStream(new FileOutputStream(file)).writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveJson(String path, Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(path),
                    object
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Snapshot loadSnapshot(String path){
        Snapshot snapshot = (Snapshot) DataManager.readIntermediateData(path);
        return snapshot;
    }
    public static void saveSnapshot(Snapshot snapshot, String path){
        DataManager.writeIntermediateData(path, snapshot);
    }

    public static List<TreeSample> loadTreeSamples(String path){
        List<TreeSample> treeSamples = (List<TreeSample>) DataManager.readIntermediateData(path);
        return treeSamples;
    }
    public static void saveTreeSamples(List<TreeSample> treeSamples, String path){
        DataManager.writeIntermediateData(path, treeSamples);
    }
}
