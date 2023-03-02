package literalTracker.lpGraph.graphFactory.visitor;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import literalTracker.lpGraph.graphFactory.GraphFactory;
import literalTracker.lpGraph.node.BaseNode;

import java.util.ArrayList;
import java.util.List;

public class BaseVisitor<T extends BaseVisitor.Arg> extends VoidVisitorAdapter<T> {
    public static class Arg{
        public GraphFactory graphFactory;
        public List<BaseNode> newlyTrackedNodes = new ArrayList<>();

        public String path;
        public String fileName;
    }
}
