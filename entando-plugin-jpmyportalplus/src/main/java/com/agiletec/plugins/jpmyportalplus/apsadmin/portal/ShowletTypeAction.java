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
package com.agiletec.plugins.jpmyportalplus.apsadmin.portal;

import java.util.List;
import java.util.Set;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.services.showlettype.ShowletType;
import com.agiletec.aps.system.services.showlettype.ShowletTypeParameter;
import com.agiletec.plugins.jpmyportalplus.aps.system.services.config.IMyPortalConfigManager;
import com.agiletec.plugins.jpmyportalplus.aps.system.services.config.model.MyPortalConfig;

/**
 * @author E.Santoboni
 */
public class ShowletTypeAction extends com.agiletec.apsadmin.portal.ShowletTypeAction {
	
	@Override
	public String edit() {
		String result = super.edit();
		if (null == result || !result.equals(SUCCESS)) return result;
		try {
			this.setSwappable(this.isSwappableType(this.getShowletTypeCode()));
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "edit");
			return FAILURE;
		}
		return SUCCESS;
	}
	
	@Override
	public String save() {
		String result = super.save();
		if (null == result || result.equals(FAILURE)) return FAILURE;
		this.updateShowletTypeConfig();
		return result;
	}
	
	private String updateShowletTypeConfig() {
		try {
			this.getShowletTypeCode();
			boolean swappable = (null != this.getSwappable()) ? this.getSwappable().booleanValue() : false;
			if (this.isCustomizable()) {
				MyPortalConfig config = this.getMyPortalConfigManager().getConfig();
				if (swappable) {
					config.getAllowedShowlets().add(this.getShowletTypeCode());
				} else {
					config.getAllowedShowlets().remove(this.getShowletTypeCode());
				}
				this.getMyPortalConfigManager().saveConfig(config);
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateShowletTypeConfig");
			return FAILURE;
		}
		return SUCCESS;
	}
	
	private boolean isCustomizable() {
		ShowletType type = this.getShowletTypeManager().getShowletType(this.getShowletTypeCode());
		if (null == type) return false;
		List<ShowletTypeParameter> typeParameters = type.getTypeParameters();
		if (!type.isUserType() && !type.isLogic() && (null != typeParameters && typeParameters.size() > 0)) return false;
		if (type.getCode().equals(this.getMyPortalConfigManager().getVoidShowletCode())) return false;
		return true;
	}
	
	public boolean isSwappableType(String showletTypeCode) {
		boolean swappable = false;
		try {
			Set<String> swappables = this.getMyPortalConfigManager().getConfig().getAllowedShowlets();
			swappable = (swappables != null && swappables.contains(showletTypeCode));
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "isSwappableType");
		}
		return swappable;
	}
	
	public Boolean getSwappable() {
		return _swappable;
	}
	public void setSwappable(Boolean swappable) {
		this._swappable = swappable;
	}
	
	protected IMyPortalConfigManager getMyPortalConfigManager() {
		return _myPortalConfigManager;
	}
	public void setMyPortalConfigManager(IMyPortalConfigManager myPortalConfigManager) {
		this._myPortalConfigManager = myPortalConfigManager;
	}
	
	protected Boolean _swappable;
	
	private IMyPortalConfigManager _myPortalConfigManager;
	
}