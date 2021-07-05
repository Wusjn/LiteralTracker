package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.Node;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;

public abstract class DeclarationNode extends BaseNode {
    public String name;
    public boolean finalValue = true;

    public DeclarationNode(LocationInSourceCode location, String name){
        super(location);
        this.name = name;
    }

    abstract public boolean match(Node valueAccess);
}
