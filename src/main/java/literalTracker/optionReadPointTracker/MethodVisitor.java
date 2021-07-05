package literalTracker.optionReadPointTracker;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.visitor.LocationRecorderAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodVisitor extends LocationRecorderAdapter<MethodVisitor.Arg> {
    public static class Arg{
        public String path;
        public String fileName;
        public List<String> configKeys;
        public HashMap<String, List<OptionReadPointNode>> optionReadPointsByConfigKey = new HashMap<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();

        public Arg(List<String> configKeys){
            this.configKeys = configKeys;
        }
    }

    public boolean isOptionReadPoint(MethodCallExpr n){
        if (!n.getNameAsString().startsWith("get")){
            return false;
        }

        ResolvedMethodDeclaration resolvedMethodDeclaration = null;
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

    public String calcConfigKey(Expression expr){
        return null;
    }

    @Override
    public void visit(MethodCallExpr n, Arg arg) {
        super.visit(n, arg);
        if (!isOptionReadPoint(n)){
            return;
        }

        List<Expression> configKeyExprs = getConfigKey(n);
        for (Expression expr : configKeyExprs){
            String configKey = calcConfigKey(expr);
            if (arg.configKeys.contains(configKey)){
                LocationInSourceCode location = new LocationInSourceCode(arg.path, arg.fileName, className, methodName, n.getRange().get(), n.toString());
                OptionReadPointNode optionReadPointNode = new OptionReadPointNode(location);
                arg.optionReadPointsByConfigKey.putIfAbsent(configKey, new ArrayList<>());
                arg.optionReadPointsByConfigKey.get(configKey).add(optionReadPointNode);

                BaseNode newNode = NodeFactory.addLPGraphNode(n, optionReadPointNode.location, optionReadPointNode);
                if (newNode!=null){
                    arg.newlyCreatedNodes.add(newNode);
                }
            }else {
                //TODO: a config without documented
            }
        }
    }
}
