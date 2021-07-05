package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionParameterDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.SerializableRange;
import literalTracker.utils.ASTUtils;

import java.io.Serializable;

public class ParameterNode extends DeclarationNode {
    public enum MethodType implements Serializable{
        Normal, Reflection, VariableLength, ThirdParty
    }

    public int index;
    public MethodType methodType;
    //public ReflectionParameterDeclaration parameterDeclaration;

    public ParameterNode(LocationInSourceCode location, String name, int index) {
        super(location, name);
        this.index = index;
        methodType = MethodType.Normal;
    }

    public ParameterNode(LocationInSourceCode location, ReflectionParameterDeclaration parameterDeclaration, String name, int index){
        super(location, name);
        this.index = index;
        //this.parameterDeclaration = parameterDeclaration;
        methodType = MethodType.Reflection;
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
                return ASTUtils.match(new SerializableRange(parameter.getRange().get()), location.range);
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }
}
