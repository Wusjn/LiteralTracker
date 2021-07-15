package literalTracker.lpGraph.node.location;

import com.github.javaparser.Range;

import java.io.Serializable;

public class SerializableRange implements Serializable {
    public static class Caret implements Serializable{
        public int line;
        public int column;
        public Caret(int line, int column){
            this.line = line;
            this.column = column;
        }
        @Override
        public String toString(){
            return this.line + ":" + this.column;
        }
    }
    public Caret begin;
    public Caret end;
    public SerializableRange(Range range){
        this.begin = new Caret(range.begin.line, range.begin.column);
        this.end = new Caret(range.end.line, range.end.column);
    }

    @Override
    public String toString() {
        return "begin:" + begin.toString() + " - end:" + end.toString();
    }

    public String toCypher(){
        return String.format("[%d,%d,%d,%d]",
                begin.line, begin.column, end.line, end.column
        );
    }
}
