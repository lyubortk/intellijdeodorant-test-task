package ru.hse.lyubortk.jdeodoranttesttask;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.Node;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Menu action which constructs an abstract syntax tree from a selection of characters.
 */
public class CreateAstAction extends AnAction {
    static final String NOTIFICATION_GROUP_DISPLAY_ID = "JDeodorant test task";

    private static final String NOTIFICATION_TITLE = "AST building error";
    private static final String NOTIFICATION_CONTENT = "Could not create AST from selected code";
    private static final String TOOL_WINDOW_ID = "Abstract Syntax Tree";
    private static final List<ParseStart<? extends Node>> PARSE_OPTIONS = Arrays.asList(
            ParseStart.COMPILATION_UNIT,
            ParseStart.BLOCK,
            ParseStart.CLASS_BODY,
            ParseStart.IMPORT_DECLARATION,
            ParseStart.MODULE_DIRECTIVE,
            ParseStart.ANNOTATION,
            ParseStart.TYPE,
            ParseStart.PACKAGE_DECLARATION
    );

    /*
     * Registers PasteActionHandlerWithNotifications handler.
     * Action classes are guaranteed to be loaded into jvm and therefore their static initialization
     * section could be used to register handlers. (This way of registering handlers in unrelated
     * classes' static initialization sections may seem strange but still it is presented in the
     * official Intellij Platform SDK DevGuide).
     */
    static {
        PasteActionHandlerWithNotifications.registerHandler();
    }

    /**
     * Constructs abstract syntax tree from text selected by the primary caret.
     * @param e  Event related to this action
     */
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();

        final Node astRootNode = parseJavaCode(document, primaryCaret);
        if (astRootNode == null) {
            sendNotification(project);
            return;
        }

        showAST(project, astRootNode);
    }

    /**
     * Enables menu item if all conditions are met:
     *   A project is open,
     *   An editor is active,
     *   Some characters are selected
     * @param e  Event related to this action
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabled(
                project != null
                        && editor != null
                        && editor.getSelectionModel().hasSelection()
        );
    }

    @Nullable
    private Node parseJavaCode(Document document, Caret primaryCaret) {
        final int start = primaryCaret.getSelectionStart();
        final int end = primaryCaret.getSelectionEnd();
        final String selectedText = document.getText(new TextRange(start, end));

        Node astRootNode = null;
        final JavaParser parser = new JavaParser();
        for (ParseStart<? extends Node> parseStart : PARSE_OPTIONS) {
            final String textToParse = parseStart.equals(ParseStart.BLOCK)
                    ? createArtificialCodeBlock(selectedText)
                    : selectedText;

            ParseResult<? extends Node> result =
                    parser.parse(parseStart, Providers.provider(textToParse));

            if (result.isSuccessful() && result.getResult().isPresent()) {
                astRootNode = result.getResult().get();
                break;
            }
        }

        // handle multiple CLASS_BODY elements (multiple methods or multiple fields)
        if (astRootNode == null) {
            ParseResult<? extends Node> result = parser.parse(
                    ParseStart.CLASS_BODY,
                    Providers.provider(createArtificialClass(selectedText))
            );
            if (result.isSuccessful() && result.getResult().isPresent()) {
                astRootNode = result.getResult().get();
            }
        }
        return astRootNode;
    }

    private void showAST(Project project, Node astRootNode) {
        final Tree tree = new Tree(constructTreeNode(astRootNode));
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        ToolWindow window = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (window == null) {
            window = toolWindowManager.registerToolWindow(
                    TOOL_WINDOW_ID,
                    true,
                    ToolWindowAnchor.RIGHT
            );
        }


        final ContentManager windowContentManager = window.getContentManager();
        final LocalDateTime now = LocalDateTime.now();
        final String tabId = String.format(
                "%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond()
        );
        final Content content = windowContentManager
                .getFactory()
                .createContent(new JBScrollPane(tree), tabId, false);
        windowContentManager.addContent(content);
    }

    @NotNull
    private static String createArtificialCodeBlock(@NotNull String selectedText) {
        return "{" + selectedText + "}";
    }

    @NotNull
    private static String createArtificialClass(@NotNull String selectedText) {
        return "class FakeClass {\n" + selectedText + "\n}";
    }

    @NotNull
    private static DefaultMutableTreeNode constructTreeNode(@NotNull Node parseNode) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(getNodeMessage(parseNode));
        for (Node child : parseNode.getChildNodes()) {
            root.add(constructTreeNode(child));
        }
        return root;
    }

    @NotNull
    private static String getNodeMessage(@NotNull Node parseNode) {
        return parseNode.getClass().getSimpleName() + ":      '" + parseNode.toString() + "'";
    }

    private static void sendNotification(@Nullable Project project) {
        Notifications.Bus.notify(new Notification(
                NOTIFICATION_GROUP_DISPLAY_ID,
                NOTIFICATION_TITLE,
                NOTIFICATION_CONTENT,
                NotificationType.ERROR
        ), project);
    }
}
