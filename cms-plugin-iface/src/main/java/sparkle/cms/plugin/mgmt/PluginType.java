package sparkle.cms.plugin.mgmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * PluginType
 * Created by bazzoni on 24/05/2015.
 */
public enum PluginType {
    ASSET_MGMT("ASSET_MGMT"), SEARCH("SEARCH");

    public static final PluginType[] ALL = {ASSET_MGMT, SEARCH};

    private final String name;

    PluginType(final String name) {
        this.name = name;
    }

    @JsonCreator
    public static PluginType forName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null for type");
        }
        if (name.toUpperCase().equals("ASSET_MGMT")) {
            return ASSET_MGMT;
        }
        if (name.toUpperCase().equals("SEARCH")) {
            return SEARCH;
        }
        throw new IllegalArgumentException("Name \"" + name + "\" does not correspond to any Feature");
    }

    public String getName() {
        return this.name;
    }

    @JsonValue
    @Override
    public String toString() {
        return getName();
    }

}
