package literalTracker.featureExtractor;

import literalTracker.Settings;
import literalTracker.utils.DataManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleEncoder {

    public static List<List<List<String>>> encode(List<TreeSample> treeSamples){
        List<List<List<String>>> codeList = new ArrayList<>();
        for (TreeSample treeSample : treeSamples){
            codeList.add(encode(treeSample));
        }
        return codeList;
    }

    public static List<List<String>> encode(TreeSample treeSample){
        List<List<String>> codes = new ArrayList<>();
        for (Sample sample : treeSample.samples){
            List<String> code = new ArrayList<>();
            for (int i = sample.types.size()-1; i>=0; i--){
                code.add(sample.types.get(i).name());
                if (sample.names.get(i) != null){
                    code.addAll(camelCaseSegmentation(sample.names.get(i)));
                }
            }
            codes.add(code);
        }

        return codes;
    }


    public static List<String> camelCaseSegmentation(String identifier){
        String identifierWithoutUnderline = identifier.replaceAll("_", " ");
        String identifierSeparatedBySpace = identifierWithoutUnderline.replaceAll("[A-Z]+", " $0").toLowerCase();
        List<String> camelCaseSegments = new ArrayList<>();
        for (String seg : identifierSeparatedBySpace.split(" ")){
            if (!seg.equals("")){
                camelCaseSegments.add(seg);
            }
        }

        return camelCaseSegments;
    }

    public static void main(String[] args) {
        System.out.println(camelCaseSegmentation("oneIdentifierName"));
        System.out.println(camelCaseSegmentation("OneMethodName"));
        System.out.println(camelCaseSegmentation("one_underlined_name"));
        System.out.println(camelCaseSegmentation("ONE_UPPER_CASE"));
    }
}
