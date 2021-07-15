package literalTracker.lpGraph.node.otherNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;

public class UnsolvedNode extends BaseNode {
    public UnsolvedNode(LocationInSourceCode location) {
        super(location);
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        if (other instanceof UnsolvedNode){
            return this;
        }else  {
            throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
        }
    }
}
