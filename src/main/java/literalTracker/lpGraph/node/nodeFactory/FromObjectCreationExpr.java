package literalTracker.lpGraph.node.nodeFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionParameterDeclaration;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.utils.ASTUtils;


//Very similar to FromMethodCallExpr, just copied the code from FromMethodCallExpr and pasted here, then fixed the compile errors
public class FromObjectCreationExpr {

    public static ParameterNode createLPGNodeFromObjectCreationExpr(Expression expr, ObjectCreationExpr objectCreationExpr, LocationInSourceCode location){
        int parameterIndex = objectCreationExpr.getArguments().indexOf(expr);
        if (parameterIndex < 0 ){
            //expr is a callee, not a parameter. expr should be a string or a enum
            return null;
        }

        ResolvedConstructorDeclaration constructorDeclaration = null;
        try {
            constructorDeclaration = objectCreationExpr.resolve();
        }catch (Exception e){
            //can't parse method from 3rd party library
            //e.printStackTrace();
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  "", objectCreationExpr.getTypeAsString(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex);
            }
            return parameterNode;
        }

        if (parameterIndex >= constructorDeclaration.getNumberOfParams()){
            // variable length parameter is used, ignore it
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  "", objectCreationExpr.getTypeAsString(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, ParameterNode.MethodType.VariableLength, null, parameterIndex);
            }
            return parameterNode;
        }

        ResolvedParameterDeclaration parameterDeclaration = constructorDeclaration.getParam(parameterIndex);
        if (parameterDeclaration instanceof JavaParserParameterDeclaration){
            //method from source file
            Parameter parameterASTNode = ((JavaParserParameterDeclaration)parameterDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(parameterASTNode, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    storage.getPath().normalize().toString(), storage.getFileName(),  constructorDeclaration.getClassName(), constructorDeclaration.getName(), parameterASTNode.getRange().get(), parameterASTNode.toString()
            );

            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, parameterDeclaration.getName(), parameterIndex);
            }
            return parameterNode;
        }else if (parameterDeclaration instanceof ReflectionParameterDeclaration){
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "",  constructorDeclaration.getClassName(), constructorDeclaration.getName(), null, null
            );
            ParameterNode parameterNode = (ParameterNode) NodeFactory.lpGraph.getNode(sourceForParameter);
            if (parameterNode == null){
                parameterNode = new ParameterNode(sourceForParameter, (ReflectionParameterDeclaration) parameterDeclaration, parameterDeclaration.getName(), parameterIndex);
            }
            return parameterNode;
        }else {
            //this should not happen
            System.exit(100);
            return null;
        }
    }
}
