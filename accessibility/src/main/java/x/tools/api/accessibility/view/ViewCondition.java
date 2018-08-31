package x.tools.api.accessibility.view;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@JsonAdapter(value = ViewCondition.JsonAdapter.class)
public class ViewCondition {
    public enum ViewConditionType {
        Callback,
        FieldLessOrEqual,
        FieldLess,
        FieldGreaterOrEqual,
        FieldGreater,
        FieldEqual,
        FieldMatchRegex,
        Father,
        Child,
        ChildCountLessOrEqual,
        ChildCountLess,
        ChildCountGreaterOrEqual,
        ChildCountGreater,
        ChildCountEqual,
        LastChild,
        True,
        False,
        And,
        Or,
        Not,
        Root;

        public static ViewConditionType create(String name) {
            if ("Callback".equals(name)) return Callback;
            if ("FieldLessOrEqual".equals(name)) return FieldLessOrEqual;
            if ("FieldLess".equals(name)) return FieldLess;
            if ("FieldGreaterOrEqual".equals(name)) return FieldGreaterOrEqual;
            if ("FieldGreater".equals(name)) return FieldGreater;
            if ("FieldEqual".equals(name)) return FieldEqual;
            if ("FieldMatchRegex".equals(name)) return FieldMatchRegex;
            if ("Father".equals(name)) return Father;
            if ("Child".equals(name)) return Child;
            if ("ChildCountLessOrEqual".equals(name)) return ChildCountLessOrEqual;
            if ("ChildCountLess".equals(name)) return ChildCountLess;
            if ("ChildCountGreaterOrEqual".equals(name)) return ChildCountGreaterOrEqual;
            if ("ChildCountGreater".equals(name)) return ChildCountGreater;
            if ("ChildCountEqual".equals(name)) return ChildCountEqual;
            if ("LastChild".equals(name)) return LastChild;
            if ("True".equals(name)) return True;
            if ("False".equals(name)) return False;
            if ("And".equals(name)) return And;
            if ("Or".equals(name)) return Or;
            if ("Not".equals(name)) return Not;
            if ("Root".equals(name)) return Root;
            return null;
        }
    }

    public interface Callback {
        boolean judge(ViewNodeInfo node);
    }
    private ViewConditionType type = null;
    private Callback callback = null;
    private String field = null;
    private Double valueNumber = null;
    private String valueString = null;
    private Boolean valueBoolean = null;
    private Pattern rePattern = null;
    private ViewCondition relCondition = null;
    private Integer childCount = null;
    private List<ViewCondition> conditions = null;

