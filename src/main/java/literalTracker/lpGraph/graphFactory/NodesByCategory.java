package literalTracker.lpGraph.graphFactory;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.otherNode.ReturnNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NodesByCategory implements Serializable {
    private List<DeclarationNode> declarationNodes = new ArrayList<>();
    private List<FieldNode> fieldNodes = new ArrayList<>();
    private List<LocalVariableNode> localVariableNodes = new ArrayList<>();
    private List<ParameterNode> parameterNodes = new ArrayList<>();
    private List<ReturnNode> returnNodes = new ArrayList<>();
    private List<BaseNode> notTrackedNodes = new ArrayList<>();

    public boolean noTrackedNodes(){
        if (fieldNodes.size() == 0 && localVariableNodes.size() == 0 && parameterNodes.size()==0 && returnNodes.size()==0){
            return true;
        }else {
            return false;
        }
    }

    public static NodesByCategory categorizeNodes(List<BaseNode> nodes){
        NodesByCategory nodesByCategory = new NodesByCategory();
        for (BaseNode node : nodes){
            if (node instanceof FieldNode){
                nodesByCategory.fieldNodes.add((FieldNode) node);
            }else if (node instanceof LocalVariableNode){
                nodesByCategory.localVariableNodes.add((LocalVariableNode) node);
            }else if (node instanceof ParameterNode){
                ParameterNode newParameterNode = (ParameterNode) node;
                if (newParameterNode.getMethodType() == ParameterNode.MethodType.Normal){
                    nodesByCategory.parameterNodes.add(newParameterNode);
                }else {
                    nodesByCategory.notTrackedNodes.add(newParameterNode);
                }
            }else if (node instanceof ReturnNode){
                nodesByCategory.returnNodes.add((ReturnNode) node);
            }else{
                nodesByCategory.notTrackedNodes.add(node);
            }
        }
        nodesByCategory.declarationNodes.addAll(nodesByCategory.fieldNodes);
        nodesByCategory.declarationNodes.addAll(nodesByCategory.parameterNodes);
        nodesByCategory.declarationNodes.addAll(nodesByCategory.localVariableNodes);

        return nodesByCategory;
    }

    public void merge(NodesByCategory other){
        fieldNodes.addAll(other.fieldNodes);
        localVariableNodes.addAll(other.localVariableNodes);
        parameterNodes.addAll(other.parameterNodes);
        returnNodes.addAll(other.returnNodes);
        notTrackedNodes.addAll(other.notTrackedNodes);
        declarationNodes.addAll(other.declarationNodes);
    }

}
