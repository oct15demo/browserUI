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
 * -------------------------------------------------------------------
 * 
 * History
 *   13.12.2005 (dill): created
 */
package org.knime.base.node.mine.subgroupminer.apriori;

import java.util.ArrayList;
import java.util.List;

/**
 * To hide the different implementations of the apriori algorithm to the
 * NodeModel, the NodeDialog simply displays the registered
 * AlgorithmDataStructure's and the NodeModel passes it to this factory.
 * 
 * @author Fabian Dill, University of Konstanz
 */
public final class AprioriAlgorithmFactory {
    
    /**
     * Register here possible implementations of the apriori algorithm to be
     * provided by the subgroup miner node (SubgroupMinerModel2).
     * 
     * @author Fabian Dill, University of Konstanz
     */
    public enum AlgorithmDataStructure {
        /** A prefix tree where the nodes are realized as arrays. * */
        ARRAY,
        /* LIST */
        /** The TIDList stores the ids of the transactions. * */
        TIDList;

        /**
         * Returns the values of this enum as a list of strings.
         * 
         * @return the values of this enum as a list of strings
         */
        public static List<String> asStringList() {
            Enum[] values = values();
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < values.length; i++) {
                list.add(values[i].name());
            }
            return list;
        }
    }

    private AprioriAlgorithmFactory() {
        // just to prohibit instantiation
    }

    /**
     * Returns an instance of the AprioriAlgorithm interface according to the
     * passed type.
     * 
     * @param type the desired algorithm implementation
     * @param bitSetLength the bitset length of the transactions, i.e. the
     *            number of items
     *            @param dbsize number of transactions
     * @return an instance of the AprioriAlgorithm
     */
    public static AprioriAlgorithm getAprioriAlgorithm(
            final AlgorithmDataStructure type, final int bitSetLength, 
            final int dbsize) {
        if (type.equals(AlgorithmDataStructure.ARRAY)) {
            return new ArrayApriori(bitSetLength, dbsize);
        } else if (type.equals(AlgorithmDataStructure.TIDList)) {
            return new TIDApriori();
        } else {
            throw new RuntimeException("Type not supported: " + type);
        }
    }
}
