package literalTracker;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.exprNode.ORPNode;
import literalTracker.lpGraph.Snapshot;
import literalTracker.utils.Counter;
import literalTracker.utils.DataManager;


import java.util.ArrayList;
import java.util.List;

public class Sandbox {

    public static void main(String[] args) {

        Settings.DataPaths dataPaths = Settings.getDataPaths();

        Snapshot snapshot = DataManager.loadSnapshot(dataPaths.getSnapshotPath());


        List<ORPNode> roots = new ArrayList<>();

        for (BaseNode node : snapshot.getOpGraph().getNodeByLocation().values()) {
            if (node instanceof ORPNode) {
                roots.add((ORPNode) node);
            }
        }
        System.out.println(roots.size());

        Counter counter = new Counter();
        for (ORPNode orpNode: roots){
            for (String configName : orpNode.getConfigKeys()){
                System.out.println(configName);
                if (configName.length()>10){
                    counter.add(1);
                }
            }
        }

        for (ORPNode orpNode : roots){
            if (orpNode.getLocation().getPath().endsWith("hadoop-common\\src\\main\\java\\org\\apache\\hadoop\\ipc\\Client.java")){
                System.out.println(orpNode.getLocation().getRange());
            }
        }

        System.out.println(counter.getCount());
    }
}
