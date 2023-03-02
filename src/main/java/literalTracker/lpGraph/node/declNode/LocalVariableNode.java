package literalTracker.lpGraph.node.declNode;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;

public class LocalVariableNode extends DeclarationNode {
    public LocalVariableNode(LocationInSourceCode location, String name) {
        super(location, name);
    }

    @Override
    public boolean match(ResolvedValueDeclaration resolvedValueDeclaration) {

        if (resolvedValueDeclaration instanceof JavaParserSymbolDeclaration){
            VariableDeclarator variableDeclarator =
                    (VariableDeclarator) ((JavaParserSymbolDeclaration) resolvedValueDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(variableDeclarator, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            return getLocation().getPath().equals(storage.getPath().normalize().toString())
                    && ASTUtils.matchRange(new SerializableRange(variableDeclarator.getRange().get()), getLocation().getRange());
        }else {
            return false;
        }

    }

}
