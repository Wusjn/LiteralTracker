package literalTracker.lpGraph.nodeFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionParameterDeclaration;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.utils.ASTUtils;

public class FromMethodLikeCallExpr {

    public static ParameterNode createLPGNodeFromMethodLikeCallExpr(
            int parameterIndex, ResolvedMethodLikeDeclaration resolvedMethodLikeDeclaration, LocationInSourceCode location
    ){

        final String methodName = resolvedMethodLikeDeclaration.getName();

        if (parameterIndex >= resolvedMethodLikeDeclaration.getNumberOfParams()){
            // variable length parameter is used, ignore it
            LocationInSourceCode LocationForParameter = new LocationInSourceCode(
                    "", "", null, methodName
            );
            return new ParameterNode(LocationForParameter, ParameterNode.MethodType.VariableLength, null, parameterIndex, methodName);
        }

        ResolvedParameterDeclaration parameterDeclaration = resolvedMethodLikeDeclaration.getParam(parameterIndex);
        if (parameterDeclaration instanceof JavaParserParameterDeclaration){
            //method from source file
            Parameter parameterASTNode = ((JavaParserParameterDeclaration)parameterDeclaration).getWrappedNode();
            CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(parameterASTNode, CompilationUnit.class);
            CompilationUnit.Storage storage = cu.getStorage().get();
            LocationInSourceCode LocationForParameter = new LocationInSourceCode(
                    storage.getPath().normalize().toString(),
                    storage.getFileName(),
                    parameterASTNode.getRange().get(),
                    parameterASTNode.toString()
            );
            return new ParameterNode(LocationForParameter, parameterASTNode.getNameAsString(), parameterIndex, methodName);
        }else if (parameterDeclaration instanceof ReflectionParameterDeclaration){
            LocationInSourceCode LocationForParameter = new LocationInSourceCode(
                    "", "", null, methodName
            );
            return new ParameterNode(LocationForParameter, ParameterNode.MethodType.Reflection, parameterDeclaration.getName(), parameterIndex, methodName);
        }else {
            //anonymous class or JavassistParameterDeclaration
            //handled same as third party method call
            LocationInSourceCode sourceForParameter = new LocationInSourceCode(
                    "", "", null, methodName
            );
            return new ParameterNode(sourceForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex, methodName);
        }
    }
}
