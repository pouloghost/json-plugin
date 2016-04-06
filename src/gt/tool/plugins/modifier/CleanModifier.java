package gt.tool.plugins.modifier;

import com.intellij.psi.PsiClass;

/**
 * Created by ghost on 2016/4/1.
 */
public class CleanModifier implements ICodeModifier {
    @Override
    public void modify(ModifyContext context) {
        PsiClass targetClass = context.psiClass;
        PsiClass jsonClass = targetClass.findInnerClassByName(Constants.JSON_CLASS_NAME, false);
        if (null == jsonClass) {
            return;
        }
        jsonClass.delete();
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }
}
