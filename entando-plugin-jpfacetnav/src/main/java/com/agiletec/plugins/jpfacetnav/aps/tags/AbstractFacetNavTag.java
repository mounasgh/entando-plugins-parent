/*
 *
 * Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
 *
 * This file is part of Entando software.
 * Entando is a free software; 
 * you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
 * 
 * See the file License for the specific language governing permissions   
 * and limitations under the License
 * 
 * 
 * 
 * Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
 *
 */
package com.agiletec.plugins.jpfacetnav.aps.tags;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.tree.ITreeNode;
import com.agiletec.aps.system.common.tree.ITreeNodeManager;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.Showlet;
import com.agiletec.aps.util.ApsWebApplicationUtils;
import com.agiletec.plugins.jpfacetnav.aps.system.JpFacetNavSystemConstants;
import com.agiletec.plugins.jpfacetnav.aps.system.services.content.showlet.IFacetNavHelper;
import com.agiletec.plugins.jpfacetnav.aps.tags.util.FacetBreadCrumbs;

/**
 * 
 * @author E.Santoboni
 */
public abstract class AbstractFacetNavTag extends TagSupport {

	/**
	 * Returns required facets
	 * @return Required facets
	 */
	protected List<String> getRequiredFacets() {
		ServletRequest request = this.pageContext.getRequest();
		String facetNodesParamName = this.getFacetNodesParamName();
		if (null == facetNodesParamName) facetNodesParamName = "facetNode";
		List<String> requiredFacets = new ArrayList<String>();
		int index = 1;
		while (null != request.getParameter(facetNodesParamName+"_"+index)) {
			String paramName = facetNodesParamName+"_"+index;
			String value = request.getParameter(paramName);
			this.addFacet(requiredFacets, value);
			index++;
		}

		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			if (paramName.equals(facetNodesParamName)) {
				String[] values = request.getParameterValues(paramName);
				for (int i=0; i<values.length; i++) {
					String value = values[i];
					this.addFacet(requiredFacets, value);
				}
			}
		}

		String selectedNode = request.getParameter("selectedNode");
		this.addFacet(requiredFacets, selectedNode);

		this.removeSelections(requiredFacets);

		this.manageCurrentSelect(selectedNode, requiredFacets);

