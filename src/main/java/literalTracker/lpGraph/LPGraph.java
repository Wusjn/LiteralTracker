package literalTracker.lpGraph;

import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.otherNode.AssignNode;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

public class LPGraph implements Serializable {
    @Getter
    private HashMap<String, BaseNode> nodeByLocation = new HashMap<>();
    @Getter
    private Set<BaseNode> unsolvedNodes = new HashSet<>();

    public BaseNode getNode(LocationInSourceCode location) {
        BaseNode node = nodeByLocation.get(location.getHashKey());
        return node;
    }

    public void addNewNode(BaseNode node) {
        if (node == null) {
            return;
        } else if (node instanceof UnsolvedNode) {
            unsolvedNodes.add(node);
        } else {
            nodeByLocation.put(node.getLocation().getHashKey(), node);
        }
    }


    public void deletePropagationOfNonFinalValue() throws LPGraphException {
        Set<BaseNode> nodesToDelete = new HashSet<>();
        for (BaseNode node : nodeByLocation.values()) {
            if (node instanceof AssignNode) {
                AssignNode assignNode = (AssignNode) node;
                if (assignNode.getTargetNode() instanceof FieldNode &&
                        assignNode.getOperator().equals("=") &&
                        !((FieldNode)assignNode.getTargetNode()).hasInitializer) {
                    //viewed as a field initialization expr
                } else {
                    nodesToDelete.add(assignNode.getTargetNode());
                }
            }
        }

        while (!nodesToDelete.isEmpty()) {
            Set<BaseNode> newNodesToDelete = new HashSet<>();
            for (BaseNode node : nodesToDelete) {
                newNodesToDelete.addAll(deletePropagationOfNonFinalValue(node));
            }
            nodesToDelete = newNodesToDelete;
        }
    }

    private List<BaseNode> deletePropagationOfNonFinalValue(BaseNode node) throws LPGraphException {
        List<BaseNode> nodesToDelete = new ArrayList<>();

        if (node instanceof DeclarationNode) {
            ((DeclarationNode) node).setFinalValue(false);
        }
        for (BaseNode nextNode : node.getNextNodes()) {
            if (nextNode instanceof DeclarationNode) {
                nextNode.getPrevNodes().remove(node);
                if (nextNode.getPrevNodes().size() == 0) {
                    deleteSingleNode(nextNode);
                    nodesToDelete.add(nextNode);
                }
            } else if (nextNode instanceof CompositeExpressionNode) {
                CompositeExpressionNode compositeExpressionNode = (CompositeExpressionNode) nextNode;
                compositeExpressionNode.clear(node);
                deleteSingleNode(nextNode);
                nodesToDelete.add(nextNode);
            } else if (nextNode instanceof AssignNode) {
                nextNode.getPrevNodes().clear();
                deleteSingleNode(nextNode);
            } else if (nextNode instanceof UnsolvedNode) {
                nextNode.getPrevNodes().clear();
                deleteSingleNode(nextNode);
            } else {
                throw new LPGraphException("Deleting unkonwn node type");
            }
        }

        node.getNextNodes().clear();
        return nodesToDelete;
    }


    private void deleteSingleNode(BaseNode node) {
        if (node instanceof UnsolvedNode) {
            unsolvedNodes.remove(node);
        } else {
            nodeByLocation.remove(node.getLocation().getHashKey());
        }
    }


    public void propagateValue() {
        List<SimpleLiteralNode> literalNodes = new ArrayList<>();
        for (BaseNode node : nodeByLocation.values()) {
            if (node instanceof SimpleLiteralNode) {
                literalNodes.add((SimpleLiteralNode) node);
                node.valueHasBeenCalculated = true;
            }
        }

        Set<BaseNode> candidateNodes = new HashSet<>();
        for (SimpleLiteralNode literalNode : literalNodes) {
            candidateNodes.addAll(literalNode.getNextNodes());
        }

        while (candidateNodes.size()!=0){
            candidateNodes = propagateValue(candidateNodes);
        }
    }

    private boolean valueCanBeCalculate(BaseNode node){
        for (BaseNode prevNode : node.getPrevNodes()){
            if (!prevNode.valueHasBeenCalculated){
                return false;
            }
        }
        return true;
    }


    private Set<BaseNode> propagateValue(Set<BaseNode> candidateNodes) {
        Set<BaseNode> newCandidateNodes = new HashSet<>();

        for (BaseNode node : candidateNodes){
            if (valueCanBeCalculate(node)){
                node.calculateValue();
                node.valueHasBeenCalculated = true;
                newCandidateNodes.addAll(node.getNextNodes());
            }
        }

        return newCandidateNodes;
    }

}