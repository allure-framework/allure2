package org.allurefw.report;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.allurefw.report.entity.Attachment;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestGroup;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author charlie (Dmitry Baev).
 */
public class ProcessStageModule extends AbstractModule {

    private final List<TestsResults> testsResults;

    private final List<Module> plugins;

    public ProcessStageModule(List<TestsResults> testsResults, List<Module> plugins) {
        this.testsResults = Collections.unmodifiableList(testsResults);
        this.plugins = plugins;
    }

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), TestCaseResult.class);
        MapBinder.newMapBinder(binder(), String.class, TestGroup.class);
        MapBinder.newMapBinder(binder(), String.class, TestGroup.class, Names.named("suite"));
        MapBinder.newMapBinder(binder(), Path.class, Attachment.class);

        testsResults.forEach(source -> {
            bindTestCases(source);
            bindTestGroups(source);
            bindAttachments(source);
        });

        MapBinder.newMapBinder(binder(), String.class, Aggregator.class);
        MapBinder.newMapBinder(binder(), String.class, Processor.class);
        MapBinder.newMapBinder(binder(), String.class, String.class, DataNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, String.class, WidgetsNamesMap.class)
                .permitDuplicates();
        MapBinder.newMapBinder(binder(), String.class, Finalizer.class);

        plugins.forEach(this::install);
    }

    private void bindAttachments(TestsResults results) {
        if (results.getAttachments() != null) {
            results.getAttachments().forEach((path, attachment) ->
                    MapBinder.newMapBinder(binder(), Path.class, Attachment.class)
                            .addBinding(path).toInstance(attachment)
            );
        }
    }

    private void bindTestGroups(TestsResults results) {
        if (results.getTestGroups() != null) {
            results.getTestGroups().forEach((testGroup) -> {
                MapBinder<String, TestGroup> mapBinder = Objects.nonNull(testGroup.getType())
                        ? MapBinder.newMapBinder(binder(), String.class, TestGroup.class, named(testGroup))
                        : MapBinder.newMapBinder(binder(), String.class, TestGroup.class);
                //TODO merge testGroups?
                mapBinder.permitDuplicates().addBinding(testGroup.getName()).toInstance(testGroup);
            });
        }
    }

    private void bindTestCases(TestsResults results) {
        if (results.getTestCases() != null) {
            results.getTestCases().forEach(testCase ->
                    Multibinder.newSetBinder(binder(), TestCaseResult.class)
                            .addBinding().toInstance(testCase)
            );
        }
    }

    private Named named(TestGroup testGroup) {
        return Names.named(testGroup.getType());
    }
}
