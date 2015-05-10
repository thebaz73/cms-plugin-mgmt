package sparkle.cms.plugin.mgmt.asset;

import java.util.HashMap;

/**
 * DummyContainer
 * Created by bazzoni on 07/05/2015.
 */
public class DummyContainer extends HashMap<String, Object> implements Container {
    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        return size() > 0;
    }
}
