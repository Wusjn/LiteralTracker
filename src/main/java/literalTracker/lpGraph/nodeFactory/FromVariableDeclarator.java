package literalTracker.lpGraph.nodeFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;


public class FromVariableDeclarator {

    public static DeclarationNode createLPGNodeFromVariableDeclarator(VariableDeclarator variableDeclarator, LocationInSourceCode location) throws LPGraphException {
        Node grandParent = variableDeclarator.getParentNode().get();
        LocationInSourceCode  sourceForNewNode = new LocationInSourceCode(location, variableDeclarator.getRange().get(), variableDeclarator.toString());

        if (grandParent instanceof FieldDeclaration){
            return new FieldNode(sourceForNewNode, variableDeclarator.getNameAsString(), variableDeclarator.getInitializer().isPresent());
        }else if (grandParent instanceof VariableDeclarationExpr){
            return new LocalVariableNode(sourceForNewNode, variableDeclarator.getNameAsString());
        }else {
            //this should not happen
            throw new LPGraphException("variable declarator can't be resolved");
        }

    }
}