		return requiredFacets;
	}

	/**
	 * Delete the facets selected through checkboxes selected.
	 * @param requiredFacets
	 */
	private void removeSelections(List<String> requiredFacets) {
		String facetNodesParamName = "facetNodeToRemove";
		ServletRequest request = this.pageContext.getRequest();
		String[] values = request.getParameterValues(facetNodesParamName);
		if (null != values) {
			for (int i=0; i<values.length; i++) {
				String value = values[i];
				requiredFacets.remove(value);
			}
		}
		int index = 1;
		while (null != request.getParameter(facetNodesParamName+"_"+index)) {
			String paramName = facetNodesParamName+"_"+index;
			String value = request.getParameter(paramName);
			requiredFacets.remove(value);
			index++;
		}
	}

	/**
	 * MANAGEMENT OF SELECTED current node (child nodes REMOVE ANY OF THE SELECTION).
	 * @param selectedNode
	 * @param requiredFacets
	 */
	private void manageCurrentSelect(String selectedNode, List<String> requiredFacets) {
		List<String> nodesToRemove = new ArrayList<String>();
		Iterator<String> requredFacetIterator = requiredFacets.iterator();
		ITreeNodeManager facetManager = this.getFacetManager();
		while (requredFacetIterator.hasNext()) {
			String reqNode = (String) requredFacetIterator.next();
			ITreeNode parent = facetManager.getNode(reqNode).getParent();
			if (this.isChildOf(parent, selectedNode)) nodesToRemove.add(reqNode);
		}
		for (int i=0; i<nodesToRemove.size(); i++) {
			String nodeToRemove = nodesToRemove.get(i);
			requiredFacets.remove(nodeToRemove);
		}
	}

	/**
	 * Returns facet manager
	 * @return Facet manager
	 */
	protected ITreeNodeManager getFacetManager() {
		IFacetNavHelper facetNavHelper = (IFacetNavHelper) ApsWebApplicationUtils.getBean(JpFacetNavSystemConstants.CONTENT_FACET_NAV_HELPER, this.pageContext);
		return facetNavHelper.getTreeNodeManager();
	}

	/**
	 * Return true if it a child of checked node
	 * @param nodeToCheck
	 * @param codeForCheck
	 * @return True if it a child of checked node
	 */
	protected boolean isChildOf(ITreeNode nodeToCheck, String codeForCheck) {
		if (nodeToCheck.getCode().equals(codeForCheck)) {
			return true;
		}
		ITreeNode parentFacet = nodeToCheck.getParent();
		if (null != parentFacet && !parentFacet.getCode().equals(parentFacet.getParent().getCode())) {
			return this.isChildOf(parentFacet, codeForCheck);
		}
		return false;
	}

	/**
	 * Add new facet
	 * @param requiredFacets
	 * @param value
	 */
	private void addFacet(List<String> requiredFacets, String value) {
		if (null != value && value.trim().length()>0 && !requiredFacets.contains(value.trim())) {
			requiredFacets.add(value.trim());
		}
	}

	/**
	 * Returns the list of objects (@ link FacetBreadCrumbs).
	 * @param requiredFacets Nodes facets required.
	 * @param reqCtx The context of the current request.
	 * @return The list of objects Breadcrumbs.
	 */
	protected List<FacetBreadCrumbs> getBreadCrumbs(List<String> requiredFacets, RequestContext reqCtx) {
		List<ITreeNode> roots = this.getFacetRoots(reqCtx);
		if (null == roots) return null;
		List<ITreeNode> finalNodes = this.getFinalNodes(requiredFacets);
		List<FacetBreadCrumbs> breadCrumbs = new ArrayList<FacetBreadCrumbs>();
		for (int i=0; i<finalNodes.size(); i++) {
			ITreeNode requiredNode = finalNodes.get(i);
			for (int j = 0; j < roots.size(); j++) {
				ITreeNode root = roots.get(j);
				if (this.isChildOf(requiredNode, root.getCode())) {
					breadCrumbs.add(new FacetBreadCrumbs(requiredNode.getCode(), root.getCode(), this.getFacetManager()));
				}
			}
		}
		return breadCrumbs;
	}

	/**
	 * Returns final nodes
	 * @param requiredFacets
	 * @return Final Nodes
	 */
	private List<ITreeNode> getFinalNodes(List<String> requiredFacets) {
		List<ITreeNode> finalNodes = new ArrayList<ITreeNode>();
		List<String> requiredFacetsCopy = new ArrayList<String>(requiredFacets);
		for (int i=0; i<requiredFacets.size(); i++) {
			String nodeToAnalize = requiredFacets.get(i);
			this.removeParentOf(nodeToAnalize, requiredFacetsCopy);
		}
		Iterator<String> requiredFacetIterator = requiredFacetsCopy.iterator();
		while (requiredFacetIterator.hasNext()) {
			String reqNode = (String) requiredFacetIterator.next();
			finalNodes.add(this.getFacetManager().getNode(reqNode));
		}
		return finalNodes;
	}

	/**
	 * Remove node parent
	 * @param nodeFromAnalize
	 * @param requiredFacetsCopy
	 */
	private void removeParentOf(String nodeFromAnalize, List<String> requiredFacetsCopy) {
		ITreeNode nodeFrom = this.getFacetManager().getNode(nodeFromAnalize);
		List<String> nodesToRemove = new ArrayList<String>();
		Iterator<String> requiredFacetIterator = requiredFacetsCopy.iterator();
		while (requiredFacetIterator.hasNext()) {
			String reqNode = (String) requiredFacetIterator.next();
			if (!nodeFromAnalize.equals(reqNode) && this.isChildOf(nodeFrom, reqNode)) {
				nodesToRemove.add(reqNode);
			}
		}
		for (int i=0; i<nodesToRemove.size(); i++) {
			String nodeToRemove = nodesToRemove.get(i);
			requiredFacetsCopy.remove(nodeToRemove);
		}
	}

	/**
	 * Returns a list of root nodes through which grant the tree. 
	 * The root nodes allow you to create blocks of selected nodes in showlet appropriate.
	 * @param reqCtx The context of the current request.
	 * @return The list of root nodes.
	 */
	protected List<ITreeNode> getFacetRoots(RequestContext reqCtx) {
		IPage page = (IPage) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_PAGE);
		Integer currentFrame = (Integer) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_FRAME);
		Showlet[] showlets = page.getShowlets();
		for (int i = 0; i < showlets.length; i++) {
			if (i == currentFrame.intValue()) continue; 
			Showlet showlet = showlets[i];
			String configParamName = JpFacetNavSystemConstants.FACET_ROOTS_SHOWLET_PARAM_NAME;
			if (null != showlet && null != showlet.getConfig() 
					&& null != showlet.getConfig().getProperty(configParamName)) {
				String facetParamConfig = showlet.getConfig().getProperty(configParamName);
				return this.getFacetRoots(facetParamConfig);
			}
		}
		return null;
	}

	/**
	 * Returns facet roots.
	 * @param facetRootNodesParam
	 * @return facet roots
	 */
	protected List<ITreeNode> getFacetRoots(String facetRootNodesParam) {
		List<ITreeNode> nodes = new ArrayList<ITreeNode>();
		String[] facetCodes = facetRootNodesParam.split(",");
		for (int j = 0; j < facetCodes.length; j++) {
			String facetCode = facetCodes[j].trim();
			ITreeNode node = this.getFacetManager().getNode(facetCode);
			if (null != node) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public String getFacetNodesParamName() {
		return _facetNodesParamName;
	}
	public void setFacetNodesParamName(String facetNodesParamName) {
		this._facetNodesParamName = facetNodesParamName;
	}

	public String getRequiredFacetsParamName() {
		return _requiredFacetsParamName;
	}
	public void setRequiredFacetsParamName(String requiredFacetsParamName) {
		this._requiredFacetsParamName = requiredFacetsParamName;
	}

	private String _facetNodesParamName;
	private String _requiredFacetsParamName;

}