package literalTracker.lpGraph.node.exprNode;

import com.github.javaparser.Range;
import com.github.javaparser.ast.expr.Expression;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.SerializableRange;
import literalTracker.utils.ASTUtils;

public class CompositeExpressionNode extends ExpressionNode {
    public BaseNode[] composedBy;
    public SerializableRange[] subRanges;
    public boolean hasAcknowledged = false;
    public boolean isSumExpression;

    public CompositeExpressionNode(Range[] subRanges, LocationInSourceCode location, boolean isSumExpression){
        super(location);
        this.subRanges = new SerializableRange[subRanges.length];
        for (int i=0; i < subRanges.length; i++){
            this.subRanges[i] = new SerializableRange(subRanges[i]);
        }
        this.composedBy = new BaseNode[subRanges.length];
        this.isSumExpression = isSumExpression;
    }

    public boolean tryAdd(Expression expr, BaseNode prevNode){
        SerializableRange range = new SerializableRange(expr.getRange().get());
        for (int i=0; i<subRanges.length; i++){
            if (ASTUtils.match(range, subRanges[i])){
                composedBy[i] = prevNode;
                return true;
            }
        }
        return false;
    }

    public boolean isComplete(){
        for (BaseNode baseNode : composedBy){
            if (baseNode == null){
                return false;
            }
        }
        return true;
    }

    public void ackComplete(){
        if (hasAcknowledged || !isComplete()){
            return;
        }
        hasAcknowledged = true;

        for (BaseNode prevNode : composedBy){
            prevNode.nextNodes.add(this);
        }

        if (isSumExpression){
            String stringValue = "";
            for (BaseNode prevNode : composedBy){
                if (prevNode.valueType == ValueType.String){
                    stringValue += prevNode.value;
                }else {
                    stringValue = null;
                    break;
                }
            }
            if (stringValue !=null){
                this.valueType = ValueType.String;
                this.value = stringValue;
            }
        }

    }

}
