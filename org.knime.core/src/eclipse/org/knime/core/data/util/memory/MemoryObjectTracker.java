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
 * Created on 19.03.2013 by Mia
 */
package org.knime.core.data.util.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContext;

/**
 * API not public yet.
 *
 * @author dietzc
 * @deprecated use {@link MemoryAlertSystem} instead
 */
@Deprecated
public final class MemoryObjectTracker {

    private static final MemoryAlertObject MEMORY_ALERT = new MemoryAlertObject();

    enum Strategy {
        /* Completely frees memory */
        FREE_ALL,
        /* Free a certain percentage of all objects in the memory.*/
        FREE_PERCENTAGE,
        /* Remove oldest object (possible) */
        FREE_ONE;
    }

    /* Release Strategy */
    private Strategy m_strategy = Strategy.FREE_ALL;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(MemoryObjectTracker.class);

    /*
    * The list of tracked objects, whose memory will be freed, if the
    * memory runs out.
    */
    private final WeakHashMap<MemoryReleasable, TimestampAndContext> m_trackedObjectHash =
        new WeakHashMap<MemoryReleasable, TimestampAndContext>();

    private long m_lastAccess;

    /** Singleton instance of this object. */
    private static MemoryObjectTracker instance;

    /** Memory Warning System. */
    private final MemoryWarningSystem m_memoryWarningSystem = MemoryWarningSystem.getInstance();

    private final Lock m_lock = new ReentrantLock();

    private final Condition m_cond = m_lock.newCondition();

    /*
     * Private constructor, singleton
     */
    private MemoryObjectTracker() {
        m_memoryWarningSystem.setPercentageUsageThreshold(0.7);
        m_memoryWarningSystem.registerListener(new MemoryWarningSystem.MemoryWarningListener() {

            @Override
            public void memoryUsageLow(final long usedMemory, final long maxMemory) {
                LOGGER.debug("Low memory encountered. Used memory: " + FileUtils.byteCountToDisplaySize(usedMemory)
                    + "; maximum memory: " + FileUtils.byteCountToDisplaySize(maxMemory) + ".");
                m_lock.lock();
                try {
                    m_cond.signalAll();
                } finally {
                    m_lock.unlock();
                }
            }
        });

        // run in separate thread so that it can be debugged and we don't mess around with system threads
      final Thread t = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    m_lock.lock();
                    try {
                        m_cond.await();
                    } catch (InterruptedException ex) {
                        break;
                    } finally {
                        m_lock.unlock();
                    }
                    LOGGER.debug("Handling memory alert");
                    freeAllMemory(1.0);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Track memory releasable objects. If the memory gets low, on all tracked objects the
     * {@link MemoryReleasable#memoryAlert(MemoryAlertObject)} method will be called and the objects itself will be
     * removed from the cache.
     *
     * @param obj
     */
    public void addMemoryReleaseable(final MemoryReleasable obj) {
        if (obj != null) {
            synchronized (m_trackedObjectHash) {
                TimestampAndContext value = new TimestampAndContext(m_lastAccess++, NodeContext.getContext());
                m_trackedObjectHash.put(obj, value);
                LOGGER.debug("Adding " + obj.getClass().getName() + " (" + m_trackedObjectHash.size() + " in total)");
            }
        }
    }

    public void removeMemoryReleaseable(final MemoryReleasable obj) {
        if (obj != null) {
            synchronized (m_trackedObjectHash) {
                String name = obj.getClass().getName();
                if (m_trackedObjectHash.remove(obj) == null) {
                    LOGGER.debug("Attempted to remove " + name + ", which was not tracked");
                } else {
                    LOGGER.debug("Removing " + name + " (" + m_trackedObjectHash.size() + " remaining)");
                }
            }
        }
    }

    /**
     * Promotes obj in LRU Cache. If obj was added to removeList before, it will be removed from removeList
     *
     * @param obj
     */
    public void promoteMemoryReleaseable(final MemoryReleasable obj) {
        if (obj != null) {
            synchronized (m_trackedObjectHash) {
                TimestampAndContext timestampAndContext = m_trackedObjectHash.get(obj);
                if (timestampAndContext != null) {
                    timestampAndContext.m_lastAccessStamp = m_lastAccess++;
                }
            }
        }
    }

    /*
    * Frees the memory of some objects in the list.
    */
    private void freeAllMemory(final double percentage) {
        int initSize = m_trackedObjectHash.size();

        List<Map.Entry<MemoryReleasable, TimestampAndContext>> entryValues =
            new ArrayList<Map.Entry<MemoryReleasable, TimestampAndContext>>(initSize);

        int count = 0;
        synchronized (m_trackedObjectHash) {
            entryValues.addAll(m_trackedObjectHash.entrySet());
        }

        LOGGER.debug("Trying to release " + entryValues.size() + " memory objects");
        for (Iterator<Map.Entry<MemoryReleasable, TimestampAndContext>> it = entryValues.iterator(); it.hasNext();) {
            Map.Entry<MemoryReleasable, TimestampAndContext> entry = it.next();
            MemoryReleasable memoryReleasable = entry.getKey();
            if (memoryReleasable != null) {
                TimestampAndContext value = entry.getValue();
                boolean isToRemove;
                NodeContext.pushContext(value.m_context);
                try {
                    isToRemove = memoryReleasable.memoryAlert(MEMORY_ALERT);
                } catch (Exception e) {
                    LOGGER.error("Exception while alerting low memory condition", e);
                    isToRemove = true;
                } finally {
                    NodeContext.removeLastContext();
                }
                if (isToRemove) {
                    synchronized (m_trackedObjectHash) {
                        m_trackedObjectHash.remove(entry);
                    }
                    count++;
                }
            }
        }
        int remaining;
        synchronized (m_trackedObjectHash) {
            remaining = m_trackedObjectHash.size();
        }
        LOGGER.debug(count + "/" + initSize + " tracked objects have been released (" + remaining + " remaining)");
    }

    /**
     * @return singleton on MemoryObjectTracker.
     */
    public static MemoryObjectTracker getInstance() {
        if (instance == null) {
            instance = new MemoryObjectTracker();
        }
        return instance;
    }

    /**
     * Executes the code path that is executed in low memory events. Currently only used in the test environment (no
     * API).
     */
    public void simulateMemoryAlert() {

    }

    /** Value class of the tracked object. Keeps last access time stamp and the node context. */
    private static final class TimestampAndContext {
        private long m_lastAccessStamp;

        private final NodeContext m_context;

        /**
         * @param lastAccessStamp ...
         * @param context ...
         */
        TimestampAndContext(final long lastAccessStamp, final NodeContext context) {
            m_lastAccessStamp = lastAccessStamp;
            m_context = context;
        }

    }
}
