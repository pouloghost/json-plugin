package gt.tool.plugins.modifier.method;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * Created by ghost on 2016/4/1.
 */
public class ToJsonArrayModifier extends AbsMethodModifier {
    @Override
    public String getMethodString(MethodContext context) {
        final Project project = mMethodContext.psiClass.getProject();
        final StringBuilder stringBuilder = new StringBuilder(300);
        final PsiClass targetClass = context.psiClass;
        stringBuilder.append("public static org.json.JSONArray toJsonArray(").
                append(targetClass.getName()).
                append("[] objs){ org.json.JSONArray jsonArray = new org.json.JSONArray();").
                append("for(").
                append(targetClass.getName()).
                append(" obj: objs){").
                append("jsonArray.put(").
                append(targetClass.getName()).
                append(".JSON.toJson(obj));}").
                append("return jsonArray;}");
        return stringBuilder.toString();
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
