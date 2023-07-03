package literalTracker.lpGraph.graphFactory.visitor;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.graphFactory.NodesByCategory;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.otherNode.ReturnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO: complete it
public class MethodCallVisitor extends BaseVisitor<MethodCallVisitor.Arg> {
    public static class Arg extends BaseVisitor.Arg{
        public Map<String, List<ReturnNode>> trackedReturnNodeByName = new HashMap<>();

        public boolean noTrackingNodes(){
            if (trackedReturnNodeByName.size() == 0){
                return true;
            }else {
                return false;
            }
        }

        public Arg(GraphFactory graphFactory, NodesByCategory nodesByCategory){
            this.graphFactory = graphFactory;

            for (ReturnNode returnNode : nodesByCategory.getReturnNodes()){
                String hashKey = returnNode.methodName;
                trackedReturnNodeByName.putIfAbsent(hashKey, new ArrayList<>());
                trackedReturnNodeByName.get(hashKey).add(returnNode);
            }
        }
    }

    public void handleCandidateMethodCallExpr(
            MethodCallExpr n,
            ResolvedMethodDeclaration resolvedMethodDeclaration,
            List<ReturnNode> candidates,
            Arg arg
    ) throws LPGraphException {

        ReturnNode matchedReturnNode = null;
        for (ReturnNode returnNode : candidates){
            if (returnNode.match(resolvedMethodDeclaration)){
                matchedReturnNode = returnNode;
                break;
            }
        }
        if (matchedReturnNode == null){
            return;
        }

        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, n.getRange().get(), n.toString());
        BaseNode newTrackingNode = arg.graphFactory.nodeFactory.createLeftValueNode(n, locationInSourceCode, matchedReturnNode);
        if (newTrackingNode != null){
            arg.newlyTrackedNodes.add(newTrackingNode);
        }
    }

    @Override
    public void visit(MethodCallExpr n, Arg arg) {
        super.visit(n, arg);

        List<ReturnNode> candidateNodes = arg.trackedReturnNodeByName.get(n.getNameAsString());
        if (candidateNodes != null){
            try {
                ResolvedMethodDeclaration resolvedMethodDeclaration = n.resolve();
                handleCandidateMethodCallExpr(n, resolvedMethodDeclaration, candidateNodes, arg);
            }catch (LPGraphException e){
                e.printStackTrace();
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
    }
}
