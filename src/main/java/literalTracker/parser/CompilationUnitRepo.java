package literalTracker.parser;

import com.github.javaparser.ast.CompilationUnit;

import java.util.HashMap;
import java.util.Map;

public class CompilationUnitRepo {

    private Map<String, CompilationUnit> compilationUnitByFilePath = new HashMap<>();

    public boolean addCompilationUnit(String filePath, CompilationUnit cu){
        if (compilationUnitByFilePath.keySet().contains(filePath)){
            return false;
        }
        compilationUnitByFilePath.put(filePath,cu);
        return true;
    }

    public CompilationUnit getCompilationUnitByFilePath(String filePath){
        return compilationUnitByFilePath.get(filePath);
    }

    public void clear(){
        compilationUnitByFilePath.clear();
    }
}
