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
package org.knime.base.node.mine.subgroupminer;

import static org.knime.core.node.port.pmml.PMMLPortObject.CDATA;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.sax.TransformerHandler;

import org.knime.base.node.mine.subgroupminer.freqitemset.AssociationRule;
import org.knime.base.node.mine.subgroupminer.freqitemset.FrequentItemSet;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.pmml.PMMLContentHandler;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is not sufficently tested yet and the API of this class might be
 * still subject to changes.
 *
 * @author Fabian Dill, University of Konstanz
 */
@Deprecated
public class PMMLAssociationRuleHandler extends PMMLContentHandler {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            PMMLAssociationRuleHandler.class);

    /** Id of this handler. */
    public static final String ID = "association.rule.handler";

    private double m_minSupport;
    private double m_minConfidence;
    private int m_nrOfTransactions;
    private int m_nrOfItems;
    private int m_nrOfItemsets;
    private int m_nrOfRules;

    private final Set<FrequentItemSet> m_itemsets;
    private final Set<AssociationRule> m_rules;
    private final Map<String, String> m_items;

    private FrequentItemSet m_currentItemSet;

    /**
     * Creates a new empty association rule handler. The initialization has to
     * be performed by registering the handler to a parser.
     */
    public PMMLAssociationRuleHandler() {
        super();
        m_items = new LinkedHashMap<String, String>();
        m_itemsets = new LinkedHashSet<FrequentItemSet>();
        m_rules = new LinkedHashSet<AssociationRule>();
    }


    /**
     * Creates an initialized association rule handler that can be used to
     * output the association rule model by invoking
     * {@link #addPMMLModel(org.w3c.dom.DocumentFragment, PMMLPortObjectSpec)}.
     * @param nrOfTransactions the number of transactions
     * @param minSupport the minimum support
     * @param minConfidence the minimum confidence
     * @param items the items
     * @param itemsets the itemsets
     * @param rules the association rules
     */
    public PMMLAssociationRuleHandler(final int nrOfTransactions,
            final double minSupport, final double minConfidence,
            final Map<String, String> items,
            final Set<FrequentItemSet> itemsets,
            final Set<AssociationRule> rules) {
        super();
        m_nrOfTransactions = nrOfTransactions;
        m_minSupport = minSupport;
        m_minConfidence = minConfidence;
        m_items = items;
        m_itemsets = itemsets;
        m_rules = rules;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start,
            final int length) throws SAXException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri,
            final String localName, final String name) throws SAXException {
        if (name.equals("Itemset")) {
            // last itemset must explicitely added here
            if (!m_itemsets.contains(m_currentItemSet)) {
                m_itemsets.add(m_currentItemSet);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName,
            final String name, final Attributes atts) throws SAXException {
        // start element -> extract minimum support, confidence, nr of items
        if (name.equals("AssociationModel")
            /* In order to support association rule PMML models previously
               written by KNIME the wrong model name is still parsed. */
                || name.equals("AssociationRuleModel")) {
            // all required attributes
            m_nrOfTransactions = Integer.parseInt(
                    atts.getValue("numberOfTransactions"));
            m_minSupport = Double.parseDouble(atts.getValue("minimumSupport"));
            m_minConfidence = Double.parseDouble(atts.getValue(
                    "minimumConfidence"));
            m_nrOfItems = Integer.parseInt(atts.getValue("numberOfItems"));
            m_nrOfItemsets = Integer.parseInt(atts.getValue(
                    "numberOfItemsets"));
            m_nrOfRules = Integer.parseInt(atts.getValue("numberOfRules"));
        } else if (name.equals("Item")) {
            // get the id and the value
            String id = atts.getValue("id");
            String value = atts.getValue("value");
            if (!m_items.containsKey(id)) {
                m_items.put(id, value);
            }
            // ignore the mapped value!
            if (atts.getValue("mappedValue") != null) {
                LOGGER.warn("Ignoring mapped value: "
                        + atts.getValue("mappedValue"));
            }
            // and the weight
            if (atts.getValue("weight") != null) {
                LOGGER.warn("Ignoring weight of item " + id + "/" + value);
            }
        } else if (name.equals("Itemset")) {
            String id = atts.getValue("id");
            if (m_currentItemSet == null) {
                m_currentItemSet = new FrequentItemSet(id);
            }
            if (!id.equals(m_currentItemSet.getId())) {
                m_itemsets.add(m_currentItemSet);
                m_currentItemSet = new FrequentItemSet(id);
            }
            if (atts.getValue("support") != null) {
                m_currentItemSet.setSupport(Double.parseDouble(
                        atts.getValue("support")));
            }
        } else if (name.equals("ItemRef")) {
            // get the referenced item id
            String itemId = atts.getValue("itemRef");
            // find the item:
            if (!m_items.containsKey(itemId)) {
                throw new SAXException(
                        "Referenced item " + itemId + " in itemset "
                        + m_currentItemSet.getId()
                        + " cannot be found in items!");
            }
            // TODO: also support String ids
            m_currentItemSet.add(Integer.parseInt(itemId));
        } else if (name.equals("AssociationRule")) {
            double support = Double.parseDouble(atts.getValue("support"));
            double confidence = Double.parseDouble(atts.getValue("confidence"));
            String antecedentId = atts.getValue("antecedent");
            String consequentId = atts.getValue("consequent");
            // The lift attribute is optional
            String value = atts.getValue("lift");
            double lift = 0.0;
            if (value != null) {
                lift = Double.parseDouble(value);
            }
            FrequentItemSet antecedent = null;
            FrequentItemSet consequent = null;
            for (FrequentItemSet set : m_itemsets) {
                if (set.getId().equals(antecedentId)) {
                    antecedent = set;
                } else if (set.getId().equals(consequentId)) {
                    consequent = set;
                }
            }
            if (consequent == null || antecedent == null) {
                throw new SAXException(
                        "One of the referenced itemsets "
                        + antecedentId + " or " + consequentId
                        + " in association rule could not be found.");
            }
            m_rules.add(new AssociationRule(
                    antecedent, consequent, support, confidence, lift));
        }
    }

    /**
     * @return the minSupport
     */
    public double getMinSupport() {
        return m_minSupport;
    }

    /**
     * @return the minConfidence
     */
    public double getMinConfidence() {
        return m_minConfidence;
    }

    /**
     * @return the nrOfTransactions
     */
    public int getNrOfTransactions() {
        return m_nrOfTransactions;
    }

    /**
     * @return the nrOfItems
     */
    public int getNrOfItems() {
        return m_nrOfItems;
    }

    /**
     * @return the nrOfItemsets
     */
    public int getNrOfItemsets() {
        return m_nrOfItemsets;
    }

    /**
     * @return the nrOfRules
     */
    public int getNrOfRules() {
        return m_nrOfRules;
    }

    /**
     * @return the items
     */
    public Map<String, String> getItems() {
        return m_items;
    }

    /**
     * @return the itemsets
     */
    public Set<FrequentItemSet> getItemsets() {
        return m_itemsets;
    }

    /**
     * @return the rules
     */
    public Set<AssociationRule> getRules() {
        return m_rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPMMLModelContent(final TransformerHandler handler,
            final PMMLPortObjectSpec spec) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, null, "functionName", CDATA,
                "associationRules");
        atts.addAttribute(null, null, "numberOfTransactions", CDATA,
                "" + m_nrOfTransactions);
        atts.addAttribute(null, null, "minimumSupport", CDATA,
                "" + m_minSupport);
        atts.addAttribute(null, null, "minimumConfidence", CDATA,
                "" + m_minConfidence);
        atts.addAttribute(null, null, "numberOfItems", CDATA, ""
                + m_items.size());
        atts.addAttribute(null, null, "numberOfItemsets", CDATA,
                "" + m_itemsets.size());
        atts.addAttribute(null, null, "numberOfRules", CDATA,
                "" + m_rules.size());
        handler.startElement(null, null, "AssociationModel", atts);
        PMMLPortObjectSpec.writeMiningSchema(spec, handler);

        //adding an empty local transformations stub that can be filled later
        handler.startElement(null, null, "LocalTransformations", null);
        handler.endElement(null, null, "LocalTransformations");

        writeItems(handler);
        writeItemsets(handler);
        writeRules(handler);
        handler.endElement(null, null, "AssociationModel");
    }

    private void writeItems(final TransformerHandler handler)
        throws SAXException {
        for (Entry<String, String> itemEntry : m_items.entrySet()) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(null, null, "id", CDATA, itemEntry.getKey());
            atts.addAttribute(null, null, "value", CDATA, itemEntry.getValue());
            handler.startElement(null, null, "Item", atts);
            handler.endElement(null, null, "Item");
        }

    }

    private void writeItemsets(final TransformerHandler handler)
        throws SAXException {
        for (FrequentItemSet set : m_itemsets) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(null, null, "id", CDATA, set.getId());
            atts.addAttribute(null, null, "support", CDATA,
                    "" + set.getSupport());
            atts.addAttribute(null, null, "numberOfItems", CDATA,
                    "" + set.getItems().size());
            handler.startElement(null, null, "Itemset", atts);
            // add here the ItemRefs to the items (use index of item as id)
            for (Integer item : set) {
                atts = new AttributesImpl();
                atts.addAttribute(null, null, "itemRef", CDATA, "" + item);
                handler.startElement(null, null, "ItemRef", atts);
                handler.endElement(null, null, "ItemRef");
            }
            handler.endElement(null, null, "Itemset");
        }
    }

    private void writeRules(final TransformerHandler handler)
        throws SAXException {
        for (AssociationRule rule : m_rules) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(null, null, "support", CDATA,
                    "" + rule.getSupport());
            atts.addAttribute(null, null, "confidence", CDATA,
                    "" + rule.getConfidence());
            atts.addAttribute(null, null, "lift", CDATA,
                    "" + rule.getLift());
            atts.addAttribute(null, null, "antecedent", CDATA,
                    rule.getAntecedent().getId());
            atts.addAttribute(null, null, "consequent", CDATA,
                    rule.getConsequent().getId());
            handler.startElement(null, null, "AssociationRule", atts);
            handler.endElement(null, null, "AssociationRule");
        }
    }
}
