package org.allurefw.report;

import java.io.InputStream;
import java.util.List;

/**
 * @author charlie (Dmitry Baev).
 * @since 2.0
 */
public interface ResultsSource {

    /**
     * Find all the results names by given glob. Then you can get the
     * content of each file using {@link #getResult(String)} method.
     *
     * @param glob the glob to find results files.
     * @return the list of found results files names.
     */
    List<String> getResultsByGlob(String glob);

    /**
     * Get the content of result by given result name.
     *
     * @param name the name of result to get content.
     * @return the input stream of result content.
     */
    InputStream getResult(String name);

    /**
     * Get the size of resource.
     *
     * @param name the name of result to get size.
     * @return the size of result.
     */
    long getSize(String name);

}
