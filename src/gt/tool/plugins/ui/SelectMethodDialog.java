package gt.tool.plugins.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class SelectMethodDialog extends DialogWrapper {
    public enum MethodType {
        toJson, fromJson, toJsonArray, fromJsonArray
    }

    private JBList mMethodList;

    public SelectMethodDialog(@Nullable Project project) {
        super(project);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        setTitle("Select What Needs Generate");
        CollectionListModel<MethodType> models = new CollectionListModel<>();
        models.add(MethodType.toJson);
        models.add(MethodType.fromJson);
        models.add(MethodType.toJsonArray);
        models.add(MethodType.fromJsonArray);

        mMethodList = new JBList(models);
        mMethodList.setCellRenderer(new DefaultListCellRenderer());

        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(mMethodList).disableAddAction();
        return LabeledComponent.create(decorator.createPanel(), "Methods");
    }

    public List<MethodType> getSelectedMethods() {
        if (null == mMethodList) {
            return null;
        }
        return mMethodList.getSelectedValuesList();
    }
}
