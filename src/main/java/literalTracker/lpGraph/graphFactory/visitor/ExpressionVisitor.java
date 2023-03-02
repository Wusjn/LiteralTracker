package literalTracker.lpGraph.graphFactory.visitor;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import literalTracker.lpGraph.LPGraphException;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.graphFactory.NodesByCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionVisitor extends BaseVisitor<ExpressionVisitor.Arg> {
    public static class Arg extends BaseVisitor.Arg{
        public Map<String, List<FieldNode>> trackedFieldByHashKey = new HashMap<>();
        public Map<String, List<DeclarationNode>> trackedLocalDeclarationByHashKey = new HashMap<>();

        public boolean noTrackingNodes(){
            if (trackedFieldByHashKey.size() == 0 && trackedLocalDeclarationByHashKey.size() == 0){
                return true;
            }else {
                return false;
            }
        }

        public Arg(GraphFactory graphFactory, NodesByCategory nodesByCategory){
            this.graphFactory = graphFactory;

            for (FieldNode fieldNode : nodesByCategory.getFieldNodes()){
                String hashKey = fieldNode.getName();
                trackedFieldByHashKey.putIfAbsent(hashKey, new ArrayList<>());
                trackedFieldByHashKey.get(hashKey).add(fieldNode);
            }

            List<DeclarationNode> declarationNodes = new ArrayList<>();
            declarationNodes.addAll(nodesByCategory.getLocalVariableNodes());
            declarationNodes.addAll(nodesByCategory.getParameterNodes());
            for (DeclarationNode declarationNode : declarationNodes){
                String hashKey = getHashKey(declarationNode);
                trackedLocalDeclarationByHashKey.putIfAbsent(hashKey, new ArrayList<>());
                trackedLocalDeclarationByHashKey.get(hashKey).add(declarationNode);
            }
        }

        public String getHashKey(DeclarationNode declarationNode){
            LocationInSourceCode location = declarationNode.getLocation();
            return location.getPath() + ":" + declarationNode.getName();
        }

        public List<FieldNode> getCandidateFieldNodes(String name){
            String key = name;
            return trackedFieldByHashKey.get(key);
        }

        public List<DeclarationNode> getCandidateLocalDeclarationNodes(String name){
            String key = path + ":" + name;
            return trackedLocalDeclarationByHashKey.get(key);
        }
    }

    public void handelCandidateExpr(
            Expression n,
            ResolvedValueDeclaration resolvedValueDeclaration,
            List<? extends DeclarationNode> candidateDeclarationNodes,
            ExpressionVisitor.Arg arg
    ) throws LPGraphException {

        DeclarationNode matchedDeclarationNode = null;
        for (DeclarationNode trackedDeclaration : candidateDeclarationNodes){
            if (trackedDeclaration.match(resolvedValueDeclaration)){
                matchedDeclarationNode = trackedDeclaration;
                break;
            }
        }
        if (matchedDeclarationNode == null){
            return;
        }

        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, n.getRange().get(), n.toString());
        BaseNode newTrackingNode = arg.graphFactory.nodeFactory.createNodeFromReference(n, locationInSourceCode, matchedDeclarationNode);
        if (newTrackingNode != null){
            arg.newlyTrackedNodes.add(newTrackingNode);
        }
    }

    @Override
    public void visit(FieldAccessExpr n, Arg arg) {
        //super.visit(n, arg);
        List<FieldNode> candidateNodes = arg.getCandidateFieldNodes(n.getNameAsString());

        if (candidateNodes != null){
            try{
                ResolvedValueDeclaration resolvedValueDeclaration = n.resolve();
                handelCandidateExpr(n, resolvedValueDeclaration, candidateNodes, arg);
            }catch (LPGraphException e){
                e.printStackTrace();
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void visit(NameExpr n, Arg arg) {
        super.visit(n, arg);
        List<DeclarationNode> candidateLocalNodes = arg.getCandidateLocalDeclarationNodes(n.getNameAsString());
        List<FieldNode> candidateFieldNodes = arg.getCandidateFieldNodes(n.getNameAsString());

        List<DeclarationNode> candidateNodes = new ArrayList<>();
        if (candidateLocalNodes != null){
            candidateNodes.addAll(candidateLocalNodes);
        }
        if (candidateFieldNodes != null){
            candidateNodes.addAll(candidateFieldNodes);
        }

        try {
            ResolvedValueDeclaration resolvedValueDeclaration = n.resolve();
            handelCandidateExpr(n, resolvedValueDeclaration, candidateNodes, arg);
        }catch (LPGraphException e){
            e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
        }
    }
}
