package com.bugsnag.android;

import static com.bugsnag.android.BugsnagTestUtils.generateImmutableConfig;

import androidx.annotation.NonNull;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Ensures that if a callback is added or removed during iteration, a
 * {@link java.util.ConcurrentModificationException} is not thrown
 */
public class ConcurrentCallbackTest {

    private final HandledState handledState
            = HandledState.newInstance(HandledState.REASON_HANDLED_EXCEPTION);
    private final Event event = new Event(new RuntimeException(),
            generateImmutableConfig(), handledState);
    private final SessionPayload sessionPayload = new SessionPayload(null,
            new ArrayList<File>(), new HashMap<String, Object>(), new HashMap<String, Object>());

    @Test
    public void testOnErrorConcurrentModification() {
        CallbackState config = new CallbackState();
        final Collection<OnErrorCallback> tasks = config.getOnErrorTasks();
        config.addOnError(new OnErrorCallback() {
            @Override
            public boolean onError(@NonNull Event event) {
                tasks.add(new OnErrorCallbackSkeleton());
                // modify the Set, when iterating to the next callback this should not crash
                return true;
            }
        });
        config.addOnError(new OnErrorCallbackSkeleton());
        config.runOnErrorTasks(event, NoopLogger.INSTANCE);
    }

    @Test
    public void testOnBreadcrumbConcurrentModification() {
        CallbackState config = new CallbackState();
        final Collection<OnBreadcrumbCallback> tasks = config.getOnBreadcrumbTasks();
        config.addOnBreadcrumb(new OnBreadcrumbCallback() {
            @Override
            public boolean onBreadcrumb(@NonNull Breadcrumb breadcrumb) {
                tasks.add(new OnBreadcrumbCallbackSkeleton());
                // modify the Set, when iterating to the next callback this should not crash
                return true;
            }
        });
        config.addOnBreadcrumb(new OnBreadcrumbCallbackSkeleton());
        config.runOnBreadcrumbTasks(new Breadcrumb("Foo"), NoopLogger.INSTANCE);
    }

    @Test
    public void testOnSessionConcurrentModification() {
        CallbackState config = new CallbackState();
        final Collection<OnSessionCallback> tasks = config.getOnSessionTasks();
        config.addOnSession(new OnSessionCallback() {
            @Override
            public boolean onSession(@NonNull SessionPayload event) {
                tasks.add(new OnSessionCallbackSkeleton());
                // modify the Set, when iterating to the next callback this should not crash
                return true;
            }
        });
        config.addOnSession(new OnSessionCallbackSkeleton());
        config.runOnSessionTasks(sessionPayload, NoopLogger.INSTANCE);
    }

    static class OnErrorCallbackSkeleton implements OnErrorCallback {
        @Override
        public boolean onError(@NonNull Event event) {
            return true;
        }
    }

    static class OnBreadcrumbCallbackSkeleton implements OnBreadcrumbCallback {
        @Override
        public boolean onBreadcrumb(@NonNull Breadcrumb breadcrumb) {
            return true;
        }
    }

    static class OnSessionCallbackSkeleton implements OnSessionCallback {
        @Override
        public boolean onSession(@NonNull SessionPayload sessionPayload) {
            return true;
        }
    }
}