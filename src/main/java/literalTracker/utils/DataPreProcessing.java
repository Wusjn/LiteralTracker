package literalTracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPreProcessing {


    public static int countJavaFileNum(File root){
        Counter counter = new Counter();
        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java") && !path.contains("test"),
                (level, path, file) -> {
                    counter.add(1);
                }
        );
        dirExplorer.explore(root);
        return counter.getCount();
    }

    public static void deleteAllNonJavaFiles(File root){
        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> !file.getName().endsWith(".java"),
                (level, path, file) -> {
                    file.delete();
                }
        );
        dirExplorer.explore(root);
    }


    public static int deleteEmptyDir(File root){
        Counter counter = new Counter();
        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> false,
                (level, path, file) -> {}
        );
        dirExplorer.setDirConfig(
                (level, path, file) -> file.listFiles().length == 0,
                (level, path, file) -> {
                    file.delete();
                    counter.add(1);
                    System.out.println("deleting " + path);
                }
        );
        dirExplorer.explore(root);
        return counter.getCount();
    }

    public static void extractProjectSource(File root, File saveTo) {

        Map<String,List<String>> sourcesByProject = new HashMap<>();

        for (File project : root.listFiles()){
            sourcesByProject.put(project.getName(), extractProjectSource(project));
        }

        //save projectSources
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(saveTo, sourcesByProject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> extractProjectSource(File root){
        List<String> sourceList = new ArrayList<>();

        DirExplorer dirExplorer = new DirExplorer(
                (level, path, file) -> false,
                (level, path, file) -> {}
        );
        dirExplorer.setDirConfig(
                (level, path, file) -> path.endsWith("src/main/java"),
                (level, path, file) -> {
                    sourceList.add(path);
                }
        );
        dirExplorer.explore(root);
        return sourceList;
    }

    public static void main(String[] args) {
        File root = new File("./data/hadoop");
        deleteAllNonJavaFiles(root);

        int deletedFileNum = 0;
        do {
            deletedFileNum = deleteEmptyDir(root);
        }while (deletedFileNum!=0);

        File saveTo = new File("./data/sources.json");
        extractProjectSource(root, saveTo);
    }
}
