package literalTracker.lpGraph.node.location;

import com.github.javaparser.Range;
import lombok.Getter;

import java.io.Serializable;

public class LocationInSourceCode implements Serializable {
    @Getter
    private String path;
    private String fileName;

    @Getter
    private SerializableRange range;
    @Getter
    private String code;


    public LocationInSourceCode(String path, String fileName, Range range, String code){
        this.path = path;
        this.fileName = fileName;
        if (range != null){
            this.range = new SerializableRange(range);
        }
        this.code = code;
    }

    public LocationInSourceCode(LocationInSourceCode oldLocation , Range newRange, String newCode){
        this(oldLocation.path, oldLocation.fileName, newRange, newCode);
    }

    public String getHashKey(){
        String hashKey = path + ":" + fileName;
        if (range!=null){
            hashKey += ":" + range.begin.line + "_" + range.begin.column;
            hashKey += ":" + range.end.line + "_" + range.end.column;
        }
        hashKey += ":" + code;
        return hashKey;
    }
}
