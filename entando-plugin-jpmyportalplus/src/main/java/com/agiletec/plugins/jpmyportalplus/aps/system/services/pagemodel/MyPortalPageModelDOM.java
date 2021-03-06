/*
*
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* JAPS and its  source-code is  licensed under the  terms of the
* GNU General Public License  as published by  the Free Software
* Foundation (http://www.fsf.org/licensing/licenses/gpl.txt).
* 
* You may copy, adapt, and redistribute this file for commercial
* or non-commercial use.
* When copying,  adapting,  or redistributing  this document you
* are required to provide proper attribution  to AgileTec, using
* the following attribution line:
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package com.agiletec.plugins.jpmyportalplus.aps.system.services.pagemodel;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.page.Showlet;
import com.agiletec.aps.system.services.showlettype.IShowletTypeManager;
import com.agiletec.aps.util.ApsProperties;

/**
 * This support class parses the XML representing the configuration of a page model
 * @author E.Santoboni
 * 
 * 
<frames>
	<frame pos="0" locked="false" column="3">
		<descr>Search in this site</descr>	
		<defaultShowlet code="searchForm"/>
	</frame>
	....
	....
</frames>
 * 
 */
public class MyPortalPageModelDOM {
	
	/**
	 * Class constructor
	 * @param xmlText The XML string to parse
	 * @param showletTypeManager The manager of the showlet type
	 * @throws ApsSystemException In case of error
	 */
	public MyPortalPageModelDOM(String xmlText, IShowletTypeManager showletTypeManager) throws ApsSystemException {
		this.decodeDOM(xmlText);
		this.buildFrames(showletTypeManager);
	}
	
	private void decodeDOM(String xmlText) throws ApsSystemException {
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		StringReader reader = new StringReader(xmlText);
		try {
			_doc = builder.build(reader);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "decodeDOM");
			throw new ApsSystemException("Error detected parsing the XML", t);
		}
	}
	
	private ApsProperties buildProperties(Element propertiesElement) {
		ApsProperties prop = new ApsProperties();
		List<Element> propertyElements = propertiesElement.getChildren(TAB_PROPERTY);
		Iterator<Element> propertyElementsIter = propertyElements.iterator();
		while (propertyElementsIter.hasNext()) {
			Element property = (Element) propertyElementsIter.next();
			prop.put(property.getAttributeValue(ATTRIBUTE_KEY), property.getText().trim());
		}
		return prop;
	}
	
	private void buildFrames(IShowletTypeManager showletTypeManager) throws ApsSystemException {
		List<Element> frameElements = _doc.getRootElement().getChildren(TAB_FRAME);
		if (null != frameElements && frameElements.size() > 0) {
			int framesNumber = frameElements.size();
			_frames = new String[framesNumber];
			_frameConfigs = new Frame[framesNumber];
			_defaultShowlet = new Showlet[framesNumber];
			_existMainFrame = false;
			Iterator<Element> frameElementsIter = frameElements.iterator();
			while (frameElementsIter.hasNext()) {
				Element frameElement = frameElementsIter.next();
				this.buildFrame(showletTypeManager, framesNumber, frameElement);
			}
		} else {
			_frames = new String[0];
			_defaultShowlet = new Showlet[0];
		}
	}
		
	private void buildFrame(IShowletTypeManager showletTypeManager,
			int framesNumber, Element frameElement) throws ApsSystemException {
		int pos = Integer.parseInt(frameElement.getAttributeValue(ATTRIBUTE_POS));
		if(pos >= framesNumber) {
			throw new ApsSystemException("The 'pos' attribute exceeds the available frames in the page model");
		} 
		Frame frame = new Frame();
		frame.setPos(pos);
		String main = frameElement.getAttributeValue(ATTRIBUTE_MAIN);
		if (null != main && main.equals("true")) {
			_existMainFrame = true;
			_mainFrame = pos;
			frame.setMainFrame(true);
		}
		String fixed = frameElement.getAttributeValue("locked");
		if (null == fixed || fixed.equals("true")) {
			frame.setLocked(true);
		}
		String column = frameElement.getAttributeValue("column");
		if (null != column) {
			try {
				frame.setColumn(Integer.parseInt(column));
			} catch (Throwable e) {
				//nothing to do
			}
		}
		Element frameDescrElement = frameElement.getChild(TAB_DESCR);
		if (null != frameDescrElement) {
			String descr = frameDescrElement.getText();
			_frames[pos] = descr;
			frame.setDescr(descr);
		}
		Element defaultShowletElement = frameElement.getChild(TAB_DEFAULT_SHOWLET);
		if (null != defaultShowletElement) {
			Showlet defaultShowlet = this.buildDefaultShowlet(defaultShowletElement, pos, showletTypeManager);
			frame.setDefaultShowlet(defaultShowlet);
			this.getDefaultShowlet()[pos] = defaultShowlet;
		}
		_frameConfigs[pos] = frame;
	}
	
	private Showlet buildDefaultShowlet(Element defaultShowletElement, int pos, IShowletTypeManager showletTypeManager) {
		Showlet showlet = new Showlet();
		String showletCode = defaultShowletElement.getAttributeValue(ATTRIBUTE_CODE);
		showlet.setType(showletTypeManager.getShowletType(showletCode));
		Element propertiesElement = defaultShowletElement.getChild(TAB_PROPERTIES);
		if (null != propertiesElement) {
			ApsProperties prop = this.buildProperties(propertiesElement);
			showlet.setConfig(prop);
		} else {
			showlet.setConfig(new ApsProperties());
		}
		_defaultShowlet[pos] = showlet;
		return showlet;
	}
	
	/**
	 * Return the sorted descriptions by frames of the page models.
	 * @return An array with the frames descriptions.
	 */
	public String[] getFrames() {
		return this._frames;
	}
	
	public Frame[] getFrameConfigs() {
		return this._frameConfigs;
	}
	
	/**
	 * Return the position of the main frame when available, otherwise return -1.
	 * @return The position of the main frame or -1 when it's not available.
	 */
	public int getMainFrame() {
		if (_existMainFrame) {
			return this._mainFrame;
		} else {
			return -1;
		}
	}
	
	/**
	 * Return the configuration of the default showlets
	 * @return The default showlets
	 */
	public Showlet[] getDefaultShowlet() {
		return this._defaultShowlet;
	}
	
	private Document _doc;
	private final String TAB_FRAME = "frame";
	private final String ATTRIBUTE_POS = "pos";
	private final String ATTRIBUTE_MAIN = "main";
	private final String TAB_DESCR = "descr";
	private final String TAB_DEFAULT_SHOWLET = "defaultShowlet";
	private final String ATTRIBUTE_CODE = "code";
	private final String TAB_PROPERTIES = "properties";
	private final String TAB_PROPERTY = "property";
	private final String ATTRIBUTE_KEY = "key";
	private boolean _existMainFrame;
	private int _mainFrame;
	private String[] _frames;
	private Showlet[] _defaultShowlet;
	
	private Frame[] _frameConfigs;
	
}