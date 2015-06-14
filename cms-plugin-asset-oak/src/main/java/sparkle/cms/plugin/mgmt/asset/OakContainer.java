package sparkle.cms.plugin.mgmt.asset;

/**
 * OakContainer
 * Created by bazzoni on 14/06/2015.
 */
public class OakContainer implements Container {
    private final boolean children;
    private final String name;

    public OakContainer(boolean children, String name) {
        this.children = children;
        this.name = name;
    }

    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        return children;
    }

    @Override
    public String toString() {
        return name;
    }

}
