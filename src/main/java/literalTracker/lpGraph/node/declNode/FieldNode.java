package literalTracker.lpGraph.node.declNode;


import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.SerializableRange;
import literalTracker.utils.ASTUtils;

public class FieldNode extends DeclarationNode {

    public FieldNode(LocationInSourceCode location, String name) {
        super(location, name);
    }

    @Override
    public boolean match(Node valueAccess) {
        ResolvedValueDeclaration resolvedValueDeclaration = null;
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
            return ASTUtils.match(new SerializableRange(variableDeclarator.getRange().get()), location.range);
        }else {
            return false;
        }
    }
}
