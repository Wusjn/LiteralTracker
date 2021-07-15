package literalTracker.visitor;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.utils.StringEscapeUtils;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;

import java.util.ArrayList;
import java.util.List;

public class LiteralVisitor extends LocationRecorderAdapter<LiteralVisitor.Arg>{
    public static class Arg{
        public GraphFactory graphFactory;

        public String path;
        public String fileName;
        public List<SimpleLiteralNode> simpleLiteralNodes = new ArrayList<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();

        public Arg(GraphFactory graphFactory){
            this.graphFactory = graphFactory;
        }
    }

    public void handleLiteralExpression(Expression expr, Arg arg, BaseNode.ValueType valueType, String literalValue){
        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, expr.getRange().get(), expr.toString());
        SimpleLiteralNode literalNode = arg.graphFactory.addLPGraphLiteralNode(literalValue, locationInSourceCode, valueType);
        if (literalNode !=null){
            arg.simpleLiteralNodes.add(literalNode);
            BaseNode.tryTrackingNode(literalNode);

            BaseNode newNode = NodeFactory.createNode(expr, literalNode.getLocation(), literalNode);
            newNode = arg.graphFactory.addLPGraphNode(newNode, literalNode);
            if (newNode!=null){
                newNode = BaseNode.tryTrackingNode(newNode);
                if (newNode != null){
                    arg.newlyCreatedNodes.add(newNode);
                }
            }
        }
    }

    @Override
    public void visit(StringLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.String, n.getValue());
    }

    @Override
    public void visit(CharLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.Char, StringEscapeUtils.escapeJava(n.getValue()));
    }

    @Override
    public void visit(IntegerLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.Integer, n.getValue());
    }

    @Override
    public void visit(BooleanLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.Boolean, "" + n.getValue());
    }

    @Override
    public void visit(DoubleLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.Double, n.getValue());
    }

    @Override
    public void visit(LongLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        handleLiteralExpression(n, arg, BaseNode.ValueType.Long, n.getValue());
    }

    @Override
    public void visit(FieldAccessExpr n, Arg arg) {
        super.visit(n,arg);

        /*try {
            ResolvedValueDeclaration resolvedValueDeclaration = n.resolve();
            if (resolvedValueDeclaration instanceof JavaParserEnumConstantDeclaration){
                createNewNode(n, arg);
            }
        }catch (Exception e){
            //e.printStackTrace();
            return;
        }*/
    }
}
