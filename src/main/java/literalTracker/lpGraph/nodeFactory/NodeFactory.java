package literalTracker.lpGraph.nodeFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.location.SerializableRange;
import literalTracker.lpGraph.node.otherNode.AssignNode;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.otherNode.PredicateNode;
import literalTracker.lpGraph.node.otherNode.ReturnNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.exprNode.ORPNode;
import literalTracker.utils.ASTUtils;

import java.util.List;
import java.util.stream.Collectors;

public class NodeFactory {

    @JsonIgnore
    public GraphFactory graphFactory;
    public NodeFactory(GraphFactory graphFactory){
        this.graphFactory = graphFactory;
    }


    public BaseNode createNodeFromLiteral(
            Expression literalExpr, LocationInSourceCode location, BaseNode.ValueType valueType, String literal
    ) throws LPGraphException {
        SimpleLiteralNode literalNode = new SimpleLiteralNode(location, valueType, literal);
        merge(literalNode);
        literalNode.tryTrackingNode();

        BaseNode leftValueNode = createLeftValueNode(literalExpr, location, literalNode);
        return leftValueNode;
    }

    public BaseNode createNodeFromORP(Expression orp, LocationInSourceCode location, List<String> configKeys) throws LPGraphException {
        ORPNode orpNode = new ORPNode(location, configKeys);
        merge(orpNode);
        orpNode.tryTrackingNode();

        BaseNode leftValueNode = createLeftValueNode(orp, location, orpNode);
        return leftValueNode;
    }

    // track a right value & create a node representing the left value. e.g. (a = b + c, a is the left value)
    public BaseNode createLeftValueNode(Expression expr, LocationInSourceCode location, BaseNode rightValueNode) throws LPGraphException {

        Expression maxConcatenatedExpression = ASTUtils.getMaxConcatenatedExpression(expr);
        if (!ASTUtils.checkValidConcatenatedExpression(maxConcatenatedExpression)){
            UnsolvedNode unsolvedNode = new UnsolvedNode(location);
            merge(unsolvedNode);
            connectNodes(unsolvedNode, rightValueNode);
            unsolvedNode.tryTrackingNode();
            return null;
        }

        List<Expression> decomposedConcatenatedExpression = ASTUtils.decomposeConcatenatedExpression(maxConcatenatedExpression);
        if (decomposedConcatenatedExpression.size() == 1){
            BaseNode node = createLeftValueNodeWithoutConsideringCompositeExpression(expr, location, rightValueNode);
            node = merge(node);
            connectNodes(node, rightValueNode);
            if (node.tryTrackingNode() && nodeCanBeTracked(node)){
                return node;
            } else {
                return null;
            }
        }else {
            LocationInSourceCode locationForCompositeExpressionNode = new LocationInSourceCode(
                    location, maxConcatenatedExpression.getRange().get(), maxConcatenatedExpression.toString()
            );
            List<Range> ranges = decomposedConcatenatedExpression.stream()
                    .map(subExpr -> subExpr.getRange().get())
                    .collect(Collectors.toList());

            CompositeExpressionNode compositeExpressionNode = new CompositeExpressionNode(
                    ranges.toArray(new Range[ranges.size()]),
                    locationForCompositeExpressionNode,
                    ASTUtils.isSumExpression(maxConcatenatedExpression)
            );
            compositeExpressionNode.tryAdd(expr, rightValueNode);
            compositeExpressionNode = (CompositeExpressionNode) merge(compositeExpressionNode);

            if (compositeExpressionNode.getUsage() == null){
                BaseNode usage = createLeftValueNodeWithoutConsideringCompositeExpression(
                        maxConcatenatedExpression, compositeExpressionNode.getLocation(), compositeExpressionNode
                );
                compositeExpressionNode.setUsage(usage);
            }
            if (compositeExpressionNode.tryTrackingNode()){
                BaseNode usageNode = compositeExpressionNode.getUsage();
                usageNode = merge(usageNode);
                connectNodes(usageNode, compositeExpressionNode);
                if (usageNode.tryTrackingNode() && nodeCanBeTracked(usageNode)){
                    return usageNode;
                }else {
                    return null;
                }
            }else {
                return null;
            }

        }

    }

