package gt.tool.plugins.modifier.method;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import gt.tool.plugins.modifier.ModifyContext;

/**
 * Created by ghost on 2016/4/1.
 */
public class MethodContext extends ModifyContext {
    public PsiElementFactory elementFactory;
    public JavaCodeStyleManager styleManager;
    public PsiClass jsonClass;

    public MethodContext(ModifyContext context) {
        super(null, null, null);
        from(context);
        Project project = context.psiClass.getProject();
        elementFactory = JavaPsiFacade.getElementFactory(project);
        styleManager = JavaCodeStyleManager.getInstance(project);
    }

    public void setJsonClass(PsiClass jsonClass) {
        this.jsonClass = jsonClass;
    }
}
