package literalTracker.utils;


import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import literalTracker.lpGraph.node.location.SerializableRange;

import java.util.ArrayList;
import java.util.List;


public class ASTUtils {
    public static Node getTargetWarpper(Node node, Class targetClass){
        Node target = node;
        while (!(targetClass.isInstance(target))){
            if (target.getParentNode().isPresent()){
                target = target.getParentNode().get();
            }else{
                break;
            }
        }
        if (targetClass.isInstance(target)){
            return target;
        }
        return null;
    }

    public static Expression getMaxConcatenatedExpression(Expression node){
        Node wapperNode = node.getParentNode().get();
        while (wapperNode instanceof BinaryExpr){
            node = (BinaryExpr) wapperNode;
            if (wapperNode.getParentNode().isPresent()) {
                wapperNode = wapperNode.getParentNode().get();
            }else {
                break;
            }
        }
        return node;
    }

    public static boolean checkValidConcatenatedExpression(Expression node, Expression self){
        if (node instanceof BinaryExpr){
            BinaryExpr binaryExpr = (BinaryExpr) node;
            return checkValidConcatenatedExpression(binaryExpr.getLeft(), self)
                    && checkValidConcatenatedExpression(binaryExpr.getRight(), self);
        }else if (node instanceof LiteralExpr || node instanceof NameExpr || node instanceof FieldAccessExpr){
            return true;
        }else if (ASTUtils.matchRange(node.getRange().get(), self.getRange().get())){
            return true;
        }else {
            return false;
        }
    }


    public static List<Expression> decomposeConcatenatedExpression(Expression node){
        List<Expression> expressions = new ArrayList<>();
        if (node instanceof BinaryExpr){
            List<Expression> leftExprs = decomposeConcatenatedExpression(((BinaryExpr) node).getLeft());
            List<Expression> rightExprs = decomposeConcatenatedExpression(((BinaryExpr) node).getRight());
            expressions.addAll(leftExprs);
            expressions.addAll(rightExprs);

            return expressions;
        }else {
            expressions.add(node);
            return expressions;
        }
    }

    public static boolean isSumExpression(Expression expr){
        if (expr instanceof BinaryExpr){
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            if (binaryExpr.getOperator() == BinaryExpr.Operator.PLUS &&
                    isSumExpression(binaryExpr.getLeft()) &&
                    isSumExpression(binaryExpr.getRight())){
                    return true;
            } else {
                return false;
            }
        }else if (expr instanceof LiteralExpr || expr instanceof NameExpr || expr instanceof FieldAccessExpr){
            return true;
        }else {
            return false;
        }
    }

    public static boolean matchRange(SerializableRange range1, SerializableRange range2){
        if (range1.begin.line == range2.begin.line &&
                range1.begin.column == range2.begin.column &&
                range1.end.line == range2.end.line &&
                range1.end.column == range2.end.column){
            return true;
        }else {
            return false;
        }
    }

    public static boolean matchRange(Range range1, Range range2){
        return matchRange(new SerializableRange(range1), new SerializableRange(range2));
    }

}
