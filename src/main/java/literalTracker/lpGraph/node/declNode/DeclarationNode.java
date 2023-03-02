package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
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

    abstract public boolean match(ResolvedValueDeclaration resolvedValueDeclaration);

    @Override
    public String onCreateCypher(String nodeName) {
        return super.onCreateCypher(nodeName) + ", " + String.format("%s.name=\"%s\"",nodeName, name);
    }
}
