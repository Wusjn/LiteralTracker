package literalTracker.lpGraph.nodeFactory;


import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;


public class FromObjectCreationExpr {

    public static BaseNode createLPGNodeFromObjectCreationExpr(Expression expr, ObjectCreationExpr objectCreationExpr, LocationInSourceCode location){
        int parameterIndex = objectCreationExpr.getArguments().indexOf(expr);
        if (parameterIndex < 0 ){
            //expr is a callee, not a parameter. expr should be a string or a enum
            return new UnsolvedNode(location);
        }

        ResolvedConstructorDeclaration constructorDeclaration;
        try {
            constructorDeclaration = objectCreationExpr.resolve();
        }catch (Exception e){
            //can't parse method from 3rd party library
            //e.printStackTrace();
            LocationInSourceCode LocationForParameter = new LocationInSourceCode(
                    "", "", null, objectCreationExpr.getTypeAsString()
            );
            return new ParameterNode(LocationForParameter, ParameterNode.MethodType.ThirdParty, null, parameterIndex, objectCreationExpr.getTypeAsString());
        }

        return FromMethodLikeCallExpr.createLPGNodeFromMethodLikeCallExpr(parameterIndex, constructorDeclaration, location);
    }
}
