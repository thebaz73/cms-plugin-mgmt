package sparkle.cms.plugin.mgmt.asset;

/**
 * JackrabbitContainer
 * Created by bazzoni on 10/06/2015.
 */
public class JackrabbitContainer implements Container {
    private final boolean children;
    private final String name;

    public JackrabbitContainer(boolean children, String name) {
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
