package gt.tool.plugins.modifier;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import gt.tool.plugins.modifier.method.*;
import gt.tool.plugins.ui.SelectMethodDialog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2016/4/1.
 */
public class ClassModifier implements ICodeModifier {
    private static final HashMap<SelectMethodDialog.MethodType, AbsMethodModifier> sMethodGenerators = new HashMap<>();

    static {
        sMethodGenerators.put(SelectMethodDialog.MethodType.toJson, new ToJsonModifier());
        sMethodGenerators.put(SelectMethodDialog.MethodType.fromJson, new FromJsonModifier());
        sMethodGenerators.put(SelectMethodDialog.MethodType.toJsonArray, new ToJsonArrayModifier());
        sMethodGenerators.put(SelectMethodDialog.MethodType.fromJsonArray, new FromJsonArrayModifier());
    }

    @Override
    public void modify(ModifyContext context) {
        MethodContext methodContext = new MethodContext(context);
        final PsiElementFactory elementFactory = methodContext.elementFactory;
        final JavaCodeStyleManager styleManager = methodContext.styleManager;

        new CleanModifier().modify(context);
        addJsonClass(context, elementFactory, styleManager);
        methodContext.setJsonClass(findJsonClass(context));

        ArrayList<ICodeModifier> modifiers = new ArrayList<>(context.methodTypes.size());
        for (SelectMethodDialog.MethodType type : context.methodTypes) {
            AbsMethodModifier modifier = sMethodGenerators.get(type);
            if (null != modifier) {
                modifier.setContext(methodContext);
                modifiers.add(modifier);
            }
        }
        if (0 == modifiers.size()) {
            return;
        }

        CombinedModifier generator = new CombinedModifier(modifiers);
        generator.modify(context);

        for (ICodeModifier modifier : modifiers) {
            if (modifier instanceof AbsMethodModifier) {
                ((AbsMethodModifier) modifier).dispose();
            }
        }
    }

    private PsiClass findJsonClass(ModifyContext context) {
        return context.psiClass.findInnerClassByName(Constants.JSON_CLASS_NAME, false);
    }

    private PsiClass addJsonClass(ModifyContext context, PsiElementFactory elementFactory, JavaCodeStyleManager styleManager) {
        final PsiClass targetClass = context.psiClass;
        PsiClass dummyClass = elementFactory.createClassFromText(getJsonClassString(context), targetClass);
        PsiClass jsonClass = dummyClass.findInnerClassByName(Constants.JSON_CLASS_NAME, false);
        styleManager.shortenClassReferences(targetClass.addBefore(jsonClass, targetClass.getLastChild()));
        return jsonClass;
    }

    private String getJsonClassString(ModifyContext context) {
        return "public static class JSON {}";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
