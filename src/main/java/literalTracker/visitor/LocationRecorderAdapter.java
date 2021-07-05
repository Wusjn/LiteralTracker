package literalTracker.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class LocationRecorderAdapter<T> extends VoidVisitorAdapter<T> {
    public boolean isInMethod = false;
    public String className = "";
    public String methodName = "";

    @Override
    public void visit(ClassOrInterfaceDeclaration n, T arg) {
        String oldClassName = className;

        className = n.getNameAsString();

        super.visit(n, arg);
        className = oldClassName;
    }

    @Override
    public void visit(MethodDeclaration n, T arg) {
        boolean oldIsInMethod = isInMethod;
        String oldMethodName = methodName;

        isInMethod = true;
        methodName = n.getNameAsString();

        super.visit(n, arg);

        methodName = oldMethodName;
        isInMethod = oldIsInMethod;
    }

    @Override
    public void visit(ConstructorDeclaration n, T arg) {
        boolean oldIsInMethod = isInMethod;
        String oldClassName = className;

        isInMethod = true;
        methodName = n.getNameAsString();

        super.visit(n, arg);

        methodName = oldClassName;
        isInMethod = oldIsInMethod;
    }
}
