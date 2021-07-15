package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;

public class LocalVariableNode extends DeclarationNode {
    public LocalVariableNode(LocationInSourceCode location, String name) {
        super(location, name);
    }

    @Override
    public boolean match(Node valueAccess) {
        if (!(valueAccess instanceof NameExpr)){
            return false;
        }
        NameExpr expr = (NameExpr) valueAccess;
        try{
            ResolvedValueDeclaration resolvedValueDeclaration = expr.resolve();
            if (resolvedValueDeclaration instanceof JavaParserVariableDeclaration){
                VariableDeclarator variableDeclarator =
                        ((JavaParserVariableDeclaration) resolvedValueDeclaration).getVariableDeclarator();
                CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(variableDeclarator, CompilationUnit.class);
                CompilationUnit.Storage storage = cu.getStorage().get();
                return getLocation().getPath().equals(storage.getPath().normalize().toString())
                        && ASTUtils.matchRange(new SerializableRange(variableDeclarator.getRange().get()), getLocation().getRange());
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        throw new Exception("Combined failed: " + this.getClass().getName() + " can not combine");
    }
}
