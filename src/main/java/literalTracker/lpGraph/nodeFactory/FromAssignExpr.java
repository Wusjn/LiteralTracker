package literalTracker.lpGraph.nodeFactory;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.utils.ASTUtils;

public class FromAssignExpr {
    public static DeclarationNode createLPGNodeFromAssignExpr(Expression expr, LocationInSourceCode location, AssignExpr assignExpr) throws LPGraphException {

        Expression target = assignExpr.getTarget();
        ResolvedValueDeclaration valueDeclaration;
        try {
            if (target instanceof NameExpr) {
                valueDeclaration = ((NameExpr) target).resolve();
            } else if (target instanceof FieldAccessExpr) {
                valueDeclaration = ((FieldAccessExpr) target).resolve();
            } else {
                throw new LPGraphException("target can't be resolved");
            }
        } catch (Exception e) {
            return null;
        }


        VariableDeclarator variableDeclarator;
        if (valueDeclaration instanceof JavaParserFieldDeclaration) {
            variableDeclarator = ((JavaParserFieldDeclaration) valueDeclaration).getVariableDeclarator();
        } else if (valueDeclaration instanceof JavaParserSymbolDeclaration) {
            variableDeclarator = (VariableDeclarator)
                    ((JavaParserSymbolDeclaration) valueDeclaration).getWrappedNode();
        } else if (valueDeclaration instanceof JavaParserParameterDeclaration) {
            Parameter parameter = ((JavaParserParameterDeclaration) valueDeclaration).getWrappedNode();
            //TODO: ignore parameter assignment now, fix it later
            return null;
        } else if (valueDeclaration instanceof ResolvedFieldDeclaration) {
            ResolvedFieldDeclaration resolvedFieldDeclaration = (ResolvedFieldDeclaration) valueDeclaration;
            try {
                FieldDeclaration fieldDeclaration = resolvedFieldDeclaration.toAst().get();
                variableDeclarator = ASTUtils.findVariableDeclaratorFromFieldDeclaration(
                        fieldDeclaration, ((NodeWithSimpleName) expr).getNameAsString()
                );
                if (variableDeclarator == null){
                    throw new LPGraphException("can't find variable declarator from field declaration");
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
        return FromVariableDeclarator.createLPGNodeFromVariableDeclarator(
                variableDeclarator, ASTUtils.getLocationFromNode(variableDeclarator)
        );

    }
}
