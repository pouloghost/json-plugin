package gt.tool.plugins.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ghost on 2016/4/1.
 */
public class TestBean {
    private int mInt = 1;
    private long mLong = 2;
    private boolean mBoolean = true;
    private float mFloat = 1.1f;
    private double mDouble = 1.2d;
    private String mString = "d";
    private TestInnerBean mObject = new TestInnerBean();
    private int[] mInts = new int[]{1, 2};
    private List<TestInnerBean> mList = new ArrayList<>();

    {
        mList.add(new TestInnerBean());
        mList.add(new TestInnerBean());
    }

    public static class JSON {
        public static String toJson(TestBean obj) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mInt", obj.mInt);
            jsonObject.put("mLong", obj.mLong);
            jsonObject.put("mBoolean", obj.mBoolean);
            jsonObject.put("mFloat", obj.mFloat);
            jsonObject.put("mDouble", obj.mDouble);
            jsonObject.put("mString", obj.mString);
            return jsonObject.toString();
        }
    }
}
