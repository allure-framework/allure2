package org.allurefw.report.environment;

import com.google.common.collect.ImmutableBiMap;
import com.google.inject.Inject;
import org.allurefw.report.Environment;
import org.allurefw.report.Finalizer;

import static java.util.stream.Collectors.toList;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class EnvironmentFinalizer implements Finalizer<Object> {
    
    private Environment env;

    @Inject
    public EnvironmentFinalizer(Environment env) {
        this.env = env;
    }

    @Override
    public Object finalize(Object identity) {
        return env.getParameters().entrySet().stream().map(
                entry -> ImmutableBiMap.of("key", entry.getKey(), "value", entry.getValue()))
                .collect(toList());
    }
}
