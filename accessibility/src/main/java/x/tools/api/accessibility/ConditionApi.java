package x.tools.api.accessibility;

import x.tools.api.accessibility.view.ViewCondition;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PVarArgs;
import x.tools.framework.api.AbstractApi;

public class ConditionApi extends AbstractApi {
    @Override
    public String getNamespace() {
        return "condition";
    }

    @Api
    public ViewCondition FieldLessOrEqual(
            @PName(name = "field")
                    String field,
            @PName(name = "valueNumber")
                    double valueNumber
    ) {
        return ViewCondition.FieldLessOrEqual(field, valueNumber);
    }

    @Api
    public ViewCondition FieldLess(
            @PName(name = "field")
                    String field,
            @PName(name = "valueNumber")
                    double valueNumber
    ) {
        return ViewCondition.FieldLess(field, valueNumber);
    }

    @Api
    public ViewCondition FieldGreaterOrEqual(
            @PName(name = "field")
                    String field,
            @PName(name = "valueNumber")
                    double valueNumber
    ) {
        return ViewCondition.FieldGreaterOrEqual(field, valueNumber);
    }

    @Api
    public ViewCondition FieldGreater(
            @PName(name = "field")
                    String field,
            @PName(name = "valueNumber")
                    double valueNumber
    ) {
        return ViewCondition.FieldGreater(field, valueNumber);
    }

    @Api
    public ViewCondition FieldEqual(
            @PName(name = "field")
                    String field,
            @PName(name = "valueNumber")
                    double valueNumber
    ) {
        return ViewCondition.FieldEqual(field, valueNumber);
    }

    @Api
    public ViewCondition FieldEqual(
            @PName(name = "field")
                    String field,
            @PName(name = "valueString")
                    String valueString
    ) {
        return ViewCondition.FieldEqual(field, valueString);
    }

    @Api
    public ViewCondition FieldEqual(
            @PName(name = "field")
                    String field,
            @PName(name = "valueBoolean")
                    boolean valueBoolean
    ) {
        return ViewCondition.FieldEqual(field, valueBoolean);
    }

    @Api
    public ViewCondition FieldMatchRegex(
            @PName(name = "field")
                    String field,
            @PName(name = "pattern")
                    String pattern
    ) {
        return ViewCondition.FieldMatchRegex(field, pattern);
    }

    @Api
    public ViewCondition Father(
            @PName(name = "father")
                    ViewCondition father
    ) {
        return ViewCondition.Father(father);
    }

    @Api
    public ViewCondition Child(
            @PName(name = "child")
                    ViewCondition child
    ) {
        return ViewCondition.Child(child);
    }

    @Api
    public ViewCondition ChildCountLessOrEqual(
            @PName(name = "count")
                    int count
    ) {
        return ViewCondition.ChildCountLessOrEqual(count);
    }

    @Api
    public ViewCondition LastChild() {
        return ViewCondition.LastChild();
    }

    @Api
    public ViewCondition ChildCountLess(
            @PName(name = "count")
                    int count
    ) {
        return ViewCondition.ChildCountLess(count);
    }

    @Api
    public ViewCondition ChildCountGreaterOrEqual(
            @PName(name = "count")
                    int count
    ) {
        return ViewCondition.ChildCountGreaterOrEqual(count);
    }

    @Api
    public ViewCondition ChildCountGreater(
            @PName(name = "count")
                    int count
    ) {
        return ViewCondition.ChildCountGreater(count);
    }

    @Api
    public ViewCondition ChildCountEqual(
            @PName(name = "count")
                    int count
    ) {
        return ViewCondition.ChildCountEqual(count);
    }

    @Api
    public ViewCondition And(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        return ViewCondition.And();
    }

    @Api
    public ViewCondition Or(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        return ViewCondition.Or();
    }

    @Api
    public ViewCondition Not(
            @PName(name = "other")
                    ViewCondition other
    ) {
        return ViewCondition.Not(other);
    }

    @Api
    public ViewCondition TextEqual(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.TextEqual(text);
    }

    @Api
    public ViewCondition ContentDescEqual(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.ContentDescEqual(text);
    }

    @Api
    public ViewCondition TextMatch(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.TextMatch(text);
    }

    @Api
    public ViewCondition ResEqual(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.ResEqual(text);
    }

    @Api
    public ViewCondition ClsEqual(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.ClsEqual(text);
    }

    @Api
    public ViewCondition IndexEqual(
            @PName(name = "index")
                    int index
    ) {
        return ViewCondition.IndexEqual(index);
    }

    @Api
    public ViewCondition IsClickableEqual(
            @PName(name = "isVisible")
                    boolean isVisible
    ) {
        return ViewCondition.IsClickableEqual(isVisible);
    }

    @Api
    public ViewCondition IsVisibleEqual(
            @PName(name = "isVisible")
                    boolean isVisible
    ) {
        return ViewCondition.IsVisibleEqual(isVisible);
    }

    @Api
    public ViewCondition IsEnableEqual(
            @PName(name = "isVisible")
                    boolean isVisible
    ) {
        return ViewCondition.IsEnableEqual(isVisible);
    }

    @Api
    public ViewCondition IsScrollable(
            @PName(name = "isVisible")
                    boolean isVisible
    ) {
        return ViewCondition.IsScrollable(isVisible);
    }

    @Api
    public ViewCondition DescMatch(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.DescMatch(text);
    }

    @Api
    public ViewCondition DescEqual(
            @PName(name = "text")
                    String text
    ) {
        return ViewCondition.DescEqual(text);
    }


}
