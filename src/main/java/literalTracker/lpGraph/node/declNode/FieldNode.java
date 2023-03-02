package literalTracker.lpGraph.node.declNode;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.utils.ASTUtils;

public class FieldNode extends DeclarationNode {
    public boolean hasInitializer;

    public FieldNode(LocationInSourceCode location, String name, boolean hasInitializer) {
        super(location, name);
        this.hasInitializer = hasInitializer;
    }

    @Override
    public boolean match(ResolvedValueDeclaration resolvedValueDeclaration) {

        VariableDeclarator variableDeclarator;
        if (resolvedValueDeclaration instanceof JavaParserFieldDeclaration){
            variableDeclarator = ((JavaParserFieldDeclaration) resolvedValueDeclaration).getVariableDeclarator();
        }else if (resolvedValueDeclaration instanceof ResolvedFieldDeclaration) {
            ResolvedFieldDeclaration resolvedFieldDeclaration = (ResolvedFieldDeclaration) resolvedValueDeclaration;
            try {
                FieldDeclaration fieldDeclaration = resolvedFieldDeclaration.toAst().get();
                variableDeclarator = ASTUtils.findVariableDeclaratorFromFieldDeclaration(
                        fieldDeclaration, resolvedValueDeclaration.getName()
                );
            }catch (Exception e){
                return false;
            }
        }else {
            return false;
        }

        CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(variableDeclarator, CompilationUnit.class);
        CompilationUnit.Storage storage = cu.getStorage().get();
        return getLocation().getPath().equals(storage.getPath().normalize().toString())
                && ASTUtils.matchRange(new SerializableRange(variableDeclarator.getRange().get()), getLocation().getRange());
    }

    @Override
    public BaseNode merge(BaseNode other) throws LPGraphException {
        return this;
    }
}
