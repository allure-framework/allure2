package org.allurefw.report.xunit;

import org.allurefw.report.Finalizer;
import org.allurefw.report.XunitData;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class TotalWidgetFinalizer implements Finalizer<XunitData> {

    @Override
    public Object finalize(XunitData identity) {
        return new XunitData()
                .withStatistic(identity.getStatistic())
                .withTime(identity.getTime());
    }
}
