package ms.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * FileContainer
 * Created by bazzoni on 07/05/2015.
 */
public class FileContainer extends File implements Container {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new <tt>File</tt> instance by converting the given
     * <tt>file:</tt> URI into an abstract pathname.
     * <p>
     * <p> The exact form of a <tt>file:</tt> URI is system-dependent, hence
     * the transformation performed by this constructor is also
     * system-dependent.
     * <p>
     * <p> For a given abstract pathname <i>f</i> it is guaranteed that
     * <p>
     * <blockquote><tt>
     * new File(</tt><i>&nbsp;f</i><tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
     * <p>
     * so long as the original abstract pathname, the URI, and the new abstract
     * pathname are all created in (possibly different invocations of) the same
     * Java virtual machine.  This relationship typically does not hold,
     * however, when a <tt>file:</tt> URI that is created in a virtual machine
     * on one operating system is converted into an abstract pathname in a
     * virtual machine on a different operating system.
     *
     * @param uri An absolute, hierarchical URI with a scheme equal to
     *            <tt>"file"</tt>, a non-empty path component, and undefined
     *            authority, query, and fragment components
     * @throws NullPointerException     If <tt>uri</tt> is <tt>null</tt>
     * @throws IllegalArgumentException If the preconditions on the parameter do not hold
     * @see #toURI()
     * @see URI
     * @since 1.4
     */
    public FileContainer(URI uri) {
        super(uri);
        loadData();
    }

    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        return list().length > 0;
    }

    /**
     * Get children number
     *
     * @return children number
     */
    @Override
    public int getChildrenNumber() {
        return list().length;
    }

    private void loadData() {
        if (!exists()) {
            logger.debug("Creating folder: " + mkdirs());
        }
    }
}
