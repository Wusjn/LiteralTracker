package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.SerializableRange;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;

public class FromVariableDeclarator {

    public static DeclarationNode createLPGNodeFromVariableDeclarator(Expression expr, VariableDeclarator variableDeclarator, LocationInSourceCode location){
        try{
            ResolvedValueDeclaration valueDeclaration = variableDeclarator.resolve();
        }catch (Exception e){
            //e.printStackTrace();
            //this should not happen, because literals can't be casted to an unresolvable type
            System.exit(100);
            return null;
        }

        Node grandParent = variableDeclarator.getParentNode().get();
        LocationInSourceCode sourceForNewNode = location.clone();
        DeclarationNode newNode = null;
        if (grandParent instanceof FieldDeclaration){
            sourceForNewNode.range = new SerializableRange(variableDeclarator.getRange().get());
            sourceForNewNode.code = variableDeclarator.toString();
            newNode = new FieldNode(sourceForNewNode, variableDeclarator.getNameAsString());
        }else if (grandParent instanceof VariableDeclarationExpr){
            sourceForNewNode.range = new SerializableRange(variableDeclarator.getRange().get());
            sourceForNewNode.code = variableDeclarator.toString();
            newNode = new LocalVariableNode(sourceForNewNode, variableDeclarator.getNameAsString());
        }else {
            //this should not happen
            System.exit(100);
            return null;
        }

        return newNode;
    }
}
