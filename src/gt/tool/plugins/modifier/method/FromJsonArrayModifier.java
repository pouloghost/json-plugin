package gt.tool.plugins.modifier.method;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * Created by ghost on 2016/4/1.
 */
public class FromJsonArrayModifier extends AbsMethodModifier {
    @Override
    public String getMethodString(MethodContext context) {
        final Project project = mMethodContext.psiClass.getProject();
        final StringBuilder stringBuilder = new StringBuilder(300);
        final PsiClass targetClass = context.psiClass;
        stringBuilder.append("public static ").
                append(targetClass.getName()).
                append("[] fromJsonArray(org.json.JSONArray jsonArray){").
                append(targetClass.getName()).
                append("[] objs = new ").
                append(targetClass.getName()).
                append("[jsonArray.length()];").
                append("for(int i = 0; i < jsonArray.length(); ++i){").
                append("objs[i] = ").
                append(targetClass.getName()).
                append(".JSON.fromJson(jsonArray.getJSONObject(i));}").
                append("return objs;}");
        return stringBuilder.toString();
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
