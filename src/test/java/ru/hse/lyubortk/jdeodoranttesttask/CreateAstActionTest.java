package ru.hse.lyubortk.jdeodoranttesttask;

import com.github.javaparser.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class CreateAstActionTest {
    private static final String BLOCK_OF_CODE = new StringJoiner("\n")
            .add("int variable = 5;")
            .add("int t = methodCall(variable);")
            .add("return t;")
            .toString();
    @Test
    void testParsesBlockOfCode() {
        Node parsedNode = assertDoesNotThrow(() -> CreateAstAction.parseJavaCode(BLOCK_OF_CODE));
        assertEquals(3, parsedNode.getChildNodes().size());
    }
}