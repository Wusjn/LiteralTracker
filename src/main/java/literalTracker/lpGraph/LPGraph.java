package literalTracker.lpGraph;

import com.github.javaparser.ast.expr.AssignExpr;
import literalTracker.lpGraph.node.AssignNode;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.LocationInSourceCode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LPGraph {
    public HashMap<String, BaseNode> nodeByLocation = new HashMap<>();

    public BaseNode getNode(LocationInSourceCode location){
        BaseNode node = nodeByLocation.get(location.getHashKey());
        return node;
    }

    public void addNewNode(BaseNode node){
        if (node == null){
            return;
        }

        BaseNode existingNode = nodeByLocation.get(node.location.getHashKey());
        if(existingNode != null && existingNode != node){
            System.exit(100);
        }
        nodeByLocation.put(node.location.getHashKey(), node);
    }

    public void deleteSingleNode(BaseNode node){
        nodeByLocation.remove(node.location.getHashKey());
    }

    public void deletePropagationOfNonFinalValue(DeclarationNode declarationNode){
        declarationNode.finalValue = false;
        for (BaseNode nextNode : declarationNode.nextNodes){
            nextNode.prevNode.remove(declarationNode);
        }
        for (BaseNode nextNode : declarationNode.nextNodes){
            if (nextNode.prevNode.size() == 0 || nextNode.prevNode.size() == 1 && nextNode.prevNode.contains(nextNode)){
                deleteNode(nextNode);
            }
        }
    }

    public void deleteNode(BaseNode prevNode){
        for (BaseNode nextNode : prevNode.nextNodes){
            nextNode.prevNode.remove(prevNode);
        }
        for (BaseNode nextNode : prevNode.nextNodes){
            if (nextNode.prevNode.size() == 0 || nextNode.prevNode.size() == 1 && nextNode.prevNode.contains(nextNode)){
                deleteNode(nextNode);
            }
        }
        deleteSingleNode(prevNode);
    }

    public void deletePropagationOfNonFinalValue(){
        List<BaseNode> nodesToDelete = new ArrayList<>();
        for (BaseNode node : nodeByLocation.values()){
            if (node instanceof AssignNode){
                AssignNode assignNode = (AssignNode) node;
                /*if (assignNode.isTarget){
                    //assert assignNode.prevNode.size() == 1;
                    nodesToDelete.add(assignNode.prevNode.toArray(new BaseNode[1])[0]);
                }*/
            }
        }
        for (BaseNode node : nodesToDelete){
            deletePropagationOfNonFinalValue((DeclarationNode) node);
        }
    }


    public List<SimpleLiteralNode> getAllLiteralNodes(){
        return null;
    }
}
