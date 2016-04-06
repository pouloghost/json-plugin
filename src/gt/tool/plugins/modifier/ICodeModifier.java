package gt.tool.plugins.modifier;

/**
 * Created by ghost on 2016/4/1.
 */
public interface ICodeModifier {
    void modify(ModifyContext context);
    int getPriority();
}
