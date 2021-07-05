package literalTracker.optionReadPointTracker;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;

public class OptionReadPointNode extends BaseNode {
    public OptionReadPointNode(LocationInSourceCode location){
        super(location);
    }
}
