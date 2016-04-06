package gt.tool.plugins.modifier.method;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import gt.tool.plugins.util.PsiTypeUtils;

/**
 * Created by ghost on 2016/4/1.
 */
public class FromJsonModifier extends AbsMethodModifier {
    private StringBuilder mStringBuilder = new StringBuilder(100);

    @Override
    public String getMethodString(MethodContext context) {
        final Project project = mMethodContext.psiClass.getProject();
        final StringBuilder stringBuilder = new StringBuilder(500);
        final PsiClass targetClass = context.psiClass;
        stringBuilder.append("public static ").
                append(targetClass.getName()).
                append(" fromJson(org.json.JSONObject jsonObject){").
                append(targetClass.getName()).
                append(" obj = new ").
                append(targetClass.getName()).
                append("();");
        for (PsiField field : context.fields) {
            appendFieldString(stringBuilder, field, project);
        }
        stringBuilder.append("return obj;}");
        return stringBuilder.toString();
    }

    private void appendFieldString(StringBuilder stringBuilder, PsiField field, Project project) {
        PsiType type = field.getType();
        if (PsiTypeUtils.isDirectPuttable(type)) {
            fieldPutStart(stringBuilder, field);
            appendDirectObject(stringBuilder, type, "\"" + field.getName() + "\"", "jsonObject");
            fieldPutEnd(stringBuilder);
        } else if (PsiTypeUtils.isArray(type)) {
            PsiType elementType = type.getDeepComponentType();
            if (!PsiTypeUtils.isDirectPuttable(elementType) &&
                    !PsiTypeUtils.isJsonSerializable(elementType)) {
                return;
            }
            String lastContainer = "jsonObject";
            String lastIndex = "\"" + field.getName() + "\"";
            final int total = type.getArrayDimensions();
            for (int i = 1; i <= total; ++i) {
                mStringBuilder.setLength(0);
                mStringBuilder.append(field.getName()).
                        append("Array").append(i);
                stringBuilder.append("org.json.JSONArray ").
                        append(mStringBuilder.substring(0)).
                        append(" = ").
                        append(lastContainer).
                        append(".getJSONArray(").
                        append(lastIndex).append(");");
                lastContainer = mStringBuilder.substring(0);
                lastIndex = "0";
            }
            //todo collection/ map as element
            appendArrayIterative(stringBuilder, field, type, elementType, 1);
        } else if (PsiTypeUtils.isCollection(type, project)) {
            appendCollection(stringBuilder, field, project, type);
        } else if (PsiTypeUtils.isMap(type, project)) {
            appendMap(stringBuilder, field, project, type);
        } else {
            String gettable = getObjectGettableString(type, "jsonObject.getJSONObject(", field.getName(), ")");
            if (null == gettable) {
                System.out.println("error generate field " + field.getName() + " of type " + field.getType().getCanonicalText());
                return;
            }
            fieldPutStart(stringBuilder, field);
            stringBuilder.append(gettable);
            fieldPutEnd(stringBuilder);
        }
    }

    private void appendArrayIterative(StringBuilder stringBuilder, PsiField field, PsiType arrayType, PsiType elementType, int ite) {
        final int total = arrayType.getArrayDimensions();
        if (ite > 1) {
            stringBuilder.append("obj.").
                    append(field.getName());
            for (int i = 1; i < ite; ++i) {
                stringBuilder.append("[").
                        append("i").
                        append(ite - 1).
                        append("]");
            }
            stringBuilder.append(" = new ").
                    append(elementType.getCanonicalText());
            for (int i = ite; i <= total; ++i) {
                stringBuilder.append("[").
                        append(field.getName()).
                        append("Array").
                        append(i).
                        append(".length()]");
            }
            stringBuilder.append(";").
                    append(field.getName()).
                    append("Array").
                    append(ite).
                    append(" = ").
                    append(field.getName()).
                    append("Array").
                    append(ite - 1).
                    append(".getJSONArray(").
                    append("i").
                    append(ite - 1).
                    append(");");
        }
        stringBuilder.append("for (int i").
                append(ite).
                append(" = 0; i").
                append(ite).
                append(" < ").
                append(field.getName()).
                append("Array").
                append(ite).
                append(".length(); ++i").
                append(ite).
                append("){\n");
        if (total == ite) {
            stringBuilder.append("obj.").
                    append(field.getName());
            for (int i = 1; i <= ite; ++i) {
                stringBuilder.append("[").
                        append("i").
                        append(i).
                        append("]");
            }
            if (PsiTypeUtils.isDirectPuttable(elementType)) {
                stringBuilder.append(" = ");
                appendDirectObject(stringBuilder, elementType, "i" + ite, field.getName(), "Array", String.valueOf(ite));
                stringBuilder.append(";\n}");
            } else {
                stringBuilder.append(" = ").
                        append(elementType.getCanonicalText()).
                        append(".JSON.fromJson(").
                        append(field.getName()).
                        append("Array").
                        append(ite).
                        append(".getJSONObject(i").
                        append(ite).
                        append("));\n}");
            }
        } else {
            appendArrayIterative(stringBuilder, field, arrayType, elementType, ite + 1);
            stringBuilder.append("}");
        }
    }

