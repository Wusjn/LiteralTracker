package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.visitor.LocationRecorderAdapter;

public class FromVariableDeclarator {

    public static DeclarationNode createLPGNodeFromVariableDeclarator(VariableDeclarator variableDeclarator, LocationInSourceCode location){
        Node grandParent = variableDeclarator.getParentNode().get();
        LocationInSourceCode  sourceForNewNode = new LocationInSourceCode(location, variableDeclarator.getRange().get(), variableDeclarator.toString());

        if (grandParent instanceof FieldDeclaration){
            return new FieldNode(sourceForNewNode, variableDeclarator.getNameAsString());
        }else if (grandParent instanceof VariableDeclarationExpr){
            return new LocalVariableNode(sourceForNewNode, variableDeclarator.getNameAsString());
        }else {
            //this should not happen
            System.exit(10081);
            return null;
        }

    }
}
