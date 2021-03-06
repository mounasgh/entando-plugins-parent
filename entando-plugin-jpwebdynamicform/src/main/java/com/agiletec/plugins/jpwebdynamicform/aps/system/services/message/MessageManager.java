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
package com.agiletec.plugins.jpwebdynamicform.aps.system.services.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.entity.ApsEntityManager;
import com.agiletec.aps.system.common.entity.IEntityDAO;
import com.agiletec.aps.system.common.entity.IEntitySearcherDAO;
import com.agiletec.aps.system.common.entity.event.EntityTypesChangingEvent;
import com.agiletec.aps.system.common.entity.event.EntityTypesChangingObserver;
import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.attribute.ITextAttribute;
import com.agiletec.aps.system.common.renderer.IEntityRenderer;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.baseconfig.ConfigInterface;
import com.agiletec.aps.system.services.keygenerator.IKeyGeneratorManager;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.plugins.jpmail.aps.services.mail.IMailManager;
import com.agiletec.plugins.jpmail.aps.services.mail.MailSendersUtilizer;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.JpwebdynamicformSystemConstants;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.Answer;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.Message;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.MessageModel;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.MessageRecordVO;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.MessageTypeNotifierConfig;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.model.SmallMessageType;
import com.agiletec.plugins.jpwebdynamicform.aps.system.services.message.parse.MessageNotifierConfigDOM;

/**
 * Implementation of the Manager of Message Object.
 * @author E.Mezzano
 */
public class MessageManager extends ApsEntityManager implements IMessageManager, EntityTypesChangingObserver, MailSendersUtilizer {

	@Override
	public void init() throws Exception {
		super.init();
		this.initSmallMessageTypes();
		this.loadNotifierConfig();
		this.checkConfig();
		ApsSystemUtils.getLogger().config(this.getClass().getName() + ": initialized " +
				super.getEntityTypes().size() + " entity types");
	}

	/**
	 * Initialize the SmallMessageType list and map.
	 */
	protected void initSmallMessageTypes() {
		Map<String, SmallMessageType> smallMessageTypes = new HashMap<String, SmallMessageType>(this.getEntityTypes().size());
		List<IApsEntity> types = new ArrayList<IApsEntity>(this.getEntityTypes().values());
		for (int i=0; i<types.size(); i++) {
			IApsEntity type = types.get(i);
			SmallMessageType smallMessageType = new SmallMessageType();
			smallMessageType.setCode(type.getTypeCode());
			smallMessageType.setDescr(type.getTypeDescr());
			smallMessageTypes.put(smallMessageType.getCode(), smallMessageType);
		}
		this.setSmallMessageTypesMap(smallMessageTypes);
	}

