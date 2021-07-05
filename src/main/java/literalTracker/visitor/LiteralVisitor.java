package literalTracker.visitor;

import com.github.javaparser.ast.expr.*;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;

import java.util.ArrayList;
import java.util.List;

public class LiteralVisitor extends LocationRecorderAdapter<LiteralVisitor.Arg>{
    public static class Arg{
        public String path;
        public String fileName;
        public List<SimpleLiteralNode> simpleLiteralNodes = new ArrayList<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();
    }

    public void createNewNode(Expression expr, Arg arg, BaseNode.ValueType valueType, String literalValue){
        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, className, methodName, expr.getRange().get(), expr.toString());
        SimpleLiteralNode literalNode = NodeFactory.createLiteralNode(literalValue, locationInSourceCode, valueType);
        if (literalNode !=null){
            arg.simpleLiteralNodes.add(literalNode);
            literalNode.hasBeenTracked = true;

            BaseNode newNode = NodeFactory.addLPGraphNode(expr, literalNode.location, literalNode);
            if (newNode!=null){
                if (!newNode.hasBeenTracked){
                    arg.newlyCreatedNodes.add(newNode);
                    newNode.hasBeenTracked = true;
                }
            }
        }
    }

    @Override
    public void visit(StringLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.String, n.getValue());
    }

    @Override
    public void visit(CharLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.Char, n.getValue());
    }

    @Override
    public void visit(IntegerLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.Integer, n.getValue());
    }

    @Override
    public void visit(BooleanLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.Boolean, "" + n.getValue());
    }

    @Override
    public void visit(DoubleLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.Double, n.getValue());
    }

    @Override
    public void visit(LongLiteralExpr n, Arg arg) {
        super.visit(n, arg);
        createNewNode(n, arg, BaseNode.ValueType.Long, n.getValue());
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
