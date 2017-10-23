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
 * History
 *   12.03.2007 (sieb): created
 */
package org.knime.base.node.mine.decisiontree2.learner2;

import org.knime.base.node.mine.decisiontree2.model.DecisionTree;
import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNode;
import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNodeLeaf;
import org.knime.base.node.mine.decisiontree2.model.DecisionTreeNodeSplit;

/**
 * Class implementing pruning schemes.
 *
 * @author Christoph Sieb, University of Konstanz
 *
 * @since 2.6
 */
public final class Pruner {

    private Pruner() {

    }

    /**
     * Prunes a {@link DecisionTree} according to the minimum description lenght
     * (MDL) principle.
     *
     * @param decTree the decision tree to prune
     */
    public static void mdlPruning(final DecisionTree decTree) {

        // traverse the tree depth first (in-fix)
        DecisionTreeNode root = decTree.getRootNode();
        mdlPruningRecurse(root);
    }

//    /**
//     * The general idea is to recursively prune the children and then compare
//     * the potential leaf estimated erro with the actual estimated error
//     * including the length of the children.
//     *
//     * @param node the node to prune
//     * @param zValue the z value according to which the error is estimated
//     *            calculated from the confidence value
//     *
//     * @return the resulting description length after pruning; this value is
//     *         used in higher levels of the recursion, i.e. for the parent node
//     */
//    private static PruningResult estimatedErrorPruningRecurse(
//            final DecisionTreeNode node, final double zValue) {
//
//        // if this is a child, just return the estimated error
//        if (node.isLeaf()) {
//            double error = node.getEntireClassCount() - node.getOwnClassCount();
//            double estimatedError =
//                    estimatedError(node.getEntireClassCount(), error, zValue);
//
//            return new PruningResult(estimatedError, node);
//        }
//
//        // holds the estimated errors of the children
//        double[] childDescriptionLength = new double[node.getChildCount()];
//        DecisionTreeNodeSplit splitNode = (DecisionTreeNodeSplit)node;
//        // prune all children
//        DecisionTreeNode[] children = splitNode.getChildren();
//        int count = 0;
//        for (DecisionTreeNode childNode : children) {
//
//            PruningResult result =
//                    estimatedErrorPruningRecurse(childNode, zValue);
//            childDescriptionLength[count] = result.getQualityValue();
//
//            // replace the child with the one from the result (could of course
//            // be the same)
//            splitNode.replaceChild(childNode, result.getNode());
//
//            count++;
//        }
//
//        // calculate the estimated error if this would be a leaf
//        double error = node.getEntireClassCount() - node.getOwnClassCount();
//        double leafEstimatedError =
//                estimatedError(node.getEntireClassCount(), error, zValue);
//
//        // calculate the current estimated error (sum of estimated errors of the
//        // children)
//        double currentEstimatedError = 0;
//        for (double childDescLength : childDescriptionLength) {
//            currentEstimatedError += childDescLength;
//        }
//
//        // define the return node
//        DecisionTreeNode returnNode = node;
//        double returnEstimatedError = currentEstimatedError;
//
//        // if the possible leaf costs are smaller, replace this node
//        // with a leaf (tollerance is 0.1)
//        if (leafEstimatedError <= currentEstimatedError + 0.1) {
//            DecisionTreeNodeLeaf newLeaf =
//                    new DecisionTreeNodeLeaf(node.getOwnIndex(), node
//                            .getMajorityClass(), node.getClassCounts());
//            newLeaf.setParent((DecisionTreeNode)node.getParent());
//            newLeaf.setPrefix(node.getPrefix());
//            returnNode = newLeaf;
//            returnEstimatedError = leafEstimatedError;
//        }
//
//        return new PruningResult(returnEstimatedError, returnNode);
//    }
//
//    /**
//     * Prunes a {@link DecisionTree} according to the estimated error pruning
//     * (Quinlan 87).
//     *
//     * @param decTree the decision tree to prune
//     * @param confidence the confidence value according to which the error is
//     *            estimated
//     */
//    public static void estimatedErrorPruning(final DecisionTree decTree,
//            final double confidence) {
//
//        // traverse the tree depth first (in-fix)
//        DecisionTreeNode root = decTree.getRootNode();
//        // double zValue = xnormi(1 - confidence);
//        estimatedErrorPruningRecurse(root, zValue);
//    }

