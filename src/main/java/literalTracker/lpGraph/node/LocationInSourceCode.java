package literalTracker.lpGraph.node;

import com.github.javaparser.Range;

import java.io.Serializable;

public class LocationInSourceCode implements Cloneable, Serializable {
    public String path;
    public String fileName;
    public String className;
    public String methodName;


    public SerializableRange range;
    public String code;

    public LocationInSourceCode(String path, String fileName, String className, String methodName, Range range, String code){
        this.path = path;
        this.fileName = fileName;
        this.className = className;
        this.methodName = methodName;

        if (range == null){
            this.range = null;
        }else {
            this.range = new SerializableRange(range);
            this.code = code;
        }
    }

    @Override
    public LocationInSourceCode clone() {
        try {
            return (LocationInSourceCode) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.exit(100);
            return null;
        }
    }

    public String getHashKey(){
        String hashKey = path + ":" + fileName + ":"  + className + ":" + methodName;
        if (range!=null){
            hashKey += ":" + range.begin.line + "_" + range.begin.column;
            hashKey += ":" + range.end.line + "_" + range.end.column;
        }
        return hashKey;
    }
}
