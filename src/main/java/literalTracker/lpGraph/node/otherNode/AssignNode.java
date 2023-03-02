package literalTracker.lpGraph.node.otherNode;

import com.github.javaparser.ast.expr.AssignExpr;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AssignNode extends BaseNode {

    private DeclarationNode targetNode;
    private String operator;

    public AssignNode(LocationInSourceCode location, DeclarationNode targetNode, AssignExpr.Operator operator) {
        super(location);
        this.targetNode = targetNode;
        this.operator = operator.asString();
    }

}
