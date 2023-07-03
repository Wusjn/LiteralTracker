package literalTracker.featureExtractor;

import java.util.ArrayList;
import java.util.List;

public class TreeSample {
    public List<Sample> samples;
    public String filePath;
    public String project = "none";
    public TreeSample(List<Sample> samples, String filePath){
        this.samples = samples;
        this.filePath = filePath;
        if (filePath.startsWith("C:\\Users\\wusj\\workspace\\LiteralTracker\\data\\hadoop\\source\\hadoop-yarn-project")){
            this.project = "yarn";
        }
        if (filePath.startsWith("C:\\Users\\wusj\\workspace\\LiteralTracker\\data\\hadoop\\source\\hadoop-common-project")){
            this.project = "common";
        }
        if (filePath.startsWith("C:\\Users\\wusj\\workspace\\LiteralTracker\\data\\hadoop\\source\\hadoop-hdfs-project")){
            this.project = "hdfs";
        }
        if (filePath.startsWith("C:\\Users\\wusj\\workspace\\LiteralTracker\\data\\hadoop\\source\\hadoop-mapreduce-project")){
            this.project = "mapreduce";
        }
        if (filePath.startsWith("C:\\Users\\wusj\\workspace\\LiteralTracker\\data\\hbase\\source")){
            this.project = "hbase";
        }
    }
}
