package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;


public abstract class ExpressionNode extends BaseNode {
    public ExpressionNode(LocationInSourceCode location){
        super(location);
    }
}
