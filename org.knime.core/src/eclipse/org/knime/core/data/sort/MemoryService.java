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
 * ------------------------------------------------------------------------
 *
 * History
 *   18.02.2010 (hofer): created
 */
package org.knime.core.data.sort;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.util.memory.MemoryAlertSystem;
import org.knime.core.node.NodeLogger;

/**
 * Used to query if memory on the heap is low. Observed are the memory pools
 * of long living objects which are "Tenured Gen" for the client vm and
 * "PS Old Gen" for the server vm.
 *
 * @author Heiko Hofer
 * @deprecated use {@link MemoryAlertSystem} instead
 */
@Deprecated
public final class MemoryService {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(MemoryService.class);

    /** The threshold for the low memory condition. */
    private long m_threshold;

    /** The observed {@link MemoryPoolMXBean}. */
    private MemoryPoolMXBean m_memPool;

    /** {@link GarbageCollectorMXBean}s associated with the observed
     * {@link MemoryPoolMXBean}. */
    private final ArrayList<GarbageCollectorMXBean> m_gcBean;

    // Flag controlling the output of isMemoryLow
    private boolean m_waitForNextGC;
    // Flag controlling the output of isMemoryLow
    private long m_lastCount;

    private final boolean m_useCollectionUsage;


    /**
     * Use MemoryService to test for low memory condition in computation
     * which are memory intensive.
     */
    public MemoryService() {
        this(0.85);
    }

    /**
     * Creates a new instance. This instance will report a low memory condition
     * when the quotient of used memory to the total amount of memory is above
     * this threshold.
     *
     * @param usedMemoryThreshold A number between 0 and 1.
     */
    public MemoryService(final double usedMemoryThreshold) {
        this(usedMemoryThreshold, -1, true);
    }

    /**
     * Added for testing purpose. It is most likely that you do not have to
     * use this constructor.
     *
     * @param usedMemoryThreshold the memory threshold
     * @param minAvailableMemory minimum amount of available memory
     * @param useCollectionUsage if collection usage should be used to
     * determine the available memory
     */
    private MemoryService(final double usedMemoryThreshold,
            final long minAvailableMemory,
            final boolean useCollectionUsage) {
        // Determine the memory pool to be observed
        List<String> toObserve = Arrays.asList("Tenured Gen", "PS Old Gen", "CMS Old Gen", "G1 Old Gen");
        for (MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (memoryPool.isUsageThresholdSupported() && toObserve.contains(memoryPool.getName())) {
                m_memPool = memoryPool;
            }
        }
        // When memory pool could not be found. This happens with sun's jvm with
        // g1gc or a jvm from another vendor is used.


        // Get the GarbageCollectorMXBeans associated with the observed
        // MemoryPoolMXBean
        m_gcBean = new ArrayList<GarbageCollectorMXBean>();
        for (GarbageCollectorMXBean gcBean
                : ManagementFactory.getGarbageCollectorMXBeans()) {
            for (String poolName : Arrays.asList(gcBean.getMemoryPoolNames())) {
                if (toObserve.contains(poolName)) {
                    m_gcBean.add(gcBean);
                }
            }
        }

        // Compute the threshold in bytes
        long maxMem = null != m_memPool ? m_memPool.getUsage().getMax()
                : Runtime.getRuntime().maxMemory();
        long usedMem = null != m_memPool ? m_memPool.getUsage().getUsed()
                : Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();


        // Workaround for a bug in G1 garbage collector:
        // http://bugs.sun.com/view_bug.do?bug_id=6880903
        List<String> jvmArgs
        = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (jvmArgs.contains("-XX:+UseG1GC")) {
            boolean xmxArgSet = false;
            for (String arg : jvmArgs) {
                if (arg.startsWith("-Xmx")) {
                    xmxArgSet = true;
                    boolean factorPresent = false;
                    int factor = -1;
                    if (arg.toLowerCase().endsWith("k")) {
                        factorPresent = true;
                        factor = 1000;
                    } else if (arg.toLowerCase().endsWith("m")) {
                        factorPresent = true;
                        factor = 1000000;
                    } else if (arg.toLowerCase().endsWith("g")) {
                        factorPresent = true;
                        factor = 1000000000;
                    }
                    if (factorPresent) {
                        maxMem = Integer.parseInt(
                                arg.substring(4, arg.length() - 1)) * factor;
                    } else {
                        maxMem = Integer.parseInt(arg.substring(4));
                    }
                    break;
                }
            }
            if (!xmxArgSet) {
                LOGGER.error("Please, set -Xmx jvm argument "
                        + "due to a bug in G1GC. Otherwise, memory "
                        + "intensive nodes might not work correctly.");
            }
        }

        m_threshold = (long)(usedMemoryThreshold * maxMem);

        if (minAvailableMemory > 0) { // only true if called from test case
            // try to free memory
            Runtime.getRuntime().gc();
            Runtime.getRuntime().gc();
            usedMem = null != m_memPool ? m_memPool.getUsage().getUsed()
                    : Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory();
            m_threshold = minAvailableMemory + usedMem;
            if (m_threshold >= maxMem) {
                throw new IllegalStateException("Can't initialize memory "
                        + "service. Min available memory not available.");
            }
        }

        m_waitForNextGC = false;
        m_lastCount = -1;
        m_useCollectionUsage = null != m_memPool
        && null != m_memPool.getCollectionUsage() ? useCollectionUsage
                : false;
    }

    /**
     * Factory to create an instance of MemoryService for use in test cases.
     * @param freeMemory amount of free memory
     * @return a new instance of MemoryService
     */
    public static MemoryService createTestCaseMemoryService(
            final long freeMemory) {
        return new MemoryService(0.0, freeMemory, false);
    }

    /**
     * This is a stateful method return true in the low memory condition. Note,
     * this method only returns true at most once between two garbage
     * collections.
     *
     * @return True if memory is low.
     */
    public boolean isMemoryLow() {
        if (m_waitForNextGC) {
            long thisCount = null != m_memPool
            ? m_gcBean.iterator().next().getCollectionCount()
                    : Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory();

            if (thisCount == m_lastCount) {
                return false;
            } else {
                m_waitForNextGC = false;
            }
        }

        MemoryUsage usage = null;
        if (m_useCollectionUsage) {
            usage = m_memPool.getCollectionUsage();
        }
        if (null == usage && null != m_memPool) {
            usage = m_memPool.getUsage();
        }
        if (null != usage) {
            boolean memoryIsLow = usage.getUsed() > m_threshold;
            if (memoryIsLow) {
                m_waitForNextGC = true;
                m_lastCount = m_gcBean.iterator().next().getCollectionCount();
                return true;
            } else {
                return false;
            }
        } else {
            long used = Runtime.getRuntime().totalMemory()
            - Runtime.getRuntime().freeMemory();
            boolean memoryIsLow = used > m_threshold;
            if (memoryIsLow) {
                m_waitForNextGC = true;
                m_lastCount = used;
                return true;
            } else {
                return false;
            }
        }
    }

}
