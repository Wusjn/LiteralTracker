package literalTracker.traverser;

import literalTracker.lpGraph.LPGraph;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class SnapShot implements Serializable {
    @Getter
    @Setter
    private LPGraph lpGraph;
    @Getter
    @Setter
    private LPGraph opGraph;
    @Getter
    @Setter
    private NodesByCategory trackedNodes;

    public SnapShot(LPGraph lpGraph, NodesByCategory trackedNodes, LPGraph opGraph){
        this.lpGraph = lpGraph;
        this.trackedNodes = trackedNodes;
        this.opGraph = opGraph;
    }
}
