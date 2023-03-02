package literalTracker.lpGraph.graphFactory.visitor;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.utils.StringEscapeUtils;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.node.location.LocationInSourceCode;

public class LiteralVisitor extends BaseVisitor<LiteralVisitor.Arg> {
    public static class Arg extends BaseVisitor.Arg{
        public Arg(GraphFactory graphFactory){
            this.graphFactory = graphFactory;
        }
    }

    public void handleLiteralExpression(Expression expr, Arg arg, BaseNode.ValueType valueType, String literalValue) throws LPGraphException {
        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, expr.getRange().get(), expr.toString());
        BaseNode newTrackingNode = arg.graphFactory.nodeFactory.createNodeFromLiteral(expr, locationInSourceCode, valueType, literalValue);
        if (newTrackingNode != null){
            arg.newlyTrackedNodes.add(newTrackingNode);
        }
    }

    @Override
    public void visit(StringLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try{
            handleLiteralExpression(n, arg, BaseNode.ValueType.String, n.getValue());
        }catch (LPGraphException e){
            e.printStackTrace();
        }
    }

    @Override
    public void visit(CharLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try {
            handleLiteralExpression(n, arg, BaseNode.ValueType.Char, StringEscapeUtils.escapeJava(n.getValue()));
        }catch (LPGraphException e){
            e.printStackTrace();
        }
    }

    @Override
    public void visit(IntegerLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try{
            handleLiteralExpression(n, arg, BaseNode.ValueType.Integer, n.getValue());
        }catch (LPGraphException e){
            e.printStackTrace();
        }
    }

    @Override
    public void visit(BooleanLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try{
            handleLiteralExpression(n, arg, BaseNode.ValueType.Boolean, "" + n.getValue());
        }catch (LPGraphException e){
            e.printStackTrace();
        }
    }

    @Override
    public void visit(DoubleLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try{
            handleLiteralExpression(n, arg, BaseNode.ValueType.Double, n.getValue());
        }catch (LPGraphException e){
            e.printStackTrace();
        }
    }

    @Override
    public void visit(LongLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        try{
            handleLiteralExpression(n, arg, BaseNode.ValueType.Long, n.getValue());
        }catch (LPGraphException e){
            e.printStackTrace();
        }
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