    private void appendMap(StringBuilder stringBuilder, PsiField field, Project project, PsiType type) {
        PsiType[] genericTypes = PsiTypeUtils.getGenericTypes(type);
        if (null == genericTypes) {
            return;
        }
        PsiType keyType = genericTypes[0];
        PsiType valueType = genericTypes[1];
        //todo array/ collection/ map as key or value
        if ((!PsiTypeUtils.isDirectPuttable(keyType) && !PsiTypeUtils.isJsonSerializable(keyType)) ||
                (!PsiTypeUtils.isDirectPuttable(valueType) && !PsiTypeUtils.isJsonSerializable(valueType))) {
            return;
        }
        stringBuilder.append("org.json.JSONObject ").
                append(field.getName()).
                append("Map = jsonObject.getJSONObject(\"").
                append(field.getName()).
                append("\");").
                append("obj.").
                append(field.getName()).
                append(" = new HashMap<>();").
                append("for(").
                append(keyType.getCanonicalText()).
                append(" key : ").
                append(field.getName()).
                append("Map.keySet()){\n obj.").
                append(field.getName()).
                append(".put(");
        if (PsiTypeUtils.isDirectPuttable(keyType)) {
            String boxName = keyType.getCanonicalText();
            if (keyType instanceof PsiPrimitiveType) {
                boxName = ((PsiPrimitiveType) keyType).getBoxedTypeName();
            }
            stringBuilder.append(boxName).
                    append(".valueOf(key)");
        } else {
            String puttable = getObjectGettableString(keyType, "key");
            stringBuilder.append(puttable);
        }
        stringBuilder.append(", ");
        if (PsiTypeUtils.isDirectPuttable(valueType)) {
            String boxName = valueType.getCanonicalText();
            if (valueType instanceof PsiPrimitiveType) {
                boxName = ((PsiPrimitiveType) valueType).getBoxedTypeName();
            }
            stringBuilder.append(boxName).
                    append(".valueOf(");
            appendDirectObject(stringBuilder, valueType, "key", field.getName(), "Map");
            stringBuilder.append(")");
        } else {
            String gettable = getObjectGettableString(valueType, field.getName(), "Map.getJSONObject(key)");
            stringBuilder.append(gettable);
        }
        stringBuilder.append(");\n};\n ");
    }

    private void appendCollection(StringBuilder stringBuilder, PsiField field, Project project, PsiType type) {
        PsiType[] genericTypes = PsiTypeUtils.getGenericTypes(type);
        if (null == genericTypes) {
            return;
        }
        stringBuilder.append("org.json.JSONArray ").
                append(field.getName()).
                append("Array = jsonObject.getJSONArray(\"").
                append(field.getName()).
                append("\");\n").
                append("obj.").
                append(field.getName()).
                append(" = ");
        if (PsiTypeUtils.isList(type)) {
            stringBuilder.append("new ArrayList<>(").
                    append(field.getName()).
                    append("Array.length());");
        } else {
            stringBuilder.append("new HashSet<>(").
                    append(field.getName()).
                    append("Array.length());");
        }
        stringBuilder.append("for(int i = 0;i < ").
                append(field.getName()).
                append("Array.length(); ++i){\n obj.").
                append(field.getName()).
                append(".add(");

        if (PsiTypeUtils.isDirectPuttable(genericTypes[0])) {
            appendDirectObject(stringBuilder, genericTypes[0], "i", field.getName(), "Array");
        } else if (PsiTypeUtils.isArray(genericTypes[0])) {
            //todo think
        } else if (PsiTypeUtils.isMap(genericTypes[0], project)) {
            //todo think
        } else {
            String gettable = getObjectGettableString(genericTypes[0], field.getName(), "Array.getJSONObject(i)");
            if (null == gettable) {
                return;
            }
            stringBuilder.append(gettable);
        }

        stringBuilder.append(");\n}");
    }

    private String getObjectGettableString(PsiType type, String... getterParts) {
        if (PsiTypeUtils.isJsonSerializable(type)) {
            mStringBuilder.setLength(0);
            mStringBuilder.append(type.getCanonicalText()).
                    append(".JSON.fromJson(");
            for (String part : getterParts) {
                mStringBuilder.append(part);
            }
            mStringBuilder.append(")");
            return mStringBuilder.substring(0);
        }
        return null;
    }

    private void appendDirectObject(StringBuilder stringBuilder, PsiType type, String index, String... getterTarget) {
        final String fullName = type.getCanonicalText();
        String simpleName = fullName.substring(fullName.lastIndexOf('.' ) + 1);
        String cast = "";
        if ("Float".equals(simpleName) || "float".equals(simpleName)) {
            simpleName = "Double";
            cast = "(float)";
        } else if ("Integer".equals(simpleName)) {
            simpleName = "Int";
        }
        stringBuilder.append(cast);
        for (String part : getterTarget) {
            stringBuilder.append(part);
        }
        stringBuilder.append(".get");
        if (Character.isLowerCase(simpleName.charAt(0))) {
            stringBuilder.append(Character.toUpperCase(simpleName.charAt(0))).
                    append(simpleName.substring(1));
        } else {
            stringBuilder.append(simpleName);
        }
        stringBuilder.append("(").
                append(index).
                append(")");
    }

    private void fieldPutStart(StringBuilder stringBuilder, PsiField field) {
        stringBuilder.append("obj.").
                append(field.getName()).
                append(" = ");
    }

    private void fieldPutEnd(StringBuilder stringBuilder) {
        stringBuilder.append(";");
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
