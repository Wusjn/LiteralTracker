package literalTracker.lpGraph.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.utils.StringEscapeUtils;
import literalTracker.lpGraph.node.location.LocationInSourceCode;
import literalTracker.utils.Counter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
@Getter
@Setter
public abstract class BaseNode implements Serializable {
    @JsonIgnore
    private Set<BaseNode> prevNodes = new HashSet<>();
    @JsonIgnore
    private Set<BaseNode> nextNodes = new HashSet<>();
    private LocationInSourceCode location;
    private boolean hasBeenTracked = false;

    public enum ValueType{
        String, Integer, Long, Double, Char, Boolean, Unknown
    }
    private ValueType valueType = ValueType.Unknown;
    //null iff the type of this node is Value.Unknown
    private String value = null;

    public BaseNode(LocationInSourceCode location){
        this.location = location;
    }
    abstract public BaseNode merge(BaseNode other) throws Exception;

    public static BaseNode tryTrackingNode(BaseNode baseNode){
        if (baseNode.hasBeenTracked){
            return null;
        }else {
            baseNode.hasBeenTracked = true;
            return baseNode;
        }
    }

    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("MERGE (n:Node {hashKey:\"%s\"})\n", StringEscapeUtils.escapeJava(getLocation().getHashKey())));
        sb.append(onCreateCypher("n"));
        return sb.toString();
    }

    public String toCypher(Counter idCounter){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("MERGE (m%d:Node {hashKey:\"%s\"})\n",idCounter.getCount() , StringEscapeUtils.escapeJava(getLocation().getHashKey())));
        sb.append(String.format("MERGE (n) -[:Propagate]-> (m%d)\n", idCounter.getCount()));
        sb.append(onCreateCypher("m" + idCounter.getCount()));
        return sb.toString();
    }

    private String onCreateCypher(String nodeName){
        return  String.format("ON CREATE SET %s.nodeType=\"%s\", %s.valueType=\"%s\", %s.value=\"%s\", %s.code=\"%s\", %s.path=\"%s\"",
                nodeName, getClass().toString(),
                nodeName, getValueType().toString(),
                nodeName, getValue(),
                nodeName, StringEscapeUtils.escapeJava(getLocation().getCode()),
                nodeName, StringEscapeUtils.escapeJava(getLocation().getPath())
        );
    }
}
