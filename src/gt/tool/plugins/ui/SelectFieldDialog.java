package gt.tool.plugins.ui;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.VerticalBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class SelectFieldDialog extends DialogWrapper {
    private final CollectionListModel<PsiField> mFieldsCollection = new CollectionListModel<>();
    private LabeledComponent<JPanel> mFieldsComponentPanel;
    private JBList mFieldList;
    private JBCheckBox mIncludeSuperFieldBox;
    private JBCheckBox mIncludeTransientFieldBox;
    private final PsiClass mClass;

    private ActionListener mCheckBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setupFieldsToShow();
        }
    };

    public SelectFieldDialog(final PsiClass psiClass) {
        super(psiClass.getProject());
        mClass = psiClass;
        setupViews();
        setupFieldsToShow();
        init();
    }

    private void setupFieldsToShow() {
        final PsiField[] fields = mIncludeSuperFieldBox.isSelected() ? mClass.getAllFields() : mClass.getFields();
        mFieldsCollection.removeAll();
        for (PsiField field : fields) {
            if (shouldAddField(field)) {
                mFieldsCollection.add(field);
            }
        }
    }

    private boolean shouldAddField(PsiField field) {
        boolean shouldAdd = mIncludeTransientFieldBox.isSelected() || !field.hasModifierProperty(PsiModifier.TRANSIENT);
        return shouldAdd && !field.hasModifierProperty(PsiModifier.STATIC);
    }

    private void setupViews() {
        setTitle("Select Fields for Json Generation");
        mFieldList = new JBList(mFieldsCollection);
        mFieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(mFieldList).disableAddAction();
        final JPanel panel = decorator.createPanel();
        mFieldsComponentPanel = LabeledComponent.create(panel, "Fields for Generation");
        mIncludeSuperFieldBox = new JBCheckBox("Include Fields from Super Class");
        mIncludeTransientFieldBox = new JBCheckBox("Include Transient Fields");
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JComponent southPanel = super.createSouthPanel();
        if (null == southPanel) {
            return null;
        }
        final VerticalBox root = new VerticalBox();
        if (null != mClass.getSuperClass()) {
            root.add(mIncludeSuperFieldBox);
        }
        root.add(mIncludeTransientFieldBox);
        root.add(southPanel);
        return root;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mFieldsComponentPanel;
    }

    public List<PsiField> getSelectedFields() {
        if (null == mFieldList) {
            return null;
        }
        return mFieldList.getSelectedValuesList();
    }
}
