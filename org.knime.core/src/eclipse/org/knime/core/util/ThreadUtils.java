/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 24.06.2013 by thor
 */
package org.knime.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContext;

/**
 * This class contains utility methods for handling {@link NodeContext}s with new threads.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public final class ThreadUtils {
    private static final NodeLogger logger = NodeLogger.getLogger(ThreadUtils.class);

    private ThreadUtils() {
    }

    private static final class ExecutorServiceWithContext implements ExecutorService{
        private final ExecutorService m_executorService;

        ExecutorServiceWithContext(final ExecutorService executorService) {
            m_executorService = executorService;
        }

        @Override
        public void execute(final Runnable command) {
            m_executorService.execute(runnableWithContext(command));
        }

        @Override
        public <T> Future<T> submit(final Runnable task, final T result) {
            return m_executorService.submit(runnableWithContext(task), result);
        }

        @Override
        public Future<?> submit(final Runnable task) {
            return m_executorService.submit(runnableWithContext(task));
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            return m_executorService.submit(callableWithContext(task));
        }

        @Override
        public List<Runnable> shutdownNow() {
            return m_executorService.shutdownNow();
        }

        @Override
        public void shutdown() {
            m_executorService.shutdown();
        }

        @Override
        public boolean isTerminated() {
            return m_executorService.isTerminated();
        }

        @Override
        public boolean isShutdown() {
            return m_executorService.isShutdown();
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            List<Callable<T>> wrappedTasks = new ArrayList<Callable<T>>();
            for (Callable<T> t : tasks) {
                wrappedTasks.add(callableWithContext(t));
            }

            return m_executorService.invokeAny(wrappedTasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException,
            ExecutionException {
            List<Callable<T>> wrappedTasks = new ArrayList<Callable<T>>();
            for (Callable<T> t : tasks) {
                wrappedTasks.add(callableWithContext(t));
            }

            return m_executorService.invokeAny(wrappedTasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
            List<Callable<T>> wrappedTasks = new ArrayList<Callable<T>>();
            for (Callable<T> t : tasks) {
                wrappedTasks.add(callableWithContext(t));
            }

            return m_executorService.invokeAll(wrappedTasks, timeout, unit);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
            List<Callable<T>> wrappedTasks = new ArrayList<Callable<T>>();
            for (Callable<T> t : tasks) {
                wrappedTasks.add(callableWithContext(t));
            }

            return m_executorService.invokeAll(wrappedTasks);
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return m_executorService.awaitTermination(timeout, unit);
        }
    }

    @SuppressWarnings("serial")
    private static final class UnnecessaryCallException extends Exception {
        // empty on purpose, this exception class is only for getting a stacktrace for the log
    }

    /**
     * Extension of {@link Runnable} that ensures that the {@link NodeContext}, which was present during creation of the
     * object, is set when the {@link #run()} method is called.
     *
     * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
     */
    public abstract static class RunnableWithContext implements Runnable {
        private final NodeContext m_nodeContext;

        /**
         * Default constructor.
         */
        protected RunnableWithContext() {
            m_nodeContext = NodeContext.getContext();
            if (m_nodeContext == null) {
                logger.debug("Unnecessary usage of RunnableWithContext because no context is available",
                    new UnnecessaryCallException());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void run() {
            NodeContext.pushContext(m_nodeContext);
            try {
                runWithContext();
            } finally {
                NodeContext.removeLastContext();
            }
        }

        /**
         * This method should do the same as {@link Runnable#run()}.
         */
        protected abstract void runWithContext();
    }

    /**
     * Extension of {@link Callable} that ensures that the {@link NodeContext}, which was present during creation of the
     * object, is set when the {@link #call()} method is called.
     *
     * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
     * @param <V> the result type of method <tt>call</tt>
     */
    public abstract static class CallableWithContext<V> implements Callable<V> {
        private final NodeContext m_nodeContext;

        /**
         * Default constructor.
         */
        protected CallableWithContext() {
            m_nodeContext = NodeContext.getContext();
            if (m_nodeContext == null) {
                logger.debug("Unnecessary usage of CallableWithContext because no context is available",
                    new UnnecessaryCallException());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final V call() throws Exception {
            NodeContext.pushContext(m_nodeContext);
            try {
                return callWithContext();
            } finally {
                NodeContext.removeLastContext();
            }
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         * @see Callable#call()
         */
        protected abstract V callWithContext() throws Exception;
    }

    /**
     * Extension of {@link Thread} that ensures that the {@link NodeContext}, which was present during creation of the
     * object, is set when the {@link #run()} method is called.
     *
     * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
     */
    public abstract static class ThreadWithContext extends Thread {
        private final NodeContext m_nodeContext;

        /**
         * Creates a new thread.
         *
         * @see Thread#Thread()
         */
        protected ThreadWithContext() {
            m_nodeContext = NodeContext.getContext();
            if (m_nodeContext == null) {
                logger.debug("Unnecessary usage of ThreadWithContext because no context is available",
                    new UnnecessaryCallException());
            }
        }

        /**
         * Creates a new thread with the given name.
         *
         * @param name the thread's name
         * @see Thread#Thread(String)
         */
        protected ThreadWithContext(final String name) {
            super(name);
            m_nodeContext = NodeContext.getContext();
            if (m_nodeContext == null) {
                logger.debug("Unnecessary usage of ThreadWithContext because no context is available",
                    new UnnecessaryCallException());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void run() {
            NodeContext.pushContext(m_nodeContext);
            try {
                runWithContext();
            } finally {
                NodeContext.removeLastContext();
            }
        }

        /**
         * This method should do the same as {@link Thread#run()}.
         */
        protected abstract void runWithContext();
    }

    private static final class RunnableWithContextImpl extends RunnableWithContext {
        private final Runnable m_origRunnable;

        RunnableWithContextImpl(final Runnable origRunnable) {
            m_origRunnable = origRunnable;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void runWithContext() {
            m_origRunnable.run();
        }
    }

    private static final class CallableWithContextImpl<V> extends CallableWithContext<V> {
        private final Callable<V> m_origCallable;

        CallableWithContextImpl(final Callable<V> origRunnable) {
            m_origCallable = origRunnable;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected V callWithContext() throws Exception {
            return m_origCallable.call();
        }
    }

    /**
     * Creates a {@link Runnable} that carries the current {@link NodeContext} with it. The context is set before the
     * original {@link Runnable} is executed and removed again in the end. This is useful if a {@link Runnable} is
     * executed by a new thread or an {@link ExecutorService} where the executing thread does not have a node context.
     *
     * @param runnable any runnable
     * @return a runnable that wraps the original runnable and sets the node context
     */
    public static Runnable runnableWithContext(final Runnable runnable) {
        return runnableWithContext(runnable, true);
    }

    /**
     * Creates a {@link Runnable} that carries the current {@link NodeContext} with it. The context is set before the
     * original {@link Runnable} is executed and removed again in the end. This is useful if a {@link Runnable} is
     * executed by a new thread or an {@link ExecutorService} where the executing thread does not have a node context.
     *
     * @param runnable any runnable
     * @param logUnnecessaryCalls <code>true</code> if unnecessary calls to this method should be logged. A call is
     *            unnecessary if the runnable is already a {@link RunnableWithContext} or if no {@link NodeContext} is
     *            available
     * @return a runnable that wraps the original runnable and sets the node context
     */
    public static Runnable runnableWithContext(final Runnable runnable, final boolean logUnnecessaryCalls) {
        if (runnable instanceof RunnableWithContext) {
            if (logUnnecessaryCalls) {
                logger.debug("Unnecessary call to runnableWithContext, because it is already a RunnableWithContext",
                    new UnnecessaryCallException());
            }
            return runnable;
        } else if (NodeContext.getContext() == null) {
            if (logUnnecessaryCalls) {
                logger.debug("Unnecessary call to runnableWithContext, because no context is available",
                    new UnnecessaryCallException());
            }
            return runnable;
        } else {
            return new RunnableWithContextImpl(runnable);
        }
    }

    /**
     * Creates a {@link Callable} that carries the current {@link NodeContext} with it. The context is set before the
     * original {@link Callable} is executed and removed again in the end. This is useful if a {@link Callable} is
     * executed by a new thread or an {@link ExecutorService} where the executing thread does not have a node context.
     *
     * @param callable any callable
     * @param <V> the Callable's return type
     * @return a {@link Callable} that wraps the original {@link Callable} and sets the node context
     */
    public static <V> Callable<V> callableWithContext(final Callable<V> callable) {
        return callableWithContext(callable, true);
    }

    /**
     * Creates a {@link Callable} that carries the current {@link NodeContext} with it. The context is set before the
     * original {@link Callable} is executed and removed again in the end. This is useful if a {@link Callable} is
     * executed by a new thread or an {@link ExecutorService} where the executing thread does not have a node context.
     *
     * @param callable any callable
     * @param logUnnecessaryCalls <code>true</code> if unnecessary calls to this method should be logged. A call is
     *            unnecessary if the runnable is already a {@link RunnableWithContext} or if no {@link NodeContext} is
     *            available
     * @param <V> the Callable's return type
     *
     * @return a {@link Callable} that wraps the original {@link Callable} and sets the node context
     */
    public static <V> Callable<V> callableWithContext(final Callable<V> callable, final boolean logUnnecessaryCalls) {
        if (callable instanceof CallableWithContext) {
            if (logUnnecessaryCalls) {
                logger.debug("Unnecessary call to callableWithContext, because it is already a CallableWithContext",
                    new UnnecessaryCallException());
            }
            return callable;
        } else if (NodeContext.getContext() == null) {
            if (logUnnecessaryCalls) {
                logger.debug("Unnecessary call to callableWithContext, because no context is available",
                    new UnnecessaryCallException());
            }
            return callable;
        } else {
            return new CallableWithContextImpl<V>(callable);
        }
    }

    /**
     * Wraps the given executor and ensures that the runnable is run within the context of the caller of
     * {@link Executor#execute(Runnable)}.
     *
     * @param executor any {@link Executor}
     * @return an {@link Executor} that wraps the original executor
     */
    public static Executor executorWithContext(final Executor executor) {
        return new Executor() {
            @Override
            public void execute(final Runnable command) {
                executor.execute(runnableWithContext(command));
            }
        };
    }

    /**
     * Wraps the given executor service and ensures that the runnable is run within the context of the caller of the
     * various execute, submit and invoke methods.
     *
     * @param executorService any {@link ExecutorService}
     * @return an {@link ExecutorService} that wraps the original executor service
     */
    public static ExecutorService executorServiceWithContext(final ExecutorService executorService) {
        return new ExecutorServiceWithContext(executorService);
    }

    /**
     * Creates a new thread the executed the given runnable in the {@link NodeContext} of the caller.
     *
     * @param runnable any runnable.
     * @return a new thread
     */
    public static Thread threadWithContext(final Runnable runnable) {
        return new ThreadWithContext() {
            @Override
            protected void runWithContext() {
                runnable.run();
            }
        };
    }

    /**
     * Creates a new thread the executed the given runnable in the {@link NodeContext} of the caller.
     *
     * @param runnable any runnable.
     * @param name the new thread's name
     * @return a new thread
     */
    public static Thread threadWithContext(final Runnable runnable, final String name) {
        return new ThreadWithContext(name) {
            @Override
            protected void runWithContext() {
                runnable.run();
            }
        };
    }
}
