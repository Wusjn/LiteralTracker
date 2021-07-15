package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;

public class SimpleLiteralNode extends ExpressionNode {

    public SimpleLiteralNode(LocationInSourceCode location, ValueType valueType, String literalValue){
        super(location);
        this.setValueType(valueType);
        this.setValue(literalValue);
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
    }
}
