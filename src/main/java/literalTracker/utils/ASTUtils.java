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
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.SerializableRange;

import java.io.Serializable;
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

    public static String getWrapperClassQualifiedName(Node n){
        ClassOrInterfaceDeclaration classNode =
                (ClassOrInterfaceDeclaration) ASTUtils.getTargetWarpper(n, ClassOrInterfaceDeclaration.class);
        if (classNode != null && classNode.getFullyQualifiedName().isPresent()){
            return classNode.getFullyQualifiedName().get();
        }
        return null;
    }

    public static String getWrapperMethodQualifiedSignature(Node n){
        MethodDeclaration methodNode =
                (MethodDeclaration) ASTUtils.getTargetWarpper(n, MethodDeclaration.class);
        try{
            return methodNode.resolve().getQualifiedSignature();
        }catch (Exception e){
            return null;
        }
    }

    public static String getWrapperMethodQName(Node n){
        MethodDeclaration methodNode =
                (MethodDeclaration) ASTUtils.getTargetWarpper(n, MethodDeclaration.class);
        try{
            return methodNode.getNameAsString();
        }catch (Exception e){
            return null;
        }
    }

    public static void getWapperCall(Node node){
        MethodCallExpr callExpr = (MethodCallExpr) getTargetWarpper(node, MethodCallExpr.class);
        if (callExpr == null){
            return;
        }
        String methodCallName = callExpr.toString();
        System.out.println(methodCallName);
        try {
            ResolvedMethodDeclaration resolvedMethodDeclaration = callExpr.resolve();
            System.out.println("---" + resolvedMethodDeclaration.getSignature());
            System.out.println("---" + resolvedMethodDeclaration.getQualifiedSignature());
            System.out.println("---" + resolvedMethodDeclaration.getClassName());
        }catch (Exception e){
            e.printStackTrace();
        }
        if (callExpr.getParentNode().isPresent()){
            if (callExpr.getParentNode().get() instanceof VariableDeclarator){
                VariableDeclarator variableDeclarator = (VariableDeclarator) callExpr.getParentNode().get();
                System.out.println("---" + variableDeclarator.getNameAsString());
            }else if (callExpr.getParentNode().get() instanceof AssignExpr){
                AssignExpr assignExpr = (AssignExpr) callExpr.getParentNode().get();
                if (assignExpr.getTarget().isNameExpr()){
                    System.out.println("---" + assignExpr.getTarget().asNameExpr().getNameAsString());
                }
            }
        }

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
            return checkValidConcatenatedExpression(binaryExpr.getLeft(), self) && checkValidConcatenatedExpression(binaryExpr.getRight(), self);
        }else if (node instanceof LiteralExpr || node instanceof NameExpr || node instanceof FieldAccessExpr){
            return true;
        }else if (ASTUtils.match(node.getRange().get(), self.getRange().get())){
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

    public static boolean match(SerializableRange range1, SerializableRange range2){
        if (range1.begin.line == range2.begin.line &&
                range1.begin.column == range2.begin.column &&
                range1.end.line == range2.end.line &&
                range1.end.column == range2.end.column){
            return true;
        }else {
            return false;
        }
    }

    public static boolean match(Range range1, Range range2){
        return match(new SerializableRange(range1), new SerializableRange(range2));
    }

    public static String calcStringConcatenationExpression(Expression expression) throws Exception {
        if (expression instanceof StringLiteralExpr){
            return ((StringLiteralExpr) expression).asString();
        } else if (expression instanceof NameExpr || expression instanceof FieldAccessExpr){
            ResolvedValueDeclaration resolvedValueDeclaration = null;
            if (expression instanceof NameExpr){
                resolvedValueDeclaration =
                        StaticJavaParser.getConfiguration().getSymbolResolver().get()
                                .resolveDeclaration(expression.asNameExpr(), ResolvedValueDeclaration.class);
                //resolvedValueDeclaration = expression.asNameExpr().resolve();
            }else {
                resolvedValueDeclaration =
                        StaticJavaParser.getConfiguration().getSymbolResolver().get()
                                .resolveDeclaration(expression.asFieldAccessExpr(), ResolvedValueDeclaration.class);
                //resolvedValueDeclaration = expression.asFieldAccessExpr().resolve();
            }
            if (resolvedValueDeclaration instanceof JavaParserFieldDeclaration) {
                JavaParserFieldDeclaration fieldDeclaration = ((JavaParserFieldDeclaration) resolvedValueDeclaration);
                VariableDeclarator variableDeclarator = fieldDeclaration.getVariableDeclarator();
                if (variableDeclarator.getInitializer().isPresent()) {
                    return calcStringConcatenationExpression(variableDeclarator.getInitializer().get());
                }
            }
        } else if (expression instanceof BinaryExpr){
            BinaryExpr expr = (BinaryExpr) expression;
            if (expr.getOperator() == BinaryExpr.Operator.PLUS){
                return calcStringConcatenationExpression(expr.getLeft()) + calcStringConcatenationExpression(expr.getRight());
            }
        }
        throw new Exception("can't calc expression: " + expression.toString());
    }

    public static String calcStringInitializer(VariableDeclarator variableDeclarator) throws Exception {
        if (variableDeclarator.getInitializer().isPresent()){
            Expression expression = variableDeclarator.getInitializer().get();
            return calcStringConcatenationExpression(expression);
        }
        throw new Exception("can't calc empty expression: " + variableDeclarator.toString());
    }
}
