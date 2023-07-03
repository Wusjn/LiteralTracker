package literalTracker;

import lombok.Getter;

public class Settings {
    public static String getNeo4jUri(){
        return "bolt://localhost:7687";
    }
    public static String getNeo4jUserName(){
        return "wusj";
    }
    public static String getNeo4jPassword(){
        return "730115";
    }
    public static boolean isTestMode(){
        return false;
    }
    public static boolean shallWriteCypher(){return false;}
    public static boolean shallCreateORPGraphAgain(){
        return true;
    }
    public static String getRepoName() {
        return "hadoop";
    }
    public static DataPaths getDataPaths(){
        if (isTestMode()){
            return testDataPath;
        }else {
            return dataPaths;
        }
    }


    @Getter
    public static class DataPaths{
        private String sourcePath;
        private String configNamesPath;
        private String metaDataPath;
        private String snapshotPath;
        private String LiteralSamplePath;
        private String ORPSamplePath;

        public DataPaths(String sourcePath,String configNamesPath,
                         String metaDataPath, String snapshotPath,
                         String LiteralSamplePath, String ORPSamplePath){
            this.sourcePath = sourcePath;
            this.configNamesPath = configNamesPath;
            this.metaDataPath = metaDataPath;
            this.snapshotPath = snapshotPath;
            this.LiteralSamplePath = LiteralSamplePath;
            this.ORPSamplePath = ORPSamplePath;
        }
    }

    private static DataPaths dataPaths = new DataPaths(
            "./data/" + getRepoName() + "/source",
            "./data/" + getRepoName() + "/configNames.json",
            "./data/" + getRepoName() + "/sources.json",
            "./data/" + getRepoName() + "/results/snapshot",
            "./data/" + getRepoName() + "/results/LiteralSamplePath.json",
            "./data/" + getRepoName() + "/results/ORPSamplePath.json"
    );
    private static DataPaths testDataPath = new DataPaths(
            "./data/test/source",
            "./data/test/configNames.json",
            "./data/test/sources.json",
            "./data/test/results/snapshot",
            "./data/test/results/LiteralSamplePath.json",
            "./data/test/results/ORPSamplePath.json"
    );

}
