package gt.tool.plugins.modifier.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import gt.tool.plugins.modifier.ICodeModifier;
import gt.tool.plugins.modifier.ModifyContext;

/**
 * Created by ghost on 2016/4/1.
 */
public abstract class AbsMethodModifier implements ICodeModifier {
    protected MethodContext mMethodContext;

    @Override
    final public void modify(ModifyContext context) {
        PsiMethod method = mMethodContext.elementFactory.createMethodFromText(getMethodString(mMethodContext), mMethodContext.jsonClass);
        final PsiClass targetClass = mMethodContext.jsonClass;
        mMethodContext.styleManager.shortenClassReferences(targetClass.addBefore(method, targetClass.getLastChild()));
    }

    public void setContext(MethodContext context) {
        mMethodContext = context;
    }

    public void dispose() {
        mMethodContext = null;
    }

    public abstract String getMethodString(MethodContext context);
}
