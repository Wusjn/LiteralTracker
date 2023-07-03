package literalTracker.lpGraph.nodeFactory;


import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;


public class FromMethodCallExpr {

    public static BaseNode createLPGNodeFromMethodCallExpr(Expression expr, MethodCallExpr methodCallExpr, LocationInSourceCode location){
        int parameterIndex = methodCallExpr.getArguments().indexOf(expr);
        if (parameterIndex < 0 ){
            //expr is a callee, not a parameter. expr should be a string or a enum
            return new UnsolvedNode(location);
        }

        ResolvedMethodDeclaration methodDeclaration;
        try {
            methodDeclaration = methodCallExpr.resolve();
        }catch (Exception e){
            //can't parse method from 3rd party library
            //e.printStackTrace();
            LocationInSourceCode LocationForParameter = new LocationInSourceCode(
                    "", "", null, methodCallExpr.getNameAsString()
            );
            return new ParameterNode(LocationForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex, methodCallExpr.getNameAsString());
        }

        return FromMethodLikeCallExpr.createLPGNodeFromMethodLikeCallExpr(parameterIndex, methodDeclaration, location);
    }


}
