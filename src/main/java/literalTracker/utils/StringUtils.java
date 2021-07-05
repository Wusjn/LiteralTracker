package literalTracker.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringUtils {
    public static String getShortname(String fullname){
        String[] splits = fullname.split("\\(")[0].split("\\.");
        //System.out.println(splits[splits.length - 1]);
        return splits[splits.length - 1];
    }

    public static boolean isPostfix(String postfix, String config){
        if (postfix.endsWith(".")){
            return false;
        }

        List<String> parts = new ArrayList<String>();
        for (String part : postfix.split("\\.")){
            if (part.strip() != ""){
                parts.add(part);
            }
        }
        Collections.reverse(parts);

        if (parts.size() == 0){
            return false;
        }

        List<String> configParts = new ArrayList<>();
        Collections.addAll(configParts,config.split("\\."));
        Collections.reverse(configParts);

        if (parts.size() > configParts.size()){
            return false;
        }
        for (int i=0;i<parts.size();i++){
            if (!parts.get(i).equals(configParts.get(i))){
                return false;
            }
        }
        return true;
    }
}
