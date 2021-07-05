package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.node.*;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.utils.ASTUtils;
import literalTracker.visitor.LocationRecorderAdapter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;


public class NodeFactory {
    public static LPGraph lpGraph = new LPGraph();

    public static BaseNode addLPGraphNode(Expression expr, LocationInSourceCode location, BaseNode prevNode){
        BaseNode newNode = null;

        Expression maxConcatenatedExpression = ASTUtils.getMaxConcatenatedExpression(expr);
        if (!ASTUtils.checkValidConcatenatedExpression(maxConcatenatedExpression, expr)){
            //TODO: leaf node, in a binary expression
            if (prevNode instanceof DeclarationNode){
                newNode = new UnsolvedNode(location);
                connectNodes(prevNode, newNode);
                lpGraph.addNewNode(newNode);
                return newNode;
            }else {
                return null;
            }
        }

        List<Expression> decomposedConcatenatedExpression = ASTUtils.decomposeConcatenatedExpression(maxConcatenatedExpression);
        if (decomposedConcatenatedExpression.size() == 1){
            newNode = expand(decomposedConcatenatedExpression.get(0), location);
            if (newNode!=null){
                if (newNode instanceof UnsolvedNode && !(prevNode instanceof DeclarationNode)){
                    return null;
                }else {
                    connectNodes(prevNode, newNode);
                }
            }
        }else {
            LocationInSourceCode sourceForCompositeExpressionNode = location.clone();
            sourceForCompositeExpressionNode.range = new SerializableRange(maxConcatenatedExpression.getRange().get());
            sourceForCompositeExpressionNode.code = maxConcatenatedExpression.toString();

            CompositeExpressionNode compositeExpressionNode = null;
            BaseNode existingNode = lpGraph.getNode(sourceForCompositeExpressionNode);
            if (existingNode == null){
                List<Range> ranges = decomposedConcatenatedExpression.stream().map(subExpr -> subExpr.getRange().get()).collect(Collectors.toList());
                compositeExpressionNode = new CompositeExpressionNode(
                        ranges.toArray(new Range[ranges.size()]),
                        sourceForCompositeExpressionNode,
                        ASTUtils.isSumExpression(maxConcatenatedExpression));
                if (!compositeExpressionNode.tryAdd(expr, prevNode)){
                    System.exit(100);
                }
                lpGraph.addNewNode(compositeExpressionNode);
            }else {
                compositeExpressionNode = (CompositeExpressionNode) existingNode;
                if(!compositeExpressionNode.tryAdd(expr, prevNode)){
                    System.exit(100);
                }
                if (compositeExpressionNode.isComplete()){
                    compositeExpressionNode.ackComplete();
                    newNode = expand(maxConcatenatedExpression, sourceForCompositeExpressionNode);
                    if (newNode != null && !(newNode instanceof UnsolvedNode)){
                        connectNodes(compositeExpressionNode, newNode);
                    }else {
                        return null;
                    }
                }
            }
        }

        lpGraph.addNewNode(newNode);
        return newNode;
    }

    public static BaseNode expand(Expression expr, LocationInSourceCode location){
        Node parentNode = expr.getParentNode().get();
        BaseNode newNode = null;

        if (parentNode instanceof MethodCallExpr){
            MethodCallExpr methodCallExpr = (MethodCallExpr) parentNode;
            newNode = FromMethodCallExpr.createLPGNodeFromMethodCallExpr(expr, methodCallExpr, location);
        }else if (parentNode instanceof ObjectCreationExpr){
            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parentNode;
            newNode = FromObjectCreationExpr.createLPGNodeFromObjectCreationExpr(expr, objectCreationExpr, location);
        }else if (parentNode instanceof VariableDeclarator){
            VariableDeclarator variableDeclarator = (VariableDeclarator) parentNode;
            newNode = FromVariableDeclarator.createLPGNodeFromVariableDeclarator(expr, variableDeclarator, location);
        }else if (parentNode instanceof AssignExpr){
            //TODO: literal assign to a variable, or variable which stores a literal has been changed
            AssignExpr assignExpr = (AssignExpr) parentNode;
            LocationInSourceCode locationForNewNode = location.clone();
            locationForNewNode.range = new SerializableRange(assignExpr.getRange().get());
            locationForNewNode.code = assignExpr.toString();

             AssignNode assignNode = (AssignNode)NodeFactory.lpGraph.getNode(locationForNewNode);
             if (assignNode == null){
                 assignNode = new AssignNode(locationForNewNode);
             }

            if (ASTUtils.match(assignExpr.getTarget().getRange().get(), expr.getRange().get())){
                //expr is target
                assignNode.hasTarget = true;
            }else{
                //expr is value
                assignNode.hasValue = true;
            }
            newNode = assignNode;
        }else {
            //TODO: leaf node, unexpected parent
            newNode = new UnsolvedNode(location);
        }

        return newNode;
    }

    public static SimpleLiteralNode createLiteralNode(String literal, LocationInSourceCode location, BaseNode.ValueType valueType){
        SimpleLiteralNode newNode = new SimpleLiteralNode(location, valueType, literal);
        lpGraph.addNewNode(newNode);
        return newNode;
    }

    public static void connectNodes(BaseNode prevNode, BaseNode newNode){
        if (newNode.hasBeenTracked){
            return;
        }
        newNode.prevNode.add(prevNode);
        prevNode.nextNodes.add(newNode);

        if (newNode instanceof FieldNode || newNode instanceof LocalVariableNode){
            if (prevNode.valueType != BaseNode.ValueType.Unknown){
                newNode.valueType = prevNode.valueType;
                newNode.value = prevNode.value;
            }
        }
    }
}
