package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import literalTracker.lpGraph.node.otherNode.AssignNode;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.utils.ASTUtils;

import java.util.List;
import java.util.stream.Collectors;

public class NodeFactory {

    public static BaseNode createNode(Expression expr, LocationInSourceCode location, BaseNode prevNode){

        Expression maxConcatenatedExpression = ASTUtils.getMaxConcatenatedExpression(expr);
        if (!ASTUtils.checkValidConcatenatedExpression(maxConcatenatedExpression, expr)){
            return new UnsolvedNode(location);
        }

        List<Expression> decomposedConcatenatedExpression = ASTUtils.decomposeConcatenatedExpression(maxConcatenatedExpression);
        if (decomposedConcatenatedExpression.size() == 1){
            return expand(decomposedConcatenatedExpression.get(0), location, prevNode);
        }else {
            LocationInSourceCode sourceForCompositeExpressionNode = new LocationInSourceCode(
                    location, maxConcatenatedExpression.getRange().get(), maxConcatenatedExpression.toString()
            );
            List<Range> ranges = decomposedConcatenatedExpression.stream()
                    .map(subExpr -> subExpr.getRange().get())
                    .collect(Collectors.toList());

            CompositeExpressionNode newNode = new CompositeExpressionNode(
                    ranges.toArray(new Range[ranges.size()]),
                    sourceForCompositeExpressionNode,
                    ASTUtils.isSumExpression(maxConcatenatedExpression)
            );
            newNode.tryAdd(expr, prevNode);
            return newNode;
        }

    }

    public static BaseNode expand(Expression expr, LocationInSourceCode location, BaseNode prevNode){
        Node parentNode = expr.getParentNode().get();

        if (parentNode instanceof MethodCallExpr){
            MethodCallExpr methodCallExpr = (MethodCallExpr) parentNode;
            return FromMethodCallExpr.createLPGNodeFromMethodCallExpr(expr, methodCallExpr, location);
        }else if (parentNode instanceof ObjectCreationExpr){
            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parentNode;
            return FromObjectCreationExpr.createLPGNodeFromObjectCreationExpr(expr, objectCreationExpr, location);
        }else if (parentNode instanceof VariableDeclarator){
            VariableDeclarator variableDeclarator = (VariableDeclarator) parentNode;
            return FromVariableDeclarator.createLPGNodeFromVariableDeclarator(variableDeclarator, location);
        }else if (parentNode instanceof AssignExpr){
            //literal assign to a variable, or variable which stores a literal has been changed
            AssignExpr assignExpr = (AssignExpr) parentNode;
            LocationInSourceCode locationForAssignNode = new LocationInSourceCode(
                    location, assignExpr.getRange().get(), assignExpr.toString()
            );
            AssignNode newNode =  new AssignNode(
                    locationForAssignNode,
                    assignExpr.getTarget().getRange().get(),
                    assignExpr.getValue().getRange().get()
            );
            newNode.tryAdd(expr, prevNode);
            return newNode;
        }else {
            //leaf node, unexpected parent
            return new UnsolvedNode(location);
        }
    }
}
