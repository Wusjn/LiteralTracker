package literalTracker.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public Parser(List<String> libraryPaths){
        try {
            TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();

            List<TypeSolver> librarySolvers = new ArrayList<>();
            for (String libraryPath : libraryPaths){
                librarySolvers.add(new JavaParserTypeSolver(libraryPath));
            }

            //append reflectionTypeSolver to librarySolvers for convenient
            librarySolvers.add(reflectionTypeSolver);
            TypeSolver combinedTypeSolver = new CombinedTypeSolver(librarySolvers.toArray(new TypeSolver[librarySolvers.size()]));

            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
            StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parse(File file, VoidVisitorAdapter visitor, Object arg) throws IOException {

        String path = file.getCanonicalPath();
        CompilationUnit cu = StaticJavaParser.parse(file);

        /*CompilationUnit cu = cuRepo.getCompilationUnitByFilePath(path);
        if (cu == null){
            cu = StaticJavaParser.parse(file);
            cuRepo.addCompilationUnit(path, cu);
        }*/

        cu.accept(visitor, arg);
    }

    public static void main(String[] args) {
        List<String> targetDirs = new ArrayList<>();
        targetDirs.add("./data/test/source");

        Parser parser = new Parser(targetDirs);
        try {
            parser.parse(new File("./data/test/source/UseCounter.java"), new Visitor(), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
