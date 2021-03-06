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
package com.agiletec.plugins.jpuserprofile.apsadmin.common;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.i18n.II18nManager;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.user.AbstractUser;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.aps.util.ApsProperties;
import com.agiletec.apsadmin.system.entity.AbstractApsEntityAction;
import com.agiletec.plugins.jpuserprofile.aps.system.services.ProfileSystemConstants;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.IUserProfileManager;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.IUserProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe action per la gestione del Profilo Utente corrente.
 * @author E.Santoboni
 */
public class CurrentUserProfileAction extends AbstractApsEntityAction implements ICurrentUserProfileAction {
    
	@Override
    public void validate() {
        if (this.getUserProfile() != null) {
            super.validate();
        }
    }
    
    @Override
    public IApsEntity getApsEntity() {
        return this.getUserProfile();
    }
    
    public IUserProfile getUserProfile() {
        return (IUserProfile) this.getRequest().getSession().getAttribute(SESSION_PARAM_NAME_CURRENT_PROFILE);
    }
    
    @Override
    public String createNew() {
        /*
		String profileTypeCode = this.getProfileTypeCode();
        try {
            UserDetails user = this.getCurrentUser();
            IUserProfile userProfile = (IUserProfile) this.getUserProfileManager().getProfile(user.getUsername());
            if (null != userProfile) {
				this.checkTypeLabels(userProfile);
                this.getRequest().getSession().setAttribute(SESSION_PARAM_NAME_CURRENT_PROFILE, userProfile);
                return "edit";
            }
            if (null == profileTypeCode || profileTypeCode.trim().length() == 0) {
                String[] args = {profileTypeCode};
                this.addFieldError("profileTypeCode", this.getText("jpuserprofile.error.new.invalidtype", args));
                return INPUT;
            }
            userProfile = (IUserProfile) this.getUserProfileManager().getEntityPrototype(profileTypeCode);
            if (null == userProfile) {
                String[] args = {profileTypeCode};
                this.addFieldError("profileTypeCode", this.getText("jpuserprofile.error.new.invalidtype", args));
                return INPUT;
            }
            userProfile.setId(user.getUsername());
			this.checkTypeLabels(userProfile);
            this.getRequest().getSession().setAttribute(SESSION_PARAM_NAME_CURRENT_PROFILE, userProfile);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "createNew");
            return FAILURE;
        }
        return SUCCESS;
		*/
		// Operation Not Allowed
        return null;
    }
    
    @Override
    public String view() {
        // Operation Not Allowed
        return null;
    }
    
    @Override
    public String edit() {
        try {
            IUserProfile userProfile = null;
            UserDetails currentUser = this.getCurrentUser();
            Object object = currentUser.getProfile();
            if (null != object && object instanceof IUserProfile) {
                String username = currentUser.getUsername();
                userProfile = this.getUserProfileManager().getProfile(username);
				this.checkTypeLabels(userProfile);
            } else {
				/*
            	List<IApsEntity> userProfileTypes = new ArrayList<IApsEntity>();
                userProfileTypes.addAll(this.getUserProfileManager().getEntityPrototypes().values());
                if (userProfileTypes.isEmpty()) {
                    throw new RuntimeException("Unexpected error - no one user profile types");
                } else if (userProfileTypes.size() == 1) {
                    userProfile = (IUserProfile) userProfileTypes.get(0);
                    userProfile.setId(currentUser.getUsername());
                } else {
                    return "chooseType";
                }
				*/
				return "currentUserWithoutProfile";
            }
            userProfile.disableAttributes(ProfileSystemConstants.ATTRIBUTE_DISABLING_CODE_ON_EDIT);
            this.getRequest().getSession().setAttribute(SESSION_PARAM_NAME_CURRENT_PROFILE, userProfile);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "edit");
            return FAILURE;
        }
        return SUCCESS;
    }
    
	protected void checkTypeLabels(IUserProfile userProfile) {
		if (null == userProfile) {
			return;
		}
		try {
			List<AttributeInterface> attributes = userProfile.getAttributeList();
			for (int i = 0; i < attributes.size(); i++) {
				AttributeInterface attribute = attributes.get(i);
				//jpuserprofile_${typeCodeKey}_${attributeNameI18nKey}
				String attributeLabelKey = "jpuserprofile_" + userProfile.getTypeCode() + "_" + attribute.getName();
				if (null == this.getI18nManager().getLabelGroup(attributeLabelKey)) {
					String attributeDescription = attribute.getDescription();
					String value = (null != attributeDescription && attributeDescription.trim().length() > 0) ? 
							attributeDescription :
							attribute.getName();
					this.addLabelGroups(attributeLabelKey, value);
				}
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "checkTypeLables");
			throw new RuntimeException("Error checking label types", t);
		}
	}
	
	protected void addLabelGroups(String key, String defaultValue) throws ApsSystemException {
		try {
			ApsProperties properties = new ApsProperties();
			Lang defaultLang = super.getLangManager().getDefaultLang();
			properties.put(defaultLang.getCode(), defaultValue);
			this.getI18nManager().addLabelGroup(key, properties);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addLabelGroups");
			throw new RuntimeException("Error adding label groups - key '" + key + "'", t);
		}
	}
	
    @Override
    public String save() {
        try {
            IUserProfile profile = this.getUserProfile();
            if (profile == null) {
                return FAILURE;
            }
            UserDetails user = this.getCurrentUser();
            ((AbstractUser) user).setProfile(profile);
            if (null == this.getUserProfileManager().getProfile(user.getUsername())) {
                this.getUserProfileManager().addProfile(user.getUsername(), profile);
            } else {
                this.getUserProfileManager().updateProfile(user.getUsername(), profile);
            }
            this.getRequest().getSession().removeAttribute(SESSION_PARAM_NAME_CURRENT_PROFILE);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "edit");
            return FAILURE;
        }
        return SUCCESS;
    }
    
    public List<IApsEntity> getUserProfileTypes() {
        List<IApsEntity> userProfileTypes = null;
        try {
            userProfileTypes = new ArrayList<IApsEntity>();
            userProfileTypes.addAll(this.getUserProfileManager().getEntityPrototypes().values());
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getUserProfileTypes");
        }
        return userProfileTypes;
    }
    
    public String getProfileTypeCode() {
        return _profileTypeCode;
    }
    public void setProfileTypeCode(String profileTypeCode) {
        this._profileTypeCode = profileTypeCode;
    }
    
	protected II18nManager getI18nManager() {
		return _i18nManager;
	}
	public void setI18nManager(II18nManager i18nManager) {
		this._i18nManager = i18nManager;
	}
	
    protected IUserProfileManager getUserProfileManager() {
        return _userProfileManager;
    }
    public void setUserProfileManager(IUserProfileManager userProfileManager) {
        this._userProfileManager = userProfileManager;
    }
    
    private String _profileTypeCode;
	private II18nManager _i18nManager;
    private IUserProfileManager _userProfileManager;
    
}