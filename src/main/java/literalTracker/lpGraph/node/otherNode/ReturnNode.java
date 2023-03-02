package literalTracker.lpGraph.node.otherNode;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;

public class ReturnNode extends BaseNode {

    public String methodSignature;
    public String methodName;

    public ReturnNode(LocationInSourceCode location, String methodSignature, String methodName) {
        super(location);
        this.methodSignature = methodSignature;
        this.methodName = methodName;
    }

    public boolean match(ResolvedMethodDeclaration resolvedMethodDeclaration){
        if (resolvedMethodDeclaration instanceof JavaParserMethodDeclaration){
            MethodDeclaration methodDeclaration =
                    ((JavaParserMethodDeclaration) resolvedMethodDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(methodDeclaration, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            return getLocation().getPath().equals(storage.getPath().normalize().toString())
                    && ASTUtils.matchRange(new SerializableRange(methodDeclaration.getRange().get()), getLocation().getRange());
        }else {
            return false;
        }
    }
}
