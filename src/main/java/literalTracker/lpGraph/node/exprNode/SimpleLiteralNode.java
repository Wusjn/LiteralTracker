package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;

public class SimpleLiteralNode extends ExpressionNode {

    public SimpleLiteralNode(LocationInSourceCode location, ValueType valueType, String literalValue){
        super(location);
        this.setValueType(valueType);
        this.getValues().add(literalValue);
    }

}
