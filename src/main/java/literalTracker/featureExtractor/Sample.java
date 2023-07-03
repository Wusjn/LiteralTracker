package literalTracker.featureExtractor;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Sample {
    public enum IdentifierType{
        VariableName, FieldName, MethodName, ParameterName, PartOf, Predicate
    }

    public Set<String> values;
    public String code;
    public List<String> configNames= new ArrayList<>();

    public List<IdentifierType> types = new ArrayList<>();
    public List<String> names = new ArrayList<>();

    public void addNode(Pair<IdentifierType,String> pair){
        types.add(pair.getKey());
        names.add(pair.getValue());
    }

}
