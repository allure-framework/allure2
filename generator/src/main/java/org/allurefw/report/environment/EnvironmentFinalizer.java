
package org.allurefw.report.environment;

import org.allurefw.report.EnvironmentData;
import org.allurefw.report.Finalizer;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class EnvironmentFinalizer implements Finalizer<EnvironmentData> {

    @Override
    public Object finalize(EnvironmentData identity) {
        return identity.getParameters();
    }
}