    public ViewConditionType getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public String getValueString() {
        return valueString;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public Pattern getRePattern() {
        return rePattern;
    }

    public ViewCondition getRelCondition() {
        return relCondition;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public List<ViewCondition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return new JsonAdapter().toJson(this);
    }

    private ViewCondition(ViewConditionType type) {
        this.type = type;
    }

    private ViewCondition(ViewConditionType type,
                         String field,
                         Double valueNumber,
                         String valueString,
                         Boolean valueBoolean,
                         Pattern rePattern,
                         ViewCondition relCondition,
                         Integer childCount,
                         List<ViewCondition> conditions)
    {
        this.type = type;
        this.field = field;
        this.valueNumber = valueNumber;
        this.valueString = valueString;
        this.valueBoolean = valueBoolean;
        this.rePattern = rePattern;
        this.relCondition = relCondition;
        this.childCount = childCount;
        this.conditions = conditions;
    }

    public ViewCondition newCopy() {
        return new ViewCondition(
            this.type,
            this.field,
            this.valueNumber,
            this.valueString,
            this.valueBoolean,
            this.rePattern,
            this.relCondition,
            this.childCount,
            this.conditions
        );
    }

    private void reset(ViewConditionType type) {
        this.type = type;
        this.field = null;
        this.valueNumber = null;
        this.valueString = null;
        this.valueBoolean = null;
        this.rePattern = null;
        this.relCondition = null;
        this.childCount = null;
        this.conditions = null;
    }

    private void assignFrom(ViewCondition condition) {
        this.type = condition.type;
        this.field = condition.field;
        this.valueNumber = condition.valueNumber;
        this.valueString = condition.valueString;
        this.valueBoolean = condition.valueBoolean;
        this.rePattern = condition.rePattern;
        this.relCondition = condition.relCondition;
        this.childCount = condition.childCount;
        this.conditions = condition.conditions;
    }


    private static Object getInfoField(ViewNodeInfo node, String field) {
        if (node == null)
            return null;
        if (node.info == null)
            return null;
        ViewInfo info = node.info;
        try {
            Field f = info.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(info);
        } catch (Throwable e) {
            return null;
        }
    }
    private static Double getInfoNumberField(ViewNodeInfo node, String field) {
        if (node == null)
            return null;
        if (node.info == null)
            return null;
        ViewInfo info = node.info;
        try {
            Field f = info.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object ret = f.get(info);
            if (ret instanceof Double) {
                return (Double) ret;
            }
            if (ret instanceof Float) {
                return ((Float) ret).doubleValue();
            }
            if (ret instanceof Integer) {
                return ((Integer) ret).doubleValue();
            }
            if (ret instanceof String) {
                return Double.valueOf(((String) ret));
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }
    private static String getInfoStringField(ViewNodeInfo node, String field) {
        if (node == null)
            return null;
        if (node.info == null)
            return null;
        ViewInfo info = node.info;
        try {
            Field f = info.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object ret = f.get(info);
            if (ret instanceof String) {
                return (String) ret;
            }
            return ret.toString();
        } catch (Throwable e) {
            return null;
        }
    }


    public static ViewCondition Callback(Callback callback) {
        ViewCondition condition = new ViewCondition(ViewConditionType.Callback);
        condition.callback = callback;
        return condition;
    }

    public static ViewCondition FieldLessOrEqual(String field, double valueNumber) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldLessOrEqual);
        condition.field = field;
        condition.valueNumber = valueNumber;
        return condition;
    }

    public static ViewCondition FieldLess(String field, double valueNumber) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldLess);
        condition.field = field;
        condition.valueNumber = valueNumber;
        return condition;
    }

    public static ViewCondition FieldGreaterOrEqual(String field, double valueNumber) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldGreaterOrEqual);
        condition.field = field;
        condition.valueNumber = valueNumber;
        return condition;
    }

    public static ViewCondition FieldGreater(String field, double valueNumber) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldGreater);
        condition.field = field;
        condition.valueNumber = valueNumber;
        return condition;
    }


    public static ViewCondition FieldEqual(String field, double valueNumber) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldEqual);
        condition.field = field;
        condition.valueNumber = valueNumber;
        return condition;
    }


    public static ViewCondition FieldEqual(String field, String valueString) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldEqual);
        condition.field = field;
        condition.valueString = valueString;
        return condition;
    }

    public static ViewCondition FieldEqual(String field, boolean valueBoolean) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldEqual);
        condition.field = field;
        condition.valueBoolean = valueBoolean;
        return condition;
    }

    public static ViewCondition FieldMatchRegex(String field, String pattern) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldMatchRegex);
        condition.field = field;
        condition.rePattern = Pattern.compile(pattern);
        return condition;
    }

    public static ViewCondition FieldMatchRegex(String field, Pattern pattern) {
        ViewCondition condition = new ViewCondition(ViewConditionType.FieldMatchRegex);
        condition.field = field;
        condition.rePattern = pattern;
        return condition;
    }

    public static ViewCondition Father(ViewCondition otherCondition) {
        ViewCondition condition = new ViewCondition(ViewConditionType.Father);
        condition.relCondition = otherCondition;
        return condition;
    }

    public static ViewCondition Child(ViewCondition otherCondition) {
        ViewCondition condition = new ViewCondition(ViewConditionType.Child);
        condition.relCondition = otherCondition;
        return condition;
    }

    public static ViewCondition ChildCountLessOrEqual(int childCount) {
        ViewCondition condition = new ViewCondition(ViewConditionType.ChildCountLessOrEqual);
        condition.childCount = childCount;
        return condition;
    }

    public static ViewCondition LastChild() {
        ViewCondition condition = new ViewCondition(ViewConditionType.LastChild);
        return condition;
    }

    public static ViewCondition ChildCountLess(int childCount) {
        ViewCondition condition = new ViewCondition(ViewConditionType.ChildCountLess);
        condition.childCount = childCount;
        return condition;
    }

    public static ViewCondition ChildCountGreaterOrEqual(int childCount) {
        ViewCondition condition = new ViewCondition(ViewConditionType.ChildCountGreaterOrEqual);
        condition.childCount = childCount;
        return condition;
    }

    public static ViewCondition ChildCountGreater(int childCount) {
        ViewCondition condition = new ViewCondition(ViewConditionType.ChildCountGreater);
        condition.childCount = childCount;
        return condition;
    }

    public static ViewCondition ChildCountEqual(int childCount) {
        ViewCondition condition = new ViewCondition(ViewConditionType.ChildCountEqual);
        condition.childCount = childCount;
        return condition;
    }

    public static ViewCondition And(ViewCondition ...conditions) {
        ViewCondition condition = new ViewCondition(ViewConditionType.And);
        condition.conditions = Arrays.asList(conditions);
        return condition;
    }

    public static ViewCondition Or(ViewCondition ...conditions) {
        ViewCondition condition = new ViewCondition(ViewConditionType.Or);
        condition.conditions = Arrays.asList(conditions);
        return condition;
    }

    public static ViewCondition Not(ViewCondition otherCondition) {
        ViewCondition condition = new ViewCondition(ViewConditionType.Not);
        condition.relCondition = otherCondition;
        return condition;
    }

    public final static ViewCondition Root = new ViewCondition(ViewConditionType.Root);
    public final static ViewCondition True = new ViewCondition(ViewConditionType.True);
    public final static ViewCondition False = new ViewCondition(ViewConditionType.False);


    public ViewCondition not() {
        if (this.type == ViewConditionType.Not) {
            this.assignFrom(this.relCondition);
        } else {
            ViewCondition condition = this.newCopy();
            this.reset(ViewConditionType.Not);
            this.relCondition = condition;
        }
        return this;
    }

    public ViewCondition and(ViewCondition ...conditions) {
        List<ViewCondition> conditionList = Arrays.asList(conditions);
        if (this.type == ViewConditionType.And) {
            List<ViewCondition> newList = new ArrayList<>();
            newList.addAll(this.conditions);
            newList.addAll(conditionList);
            this.conditions = newList;
        } else {
            ViewCondition newCondition = this.newCopy();
            this.reset(ViewConditionType.And);

            ArrayList<ViewCondition> newConditions = new ArrayList<>();
            newConditions.add(newCondition);
            newConditions.addAll(conditionList);
            this.conditions = newConditions;
        }
        return this;
    }

    public ViewCondition and(ViewCondition condition) {
        if (this.type == ViewConditionType.And) {
            List<ViewCondition> newList = new ArrayList<>();
            newList.addAll(this.conditions);
            newList.add(condition);
            this.conditions = newList;
        } else {
            ViewCondition newCondition = this.newCopy();
            this.reset(ViewConditionType.And);

            ArrayList<ViewCondition> newConditions = new ArrayList<>();
            newConditions.add(newCondition);
            newConditions.add(condition);
            this.conditions = newConditions;
        }
        return this;
    }

    public ViewCondition or(ViewCondition ...conditions) {
        List<ViewCondition> conditionList = Arrays.asList(conditions);
        if (this.type == ViewConditionType.Or) {
            List<ViewCondition> newList = new ArrayList<>();
            newList.addAll(this.conditions);
            newList.addAll(conditionList);
            this.conditions = newList;
        } else {
            ViewCondition newCondition = this.newCopy();
            this.reset(ViewConditionType.Or);

            ArrayList<ViewCondition> newConditions = new ArrayList<>();
            newConditions.add(newCondition);
            newConditions.addAll(conditionList);
            this.conditions = newConditions;
        }
        return this;
    }

    public ViewCondition or(ViewCondition condition) {
        if (this.type == ViewConditionType.Or) {
            List<ViewCondition> newList = new ArrayList<>();
            newList.addAll(this.conditions);
            newList.add(condition);
            this.conditions = newList;
        } else {
            ViewCondition newCondition = this.newCopy();
            this.reset(ViewConditionType.Or);

            ArrayList<ViewCondition> newConditions = new ArrayList<>();
            newConditions.add(newCondition);
            newConditions.add(condition);
            this.conditions = newConditions;
        }
        return this;
    }


    public static ViewCondition TextEqual(String text) {
        return FieldEqual("text", text);
    }

    public static ViewCondition ContentDescEqual(String text) {
        return FieldEqual("desc", text);
    }

    public static ViewCondition TextMatch(String text) {
        return FieldMatchRegex("text", text);
    }

    public static ViewCondition TextMatch(Pattern text) {
        return FieldMatchRegex("text", text);
    }

    public static ViewCondition ResEqual(String text) {
        return FieldEqual("res", text);
    }

    public static ViewCondition ClsEqual(String text) {
        return FieldEqual("cls", text);
    }

    public static ViewCondition IndexEqual(int index) {
        return FieldEqual("index", index);
    }

    public static ViewCondition IsClickableEqual(boolean isVisible) {
        return FieldEqual("is_clickable", isVisible);
    }

    public static ViewCondition IsVisibleEqual(boolean isVisible) {
        return FieldEqual("is_visible", isVisible);
    }

    public static ViewCondition IsEnableEqual(boolean isVisible) {
        return FieldEqual("is_enable", isVisible);
    }

    public static ViewCondition IsScrollable(boolean isVisible) {
        return FieldEqual("is_scrollable", isVisible);
    }

    public static ViewCondition DescMatch(Pattern text) {
        return FieldMatchRegex("desc", text);
    }

    public static ViewCondition DescMatch(String text) {
        return FieldMatchRegex("desc", text);
    }

    public static ViewCondition DescEqual(String text) {
        return FieldEqual("desc", text);
    }

    public boolean judge(ViewNodeInfo node) {
        if (node == null)
            return false;

        switch (this.type) {
            case Callback: {
                if (this.callback == null) throw new AssertionError();

                return this.callback.judge(node);
            }
            case FieldLessOrEqual: {
                if (this.valueNumber == null) throw new AssertionError();

                Double number = getInfoNumberField(node, this.field);
                if (number == null) {
                    return false;
                }
                return number <= this.valueNumber;
            }
            case FieldLess: {
                if (this.valueNumber == null) throw new AssertionError();

                Double number = getInfoNumberField(node, this.field);
                if (number == null) {
                    return false;
                }
                return number < this.valueNumber;
            }
            case FieldGreaterOrEqual: {
                if (this.valueNumber == null) throw new AssertionError();

                Double number = getInfoNumberField(node, this.field);
                if (number == null) {
                    return false;
                }
                return number >= this.valueNumber;
            }
            case FieldGreater: {
                if (this.valueNumber == null) throw new AssertionError();

                Double number = getInfoNumberField(node, this.field);
                if (number == null) {
                    return false;
                }
                return number > this.valueNumber;
            }
            case FieldEqual: {
                Object field = getInfoField(node, this.field);
                if (field == null) {
                    return false;
                }
                if (field instanceof Double || double.class.equals(field.getClass())) {
                    if (this.valueNumber == null) throw new AssertionError();
                    return this.valueNumber.doubleValue() == ((Double) field).doubleValue();
                }
                if (field instanceof Float || float.class.equals(field.getClass())) {
                    if (this.valueNumber == null) throw new AssertionError();
                    return this.valueNumber.doubleValue() == ((Float) field).doubleValue();
                }
                if (field instanceof Integer || int.class.equals(field.getClass())) {
                    if (this.valueNumber == null) throw new AssertionError();
                    return this.valueNumber.doubleValue() == ((Integer) field).doubleValue();
                }
                if (field instanceof String) {
                    if (this.valueString == null) throw new AssertionError();
                    if (this.valueString.equals(field)) {
                        return true;
//                    } else {
//                        // 针对 AccessibilityNodeInfo 特殊处理
//                        if (node.info.aid != ViewInfo.UNDEFINED_AID
//                                && "text".equals(this.field)
//                                && (node.children == null || node.children.isEmpty())
//                                ) {
//                            boolean isMatch = node.info.getAccessibilityNodeInfoRet((nodeInfo) -> {
//                                if (nodeInfo == null)
//                                    return false;
//                                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(this.valueString);
//                                if (nodes != null && !nodes.isEmpty()) {
//                                    for (AccessibilityNodeInfo itNode: nodes) {
//                                        CharSequence text = itNode.getText();
//                                        String sText = text == null ? "" : text.toString();
//                                        try {
//                                            itNode.recycle();
//                                        } catch (Throwable t) {}
//                                        if (sText.equals(this.valueString)) {
//                                            return true;
//                                        }
//                                    }
//                                }
//                                return false;
//                            });
//                            if (isMatch)
//                                return true;
//                        }
                    }
                }
                if (field instanceof Boolean || boolean.class.equals(field.getClass())) {
                    if (this.valueBoolean == null && this.valueNumber == null) throw new AssertionError();
                    if (this.valueBoolean != null)
                        return this.valueBoolean.equals(field);
                    else
                        return (this.valueNumber.doubleValue() != 0) == ((Boolean) field).booleanValue();
                }
                return false;
            }
            case FieldMatchRegex: {
                String field = getInfoStringField(node, this.field);
                if (field == null) {
                    return false;
                }
                if (this.rePattern == null) throw new AssertionError();

                if (this.rePattern.matcher(field).matches())
                    return true;

                return false;
            }
            case Father: {
                if (this.relCondition == null) throw new AssertionError();
                return this.relCondition.judge(node.parent);
            }
            case Child: {
                if (this.relCondition == null) throw new AssertionError();
                if (node.children == null)
                    return false;
                for(ViewNodeInfo child: node.children) {
                    if (this.relCondition.judge(child))
                        return true;
                }
                return false;
            }
            case ChildCountLessOrEqual: {
                if (this.childCount == null) throw new AssertionError();
                int childCount = 0;
                if (node.children == null)
                    childCount = 0;
                return childCount <= this.childCount.intValue();
            }
            case ChildCountLess: {
                if (this.childCount == null) throw new AssertionError();
                int childCount = 0;
                if (node.children == null)
                    childCount = 0;
                return childCount < this.childCount.intValue();
            }
            case ChildCountGreaterOrEqual: {
                if (this.childCount == null) throw new AssertionError();
                int childCount = 0;
                if (node.children == null)
                    childCount = 0;
                return childCount >= this.childCount.intValue();
            }
            case ChildCountGreater: {
                if (this.childCount == null) throw new AssertionError();
                int childCount = 0;
                if (node.children == null)
                    childCount = 0;
                return childCount > this.childCount.intValue();
            }
            case ChildCountEqual: {
                if (this.childCount == null) throw new AssertionError();
                int childCount = 0;
                if (node.children == null)
                    childCount = 0;
                return childCount == this.childCount.intValue();
            }
            case LastChild: {
                if (node.parent == null)
                    return false;
                int parentChildCount = node.parent.children.size();
                return node.info.index == parentChildCount - 1;
            }
            case True: {
                return true;
            }
            case False: {
                return false;
            }
            case And: {
                if (this.conditions == null) throw new AssertionError();
                for(ViewCondition condition: this.conditions) {
                    if (!condition.judge(node))
                        return false;
                }
                return true;
            }
            case Or: {
                if (this.conditions == null) throw new AssertionError();
                for(ViewCondition condition: this.conditions) {
                    if (condition.judge(node))
                        return true;
                }
                return false;
            }
            case Not: {
                if (this.relCondition == null) throw new AssertionError();
                return !this.relCondition.judge(node);
            }
            case Root: {
                return node.parent == null;
            }
        }
        return false;
    }

    public static class JsonAdapter extends TypeAdapter<ViewCondition> {

        @Override
        public void write(JsonWriter out, ViewCondition value) throws IOException {
            out.beginObject();
            out.name("type"); out.value(value.type == null ? "???" : value.type.name());
            switch (value.type) {
                case FieldLessOrEqual:
                case FieldLess:
                case FieldGreaterOrEqual:
                case FieldGreater:
                case FieldEqual: {
                    out.name("field"); out.value(value.field);
                    out.name("value");
                    if (value.valueString != null) {
                        out.value(value.valueString);
                    } else if (value.valueNumber != null) {
                        out.value(value.valueNumber);
                    } else if (value.valueBoolean != null) {
                        out.value(value.valueBoolean);
                    } else {
                        throw new Error("value all null!");
                    }
                    break;
                }

                case FieldMatchRegex: {
                    out.name("field"); out.value(value.field);
                    out.name("re_pattern"); out.value(value.rePattern.pattern());
                    break;
                }

                case Father:
                case Child:
                case Not: {
                    out.name("rel_condition");
                    this.write(out, value.relCondition);
                    break;
                }

                case ChildCountLessOrEqual:
                case ChildCountLess:
                case ChildCountEqual:
                case ChildCountGreater:
                case ChildCountGreaterOrEqual: {
                    out.name("child_count");
                    out.value(value.childCount);
                    break;
                }

                case And:
                case Or: {
                    out.name("conditions");
                    out.beginArray();
                    for (ViewCondition condition: value.conditions) {
                        this.write(out, condition);
                    }
                    out.endArray();
                    break;
                }

                case LastChild:
                case True:
                case False:
                case Root: {
                    break;
                }

                default: {
                    throw new Error("unknown type: " + value.type);
                }
            }
            out.endObject();

        }

        @Override
        public ViewCondition read(JsonReader in) throws IOException {
            in.beginObject();
            ViewConditionType type = null;
            String field = null;
            Double valueNumber = null;
            String valueString = null;
            Boolean valueBoolean = null;
            Pattern rePattern = null;
            ViewCondition relCondition = null;
            Integer childCount = null;
            List<ViewCondition> conditions = null;
            while (in.hasNext()) {
                String name = in.nextName();
//                log("------------ ViewCondition.JsonAdapter name: " + name);
                if ("type".equals(name)) {
                    type = ViewConditionType.create(in.nextString());
//                    log("------------ ViewCondition.JsonAdapter type: " + type);
                } else if ("field".equals(name)) {
                    field = in.nextString();
//                    log("------------ ViewCondition.JsonAdapter field: " + field);
                } else if ("value".equals(name)) {
                    switch (in.peek()) {
                        case NUMBER: {
                            valueNumber = in.nextDouble();
//                            log("------------ ViewCondition.JsonAdapter value: " + valueNumber);
                            break;
                        }
                        case BOOLEAN: {
                            valueBoolean = in.nextBoolean();
//                            log("------------ ViewCondition.JsonAdapter value: " + valueBoolean);
                            break;
                        }
                        case STRING: {
                            valueString = in.nextString();
//                            log("------------ ViewCondition.JsonAdapter value: " + valueString);
                            break;
                        }
                    }
                } else if ("re_pattern".equals(name)) {
                    String reStr = in.nextString();
                    rePattern = Pattern.compile(reStr);
//                    log("------------ ViewCondition.JsonAdapter re_pattern: " + reStr);
                } else if ("rel_condition".equals(name)) {
                    relCondition = this.read(in);
//                    log("------------ ViewCondition.JsonAdapter rel_condition: " + relCondition);
                } else if ("child_count".equals(name)) {
                    childCount = in.nextInt();
                } else if ("conditions".equals(name)) {
                    conditions = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        ViewCondition condition = this.read(in);
                        conditions.add(condition);
//                        log("------------ ViewCondition.JsonAdapter conditions: " + condition);
                    }
                    in.endArray();
                }
            }
            in.endObject();
            return new ViewCondition(
                    type,
                    field,
                    valueNumber,
                    valueString,
                    valueBoolean,
                    rePattern,
                    relCondition,
                    childCount,
                    conditions
            );
        }
    }
}
