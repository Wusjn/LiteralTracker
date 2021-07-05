package literalTracker.lpGraph.node;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public abstract class BaseNode implements Serializable {
    @JsonIgnore
    public Set<BaseNode> prevNode = new HashSet<>();
    @JsonIgnore
    public Set<BaseNode> nextNodes = new HashSet<>();
    public LocationInSourceCode location;
    public boolean hasBeenTracked = false;

    public enum ValueType{
        String, Integer, Long, Double, Char, Boolean, Unknown
    }
    public ValueType valueType = ValueType.Unknown;
    //null iff the type of this node is Value.Unknown
    public String value = null;

    public BaseNode(LocationInSourceCode location){
        this.location = location;
    }
}
