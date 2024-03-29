package ru.hse.lyubortk.jdeodoranttesttask;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.Transferable;

import static ru.hse.lyubortk.jdeodoranttesttask.CreateAstAction.NOTIFICATION_GROUP_DISPLAY_ID;

/**
 * This class is an implementation of EditorActionHandler and EditorTextInsertHandler which
 * creates notification when some text is pasted into the editor.
 */
public class PasteActionHandlerWithNotifications extends EditorActionHandler implements EditorTextInsertHandler {
    private static final String NOTIFICATION_TITLE = "Paste action performed";
    private static final String NOTIFICATION_CONTENT = "Запахам в коде скажем нет!";

    private final EditorActionHandler originalHandler;

    public PasteActionHandlerWithNotifications(@Nullable EditorActionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    protected void doExecute(@NotNull Editor editor,
                             @Nullable Caret caret,
                             DataContext dataContext) {
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }
        sendNotification(editor.getProject());
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {
        if (originalHandler != null && originalHandler instanceof EditorTextInsertHandler) {
            ((EditorTextInsertHandler)originalHandler).execute(editor, dataContext, producer);
        }
        sendNotification(editor.getProject());
    }

    private static void sendNotification(@Nullable Project project) {
        Notifications.Bus.notify(new Notification(
                NOTIFICATION_GROUP_DISPLAY_ID,
                NOTIFICATION_TITLE,
                NOTIFICATION_CONTENT,
                NotificationType.INFORMATION
        ), project);
    }
}
