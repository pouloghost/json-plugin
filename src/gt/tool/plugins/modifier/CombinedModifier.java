package gt.tool.plugins.modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class CombinedModifier implements ICodeModifier {
    private List<ICodeModifier> mModifiers;

    public CombinedModifier(List<? extends ICodeModifier> modifiers) {
        mModifiers = new ArrayList<>(10);
        mModifiers.addAll(modifiers);
        Collections.sort(mModifiers, new Comparator<ICodeModifier>() {
            @Override
            public int compare(ICodeModifier o1, ICodeModifier o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
    }

    @Override
    public void modify(ModifyContext context) {
        for (ICodeModifier generator : mModifiers) {
            generator.modify(context);
        }
    }

    @Override
    public int getPriority() {
        if (0 == mModifiers.size()) {
            return Integer.MAX_VALUE;
        }
        return mModifiers.get(0).getPriority();
    }
}
