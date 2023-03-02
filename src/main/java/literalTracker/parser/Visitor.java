package literalTracker.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumConstantDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import literalTracker.lpGraph.node.otherNode.ReturnNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import literalTracker.utils.ASTUtils;


public class Visitor extends VoidVisitorAdapter<String> {

    @Override
    public void visit(NameExpr n, String arg) {
        super.visit(n, arg);
        if (n.getNameAsString().equals("conf")){
            try{
                NameExpr nameExpr = n;
                if (nameExpr.getNameAsString().contains("Configuration")){
                    System.out.println(true);
                }else {
                    ResolvedValueDeclaration resolvedValueDeclaration = nameExpr.resolve();
                    if (resolvedValueDeclaration.getType().describe().contains("Configuration")){
                        System.out.println(true);
                    }else {
                        System.out.println(false);
                    }
                }
            }catch (UnsolvedSymbolException e){
                if (e.getName().contains("Configuration")){
                    System.out.println(true);
                }else{
                    System.out.println(false);
                }
            }
        }
    }

    @Override
    public void visit(VariableDeclarator n, String arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, String arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, String arg) {
        super.visit(n, arg);
    }

}
