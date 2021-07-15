package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.Node;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.utils.Counter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DeclarationNode extends BaseNode {
    private String name;
    private boolean finalValue = true;


    public DeclarationNode(LocationInSourceCode location, String name){
        super(location);
        this.name = name;
    }

    abstract public boolean match(Node valueAccess);

    @Override
    public String toCypher(Counter idCounter) {
        return super.toCypher(idCounter) + " ," + String.format("m%d.name=\"%s\"",idCounter.getCount(), name);
    }
}