	protected void loadNotifierConfig() throws ApsSystemException {
		try {
			ConfigInterface configManager = this.getConfigManager();
			String xml = configManager.getConfigItem(JpwebdynamicformSystemConstants.MESSAGE_NOTIFIER_CONFIG_ITEM);
			if (xml == null) {
				throw new ApsSystemException("Configuration item not present: " + JpwebdynamicformSystemConstants.MESSAGE_NOTIFIER_CONFIG_ITEM);
			}
			MessageNotifierConfigDOM configDOM = new MessageNotifierConfigDOM();
			this.setNotifierConfigMap(configDOM.extractConfig(xml));
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "loadNotifierConfig");
			throw new ApsSystemException("Error initializing the configuration", t);
		}
	}

	@Override
	public void updateFromEntityTypesChanging(EntityTypesChangingEvent event) {
		try {
			if (this.getName().equals(event.getEntityManagerName())) {
				switch (event.getOperationCode()) {
				case EntityTypesChangingEvent.REMOVE_OPERATION_CODE:
					String typeCode = event.getOldEntityType().getTypeCode();
					this.removeNotifierConfig(typeCode);
					ApsSystemUtils.getLogger().finest("Removed notifier configuration for entity type " + typeCode);
					break;
				case EntityTypesChangingEvent.INSERT_OPERATION_CODE:
					MessageTypeNotifierConfig config = new MessageTypeNotifierConfig();
					config.setTypeCode(event.getNewEntityType().getTypeCode());
					config.setStore(true);
					this.saveNotifierConfig(config);
					break;
				default:
					break;
				}
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateFromEntityTypesChanging");
		}
	}

	/**
	 * Returns the notifier's configuration for a given type of message.
	 * @param type The type of the message.
	 * @return The notifier's configuration for a given type of message.
	 */
	@Override
	public MessageTypeNotifierConfig getNotifierConfig(String typeCode) {
		return _messageNotifierConfigMap.get(typeCode);
	}

	@Override
	public void saveNotifierConfig(MessageTypeNotifierConfig config) throws ApsSystemException {
		try {
			MessageNotifierConfigDOM configDOM = new MessageNotifierConfigDOM();
			Map<String, MessageTypeNotifierConfig> configMap = this.getNotifierConfigMap();
			configMap.put(config.getTypeCode(), config);

			String xml = configDOM.createConfigXml(configMap);
			ConfigInterface configManager = this.getConfigManager();
			configManager.updateConfigItem(JpwebdynamicformSystemConstants.MESSAGE_NOTIFIER_CONFIG_ITEM, xml);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "saveNotifierConfig");
			this.loadNotifierConfig();
			throw new ApsSystemException("Error updating notifier configuration");
		}
	}

	protected Map<String, MessageTypeNotifierConfig> getNotifierConfigMap() {
		return _messageNotifierConfigMap;
	}

	protected void setNotifierConfigMap(Map<String, MessageTypeNotifierConfig> messageNotifierConfigMap) {
		this._messageNotifierConfigMap = messageNotifierConfigMap;
	}

	/**
	 * Remove the notifier's configuration for the given type of message.
	 * @param type The type of the message.
	 * @throws ApsSystemException
	 */
	protected void removeNotifierConfig(String typeCode) throws ApsSystemException {
		try {
			MessageNotifierConfigDOM configDOM = new MessageNotifierConfigDOM();
			Map<String, MessageTypeNotifierConfig> configMap = this.getNotifierConfigMap();
			configMap.remove(typeCode);

			String xml = configDOM.createConfigXml(configMap);
			ConfigInterface configManager = this.getConfigManager();
			configManager.updateConfigItem(JpwebdynamicformSystemConstants.MESSAGE_NOTIFIER_CONFIG_ITEM, xml);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "removeNotifierConfig");
			this.loadNotifierConfig();
			throw new ApsSystemException("Error updating notifier configuration");
		}
	}

	protected void checkConfig() {
		Map<String, MessageTypeNotifierConfig> notifierConfig = this.getNotifierConfigMap();
		for (String messageType : this.getSmallMessageTypesMap().keySet()) {
			if (!notifierConfig.containsKey(messageType)) {
				ApsSystemUtils.getLogger().log(Level.WARNING, "Message Type " + messageType + " hasn't notifier configuration!");
			}
		}
	}

	@Override
	public String[] getSenderCodes() {
		Collection<MessageTypeNotifierConfig> notifierConfigs = this.getNotifierConfigMap().values();
		String[] senderCodes = new String[notifierConfigs.size()];
		int i = 0;
		for (MessageTypeNotifierConfig config : notifierConfigs) {
			senderCodes[i++] = config.getSenderCode();
		}
		return senderCodes;
	}

	@Override
	public Message createMessageType(String typeCode) {
		Message message = (Message) super.getEntityPrototype(typeCode);
		return message;
	}

	@Override
	public List<SmallMessageType> getSmallMessageTypes() {
		List<SmallMessageType> smallMessageTypes = new ArrayList<SmallMessageType>();
		smallMessageTypes.addAll(this._smallMessageTypes.values());
		Collections.sort(smallMessageTypes);
		return smallMessageTypes;
	}

	@Override
	public Map<String, SmallMessageType> getSmallMessageTypesMap() {
		return this._smallMessageTypes;
	}
	protected void setSmallMessageTypesMap(Map<String, SmallMessageType> smallMessageTypes) {
		this._smallMessageTypes = smallMessageTypes;
	}

	@Override
	public String getMailAttributeName(String typeCode) {
		MessageTypeNotifierConfig config = this.getNotifierConfig(typeCode);
		String mailAttrName = null;
		if (config != null) {
			mailAttrName = config.getMailAttrName();
		}
		return mailAttrName;
	}

	@Override
	public List<String> loadMessagesId(EntitySearchFilter[] filters) throws ApsSystemException {
		List<String> contentsId = null;
		try {
			contentsId = this.getEntitySearcherDao().searchId(filters);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "loadMessagesId");
			throw new ApsSystemException("Errore caricamento id message", t);
		}
		return contentsId;
	}

	@Override
	public List<String> loadMessagesId(EntitySearchFilter[] filters, boolean answered) throws ApsSystemException {
		List<String> contentsId = null;
		try {
			contentsId = ((IMessageSearcherDAO) this.getEntitySearcherDao()).searchId(filters, answered);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "loadMessagesId");
			throw new ApsSystemException("Errore caricamento id message", t);
		}
		return contentsId;
	}

	@Override
	public Message getMessage(String id) throws ApsSystemException {
		Message message = null;
		try {
			MessageRecordVO messageRecord = (MessageRecordVO) this.getMessageDAO().loadEntityRecord(id);
			if (messageRecord != null) {
				message = (Message) createEntityFromXml(messageRecord.getTypeCode(), messageRecord.getXml());
				message.setUsername(messageRecord.getUsername());
				message.setCreationDate(messageRecord.getCreationDate());
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "getMessage");
			throw new ApsSystemException("Error loading messageRecord", t);
		}
		return message;
	}

	@Override
	public void addMessage(Message message) throws ApsSystemException {
		try {
			MessageTypeNotifierConfig config = this.getNotifierConfig(message.getTypeCode());
			if (config == null || config.isStore()) {
				int key = this.getKeyGeneratorManager().getUniqueKeyCurrentValue();
				String id = message.getTypeCode() + key;
				message.setId(id);
				this.getMessageDAO().addEntity(message);
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addMessage");
			throw new ApsSystemException("Error saving message", t);
		}
	}

	@Override
	public void sendMessage(Message message) throws ApsSystemException {
		try {
			this.sendMessageNotification(message);
			this.addMessage(message);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addMessage");
			throw new ApsSystemException("Error saving message", t);
		}
	}

	@Override
	public void deleteMessage(String messageId) throws ApsSystemException {
		try {
			this.getMessageDAO().deleteEntity(messageId);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "deleteMessage");
			throw new ApsSystemException("Error deleting message " + messageId, t);
		}
	}

	@Override
	public boolean sendAnswer(Answer answer) throws ApsSystemException {
		try {
			int key = this.getKeyGeneratorManager().getUniqueKeyCurrentValue();
			answer.setAnswerId(String.valueOf(key));
			this.getMessageDAO().addAnswer(answer);
			boolean sent = this.sendAnswerNotification(answer);
			return sent;
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addAnswer");
			throw new ApsSystemException("Error saving answer", t);
		}
	}

	@Override
	public List<Answer> getAnswers(String messageId) throws ApsSystemException {
		try {
			return this.getMessageDAO().loadAnswers(messageId);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addMessage");
			throw new ApsSystemException("Error saving message", t);
		}
	}

	protected boolean sendMessageNotification(Message message) throws ApsSystemException {
		boolean sent = false;
		try {
			MessageTypeNotifierConfig config = this.getNotifierConfig(message.getTypeCode());
			if (config != null && config.isNotifiable()) {
				MessageModel messageModel = config.getMessageModel();
				String renderingLangCode = this.getLangManager().getDefaultLang().getCode();
				String subject = this.getEntityRenderer().render(message, messageModel.getSubjectModel(), renderingLangCode, false);
				String text = this.getEntityRenderer().render(message, messageModel.getBodyModel(), renderingLangCode, true);

				String[] recipientsTo = config.getRecipientsTo();
				String[] recipientsCc = config.getRecipientsCc();
				String[] recipientsBcc = config.getRecipientsBcc();
				String senderCode = config.getSenderCode();
				this.getMailManager().sendMail(text, subject,
						recipientsTo, recipientsCc, recipientsBcc, senderCode, IMailManager.CONTENTTYPE_TEXT_HTML);
				sent = true;
			} else {
				ApsSystemUtils.getLogger().log(Level.WARNING, "Message notification not sent! Message lacking in notifier configuration.");
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "sendMessageNotification");
			throw new ApsSystemException("Error sending notification to message " + message.getId(), t);
		}
		return sent;
	}

	protected boolean sendAnswerNotification(Answer answer) throws ApsSystemException {
		boolean sent = false;
		try {
			Message message = this.getMessage(answer.getMessageId());
			MessageTypeNotifierConfig config = this.getNotifierConfig(message.getTypeCode());
			if (config != null) {
				String email = this.extractUserMail(message, config);
				if (null!=email) {
					String renderingLangCode = message.getLangCode();
					if (renderingLangCode==null || this.getLangManager().getLang(renderingLangCode)==null) {
						renderingLangCode = this.getLangManager().getDefaultLang().getCode();
					}
					MessageModel messageModel = config.getMessageModel();
					String subject = this.getEntityRenderer().render(message, messageModel.getSubjectModel(), renderingLangCode, false);
					subject = "RE: " + subject;
					String text = answer.getText();
					String senderCode = config.getSenderCode();
					String[] recipientsTo = new String[] { email };
					Properties attachmentFiles = answer.getAttachments();
					this.getMailManager().sendMail(text, subject, IMailManager.CONTENTTYPE_TEXT_PLAIN,
							attachmentFiles, recipientsTo, null, null, senderCode);
					sent = true;
				} else {
					ApsSystemUtils.getLogger().log(Level.WARNING, "ATTENTION: email Attribute \"" +
							config.getMailAttrName() + "\" for Message \"" + message.getId() +
							"\" isn't valued!!\nCheck \"jpwebdynamicform_messageTypes\" Configuration or " +
							"\"" + JpwebdynamicformSystemConstants.MESSAGE_NOTIFIER_CONFIG_ITEM + "\" Configuration");
				}
			} else {
				ApsSystemUtils.getLogger().log(Level.WARNING, "Answer not sent! Message lacking in notifier configuration.");
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "sendAnswerNotification");
			// Do not launch any exception
//			throw new ApsSystemException("Error sending notification for answer " + answer.getAnswerId(), t);
		}
		return sent;
	}

	protected String extractUserMail(Message message, MessageTypeNotifierConfig config) {
		String email = null;
		if (null!=config) {
			String mailAttrName = config.getMailAttrName();
			if (null!=mailAttrName && mailAttrName.length()>0) {
				ITextAttribute mailAttribute = (ITextAttribute) message.getAttribute(mailAttrName);
				if (mailAttribute!=null) {
					email = mailAttribute.getText();
				}
			}
		}
		return email;
	}

	@Override
	public IApsEntity getEntity(String entityId) throws ApsSystemException {
		return this.getMessage(entityId);
	}

	@Override
	protected IEntityDAO getEntityDao() {
		return (IEntityDAO) this._messageDAO;
	}

	/**
	 * Returns the MessageDAO.
	 * @return The MessageDAO.
	 */
	protected IMessageDAO getMessageDAO() {
		return _messageDAO;
	}

	/**
	 * Sets the MessageDAO. Must be used with Spring bean injection.
	 * @param messageDAO The MessageDAO.
	 */
	public void setMessageDAO(IMessageDAO messageDAO) {
		this._messageDAO = messageDAO;
	}

	@Override
	protected IEntitySearcherDAO getEntitySearcherDao() {
		return _entitySearcherDAO;
	}

	/**
	 * Sets the EntitySearcherDAO implementation for Message service. Must be used with Spring bean injection.
	 * @param entitySearcherDAO The EntitySearcherDAO implementation for Message service.
	 */
	public void setEntitySearcherDAO(IEntitySearcherDAO entitySearcherDAO) {
		this._entitySearcherDAO = entitySearcherDAO;
	}

	/**
	 * Returns the KeyGeneratorManager.
	 * @return The KeyGeneratorManager.
	 */
	protected IKeyGeneratorManager getKeyGeneratorManager() {
		return _keyGeneratorManager;
	}

	/**
	 * Sets the KeyGeneratorManager. Must be used with Spring bean injection.
	 * @param keyGeneratorManager The KeyGeneratorManager.
	 */
	public void setKeyGeneratorManager(IKeyGeneratorManager keyGeneratorManager) {
		this._keyGeneratorManager = keyGeneratorManager;
	}

	/**
	 * Returns the configuration manager.
	 * @return The Configuration manager.
	 */
	protected ConfigInterface getConfigManager() {
		return _configManager;
	}

	/**
	 * Set method for Spring bean injection.<br /> Set the Configuration manager.
	 * @param configManager The Configuration manager.
	 */
	public void setConfigManager(ConfigInterface configManager) {
		this._configManager = configManager;
	}

	protected ILangManager getLangManager() {
		return _langManager;
	}
	public void setLangManager(ILangManager langManager) {
		this._langManager = langManager;
	}

	protected IEntityRenderer getEntityRenderer() {
		return _entityRenderer;
	}
	public void setEntityRenderer(IEntityRenderer entityRenderer) {
		this._entityRenderer = entityRenderer;
	}

	protected IMailManager getMailManager() {
		return _mailManager;
	}
	public void setMailManager(IMailManager mailManager) {
		this._mailManager = mailManager;
	}

	/**
	 * Mappa dei prototipi dei tipi di contenuti il forma Small,
	 * indicizzati in base al codice del tipo.
	 */
	private Map<String, SmallMessageType> _smallMessageTypes;
	/**
	 * A map containing the configuration of the notifier for each message type, indentified by the type code.
	 */
	private Map<String, MessageTypeNotifierConfig> _messageNotifierConfigMap;

	private IMessageDAO _messageDAO;
	private IEntitySearcherDAO _entitySearcherDAO;
	private IKeyGeneratorManager _keyGeneratorManager;
	private ConfigInterface _configManager;

	private ILangManager _langManager;
	private IEntityRenderer _entityRenderer;
	private IMailManager _mailManager;

}