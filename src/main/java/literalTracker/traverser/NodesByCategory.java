package literalTracker.traverser;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;

import java.util.ArrayList;
import java.util.List;

public class NodesByCategory {
    public List<DeclarationNode> declarationNodes = new ArrayList<>();
    public List<FieldNode> fieldNodes = new ArrayList<>();
    public List<LocalVariableNode> localVariableNodes = new ArrayList<>();
    public List<ParameterNode> parameterNodes = new ArrayList<>();
    public List<BaseNode> leafNodes = new ArrayList<>();

    public boolean noTrackedNodes(){
        if (fieldNodes.size() == 0 && localVariableNodes.size() == 0 && parameterNodes.size()==0){
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
                if (newParameterNode.methodType == ParameterNode.MethodType.Normal){
                    nodesByCategory.parameterNodes.add(newParameterNode);
                }else {
                    nodesByCategory.leafNodes.add(newParameterNode);
                }
            }else {
                nodesByCategory.leafNodes.add(node);
            }
        }
        nodesByCategory.declarationNodes.addAll(nodesByCategory.fieldNodes);
        nodesByCategory.declarationNodes.addAll(nodesByCategory.parameterNodes);
        nodesByCategory.declarationNodes.addAll(nodesByCategory.localVariableNodes);

        return nodesByCategory;
    }

}
