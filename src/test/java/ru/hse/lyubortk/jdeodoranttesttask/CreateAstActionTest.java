package ru.hse.lyubortk.jdeodoranttesttask;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class CreateAstActionTest {
    private static final String BLOCK_OF_CODE = new StringJoiner("\n")
            .add("int variable = 5;")
            .add("int t = methodCall(variable);")
            .add("return t;")
            .toString();

    private static final String MULTIPLE_METHODS = new StringJoiner("\n")
            .add("void methodOne() {")
            .add("int t = methodCall(variable);")
            .add("}")
            .add("void methodTwo() {")
            .add("}")
            .toString();

    private static final String JAVA_FILE_WITH_IMPORTS_AND_PACKAGE = new StringJoiner("\n")
            .add("package ru.hse.lyubortk.jdeodoranttesttask;")
            .add("import com.github.javaparser.JavaParser;")
            .add("public class Test {")
            .add("public Test() {")
            .add("}")
            .add("}")
            .toString();

    private static final String BROKEN_INPUT = new StringJoiner("\n")
            .add("void methodOne() {")
            .add("int t = methodCall(variable);")
            .toString();


    @Test
    void testParsesBlockOfCode() {
        Node parsedNode = assertDoesNotThrow(() -> CreateAstAction.parseJavaCode(BLOCK_OF_CODE));
        assertEquals(3, parsedNode.getChildNodes().size());
        assertTrue(parsedNode instanceof BlockStmt);
        assertTrue(parsedNode.getChildNodes().get(0) instanceof ExpressionStmt);
        assertTrue(parsedNode.getChildNodes().get(1) instanceof ExpressionStmt);
        assertTrue(parsedNode.getChildNodes().get(2) instanceof ReturnStmt);
    }

    @Test
    void testParsesMultipleMethods() {
        Node parsedNode = assertDoesNotThrow(() -> CreateAstAction.parseJavaCode(MULTIPLE_METHODS));
        assertEquals(3, parsedNode.getChildNodes().size());
        assertTrue(parsedNode instanceof ClassOrInterfaceDeclaration);
        assertTrue(parsedNode.getChildNodes().get(0) instanceof SimpleName);
        assertTrue(parsedNode.getChildNodes().get(1) instanceof MethodDeclaration);
        assertTrue(parsedNode.getChildNodes().get(2) instanceof MethodDeclaration);
    }

    @Test
    void testParsesPackageAndImports() {
        Node parsedNode = assertDoesNotThrow(
                () -> CreateAstAction.parseJavaCode(JAVA_FILE_WITH_IMPORTS_AND_PACKAGE)
        );
        assertNotNull(parsedNode);
    }

    @Test
    void testDoesNotParseBrokenInput() {
        Node parsedNode = assertDoesNotThrow(() -> CreateAstAction.parseJavaCode(BROKEN_INPUT));
        assertNull(parsedNode);
    }
}