package literalTracker.lpGraph.node.exprNode;

import com.github.javaparser.Range;
import com.github.javaparser.ast.expr.Expression;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CompositeExpressionNode extends ExpressionNode {
    private BaseNode[] composedBy;
    private SerializableRange[] subRanges;
    private BaseNode usage;
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

    private Set<String> getStringCartesianProduct(Set<String> a, Set<String> b){
        Set<String> cartesianProduct = new HashSet<>();
        if (a.size() == 0){
            cartesianProduct.addAll(b);
            return cartesianProduct;
        }else if (b.size() == 0){
            cartesianProduct.addAll(a);
            return cartesianProduct;
        }else {
            for (String prefix : a){
                for (String postfix : b){
                    cartesianProduct.add(prefix + postfix);
                }
            }
        }
        return cartesianProduct;
    }

    private void ackComplete(){
        if (hasAcknowledged){
            return;
        }
        hasAcknowledged = true;

        for (BaseNode prevNode : composedBy){
            prevNode.getNextNodes().add(this);
            getPrevNodes().add(prevNode);
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
    public BaseNode merge(BaseNode other) throws LPGraphException {
        if (other instanceof CompositeExpressionNode){
            CompositeExpressionNode otherNode = (CompositeExpressionNode) other;
            for (int i=0; i<subRanges.length; i++){
                BaseNode subNode = composedBy[i];
                BaseNode otherSubNode = otherNode.composedBy[i];
                if (subNode == null && otherSubNode != null){
                    composedBy[i] = otherSubNode;
                }else if(subNode != null && otherSubNode != null){
                    throw new LPGraphException("Combined failed: incompatible CompositeExpressionNode subNodes\"");
                }
            }

            if (isComplete()){
                ackComplete();
            }
            return this;
        }else {
            return super.merge(other);
        }
    }

    @Override
    public boolean tryTrackingNode(){
        if (hasAcknowledged){
            return super.tryTrackingNode();
        }else {
            return false;
        }
    }

    @Override
    public void calculateValue(){
        if (isSumExpression){
            Set<String> stringValues = new HashSet<>();
            for (BaseNode prevNode : composedBy){
                if (prevNode.getValueType() == ValueType.String){
                    stringValues = getStringCartesianProduct(stringValues, prevNode.getValues());
                }else {
                    stringValues = null;
                    break;
                }
            }
            if (stringValues !=null){
                this.setValueType(ValueType.String);
                this.getValues().addAll(stringValues);
            }
        }
    }

}
