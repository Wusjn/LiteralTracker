package literalTracker.lpGraph;

import literalTracker.lpGraph.node.*;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.exprNode.ExpressionNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;
import lombok.Getter;
import lombok.Setter;


public class GraphFactory {
    @Getter
    @Setter
    private LPGraph lpGraph = new LPGraph();

    public BaseNode addLPGraphNode(BaseNode newNode, BaseNode prevNode){
        if (newNode instanceof UnsolvedNode && prevNode instanceof ExpressionNode){
            //ignore it, they have the same range
            return null;
        }else {
            BaseNode existingNode = lpGraph.getNode(newNode.getLocation());
            if (existingNode != null){
                try{
                    //important, must be existingNode.combine(newNode), cuz there are links among existing nodes
                    newNode = existingNode.merge(newNode);
                }catch (Exception e){
                    e.printStackTrace();
                    System.exit(10080);
                }
            }
        }
        if (!(newNode instanceof CompositeExpressionNode)){
            connectNodes(prevNode, newNode);
        }
        lpGraph.addNewNode(newNode);

        return newNode;
    }

    public void connectNodes(BaseNode prevNode, BaseNode newNode){
        newNode.getPrevNodes().add(prevNode);
        prevNode.getNextNodes().add(newNode);

        if (newNode instanceof FieldNode || newNode instanceof LocalVariableNode){
            if (prevNode.getValueType() != BaseNode.ValueType.Unknown){
                newNode.setValueType(prevNode.getValueType());
                newNode.setValue(prevNode.getValue());
            }
        }
    }

    public SimpleLiteralNode addLPGraphLiteralNode(String literal, LocationInSourceCode location, BaseNode.ValueType valueType){
        SimpleLiteralNode newNode = new SimpleLiteralNode(location, valueType, literal);
        lpGraph.addNewNode(newNode);
        return newNode;
    }

}
