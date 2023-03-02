package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ParameterNode extends DeclarationNode {
    public enum MethodType implements Serializable{
        Normal, Reflection, VariableLength, ThirdParty
    }

    private int index;
    private MethodType methodType;
    //public ReflectionParameterDeclaration parameterDeclaration;
    private String methodName;

    public ParameterNode(LocationInSourceCode location, String name, int index, String methodName) {
        super(location, name);
        this.index = index;
        methodType = MethodType.Normal;
        this.methodName = methodName;
    }

    public ParameterNode(LocationInSourceCode location, MethodType methodType, String name, int index, String methodName){
        super(location, name);
        this.index = index;
        this.methodType = methodType;
        this.methodName = methodName;
    }

    @Override
    public boolean match(ResolvedValueDeclaration resolvedValueDeclaration) {


        if (resolvedValueDeclaration instanceof JavaParserParameterDeclaration){
            Parameter parameter =
                    ((JavaParserParameterDeclaration) resolvedValueDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(parameter, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            return getLocation().getPath().equals(storage.getPath().normalize().toString())
                    && ASTUtils.matchRange(new SerializableRange(parameter.getRange().get()), getLocation().getRange());
        }else {
            return false;
        }

    }

    @Override
    public BaseNode merge(BaseNode other) throws LPGraphException {
        return this;
    }

    @Override
    public String onCreateCypher(String nodeName) {
        return super.onCreateCypher(nodeName) + String.format(", %s.methodType=\"%s\", %s.index=%d, %s.methodName=\"%s\"",
                nodeName, methodType.name(),
                nodeName, index,
                nodeName, methodName
        );
    }
}
