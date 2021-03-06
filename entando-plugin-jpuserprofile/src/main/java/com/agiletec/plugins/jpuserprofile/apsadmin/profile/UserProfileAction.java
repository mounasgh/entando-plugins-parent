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
package com.agiletec.plugins.jpuserprofile.apsadmin.profile;

import java.util.ArrayList;
import java.util.List;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.apsadmin.system.entity.AbstractApsEntityAction;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.IUserProfileManager;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.IUserProfile;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.UserProfile;

/**
 * @author E.Santoboni
 */
public class UserProfileAction extends AbstractApsEntityAction {
    
    public String edit() {
        String username = this.getUsername();
        try {
            String chechUsernameResult = this.checkUsername(username);
            if (null != chechUsernameResult) return chechUsernameResult;
            IUserProfile userProfile = (IUserProfile) this.getUserProfileManager().getProfile(username);
            if (null == userProfile) {
                List<IApsEntity> userProfileTypes = new ArrayList<IApsEntity>();
                userProfileTypes.addAll(this.getUserProfileManager().getEntityPrototypes().values());
                if (userProfileTypes.size() == 0) {
                    throw new RuntimeException("Unexpected error - no one user profile types");
                } else if (userProfileTypes.size() == 1) {
                    userProfile = (IUserProfile) userProfileTypes.get(0);
                    userProfile.setId(username);
                } else {
                    return "chooseType";
                }
            }
            this.getRequest().getSession().setAttribute(USERPROFILE_ON_SESSION, userProfile);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "edit");
            return FAILURE;
        }
        return SUCCESS;
    }
    
    public String createNew() {
        String username = this.getUsername();
        String profileTypeCode = this.getProfileTypeCode();
        try {
            String chechUsernameResult = this.checkUsername(username);
            if (null != chechUsernameResult) return chechUsernameResult;
            IUserProfile userProfile = (IUserProfile) this.getUserProfileManager().getProfile(username);
            if (null != userProfile) {
                this.getRequest().getSession().setAttribute(USERPROFILE_ON_SESSION, userProfile);
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
            userProfile.setId(this.getUsername());
            this.getRequest().getSession().setAttribute(USERPROFILE_ON_SESSION, userProfile);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "createNew");
            return FAILURE;
        }
        return SUCCESS;
    }
    
    public String save() {
        try {
            IUserProfile userProfile = (IUserProfile) this.getApsEntity();
            String username = userProfile.getUsername();
            if (null == this.getUserProfileManager().getProfile(userProfile.getUsername())) {
                this.getUserProfileManager().addProfile(username, userProfile);
            } else {
                this.getUserProfileManager().updateProfile(username, userProfile);
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "save");
            return FAILURE;
        }
        return SUCCESS;
    }
    
    public String view() {
        String username = this.getUsername();
        try {
            String chechUsernameResult = this.checkUsername(username);
            if (null != chechUsernameResult) return chechUsernameResult;
            IUserProfile userProfile = (IUserProfile) this.getUserProfileManager().getProfile(username);
            if (null == userProfile) {
                String[] args = {username};
                this.addFieldError("username", this.getText("jpuserprofile.error.view.userWithoutProfile", args));
                return INPUT;
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "view");
            return FAILURE;
        }
        return SUCCESS;
    }
    
    private String checkUsername(String username) {
        if (null == username || username.trim().length() == 0) {
            String[] args = {username};
            this.addFieldError("username", this.getText("jpuserprofile.error.new.invalidusername", args));
            return INPUT;
        }
        return null;
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
    
    public UserProfile getUserProfile() {
        return (UserProfile) this.getApsEntity();
    }
    
    public IUserProfile getUserProfile(String username) {
        IUserProfile userProfile = null;
        try {
            userProfile = this.getUserProfileManager().getProfile(username);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getUserProfile", "Error extracting user profile by username " + username);
        }
        return userProfile;
    }
    
    public IApsEntity getApsEntity() {
        return (UserProfile) this.getRequest().getSession().getAttribute(USERPROFILE_ON_SESSION);
    }
    
    public String getUsername() {
        return _username;
    }
    public void setUsername(String username) {
        this._username = username;
    }
    
    public String getProfileTypeCode() {
        return _profileTypeCode;
    }
    public void setProfileTypeCode(String profileTypeCode) {
        this._profileTypeCode = profileTypeCode;
    }
    
    protected IUserProfileManager getUserProfileManager() {
        return _userProfileManager;
    }
    public void setUserProfileManager(IUserProfileManager userProfileManager) {
        this._userProfileManager = userProfileManager;
    }
    
    private String _username;
    private String _profileTypeCode;
    private IUserProfileManager _userProfileManager;
    public static final String USERPROFILE_ON_SESSION = "jpuserprofile_profileOnSession";
    
}