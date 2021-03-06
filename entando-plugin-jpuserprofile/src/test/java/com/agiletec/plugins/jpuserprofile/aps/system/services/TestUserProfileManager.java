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
package com.agiletec.plugins.jpuserprofile.aps.system.services;

import com.agiletec.aps.system.common.entity.model.ApsEntityRecord;
import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import java.util.Calendar;
import java.util.Date;

import com.agiletec.plugins.jpuserprofile.aps.ApsPluginBaseTestCase;

import com.agiletec.aps.system.common.entity.model.attribute.DateAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.MonoTextAttribute;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jpuserprofile.aps.system.services.ProfileSystemConstants;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.IUserProfileManager;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.IUserProfile;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.UserProfile;
import com.agiletec.plugins.jpuserprofile.aps.system.services.profile.model.UserProfileRecord;
import java.util.List;

/**
 * @author E.Santoboni
 */
public class TestUserProfileManager extends ApsPluginBaseTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.init();
	}
	
	public void testInitialize() {
		assertNotNull(this._profileManager);
	}
	
	public void testAttributeSupportObjects() throws Throwable {
		assertTrue(this._profileManager.getAttributeRoles().size()>=3);
		assertTrue(this._profileManager.getAttributeDisablingCodes().size()>=1);
	}
	
	public void testAddProfile() throws Throwable {
		String username = "admin";
		Date birthdate = this.getBirthdate(1982, 10, 25);
		IUserProfile profile = this.createProfile("stefano", "puddu", "spuddu@agiletec.it", birthdate, "it");
		try {
			this._profileManager.addProfile("admin", profile);
			IUserProfile added = this._profileManager.getProfile(username);
			assertEquals("spuddu@agiletec.it", added.getValue("email"));
			assertEquals(username, added.getUsername());
			MonoTextAttribute emailAttr = (MonoTextAttribute) ((IUserProfile) profile).getAttribute("email");
			emailAttr.setText("agiletectest@gmail.com");
			this._profileManager.updateProfile(profile.getUsername(), profile);
			IUserProfile updated = this._profileManager.getProfile(username);
			assertEquals("agiletectest@gmail.com", updated.getValue("email"));
			assertEquals(username, added.getUsername());
		} catch (Throwable t) {
			throw t;
		} finally {
			this._profileManager.deleteProfile(username);
			assertNull(this._profileManager.getProfile(username));
		}
	}
	
	private IUserProfile createProfile(String name,	String surname, String email, Date birthdate, String language) {
		IUserProfile profile = this._profileManager.getDefaultProfileType();
		MonoTextAttribute nameAttr = (MonoTextAttribute) profile.getAttribute("Name");
		nameAttr.setText(name);
		MonoTextAttribute surnameAttr = (MonoTextAttribute) profile.getAttribute("Surname");
		surnameAttr.setText(surname);
		MonoTextAttribute emailAttr = (MonoTextAttribute) profile.getAttribute("email");
		emailAttr.setText(email);
		DateAttribute birthdateAttr = (DateAttribute) profile.getAttribute("birthdate");
		birthdateAttr.setDate(birthdate);
		MonoTextAttribute languageAttr = (MonoTextAttribute) profile.getAttribute("language");
		languageAttr.setText(language);
		((UserProfile) profile).setPublicProfile(true);
		return profile;
	}
	
	private Date getBirthdate(int year, int month, int day){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		Date birthdate = new Date(calendar.getTimeInMillis());
		return birthdate;
	}
	
	public void testRemoveInsesistentUser() throws ApsSystemException {
		assertNull(this._profileManager.getProfile("pippo"));
		this._profileManager.deleteProfile("pippo");
	}
	
	public void testSearchProfiles() throws Throwable {
		List<String> usernames = this._profileManager.searchId(null);
		assertNotNull(usernames);
    	assertEquals(4, usernames.size());
		
		EntitySearchFilter usernameFilter1 = new EntitySearchFilter(IUserProfileManager.ENTITY_ID_FILTER_KEY, false);
    	usernameFilter1.setOrder(EntitySearchFilter.ASC_ORDER);
		EntitySearchFilter[] filters1 = {usernameFilter1};
		usernames = this._profileManager.searchId(filters1);
		assertNotNull(usernames);
    	String[] expected1 = {"editorCoach", "editorCustomers", "mainEditor", "pageManagerCoach"};
    	assertEquals(expected1.length, usernames.size());
    	this.verifyOrder(usernames, expected1);
		
		EntitySearchFilter usernameFilter2 = new EntitySearchFilter(IUserProfileManager.ENTITY_ID_FILTER_KEY, false, "oa", true);
    	usernameFilter2.setOrder(EntitySearchFilter.ASC_ORDER);
		EntitySearchFilter[] filters2 = {usernameFilter2};
		usernames = this._profileManager.searchId(filters2);
		assertNotNull(usernames);
    	String[] expected2 = {"editorCoach", "pageManagerCoach"};
    	assertEquals(expected2.length, usernames.size());
    	this.verifyOrder(usernames, expected2);
		
		EntitySearchFilter surnameFilter = new EntitySearchFilter("Surname", true, "on", true);
		EntitySearchFilter[] filters3 = {usernameFilter1, surnameFilter};
		usernames = this._profileManager.searchId(filters3);
		assertNotNull(usernames);
    	String[] expected3 = {"editorCoach", "pageManagerCoach"};
    	assertEquals(expected3.length, usernames.size());
    	this.verifyOrder(usernames, expected3);
	}
	
	public void testSearchProfileRecords() throws Throwable {
		List<ApsEntityRecord> records = this._profileManager.searchRecords(null);
		assertNotNull(records);
    	assertEquals(4, records.size());
		
		EntitySearchFilter usernameFilter1 = new EntitySearchFilter(IUserProfileManager.ENTITY_ID_FILTER_KEY, false);
    	usernameFilter1.setOrder(EntitySearchFilter.ASC_ORDER);
		EntitySearchFilter[] filters1 = {usernameFilter1};
		records = this._profileManager.searchRecords(filters1);
		assertNotNull(records);
    	String[] expected1 = {"editorCoach", "editorCustomers", "mainEditor", "pageManagerCoach"};
    	assertEquals(expected1.length, records.size());
    	this.verifyRecordOrder(records, expected1);
		
		EntitySearchFilter usernameFilter2 = new EntitySearchFilter(IUserProfileManager.ENTITY_ID_FILTER_KEY, false, "oa", true);
    	usernameFilter2.setOrder(EntitySearchFilter.ASC_ORDER);
		EntitySearchFilter[] filters2 = {usernameFilter2};
		records = this._profileManager.searchRecords(filters2);
		assertNotNull(records);
    	String[] expected2 = {"editorCoach", "pageManagerCoach"};
    	assertEquals(expected2.length, records.size());
    	this.verifyRecordOrder(records, expected2);
		
		EntitySearchFilter surnameFilter = new EntitySearchFilter("Surname", true, "on", true);
		EntitySearchFilter[] filters3 = {usernameFilter1, surnameFilter};
		records = this._profileManager.searchRecords(filters3);
		assertNotNull(records);
    	String[] expected3 = {"editorCoach", "pageManagerCoach"};
    	assertEquals(expected3.length, records.size());
    	this.verifyRecordOrder(records, expected3);
	}
	
    private void verifyOrder(List<String> usernames, String[] order) {
    	for (int i=0; i<usernames.size(); i++) {
    		assertEquals(order[i], usernames.get(i));
    	}
	}
    
    private void verifyRecordOrder(List<ApsEntityRecord> records, String[] order) {
    	for (int i=0; i<records.size(); i++) {
			ApsEntityRecord record = records.get(i);
    		assertEquals(order[i], record.getId());
    	}
	}
    
	private void init() throws Exception {
    	try {
    		this._profileManager = (IUserProfileManager) this.getService(ProfileSystemConstants.USER_PROFILE_MANAGER);
		} catch (Exception e) {
			throw e;
		}
    }
	
	private IUserProfileManager _profileManager;
	
}
