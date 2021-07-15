package literalTracker.lpGraph;

import com.github.javaparser.ast.expr.AssignExpr;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.otherNode.AssignNode;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LPGraph implements Serializable {
    @Getter
    private HashMap<String, BaseNode> nodeByLocation = new HashMap<>();
    @Getter
    private HashMap<String, BaseNode> deletedNodesByLocation = new HashMap<>();

    public BaseNode getNode(LocationInSourceCode location){
        BaseNode node = nodeByLocation.get(location.getHashKey());
        return node;
    }

    public void addNewNode(BaseNode node){
        if (node == null){
            return;
        }
        nodeByLocation.put(node.getLocation().getHashKey(), node);
    }


    public void deletePropagationOfNonFinalValue(){
        List<BaseNode> nodesToDelete = new ArrayList<>();
        for (BaseNode node : nodeByLocation.values()){
            if (node instanceof AssignNode){
                AssignNode assignNode = (AssignNode) node;
                if (assignNode.getTargetNode() != null){
                    //assert assignNode.prevNode.size() == 1;
                    nodesToDelete.add(assignNode.getTargetNode());
                }
            }
        }

        while (!nodesToDelete.isEmpty()){
            List<BaseNode> newNodesToDelete = new ArrayList<>();
            for (BaseNode node : nodesToDelete){
                newNodesToDelete.addAll(deletePropagationOfNonFinalValue(node));
            }
            nodesToDelete = newNodesToDelete;
        }
    }

    public List<BaseNode> deletePropagationOfNonFinalValue(BaseNode node){
        List<BaseNode> nodesToDelete = new ArrayList<>();

        if (node instanceof DeclarationNode){
            ((DeclarationNode) node).setFinalValue(false);
        }
        for (BaseNode nextNode : node.getNextNodes()){
            if (nextNode instanceof FieldNode || nextNode instanceof LocalVariableNode){
                nextNode.getPrevNodes().clear();
                deleteSingleNode(nextNode);
                nodesToDelete.add(nextNode);
            }else if (nextNode instanceof ParameterNode){
                nextNode.getPrevNodes().remove(node);
                if (nextNode.getPrevNodes().size() == 0){
                    deleteSingleNode(nextNode);
                    nodesToDelete.add(nextNode);
                }
            }else if (nextNode instanceof CompositeExpressionNode){
                CompositeExpressionNode compositeExpressionNode = (CompositeExpressionNode) nextNode;
                compositeExpressionNode.clear(node);
                deleteSingleNode(nextNode);
                nodesToDelete.add(nextNode);
            }else if (nextNode instanceof AssignNode){
                nextNode.getPrevNodes().remove(node);

                AssignNode assignNode = (AssignNode) nextNode;
                if (node == assignNode.getTargetNode()){
                    assignNode.setTargetNode(null);
                    if (assignNode.getValueNode() == null){
                        deleteSingleNode(assignNode);
                    }
                }else {
                    assignNode.setValueNode(null);
                    if (assignNode.getTargetNode() == null){
                        deleteSingleNode(assignNode);
                    }
                }
            }else if (nextNode instanceof UnsolvedNode){
                nextNode.getPrevNodes().clear();
                deleteSingleNode(nextNode);
            }else {
                // this should not happen
                System.exit(10086);
            }
        }

        node.getNextNodes().clear();
        return nodesToDelete;
    }


    public void deleteSingleNode(BaseNode node){
        nodeByLocation.remove(node.getLocation().getHashKey());
        deletedNodesByLocation.put(node.getLocation().getHashKey(), node);
    }

}