    /**
     * The general idea is to recursively prune the children and then compare
     * the potential leaf description length with the actual length including
     * the length of the children.
     *
     * @param node the node to prune
     *
     * @return the resulting description length after pruning; this value is
     *         used in higher levels of the recursion, i.e. for the parent node
     */
    private static PruningResult mdlPruningRecurse(
            final DecisionTreeNode node) {

        // if this is a child, just return the description length
        // this is the cost for determining whether it is a leaf or has two
        // children, i.e. the cost is 1 and the cost for the errors in the
        // leaf
        if (node.isLeaf()) {
            double error = node.getEntireClassCount() - node.getOwnClassCount();

            // 1.0 is the cost for encoding a node in general (leaf or internal
            // node => 1Bit)
            return new PruningResult(error + 1.0, node);
        }

        // holds the description length of the children
        double[] childDescriptionLength = new double[node.getChildCount()];
        DecisionTreeNodeSplit splitNode = (DecisionTreeNodeSplit)node;
        // prune all children
        DecisionTreeNode[] children = splitNode.getChildren();
        int count = 0;
        for (DecisionTreeNode childNode : children) {

            PruningResult result = mdlPruningRecurse(childNode);
            childDescriptionLength[count] = result.getQualityValue();

            // replace the child with the one from the result (could of course
            // be the same)
            splitNode.replaceChild(childNode, result.getNode());

            count++;
        }

        // calculate the cost if this would be a leaf
        double leafCost =
                node.getEntireClassCount() - node.getOwnClassCount() + 1.0;
        // calculate the current cost including the children
        double currentCost = 1.0 + Math.log(node.getChildCount()) / Math.log(2);
        for (double childDescLength : childDescriptionLength) {
            currentCost += childDescLength;
        }

        // define the return node
        DecisionTreeNode returnNode = node;
        double returnCost = currentCost;

        // if the possible leaf costs are smaller, replace this node
        // with a leaf
        if (leafCost <= currentCost) {
            DecisionTreeNodeLeaf newLeaf =
                    new DecisionTreeNodeLeaf(node.getOwnIndex(), node
                            .getMajorityClass(), node.getClassCounts());
            newLeaf.setParent(node.getParent());
            newLeaf.setPrefix(node.getPrefix());
            returnNode = newLeaf;
            returnCost = leafCost;
        }

        return new PruningResult(returnCost, returnNode);
    }

//    private static double estimatedError(final double all, final double error,
//            final double zValue) {
//        double f = error / all;
//        double z = zValue;
//        double N = all;
//
//        double estimatedError =
//                (f + z * z / (2 * N) + z
//                        * Math.sqrt(f / N - f * f / N + z * z / (4 * N * N)))
//                        / (1 + z * z / N);
//
//        // return the weighted value
//        return estimatedError * all;
//    }
//
    /**
     * Prunes a {@link DecisionTree} according to the training error. I.e.
     * if the error in the subtree according to the training data is the same
     * as in the current node, the subtree is pruned, as nothing is gained.
     *
     * @param decTree the decision tree to prune
     */
    public static void trainingErrorPruning(final DecisionTree decTree) {

        // traverse the tree depth first (in-fix)
        DecisionTreeNode root = decTree.getRootNode();
        trainingErrorPruningRecurse(root);
    }

    /**
     * The recursion for the training error based pruning.
     *
     * @param node the node to prune
     *
     * @return the resulting error; this value is
     *         used in higher levels of the recursion, i.e. for the parent node
     */
    private static PruningResult trainingErrorPruningRecurse(
            final DecisionTreeNode node) {

        // if this is a child, just return the error rate
        if (node.isLeaf()) {
            double error = node.getEntireClassCount() - node.getOwnClassCount();

            return new PruningResult(error, node);
        }

        // holds the error rates of the children
        double[] childErrorRates = new double[node.getChildCount()];
        // this node must be a split node
        DecisionTreeNodeSplit splitNode = (DecisionTreeNodeSplit)node;

        // prune all children
        DecisionTreeNode[] children = splitNode.getChildren();
        int count = 0;
        for (DecisionTreeNode childNode : children) {

            PruningResult result = trainingErrorPruningRecurse(childNode);
            childErrorRates[count] = result.getQualityValue();

            // replace the child with the one from the result (could of course
            // be the same)
            splitNode.replaceChild(childNode, result.getNode());

            count++;
        }

        // calculate the error if this would be a leaf
        double leafError =
                node.getEntireClassCount() - node.getOwnClassCount();
        // calculate the current error including the children
        double currentError = 0.0;
        for (double childError : childErrorRates) {
            currentError += childError;
        }

        // define the return node
        DecisionTreeNode returnNode = node;
        double returnError = currentError;

        // if the possible leaf error is smaller, replace this node
        // with a leaf
        if (leafError - 0.001 <= currentError) {
            DecisionTreeNodeLeaf newLeaf =
                    new DecisionTreeNodeLeaf(node.getOwnIndex(), node
                            .getMajorityClass(), node.getClassCounts());
            newLeaf.setParent(node.getParent());
            newLeaf.setPrefix(node.getPrefix());
            returnNode = newLeaf;
            returnError = leafError;
        }

        return new PruningResult(returnError, returnNode);
    }
}
