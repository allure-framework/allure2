package org.allurefw.report;

import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public abstract class AbstractPlugin extends AbstractModule {

    private final Plugin pluginAnnotation = getClass().getAnnotation(Plugin.class);

    public <T> AggregatorBuilder<T> aggregator(Class<? extends Aggregator<T>> aggregatorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Aggregator.class)
                .addBinding(uid).to(aggregatorClass);

        return new AggregatorBuilder<>(uid);
    }

    public void processor(Class<? extends Processor> processorClass) {
        String uid = UUID.randomUUID().toString();
        MapBinder.newMapBinder(binder(), String.class, Processor.class)
                .addBinding(uid).to(processorClass);
    }

    public void reportPlugin(String reportPluginName, String resourcePathPrefix) {
        Map<String, URL> resources = findResources(resourcePathPrefix);
        String uid = UUID.randomUUID().toString();

        resources.forEach((path, url) -> {
            MapBinder.newMapBinder(binder(), String.class, URL.class)
                    .addBinding(uid).toInstance(url);
            MapBinder.newMapBinder(binder(), String.class, String.class, ResourcesNamesMap.class)
                    .addBinding(uid).toInstance(path);
        });
    }

    private Map<String, URL> findResources(String pathPrefix) {
        try {
            String prefix = pathPrefix + "/";
            ClassPath path = ClassPath.from(getClass().getClassLoader());
            return path.getResources().stream()
                    .filter(info -> info.getResourceName().startsWith(prefix))
                    .collect(Collectors.toMap(
                            info -> info.getResourceName().substring(prefix.length()),
                            ClassPath.ResourceInfo::url)
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the plugins name.
     */
    public final String getPluginName() {
        Objects.requireNonNull(pluginAnnotation);
        return pluginAnnotation.name();
    }

    /**
     * Returns the plugins scope.
     */
    public final PluginScope getPluginScope() {
        Objects.requireNonNull(pluginAnnotation);
        return pluginAnnotation.scope();
    }

    public class AggregatorBuilder<T> {

        private String uid;

        public AggregatorBuilder(String uid) {
            this.uid = uid;
        }

        public AggregatorBuilder<T> toReportData(String fileName) {
            MapBinder.newMapBinder(binder(), String.class, String.class, ReportFilesNamesMap.class)
                    .addBinding(uid).toInstance(fileName);
            return this;
        }

        public AggregatorBuilder<T> toWidget(String widgetName, Class<? extends Finalizer<T>> finalizerClass) {
            MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                    .addBinding(uid).toInstance(widgetName);

            MapBinder.newMapBinder(binder(), String.class, Finalizer.class)
                    .addBinding(widgetName).to(finalizerClass);
            return this;
        }
    }
}
