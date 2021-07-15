package literalTracker.visitor;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.GraphFactory;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.traverser.NodesByCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionVisitor extends LocationRecorderAdapter<ExpressionVisitor.Arg> {
    public static class Arg{
        public GraphFactory graphFactory;

        public String path;
        public String fileName;
        public Map<String, List<FieldNode>> trackedFieldByHashKey = new HashMap<>();
        public Map<String, List<DeclarationNode>> trackedDeclarationByHashKey = new HashMap<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();

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
                trackedDeclarationByHashKey.putIfAbsent(hashKey, new ArrayList<>());
                trackedDeclarationByHashKey.get(hashKey).add(declarationNode);
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

        public List<DeclarationNode> getCandidateDeclarationNodes(String name){
            String key = path + ":" + name;
            return trackedDeclarationByHashKey.get(key);
        }
    }

    public void handelCandidateExpr(Expression n, List<? extends DeclarationNode> candidateDeclarationNodes, ExpressionVisitor.Arg arg){
        DeclarationNode matchedDeclarationNode = null;

        if (candidateDeclarationNodes == null){
            return;
        }
        for (DeclarationNode trackedDeclaration : candidateDeclarationNodes){
            if (trackedDeclaration.match(n)){
                matchedDeclarationNode = trackedDeclaration;
                break;
            }
        }
        if (matchedDeclarationNode == null){
            return;
    }

        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, n.getRange().get(), n.toString());

        BaseNode createdNode = NodeFactory.createNode(n, locationInSourceCode, matchedDeclarationNode);
        createdNode = arg.graphFactory.addLPGraphNode(createdNode, matchedDeclarationNode);
        if (createdNode !=null){
            createdNode = BaseNode.tryTrackingNode(createdNode);
            if (createdNode != null){
                arg.newlyCreatedNodes.add(createdNode);
            }
        }
    }

    @Override
    public void visit(FieldAccessExpr n, Arg arg) {
        super.visit(n, arg);
        List<FieldNode> candidateNodes = arg.getCandidateFieldNodes(n.getNameAsString());
        handelCandidateExpr(n, candidateNodes, arg);
    }

    @Override
    public void visit(NameExpr n, Arg arg) {
        super.visit(n, arg);
        List<DeclarationNode> candidateNodes = arg.getCandidateDeclarationNodes(n.getNameAsString());
        List<FieldNode> candidateFieldNodes = arg.getCandidateFieldNodes(n.getNameAsString());
        if (candidateNodes != null){
            if (candidateFieldNodes != null){
                candidateNodes.addAll(candidateFieldNodes);
            }
            handelCandidateExpr(n, candidateNodes, arg);
        }else {
            handelCandidateExpr(n, candidateFieldNodes, arg);
        }

    }
}
