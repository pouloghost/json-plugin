package gt.tool.plugins.modifier;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import gt.tool.plugins.ui.SelectMethodDialog;

import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class ModifyContext {
    public List<SelectMethodDialog.MethodType> methodTypes;
    public List<PsiField> fields;
    public PsiClass psiClass;

    public ModifyContext(PsiClass rootClass, List<PsiField> selectedFields, List<SelectMethodDialog.MethodType> selectedMethod) {
        psiClass = rootClass;
        fields = selectedFields;
        methodTypes = selectedMethod;
    }

    public void from(ModifyContext context) {
        this.psiClass = context.psiClass;
        this.fields = context.fields;
        this.methodTypes = context.methodTypes;
    }
}
