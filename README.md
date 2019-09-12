# intellijdeodorant-test-task

This is a completed entrance test assignment for IntelliJDeodorant project. \
The solution is an IntellijIdea plugin written in Java8 which consists of two classes:

- ```PasteActionHandlerWithNotification```
- ```CreateAstAction```

### PasteActionHandlerWithNotification:

A handler for ```ACTION_EDITOR_PASTE``` and ```ACTION_EDITOR_PASTE_SIMPLE``` 
(standard actions supported by IDEA)
which creates notifications when some text is pasted into the editor. 
Notifications contain the following line: *Запахам в коде скажем нет!*.

![](/github_pictures/PasteActionHandlerWithNotifications.png?raw=true "PasteActionHandlerWithNotification")

### CreateAstAction:

This class is aimed at showing an abstract syntax tree of a selected block of Java code.
Invocation buttons are presented in the main menu (the upper toolbar) 
and in the editor popup menu (the one that is shown on right-click). 
Both of the buttons are only enabled if some code is selected in the editor. 
This class' workflow consists of the following main steps:
- User selects code in the editor and invokes the action
- Java code is parsed with [JavaParser](https://github.com/javaparser/javaparser).
- The resulting tree is reorganized into ```Tree``` visual component and then shown 
in an '*Abstract Syntax Tree*' tool window (which will be pinned to the right toolbar on first invocation). 

Each generated AST (one for each invokation) is shown on a separate tab in the mentioned tool window.

![](/github_pictures/CreateAstAction.png?raw=true "CreateAstAction")
