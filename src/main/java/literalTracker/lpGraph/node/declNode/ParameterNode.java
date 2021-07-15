package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;
import literalTracker.utils.Counter;
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

    public ParameterNode(LocationInSourceCode location, String name, int index) {
        super(location, name);
        this.index = index;
        methodType = MethodType.Normal;
    }

    public ParameterNode(LocationInSourceCode location, MethodType methodType, String name, int index){
        super(location, name);
        this.index = index;
        this.methodType = methodType;
    }

    @Override
    public boolean match(Node valueAccess) {
        if (!(valueAccess instanceof NameExpr)){
            return false;
        }
        NameExpr expr = (NameExpr) valueAccess;
        try{
            ResolvedValueDeclaration resolvedValueDeclaration = expr.resolve();
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
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        if (!(other instanceof ParameterNode)){
            throw new Exception("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
        }else {
            return this;
        }
    }

    @Override
    public String toCypher(Counter idCounter) {
        return super.toCypher(idCounter) + String.format(", m%d.methodType=\"%s\", m%d.index=%d",
                idCounter.getCount(), methodType.name(),
                idCounter.getCount(), index
        );
    }
}
