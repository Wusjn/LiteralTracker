package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistParameterDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionParameterDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.utils.ASTUtils;

public class FromMethodCallExpr {

    public static ParameterNode createLPGNodeFromMethodCallExpr(Expression expr, MethodCallExpr methodCallExpr, LocationInSourceCode location){
        int parameterIndex = methodCallExpr.getArguments().indexOf(expr);
        if (parameterIndex < 0 ){
            //expr is a callee, not a parameter. expr should be a string or a enum
            return null;
        }

        ResolvedMethodDeclaration methodDeclaration = null;
        try {
            methodDeclaration = methodCallExpr.resolve();
        }catch (Exception e){
            //can't parse method from 3rd party library
            //e.printStackTrace();
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  "", methodCallExpr.getNameAsString(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex);
            }
            return parameterNode;
        }

        if (parameterIndex >= methodDeclaration.getNumberOfParams()){
            // variable length parameter is used, ignore it
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  "", methodCallExpr.getNameAsString(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, ParameterNode.MethodType.VariableLength, null, parameterIndex);
            }
            return parameterNode;
        }

        ResolvedParameterDeclaration parameterDeclaration = methodDeclaration.getParam(parameterIndex);
        if (parameterDeclaration instanceof JavaParserParameterDeclaration){
            //method from source file
            Parameter parameterASTNode = ((JavaParserParameterDeclaration)parameterDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(parameterASTNode, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    storage.getPath().normalize().toString(), storage.getFileName(),  methodDeclaration.getClassName(), methodDeclaration.getName(), parameterASTNode.getRange().get(), parameterASTNode.toString()
            );

            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, parameterASTNode.getNameAsString(), parameterIndex);
            }
            return parameterNode;
        }else if (parameterDeclaration instanceof ReflectionParameterDeclaration){
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  methodDeclaration.getClassName(), methodDeclaration.getName(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, (ReflectionParameterDeclaration) parameterDeclaration, parameterDeclaration.getName(), parameterIndex);
            }
            return parameterNode;
        }else {
            //anonymous class or JavassistParameterDeclaration
            //handled same as third party method call
            //System.exit(100);
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  "", methodCallExpr.getNameAsString(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex);
            }
            return parameterNode;
        }
    }
}
