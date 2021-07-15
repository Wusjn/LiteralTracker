package literalTracker.orpTracker;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.utils.ASTUtils;
import literalTracker.visitor.LocationRecorderAdapter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MethodVisitor extends LocationRecorderAdapter<MethodVisitor.Arg> {
    public static class Arg{
        @Getter
        private GraphFactory graphFactory;
        @Getter
        private LPGraph lpGraph;

        public String path;
        public String fileName;
        private List<String> configKeys;
        public List<ORPNode> optionReadPoints = new ArrayList<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();

        public Arg(GraphFactory graphFactory , List<String> configKeys, LPGraph lpGraph){
            this.configKeys = configKeys;
            this.graphFactory = graphFactory;
            this.lpGraph = lpGraph;
        }
    }

    public boolean isOptionReadPoint(MethodCallExpr n){
        if (!n.getNameAsString().startsWith("get")){
            return false;
        }

        ResolvedMethodDeclaration resolvedMethodDeclaration;
        try{
            resolvedMethodDeclaration = n.resolve();
        }catch (Exception e){
            return false;
        }

        if (resolvedMethodDeclaration.getClassName().contains("Configuration")){
            return true;
        }else {
            return false;
        }

    }

    public List<Expression> getConfigKey(MethodCallExpr n){
        return n.getArguments();
    }

    public String calcConfigKey(Expression expr, Arg arg){
        LocationInSourceCode location = new LocationInSourceCode(
                arg.path, arg.fileName, expr.getRange().get(), expr.toString());
        BaseNode existingNode = arg.lpGraph.getNode(location);
        if (existingNode != null){
            if (existingNode.getValueType() == BaseNode.ValueType.String){
                return existingNode.getValue();
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

                VariableDeclarator variableDeclarator;
                if (valueDeclaration instanceof JavaParserFieldDeclaration){
                    variableDeclarator = ((JavaParserFieldDeclaration) valueDeclaration).getVariableDeclarator();
                }else if (valueDeclaration instanceof JavaParserVariableDeclaration){
                    variableDeclarator = ((JavaParserVariableDeclaration) valueDeclaration).getVariableDeclarator();
                }else{
                    return null;
                }

                CompilationUnit cu = (CompilationUnit) ASTUtils.getTargetWarpper(variableDeclarator, CompilationUnit.class);
                CompilationUnit.Storage storage = cu.getStorage().get();
                LocationInSourceCode declarationLocation = new LocationInSourceCode(
                        storage.getPath().normalize().toString(),
                        storage.getFileName(),
                        expr.getRange().get(),
                        expr.toString()
                );
                BaseNode existingNode2 = arg.lpGraph.getNode(declarationLocation);
                if (existingNode2 != null) {
                    if (existingNode2.getValueType() == BaseNode.ValueType.String) {
                        return existingNode2.getValue();
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

    @Override
    public void visit(MethodCallExpr n, Arg arg) {
        super.visit(n, arg);
        if (!isOptionReadPoint(n)){
            return;
        }

        List<Expression> configKeyExprs = getConfigKey(n);
        List<String> configKeys = new ArrayList<>();
        for (Expression expr : configKeyExprs) {
            String configKey = calcConfigKey(expr, arg);
            if (configKey != null) {
                configKeys.add(configKey);
            }
        }
        LocationInSourceCode location = new LocationInSourceCode(arg.path, arg.fileName, n.getRange().get(), n.toString());
        ORPNode orpNode = new ORPNode(location, configKeys);
        arg.optionReadPoints.add(orpNode);
        arg.graphFactory.getLpGraph().addNewNode(orpNode);

        BaseNode newNode = NodeFactory.createNode(n, orpNode.getLocation(), orpNode);
        newNode = arg.graphFactory.addLPGraphNode(newNode, orpNode);
        if (newNode!=null){
            newNode = BaseNode.tryTrackingNode(newNode);
            if (newNode != null){
                arg.newlyCreatedNodes.add(newNode);
            }
        }

    }
}
