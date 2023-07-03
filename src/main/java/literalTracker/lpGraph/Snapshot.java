package literalTracker.lpGraph;

import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.graphFactory.NodesByCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Snapshot implements Serializable {
    @Getter
    @Setter
    private LPGraph lpGraph;
    @Getter
    @Setter
    private LPGraph opGraph;
    @Getter
    @Setter
    private NodesByCategory trackedNodes;

    public Snapshot(LPGraph lpGraph, NodesByCategory trackedNodes, LPGraph opGraph){
        this.lpGraph = lpGraph;
        this.trackedNodes = trackedNodes;
        this.opGraph = opGraph;
    }
}
