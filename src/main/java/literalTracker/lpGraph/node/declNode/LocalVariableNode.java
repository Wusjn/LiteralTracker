package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.SerializableRange;
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
                return ASTUtils.match(new SerializableRange(variableDeclarator.getRange().get()), location.range);
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }
}
