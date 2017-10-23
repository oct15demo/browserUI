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
 */
package org.knime.core.util;

import static org.knime.core.node.KNIMEConstants.PROPERTY_MAX_LOGFILESIZE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;

/**
 * This is a special appender for KNIME that writes into the
 * <code>knime.log</code> file, which is typically located in the current
 * workspace. If the log file gets bigger than a certain size the
 * file is gzipped and renamed and a new empty file is created.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class LogfileAppender extends FileAppender {
    private final File m_logFile;
    /** Maximum size of log file before it is split (in bytes). */
    public static final long MAX_LOG_SIZE_DEFAULT = 10 * 1024 * 1024; // 10MB
    private long m_maxLogSize;

    /**
     * Creates a new LogfileAppender.
     */
    public LogfileAppender() {
        this(new File(KNIMEConstants.getKNIMEHomeDir() + File.separator));
    }

    /**
     * @param logFileDir the directory in which the log file should be created
     * @since 2.12
     */
    public LogfileAppender(final File logFileDir) {
        String maxSizeString = System.getProperty(PROPERTY_MAX_LOGFILESIZE);
        if (maxSizeString == null) {
            m_maxLogSize = MAX_LOG_SIZE_DEFAULT;
        } else {
            maxSizeString = maxSizeString.toLowerCase().trim();
            int multiplier;
            if (maxSizeString.endsWith("m")) {
                multiplier = 1024 * 1024;
                maxSizeString = maxSizeString.substring(
                    0, maxSizeString.length() - 1).trim();
            } else if (maxSizeString.endsWith("k")) {
                multiplier = 1024;
                maxSizeString = maxSizeString.substring(
                    0, maxSizeString.length() - 1).trim();
            } else {
                multiplier = 1;
            }
            try {
                m_maxLogSize = multiplier * Long.parseLong(maxSizeString);
            } catch (Throwable e) {
                System.err.println("Unable to parse maximum log size property "
                        + PROPERTY_MAX_LOGFILESIZE + " (\""
                        + System.getProperty(PROPERTY_MAX_LOGFILESIZE) + "\"), "
                        + "using default size");
                m_maxLogSize = MAX_LOG_SIZE_DEFAULT;
            }
        }
        // check if log file directory exists
        if (!logFileDir.exists()) {
            logFileDir.mkdirs();
        }
        m_logFile = new File(logFileDir, NodeLogger.LOG_FILE);
        setFile(m_logFile.getAbsolutePath());
        setImmediateFlush(true);
        setEncoding("UTF-8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        if (m_maxLogSize > 0 && m_logFile.exists()
                && (m_logFile.length() > m_maxLogSize) && m_logFile.canRead()) {
            compressOldLog();
        }
        super.activateOptions();
    }

    /** This is not private only because that it is visible inside the thread
     * below.
     */
    private void compressOldLog() {
        synchronized (m_logFile) {
            LogLog.debug("Compressing log file '" + m_logFile + "'");
            final File tmpFile = new File(m_logFile.getAbsolutePath() + ".old");
            closeFile();
            try {
                Files.move(m_logFile.toPath(), tmpFile.toPath());
            } catch (IOException e) {
                LogLog.warn("Can not move log file: " + e.getMessage());
            }
            setFile(m_logFile.getAbsolutePath());
            final Thread t = new Thread() {
                @Override
                public void run() {
                    synchronized (m_logFile) {
                        if (tmpFile.exists()) {
                            try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
                                    final GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(
                                        new File(tmpFile.getAbsolutePath() + ".gz")));) {
                                byte[] buf = new byte[4096];
                                int count;
                                while ((count = in.read(buf)) > 0) {
                                    out.write(buf, 0, count);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (!tmpFile.delete()) {
                                LogLog.warn("Failed to delete temporary log file");
                            }
                        }
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void subAppend(final LoggingEvent event) {
        super.subAppend(event);
        if (m_maxLogSize > 0 && m_logFile.length() > m_maxLogSize) {
            compressOldLog();

            try {
                // This will also close the file. This is OK since multiple
                // close operations are safe.
                setFile(m_logFile.getAbsolutePath(), false, bufferedIO,
                        bufferSize);
            } catch (IOException e) {
                LogLog.error("setFile(" + fileName + ", false) call failed.",
                                e);
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return super.hashCode();
    }
    /**
     * {@inheritDoc}
     * We have to compare the name of the logger in the equals method to prevent duplicate log file registration
     * in the NodeLogger#addWorkflowDirAppender() method !!!
     */
    @Override
    public boolean equals(final Object obj) {
        //We have to compare the name of the logger in the equals method to prevent duplicate log file registration
        //in the NodeLogger#addWorkflowDirAppender() method !!!
        if (name != null && (obj instanceof FileAppender)) {
            return name.equals(((FileAppender)obj).getName());
        }
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_logFile.toString();
    }
}
