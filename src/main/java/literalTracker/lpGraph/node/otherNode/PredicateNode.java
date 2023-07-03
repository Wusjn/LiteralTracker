package literalTracker.lpGraph.node.otherNode;

import literalTracker.lpGraph.node.location.LocationInSourceCode;

public class PredicateNode extends UnsolvedNode {
    public String condition;

    public PredicateNode(LocationInSourceCode location, String condition) {
        super(location);
        this.condition = condition;
    }
}
