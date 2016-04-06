package gt.tool.plugins.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import gt.tool.plugins.modifier.Constants;

/**
 * Created by ghost on 2016/4/1.
 */
public class PsiTypeUtils {
    public static boolean isArray(PsiType type) {
        return 0 != type.getArrayDimensions();
    }

    public static boolean isCollection(PsiType type, Project project) {
        PsiType collectionType = PsiType.getTypeByName("java.util.Collection", project, type.getResolveScope());
        return collectionType.isAssignableFrom(type);
    }

    public static PsiType[] getGenericTypes(PsiType type) {
        if (type instanceof PsiClassReferenceType) {
            return ((PsiClassReferenceType) type).getParameters();
        }
        return null;
    }

    public static boolean isMap(PsiType type, Project project) {
        PsiType collectionType = PsiType.getTypeByName("java.util.Map", project, type.getResolveScope());
        return collectionType.isAssignableFrom(type);
    }

    public static boolean isJsonSerializable(PsiType type) {
        PsiClass psiClass = getClass(type);
        if (null == psiClass) {
            return false;
        }
        PsiClass jsonClass = psiClass.findInnerClassByName(Constants.JSON_CLASS_NAME, false);
        return null != jsonClass;
    }

    public static boolean isDirectPuttable(PsiType type) {
        return type instanceof PsiPrimitiveType || type.getCanonicalText().startsWith("java.lang.");
    }

    public static PsiClass getClass(PsiType type) {
        if (type instanceof PsiClassType) {
            return ((PsiClassType) type).resolve();
        }
        return null;
    }

    public static boolean isList(PsiType type) {
        return type.getCanonicalText().startsWith("java.util.List") ||
                type.getCanonicalText().startsWith("java.util.Vector") ||
                type.getCanonicalText().startsWith("java.util.ArrayList") ||
                type.getCanonicalText().startsWith("java.util.LinkedList");
    }
}
