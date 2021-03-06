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
package org.entando.entando.plugins.jpuserprofile.aps.system.services.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.JAXBEntity;
import com.agiletec.aps.system.common.entity.model.attribute.AbstractTextAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.IUserProfile;

/**
 * @author E.Santoboni
 */
@XmlRootElement(name = "userProfile")
@XmlType(propOrder = {"firstname", "surname", "mail"})
public class JAXBUserProfile extends JAXBEntity {
    
    public JAXBUserProfile() {}
    
    public JAXBUserProfile(IApsEntity mainEntity, String langCode) {
        super(mainEntity, langCode);
        IUserProfile profile = (IUserProfile) mainEntity;
        String fnan = profile.getFirstNameAttributeName();
        String snan = profile.getSurnameAttributeName();
        String man = profile.getMailAttributeName();
        this.setFirstname((String) profile.getValue(fnan));
        this.setSurname((String) profile.getValue(snan));
        this.setMail((String) profile.getValue(man));
    }
    
    public IApsEntity buildEntity(IApsEntity prototype, ICategoryManager categoryManager) {
        IUserProfile profile = (IUserProfile) super.buildEntity(prototype, categoryManager);
        this.valorizeTextAttribute(profile.getFirstNameAttributeName(), this.getFirstname(), profile);
        this.valorizeTextAttribute(profile.getSurnameAttributeName(), this.getSurname(), profile);
        this.valorizeTextAttribute(profile.getMailAttributeName(), this.getMail(), profile);
        return profile;
    }
    
    private void valorizeTextAttribute(String attributeName, String value, IUserProfile profile) {
        if (null == attributeName || value == null) {
            return;
        }
        AttributeInterface attribute = (AttributeInterface) profile.getAttribute(attributeName);
        if (null == attribute || !(attribute instanceof AbstractTextAttribute)) {
            return;
        }
        AbstractTextAttribute textAttribute = (AbstractTextAttribute) attribute;
        textAttribute.setText(value, null);
    }
    
    @XmlElement(name = "firstname", required = false)
    public String getFirstname() {
        return _firstname;
    }
    public void setFirstname(String firstname) {
        this._firstname = firstname;
    }
    
    @XmlElement(name = "surname", required = false)
    public String getSurname() {
        return _surname;
    }
    public void setSurname(String surname) {
        this._surname = surname;
    }
    
    @XmlElement(name = "mail", required = false)
    public String getMail() {
        return _mail;
    }
    public void setMail(String mail) {
        this._mail = mail;
    }
    
    private String _firstname;
    private String _surname;
    private String _mail;
    
}
