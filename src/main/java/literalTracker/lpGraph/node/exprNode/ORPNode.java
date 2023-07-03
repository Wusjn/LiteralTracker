package literalTracker.lpGraph.node.exprNode;

import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import lombok.Getter;

import java.util.List;

public class ORPNode extends ExpressionNode {
    @Getter
    private List<String> configKeys;

    public ORPNode(LocationInSourceCode location, List<String> configKeys){
        super(location);
        this.configKeys = configKeys;
    }

    @Override
    public String onCreateCypher(String nodeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<configKeys.size();i++){
            sb.append(String.format("\"%s\"", configKeys.get(i)));
            if (i!=configKeys.size()-1){
                sb.append(",");
            }
        }
        sb.append("]");
        return super.onCreateCypher(nodeName) + ", " + String.format("%s.configKeys=%s",nodeName, sb.toString());
    }
}
