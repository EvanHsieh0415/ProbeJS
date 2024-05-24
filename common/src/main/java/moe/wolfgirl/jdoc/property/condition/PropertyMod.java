package moe.wolfgirl.jdoc.property.condition;

import com.google.gson.JsonObject;
import moe.wolfgirl.jdoc.IConditional;
import moe.wolfgirl.jdoc.property.AbstractProperty;
import dev.architectury.platform.Platform;

public class PropertyMod extends AbstractProperty<PropertyMod> implements IConditional {

    private String mod;

    public PropertyMod() {
    }

    public PropertyMod(String mod) {
        this.mod = mod;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("mod", mod);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        mod = object.get("mod").getAsString();
    }

    public boolean test() {
        return Platform.isModLoaded(mod);
    }

    @Override
    public PropertyMod copy() {
        return new PropertyMod(mod);
    }
}
