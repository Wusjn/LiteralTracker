package literalTracker.lpGraph.node.otherNode;

import com.github.javaparser.Range;
import com.github.javaparser.ast.expr.Expression;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.utils.ASTUtils;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AssignNode extends BaseNode {
    private SerializableRange targetRange;
    private SerializableRange valueRange;
    private DeclarationNode targetNode;
    private BaseNode valueNode;

    public AssignNode(LocationInSourceCode location, Range targetRange, Range valueRange) {
        super(location);
        this.targetRange = new SerializableRange(targetRange);
        this.valueRange = new SerializableRange(valueRange);
    }

    public boolean tryAdd(Expression expr, BaseNode prevNode){
        SerializableRange range = new SerializableRange(expr.getRange().get());
        if (ASTUtils.matchRange(range, targetRange)){
            targetNode = (DeclarationNode) prevNode;
            return true;
        }else if (ASTUtils.matchRange(range, valueRange)){
            valueNode = prevNode;
            return true;
        }else {
            return false;
        }
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        if (!(other instanceof AssignNode)){
            throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
        }else {
            AssignNode otherAssignNode = (AssignNode) other;

            if (this.targetNode == null && otherAssignNode.targetNode != null){
                this.targetNode = otherAssignNode.targetNode;
            }else if (this.targetNode != null && otherAssignNode.targetNode == null){
                // do nothing
            }else {
                throw new Exception("Combined failed: incompatible AssignExpr targetRange");
            }

            if (this.valueNode == null && otherAssignNode.valueNode != null){
                this.valueNode = otherAssignNode.valueNode;
            }else if (this.valueNode != null && otherAssignNode.valueNode == null){
                // do nothing
            }else {
                throw new Exception("Combined failed: incompatible AssignExpr valueRange");
            }

            return this;
        }
    }
}
