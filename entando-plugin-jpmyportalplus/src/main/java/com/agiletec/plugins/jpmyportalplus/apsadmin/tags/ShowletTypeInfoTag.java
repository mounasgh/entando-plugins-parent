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
package com.agiletec.plugins.jpmyportalplus.apsadmin.tags;

import java.util.Set;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.services.showlettype.ShowletType;
import com.agiletec.aps.system.services.showlettype.ShowletTypeParameter;
import com.agiletec.aps.util.ApsWebApplicationUtils;
import com.agiletec.plugins.jpmyportalplus.aps.system.JpmyportalplusSystemConstants;
import com.agiletec.plugins.jpmyportalplus.aps.system.services.config.IMyPortalConfigManager;
import java.util.List;

/**
 * Tag equals to {@link com.agiletec.apsadmin.tags.ShowletTypeInfoTag} tag.
 * Return also a "swappable" property of the type.
 * If the showlet type is uncompatible with MyPortal Engine, return -1.
 * If the showlet type is unswappable return 0, else return 1.
 * @author E.Santoboni
 */
public class ShowletTypeInfoTag extends com.agiletec.apsadmin.tags.ShowletTypeInfoTag {
	
	@Override
	protected Object getPropertyValue(Object masterObject, String propertyValue) {
		Object value = super.getPropertyValue(masterObject, propertyValue);
		try {
			if (null == value && null != propertyValue && propertyValue.equals("swappable")) {
				ShowletType type = (ShowletType) masterObject;
				IMyPortalConfigManager myPortalConfigManager = (IMyPortalConfigManager) ApsWebApplicationUtils.getBean(JpmyportalplusSystemConstants.MYPORTAL_CONFIG_MANAGER, this.pageContext);
				if (this.isCustomizable(myPortalConfigManager, type)) {
					Set<String> swappables = myPortalConfigManager.getConfig().getAllowedShowlets();
					boolean swappable = (null != swappables && swappables.contains(type.getCode()));
					value = (swappable) ? new Integer(1) : new Integer(0);
				} else {
					value = new Integer(-1);
				}
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "getPropertyValue");
		}
		return value;
	}
	
	private boolean isCustomizable(IMyPortalConfigManager myPortalConfigManager, ShowletType type) {
		if (null == type) return false;
		List<ShowletTypeParameter> typeParameters = type.getTypeParameters();
		if (!type.isUserType() && !type.isLogic() && (null != typeParameters && typeParameters.size() > 0)) return false;
		if (type.getCode().equals(myPortalConfigManager.getVoidShowletCode())) return false;
		return true;
	}
	
}