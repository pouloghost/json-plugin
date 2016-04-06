package gt.tool.plugins.modifier.method;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import gt.tool.plugins.util.PsiTypeUtils;

/**
 * Created by ghost on 2016/4/1.
 */
public class ToJsonModifier extends AbsMethodModifier {
    private StringBuilder mStringBuilder = new StringBuilder(100);

    @Override
    public String getMethodString(MethodContext context) {
        final Project project = mMethodContext.psiClass.getProject();
        final StringBuilder stringBuilder = new StringBuilder(500);
        final PsiClass targetClass = context.psiClass;
        stringBuilder.append("public static org.json.JSONObject toJson(").
                append(targetClass.getName()).
                append(" obj){ org.json.JSONObject jsonObject = new org.json.JSONObject();");
        for (PsiField field : context.fields) {
            appendFieldString(stringBuilder, field, project);
        }
        stringBuilder.append("return jsonObject;}");
        return stringBuilder.toString();
    }

    private void appendFieldString(StringBuilder stringBuilder, PsiField field, Project project) {
        PsiType type = field.getType();
        if (PsiTypeUtils.isDirectPuttable(type)) {
            appendDirectField(stringBuilder, field);
        } else if (PsiTypeUtils.isArray(type)) {
            PsiType elementType = type.getDeepComponentType();
            if (PsiTypeUtils.isDirectPuttable(elementType)) {
                fieldPutStart(stringBuilder, field);
                stringBuilder.append("new org.json.JSONArray(obj.").
                        append(field.getName()).
                        append(")");
                fieldPutEnd(stringBuilder);
            } else if (PsiTypeUtils.isJsonSerializable(elementType)) {
                appendArrayIterative(stringBuilder, field, type, elementType, 1);
                fieldPutStart(stringBuilder, field);
                stringBuilder.append(field.getName()).
                        append("Array1");
                fieldPutEnd(stringBuilder);
            }
            //todo collection/ map as element
        } else if (PsiTypeUtils.isCollection(type, project)) {
            appendCollection(stringBuilder, field, project, type);
        } else if (PsiTypeUtils.isMap(type, project)) {
            appendMap(stringBuilder, field, project, type);
        } else {
            String puttable = getObjectPuttableString(type, "obj.", field.getName());
            if (null == puttable) {
                System.out.println("error generate field " + field.getName() + " of type " + field.getType().getCanonicalText());
                return;
            }
            fieldPutStart(stringBuilder, field);
            stringBuilder.append(puttable);
            fieldPutEnd(stringBuilder);
        }
    }

    private void appendArrayIterative(StringBuilder stringBuilder, PsiField field, PsiType arrayType, PsiType elementType, int ite) {
        final int total = arrayType.getArrayDimensions();
        stringBuilder.append("org.json.JSONArray ").
                append(field.getName()).
                append("Array").
                append(ite).
                append(" = new org.json.JSONArray();\n for(int i").
                append(ite).
                append(" = 0; i").
                append(ite).
                append(" < obj.").
                append(field.getName());
        for (int i = 1; i < ite; ++i) {
            stringBuilder.append("[0]");
        }
        stringBuilder.append(".length; ++i").
                append(ite).
                append("){");

        if (total == ite) {
            stringBuilder.append(field.getName()).
                    append("Array").
                    append(ite).append(".put(");
            stringBuilder.append(elementType.getCanonicalText()).append(".JSON.toJson(").append("obj.").
                    append(field.getName());
            for (int i = 1; i <= ite; ++i) {
                stringBuilder.append("[i").append(i).append("]");
            }
            stringBuilder.append(")");
        } else {
            appendArrayIterative(stringBuilder, field, arrayType, elementType, ite + 1);
            stringBuilder.append(field.getName()).
                    append("Array").
                    append(ite).append(".put(").append(field.getName()).append("Array").append(ite + 1);
        }
        stringBuilder.append(");\n}");
    }

