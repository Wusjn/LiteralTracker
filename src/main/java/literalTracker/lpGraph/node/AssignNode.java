package literalTracker.lpGraph.node;

import literalTracker.lpGraph.node.declNode.DeclarationNode;

public class AssignNode extends BaseNode{
    public boolean hasTarget;
    public boolean hasValue;

    public AssignNode(LocationInSourceCode location) {
        super(location);
    }
}
