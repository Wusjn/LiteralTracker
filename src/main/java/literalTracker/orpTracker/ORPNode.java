package literalTracker.orpTracker;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.exprNode.ExpressionNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import lombok.Getter;

import java.util.List;

public class ORPNode extends ExpressionNode {
    @Getter
    private List<String> configKeys;

    public ORPNode(LocationInSourceCode location, List<String> configKeys){
        super(location);
        this.configKeys = configKeys;
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
    }
}