    private void appendMap(StringBuilder stringBuilder, PsiField field, Project project, PsiType type) {
        PsiType[] genericTypes = PsiTypeUtils.getGenericTypes(type);
        if (null == genericTypes) {
            return;
        }
        PsiType keyType = genericTypes[0];
        PsiType valueType = genericTypes[1];
        if (PsiTypeUtils.isDirectPuttable(keyType) &&
                PsiTypeUtils.isDirectPuttable(valueType)) {
            appendDirectField(stringBuilder, field);
        }
        //todo array/ collection/ map as key or value
        if ((!PsiTypeUtils.isDirectPuttable(keyType) && !PsiTypeUtils.isJsonSerializable(keyType)) ||
                (!PsiTypeUtils.isDirectPuttable(valueType) && !PsiTypeUtils.isJsonSerializable(valueType))) {
            return;
        }
        stringBuilder.append("org.json.JSONObject ").
                append(field.getName()).
                append("Map = new org.json.JSONObject();\n for(").
                append(keyType.getCanonicalText()).
                append(" key: obj.").
                append(field.getName()).
                append(".keySet()){").
                append(field.getName()).
                append("Map.put(");
        if (PsiTypeUtils.isDirectPuttable(keyType)) {
            stringBuilder.append("String.valueOf(key)");
        } else {
            String puttable = getObjectPuttableString(keyType, "key");
            stringBuilder.append(puttable);
        }
        stringBuilder.append(", ");
        if (PsiTypeUtils.isDirectPuttable(valueType)) {
            stringBuilder.append("String.valueOf(obj.").append(field.getName()).append(".get(key))");
        } else {
            String puttable = getObjectPuttableString(valueType, "obj.", field.getName(), ".get(key)");
            stringBuilder.append(puttable);
        }
        stringBuilder.append(");}");
        fieldPutStart(stringBuilder, field);
        stringBuilder.append("").
                append(field.getName()).
                append("Map");
        fieldPutEnd(stringBuilder);
    }

    private void appendDirectField(StringBuilder stringBuilder, PsiField field) {
        fieldPutStart(stringBuilder, field);
        stringBuilder.append("obj.").
                append(field.getName());
        fieldPutEnd(stringBuilder);
    }

    private void appendCollection(StringBuilder stringBuilder, PsiField field, Project project, PsiType type) {
        PsiType[] genericTypes = PsiTypeUtils.getGenericTypes(type);
        if (null == genericTypes) {
            return;
        }
        if (PsiTypeUtils.isDirectPuttable(genericTypes[0])) {
            appendDirectField(stringBuilder, field);
        } else if (PsiTypeUtils.isArray(genericTypes[0])) {
            //todo think
        } else if (PsiTypeUtils.isMap(genericTypes[0], project)) {
            //todo think
        } else {
            String puttable = getObjectPuttableString(genericTypes[0], "value");
            if (null == puttable) {
                return;
            }
            stringBuilder.append("org.json.JSONArray ").
                    append(field.getName()).
                    append("Array = new org.json.JSONArray();").
                    append("for(").
                    append(genericTypes[0].getCanonicalText()).
                    append(" value: obj.").
                    append(field.getName()).
                    append("){").
                    append(field.getName()).
                    append("Array.put(").
                    append(puttable).
                    append(");}");
            fieldPutStart(stringBuilder, field);
            stringBuilder.append(field.getName()).
                    append("Array");
            fieldPutEnd(stringBuilder);
        }
    }

    private String getObjectPuttableString(PsiType type, String... getterParts) {
        if (PsiTypeUtils.isJsonSerializable(type)) {
            mStringBuilder.setLength(0);
            mStringBuilder.append(type.getCanonicalText()).
                    append(".JSON.toJson(");
            for (String part : getterParts) {
                mStringBuilder.append(part);
            }
            mStringBuilder.append(")");
            return mStringBuilder.substring(0);
        }
        return null;
    }

    private void fieldPutStart(StringBuilder stringBuilder, PsiField field) {
        stringBuilder.append("jsonObject.put(\"").
                append(field.getName()).
                append("\", ");
    }

    private void fieldPutEnd(StringBuilder stringBuilder) {
        stringBuilder.append(");");
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
