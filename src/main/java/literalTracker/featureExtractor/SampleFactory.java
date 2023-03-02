package literalTracker.featureExtractor;

import com.github.javaparser.ast.expr.AssignExpr;
import javafx.util.Pair;
import literalTracker.lpGraph.LPGraph;
import literalTracker.lpGraph.node.BaseNode;
import literalTracker.lpGraph.node.declNode.DeclarationNode;
import literalTracker.lpGraph.node.declNode.FieldNode;
import literalTracker.lpGraph.node.declNode.LocalVariableNode;
import literalTracker.lpGraph.node.declNode.ParameterNode;
import literalTracker.lpGraph.node.exprNode.CompositeExpressionNode;
import literalTracker.lpGraph.node.exprNode.SimpleLiteralNode;
import literalTracker.lpGraph.node.exprNode.ORPNode;
import literalTracker.lpGraph.node.otherNode.AssignNode;
import literalTracker.lpGraph.node.otherNode.PredicateNode;
import literalTracker.lpGraph.node.otherNode.ReturnNode;
import literalTracker.lpGraph.node.otherNode.UnsolvedNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleFactory {
    public static List<Sample> fromNode(BaseNode node, List<BaseNode> visitedNodes) throws Exception {
        visitedNodes.add(node);

        List<Sample> samples = new ArrayList<>();
        for (BaseNode nextNode : node.getNextNodes()){
            if (visitedNodes.contains(nextNode)){
                continue;
            }
            samples.addAll(fromNode(nextNode, visitedNodes));
        }
        if (samples.size() == 0){
            Sample sample = new Sample();
            addNodeToSample(node, sample);
            samples.add(sample);

            visitedNodes.remove(visitedNodes.size()-1);
            return samples;
        }else {
            for (Sample sample : samples){
                addNodeToSample(node, sample);
            }

            visitedNodes.remove(visitedNodes.size()-1);
            return samples;
        }
    }

    private static void addNodeToSample(BaseNode node, Sample sample) throws Exception {
        if (node instanceof ParameterNode){
            sample.addNode(new Pair<>(Sample.IdentifierType.ParameterName, ((DeclarationNode) node).getName()));
            sample.addNode(new Pair<>(Sample.IdentifierType.MethodName, ((ParameterNode) node).getMethodName()));
        }else if (node instanceof LocalVariableNode){
            sample.addNode(new Pair<>(Sample.IdentifierType.VariableName, ((DeclarationNode) node).getName()));
        }else if (node instanceof FieldNode){
            sample.addNode(new Pair<>(Sample.IdentifierType.FieldName, ((DeclarationNode) node).getName()));
        }else if (node instanceof ORPNode){
            sample.configNames.addAll(((ORPNode) node).getConfigKeys());
            sample.code = node.getLocation().getCode();
        }else if (node instanceof SimpleLiteralNode){
            sample.values = node.getValues();
        }else if (node instanceof CompositeExpressionNode){
            sample.addNode(new Pair<>(Sample.IdentifierType.PartOf, null));
        }else if (node instanceof ReturnNode){
            //do nothing
        }else if (node instanceof PredicateNode){
            sample.addNode(new Pair<>(Sample.IdentifierType.Predicate, null));
        }else if (node instanceof UnsolvedNode || node instanceof AssignNode){
            //do nothing
        } else {
            throw new Exception("unsupported node type");
        }
    }


    public static List<TreeSample> fromLPGraph(LPGraph lpGraph) throws Exception {
        List<BaseNode> roots = new ArrayList<>();

        for (BaseNode node : lpGraph.getNodeByLocation().values()){
            if (node instanceof SimpleLiteralNode){
                roots.add(node);
            }else if (node instanceof CompositeExpressionNode){
                roots.add(node);
            }
        }

        List<TreeSample> treeSamples = new ArrayList<>();
        for (BaseNode root : roots){
            treeSamples.add(new TreeSample(fromNode(root, new ArrayList<>())));
        }

        return treeSamples;
    }

    public static List<TreeSample> fromOPGraph(LPGraph opGraph) throws Exception {
        List<BaseNode> roots = new ArrayList<>();

        for (BaseNode node : opGraph.getNodeByLocation().values()) {
            if (node instanceof ORPNode) {
                roots.add(node);
            }
        }

        List<TreeSample> treeSamples = new ArrayList<>();
        for (BaseNode root : roots) {
            treeSamples.add(new TreeSample(fromNode(root, new ArrayList<>())));
        }

        /*
        Map<String,List<Sample>> samplesByConfigName = new HashMap<>();
        for (Sample sample: samples){
            for (String configName: sample.configNames){
                samplesByConfigName.putIfAbsent(configName, new ArrayList<>());
                samplesByConfigName.get(configName).add(sample);
            }
        }
         */

        return treeSamples;
    }
}
