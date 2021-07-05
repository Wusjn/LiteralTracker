package literalTracker.visitor;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.nodeFactory.NodeFactory;
import literalTracker.traverser.NodesByCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionVisitor extends LocationRecorderAdapter<ExpressionVisitor.Arg> {
    public static class Arg{
        public String path;
        public String fileName;
        public Map<String, List<FieldNode>> trackedFieldByHashKey = new HashMap<>();
        public Map<String, List<DeclarationNode>> trackedDeclarationByHashKey = new HashMap<>();
        public List<BaseNode> newlyCreatedNodes = new ArrayList<>();

        public Arg(NodesByCategory nodesByCategory){
            for (FieldNode fieldNode : nodesByCategory.fieldNodes){
                String hashKey = fieldNode.name;
                trackedFieldByHashKey.putIfAbsent(hashKey, new ArrayList<>());
                trackedFieldByHashKey.get(hashKey).add(fieldNode);
            }

            List<DeclarationNode> declarationNodes = new ArrayList<>();
            declarationNodes.addAll(nodesByCategory.localVariableNodes);
            declarationNodes.addAll(nodesByCategory.parameterNodes);
            for (DeclarationNode declarationNode : declarationNodes){
                String hashKey = getHashKey(declarationNode);
                trackedDeclarationByHashKey.putIfAbsent(hashKey, new ArrayList<>());
                trackedDeclarationByHashKey.get(hashKey).add(declarationNode);
            }
        }

        public String getHashKey(DeclarationNode declarationNode){
            LocationInSourceCode location = declarationNode.location;
            return location.path + ":" + location.fileName + ":" + declarationNode.name;
        }

        public List<FieldNode> getCandidateFieldNodes(String name){
            String key = name;
            return trackedFieldByHashKey.get(key);
        }

        public List<DeclarationNode> getCandidateDeclarationNodes(String name){
            String key = path + ":" + fileName + ":" + name;
            return trackedDeclarationByHashKey.get(key);
        }
    }

    public void processCandidateExpr(Expression n, List<? extends DeclarationNode> candidateDeclarationNodes, ExpressionVisitor.Arg arg){
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

        LocationInSourceCode locationInSourceCode = new LocationInSourceCode(arg.path, arg.fileName, className, methodName, n.getRange().get(), n.toString());

        BaseNode createdNode = NodeFactory.addLPGraphNode(n, locationInSourceCode, matchedDeclarationNode);
        if (createdNode !=null && !createdNode.hasBeenTracked){
            arg.newlyCreatedNodes.add(createdNode);
            createdNode.hasBeenTracked = true;
        }
    }

    @Override
    public void visit(FieldAccessExpr n, Arg arg) {
        super.visit(n, arg);
        List<FieldNode> candidateNodes = arg.getCandidateFieldNodes(n.getNameAsString());
        processCandidateExpr(n, candidateNodes, arg);
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
            processCandidateExpr(n, candidateNodes, arg);
        }else {
            processCandidateExpr(n, candidateFieldNodes, arg);
        }

    }
}
