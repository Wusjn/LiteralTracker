package literalTracker.lpGraph.node.exprNode;

import com.github.javaparser.Range;
import com.github.javaparser.ast.expr.Expression;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class CompositeExpressionNode extends ExpressionNode {
    private BaseNode[] composedBy;
    private SerializableRange[] subRanges;
    private boolean hasAcknowledged = false;
    private boolean isSumExpression;

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
            if (ASTUtils.matchRange(range, subRanges[i])){
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
            prevNode.getNextNodes().add(this);
            getPrevNodes().add(prevNode);
        }

        if (isSumExpression){
            String stringValue = "";
            for (BaseNode prevNode : composedBy){
                if (prevNode.getValueType() == ValueType.String){
                    stringValue += prevNode.getValue();
                }else {
                    stringValue = null;
                    break;
                }
            }
            if (stringValue !=null){
                this.setValueType(ValueType.String);
                this.setValue(stringValue);
            }
        }

    }

    public void clear(BaseNode node){
        setPrevNodes(null);
        for (BaseNode prevNode : composedBy){
            if (node != prevNode){
                prevNode.getNextNodes().remove(this);
            }
        }
        Arrays.fill(composedBy, null);
    }


    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        if (other instanceof CompositeExpressionNode){
            CompositeExpressionNode otherNode = (CompositeExpressionNode) other;
            for (int i=0; i<subRanges.length; i++){
                BaseNode subNode = composedBy[i];
                BaseNode otherSubNode = otherNode.composedBy[i];
                if (subNode == null && otherSubNode != null){
                    composedBy[i] = otherSubNode;
                }else if(subNode != null && otherSubNode != null){
                    throw new Exception("Combined failed: incompatible CompositeExpressionNode subNodes\"");
                }
            }

            if (isComplete()){
                ackComplete();
            }
            return this;
        }else {
            throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
        }
    }
}
