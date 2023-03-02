package literalTracker.lpGraph.graphFactory.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.utils.ASTUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ORPVisitor extends BaseVisitor<ORPVisitor.Arg> {
    public static class Arg extends BaseVisitor.Arg {
        @Getter
        private LPGraph lpGraph;

        private List<String> configKeys;

        public Arg(GraphFactory graphFactory , List<String> configKeys, LPGraph lpGraph){
            this.graphFactory = graphFactory;

            this.configKeys = configKeys;
            this.lpGraph = lpGraph;
        }
    }

    public boolean isOptionReadPoint(MethodCallExpr n){
        if (!n.getNameAsString().startsWith("get")){
            return false;
        }

        //ignore option read point in Configuration class
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                (ClassOrInterfaceDeclaration) ASTUtils.getTargetWarpper(n, ClassOrInterfaceDeclaration.class);
        if (classOrInterfaceDeclaration != null && classOrInterfaceDeclaration.getNameAsString().contains("Configuration")){
            return false;
        }



        ResolvedMethodDeclaration resolvedMethodDeclaration;
        try{
            resolvedMethodDeclaration = n.resolve();
            if (resolvedMethodDeclaration.getClassName().contains("Configuration")){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            //method can't be resolved
        }

        try{
            NameExpr nameExpr = n.getScope().get().asNameExpr();
            if (nameExpr.getNameAsString().contains("Configuration")){
                return true;
            }else {
                ResolvedValueDeclaration resolvedValueDeclaration = nameExpr.resolve();
                if (resolvedValueDeclaration.getType().describe().contains("Configuration")){
                    return true;
                }else {
                    return false;
                }
            }
        }catch (UnsolvedSymbolException e){
            if (e.getName().contains("Configuration")){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    public List<Expression> getConfigKey(MethodCallExpr n){
        return n.getArguments();
    }

    public List<String> calcConfigKey(Expression expr, Arg arg){
        LocationInSourceCode location = new LocationInSourceCode(
                arg.path, arg.fileName, expr.getRange().get(), expr.toString());

        /*
        if (expr.toString().equals("HdfsClientConfigKeys.Read.ShortCircuit.KEY")) {
            System.out.println();
        }
         */


        BaseNode existingNode = arg.lpGraph.getNode(location);
        if (existingNode != null){
            if (existingNode.getValueType() == BaseNode.ValueType.String){
                List<String> configKeys = new ArrayList<>();
                configKeys.addAll(existingNode.getValues());
                return configKeys;
            }else {
                return null;
            }
        }else {
            if (expr instanceof NameExpr || expr instanceof FieldAccessExpr){
                ResolvedValueDeclaration valueDeclaration;
                try{
                    if (expr instanceof FieldAccessExpr){
                        valueDeclaration = ((FieldAccessExpr) expr).resolve();
                    }else {
                        valueDeclaration = ((NameExpr) expr).resolve();
                    }
                }catch (Exception e){
                    return null;
                }

                Node declaratorNode;
                if (valueDeclaration instanceof JavaParserFieldDeclaration){
                    declaratorNode = ((JavaParserFieldDeclaration) valueDeclaration).getVariableDeclarator();
                }else if (valueDeclaration instanceof JavaParserSymbolDeclaration){
                    declaratorNode = ((JavaParserSymbolDeclaration) valueDeclaration).getWrappedNode();
                }else if (valueDeclaration instanceof JavaParserParameterDeclaration){
                    declaratorNode = ((JavaParserParameterDeclaration)valueDeclaration).getWrappedNode();
                }else if (valueDeclaration instanceof ResolvedFieldDeclaration){
                    ResolvedFieldDeclaration resolvedFieldDeclaration = (ResolvedFieldDeclaration) valueDeclaration;
                    try{
                        FieldDeclaration fieldDeclaration = resolvedFieldDeclaration.toAst().get();
                        declaratorNode = ASTUtils.findVariableDeclaratorFromFieldDeclaration(
                                fieldDeclaration, ((NodeWithSimpleName) expr).getNameAsString()
                        );
                    }catch (Exception e){
                        return null;
                    }
                }else {
                    return null;
                }

                CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(declaratorNode, CompilationUnit.class);
                CompilationUnit.Storage storage = cu.getStorage().get();
                LocationInSourceCode declarationLocation = new LocationInSourceCode(
                        storage.getPath().normalize().toString(),
                        storage.getFileName(),
                        declaratorNode.getRange().get(),
                        declaratorNode.toString()
                );

                /*
                String path = declarationLocation.getPath();
                List<BaseNode> nodes = new ArrayList<>();
                for (String key: arg.lpGraph.getNodeByLocation().keySet()){
                    if (key.startsWith(path)){
                        nodes.add(arg.getLpGraph().getNodeByLocation().get(key));
                    }
                }
                */

                BaseNode existingNode2 = arg.lpGraph.getNode(declarationLocation);
                if (existingNode2 != null) {
                    if (existingNode2.getValueType() == BaseNode.ValueType.String) {
                        List<String> configKeys = new ArrayList<>();
                        configKeys.addAll(existingNode2.getValues());
                        return configKeys;
                    } else if (existingNode2 instanceof ParameterNode){
                        ParameterNode parameterNode = (ParameterNode) existingNode2;
                        List<String> configKeys = new ArrayList<>();
                        configKeys.addAll(parameterNode.getValues());
                        return configKeys;
                    } else {
                        return null;
                    }
                }else {
                    return null;
                }

            }else {
                return null;
            }
        }
    }

    private void handleORP(MethodCallExpr n, Arg arg) throws LPGraphException {

        List<Expression> configKeyExprs = getConfigKey(n);
        List<String> configKeys = new ArrayList<>();
        for (Expression expr : configKeyExprs) {
            List<String> possibleConfigKeys = calcConfigKey(expr, arg);
            if (possibleConfigKeys != null) {
                configKeys.addAll(possibleConfigKeys);
            }
        }

        LocationInSourceCode location = new LocationInSourceCode(arg.path, arg.fileName, n.getRange().get(), n.toString());
        BaseNode newTrackingNode = arg.graphFactory.nodeFactory.createNodeFromORP(n, location, configKeys);
        if (newTrackingNode != null){
            arg.newlyTrackedNodes.add(newTrackingNode);
        }
    }

    @Override
    public void visit(MethodCallExpr n, Arg arg) {
        super.visit(n, arg);
        if (!isOptionReadPoint(n)){
            return;
        }

        try {
            handleORP(n,arg);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
