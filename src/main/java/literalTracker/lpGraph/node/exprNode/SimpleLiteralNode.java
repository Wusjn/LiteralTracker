package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;

import java.beans.Expression;

public class SimpleLiteralNode extends ExpressionNode {

    public SimpleLiteralNode(LocationInSourceCode location, ValueType valueType, String literalValue){
        super(location);
        this.valueType = valueType;
        this.value = literalValue;
    }
}
