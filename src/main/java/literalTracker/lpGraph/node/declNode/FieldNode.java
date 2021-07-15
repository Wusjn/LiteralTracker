package literalTracker.lpGraph.node.declNode;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;

public class FieldNode extends DeclarationNode {

    public FieldNode(LocationInSourceCode location, String name) {
        super(location, name);
    }

    @Override
    public boolean match(Node valueAccess) {
        ResolvedValueDeclaration resolvedValueDeclaration;
        try{
            if (valueAccess instanceof FieldAccessExpr){
                FieldAccessExpr expr = (FieldAccessExpr) valueAccess;
                resolvedValueDeclaration = expr.resolve();
            }else if (valueAccess instanceof NameExpr){
                NameExpr expr = (NameExpr) valueAccess;
                resolvedValueDeclaration = expr.resolve();
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }

        if (resolvedValueDeclaration instanceof JavaParserFieldDeclaration){
            VariableDeclarator variableDeclarator = ((JavaParserFieldDeclaration) resolvedValueDeclaration).getVariableDeclarator();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(variableDeclarator, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            return getLocation().getPath().equals(storage.getPath().normalize().toString())
                    && ASTUtils.matchRange(new SerializableRange(variableDeclarator.getRange().get()), getLocation().getRange());
        }else {
            return false;
        }
    }

    @Override
    public BaseNode merge(BaseNode other) throws Exception {
        throw new Exception("Combined failed: " + this.getClass().getName() + " can not combine");
    }
}