    // track a right value & create a node representing the left value., without considering composite expression
    public BaseNode createLeftValueNodeWithoutConsideringCompositeExpression(
            Expression refExpr, LocationInSourceCode location, BaseNode referredNode
    )throws LPGraphException{
        Node parentNode = refExpr.getParentNode().get();

        if (parentNode instanceof MethodCallExpr){
            MethodCallExpr methodCallExpr = (MethodCallExpr) parentNode;
            return FromMethodCallExpr.createLPGNodeFromMethodCallExpr(refExpr, methodCallExpr, location);
        }else if (parentNode instanceof ObjectCreationExpr){
            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parentNode;
            return FromObjectCreationExpr.createLPGNodeFromObjectCreationExpr(refExpr, objectCreationExpr, location);
        }else if (parentNode instanceof VariableDeclarator){
            VariableDeclarator variableDeclarator = (VariableDeclarator) parentNode;
            return FromVariableDeclarator.createLPGNodeFromVariableDeclarator(variableDeclarator, location);
        }else if (parentNode instanceof AssignExpr){
            AssignExpr assignExpr = (AssignExpr) parentNode;

            if (assignExpr.getValue() == refExpr) {
                if (assignExpr.getOperator() == AssignExpr.Operator.ASSIGN){
                    DeclarationNode declarationNode = FromAssignExpr.createLPGNodeFromAssignExpr(refExpr, location, assignExpr);
                    if (declarationNode != null && declarationNode instanceof FieldNode){
                        return declarationNode;
                    }
                }
                return new UnsolvedNode(location);
            }else if (assignExpr.getTarget() == refExpr){
                LocationInSourceCode locationForAssignNode = new LocationInSourceCode(
                        location, assignExpr.getRange().get(), assignExpr.toString()
                );
                AssignNode newNode =  new AssignNode(
                        locationForAssignNode,
                        (DeclarationNode) referredNode,
                        assignExpr.getOperator()
                );
                return newNode;
            }else {
                throw new LPGraphException("something wrong with this assign expression");
            }
        }else if (parentNode instanceof ReturnStmt){
            MethodDeclaration methodDeclaration = (MethodDeclaration) ASTUtils.getTargetWarpper(parentNode, MethodDeclaration.class);
            if (methodDeclaration == null || !ASTUtils.methodContainOneReturnStmt(methodDeclaration)){
                return new UnsolvedNode(location);
            }
            if (referredNode instanceof SimpleLiteralNode || referredNode instanceof ORPNode){
                LocationInSourceCode locationForReturnNode = new LocationInSourceCode(
                        location, methodDeclaration.getRange().get(), methodDeclaration.getSignature().toString());
                ReturnNode returnNode = new ReturnNode(
                        locationForReturnNode, methodDeclaration.getSignature().toString(), methodDeclaration.getNameAsString()
                );
                return returnNode;
            }else {

                return new UnsolvedNode(location);
            }
        }else{
            //leaf node, unexpected parent
            IfStmt ifStmt = (IfStmt) ASTUtils.getTargetWarpper(parentNode, IfStmt.class);
            if (ifStmt != null){
                SerializableRange conditionRange = new SerializableRange(ifStmt.getCondition().getRange().get());
                if (conditionRange.contain(location.getRange())){
                    return new PredicateNode(location, ifStmt.getCondition().toString());
                }
            }

            return new UnsolvedNode(location);
        }
    }

    private BaseNode merge(BaseNode node) throws LPGraphException{

        if (node instanceof UnsolvedNode){
            graphFactory.lpGraph.addNewNode(node);
            return node;
        }else {
            BaseNode existingNode = graphFactory.lpGraph.getNode(node.getLocation());
            if (existingNode != null){
                //important, must be existingNode.combine(newNode), cuz there are links among existing nodes
                node = existingNode.merge(node);
            }else {
                graphFactory.lpGraph.addNewNode(node);
            }
            return node;
        }

    }

    public void connectNodes(BaseNode newNode, BaseNode prevNode){
        newNode.getPrevNodes().add(prevNode);
        prevNode.getNextNodes().add(newNode);
    }

    public boolean nodeCanBeTracked(BaseNode node){
        if (node instanceof DeclarationNode || node instanceof ReturnNode){
            return true;
        }else {
            return false;
        }
    }
}
