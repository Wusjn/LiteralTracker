package literalTracker.lpGraph.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.utils.StringEscapeUtils;
import literalTracker.lpGraph.LPGraphException;
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

    public boolean valueHasBeenCalculated = false;
    public enum ValueType{
        String, Integer, Long, Double, Char, Boolean, Unknown
    }
    private ValueType valueType = ValueType.Unknown;
    //validate iff the type of this node is Value.Unknown
    private Set<String> values = new HashSet<>();
    public void calculateValue(){
        for (BaseNode prevNode : prevNodes){
            if (prevNode.getValueType() == ValueType.String){
                values.addAll(prevNode.getValues());
            }
        }
        if (values.size() > 0){
            valueType = ValueType.String;
        }
    }

    public BaseNode(LocationInSourceCode location){
        this.location = location;
    }
    public BaseNode merge(BaseNode other) throws LPGraphException{
        throw new LPGraphException("Combined failed: " + this.getClass().getName() + " and " + other.getClass().getName());
    }

    public boolean tryTrackingNode(){
        if (hasBeenTracked){
            return false;
        }else {
            hasBeenTracked = true;
            return true;
        }
    }

    private String getCypherHashKey(){
        return getLocation().getHashKey() + ":" + this.getClass().getSimpleName();
    }

    public String toCypherLink() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("MERGE (n:Node {hashKey:\"%s\"})\n", StringEscapeUtils.escapeJava(getCypherHashKey())));
        sb.append(onCreateCypher("n"));
        return sb.toString();
    }

    public String toCypherLink(Counter idCounter){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("MERGE (m%d:Node {hashKey:\"%s\"})\n",idCounter.getCount() , StringEscapeUtils.escapeJava(getCypherHashKey())));
        sb.append(String.format("MERGE (n) -[:Propagate]-> (m%d)\n", idCounter.getCount()));
        sb.append(onCreateCypher("m" + idCounter.getCount()));
        return sb.toString();
    }

    public String onCreateCypher(String nodeName){
        return  String.format("ON CREATE SET %s.nodeType=\"%s\", %s.valueType=\"%s\", %s.valueSize=\"%d\", %s.code=\"%s\", %s.path=\"%s\"",
                nodeName, getClass().toString(),
                nodeName, getValueType().toString(),
                //TODO: get it correct
                nodeName, values.size(),
                nodeName, StringEscapeUtils.escapeJava(getLocation().getCode()),
                nodeName, StringEscapeUtils.escapeJava(getLocation().getPath())
        );
    }
}
