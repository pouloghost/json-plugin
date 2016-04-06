package gt.tool.plugins;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import gt.tool.plugins.modifier.ClassModifier;
import gt.tool.plugins.modifier.ModifyContext;
import gt.tool.plugins.ui.ErrorDialog;
import gt.tool.plugins.ui.SelectFieldDialog;
import gt.tool.plugins.ui.SelectMethodDialog;

import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class GenerateJsonAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final Document document = editor.getDocument();
        PsiClass rootClass = getTargetClass(e, editor);

        if (null == rootClass) {
            new ErrorDialog(project).show();
            return;
        }

        SelectFieldDialog fieldDialog = new SelectFieldDialog(rootClass);
        fieldDialog.show();
        if (!fieldDialog.isOK()) {
            return;
        }

        SelectMethodDialog methodDialog = new SelectMethodDialog(project);
        methodDialog.show();
        if (!methodDialog.isOK()) {
            return;
        }
        generateCode(rootClass, fieldDialog.getSelectedFields(), methodDialog.getSelectedMethods());
    }

    private void generateCode(final PsiClass rootClass, final List<PsiField> selectedFields, final List<SelectMethodDialog.MethodType> selectedMethods) {
        if (selectedMethods.contains(SelectMethodDialog.MethodType.toJsonArray) &&
                !selectedMethods.contains(SelectMethodDialog.MethodType.toJson)) {
            selectedMethods.add(SelectMethodDialog.MethodType.toJson);
        }
        if (selectedMethods.contains(SelectMethodDialog.MethodType.fromJsonArray) &&
                !selectedMethods.contains(SelectMethodDialog.MethodType.fromJson)) {
            selectedMethods.add(SelectMethodDialog.MethodType.fromJson);
        }
        new WriteCommandAction.Simple(rootClass.getProject(), rootClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                new ClassModifier().modify(new ModifyContext(rootClass, selectedFields, selectedMethods));
            }
        }.execute();
    }

    private PsiClass getTargetClass(AnActionEvent e, Editor editor) {
        final PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final PsiClass psiClass = getTargetClass(e, editor);
        e.getPresentation().setVisible((null != project && null != editor && null != psiClass &&
                !psiClass.isEnum() && 0 != psiClass.getAllFields().length));
    }

}
