package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;


public abstract class ExpressionNode extends BaseNode {
    public ExpressionNode(LocationInSourceCode location){
        super(location);
    }
}
