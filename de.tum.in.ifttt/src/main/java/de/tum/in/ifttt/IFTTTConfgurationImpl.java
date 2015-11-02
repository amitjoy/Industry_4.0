/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.ifttt;

import java.util.List;
import java.util.Map;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * This is used to configure IFTTT Email Channel
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.ifttt")
@Service(value = { IFTTTConfiguration.class })
public class IFTTTConfgurationImpl implements ConfigurableComponent, IFTTTConfiguration {

	/**
	 * Configurable property to set different email IFTTT hashtags
	 */
	private static final String IFTTT_EMAIL_HASHTAGS = "ifttt.email.hashtags";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IFTTTConfgurationImpl.class);

	/**
	 * Configurable Property to set SMTP Server Address
	 */
	private static final String SMTP_HOST = "smtp.host";

	/**
	 * Configurable Property to set SMTP Server Password
	 */
	private static final String SMTP_PASSWORD = "smtp.password";

	/**
	 * Configurable Property to set SMTP Server Port
	 */
	private static final String SMTP_PORT = "smtp.port";

	/**
	 * Configurable Property to set SMTP Server Username
	 */
	private static final String SMTP_USERNAME = "smtp.username";

	/**
	 * Placeholder for hashtags
	 */
	private String m_hashTags;
	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for SMTP Host Address
	 */
	private String m_smtpHost;

	/**
	 * Placeholder for SMTP Password
	 */
	private String m_smtpPassword;

	/**
	 * Placeholder for SMTP Port
	 */
	private int m_smtpPort;

	/**
	 * Placeholder for SMTP Username
	 */
	private String m_smtpUsername;

	/* Constructor */
	public IFTTTConfgurationImpl() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating IFTTT Component...");

		this.m_properties = properties;
		this.extractConfiguration();

		LOGGER.info("Activating IFTTT Component... Done.");

	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating IFTTT Component...");

		LOGGER.debug("Deactivating IFTTT Component... Done.");
	}

	/**
	 * Used to extract configuration for populating placeholders with respective
	 * values
	 */
	private void extractConfiguration() {
		this.m_hashTags = (String) this.m_properties.get(IFTTT_EMAIL_HASHTAGS);
		this.m_smtpUsername = (String) this.m_properties.get(SMTP_USERNAME);
		this.m_smtpPassword = (String) this.m_properties.get(SMTP_PASSWORD);
		this.m_smtpPort = (Integer) this.m_properties.get(SMTP_PORT);
		this.m_smtpHost = (String) this.m_properties.get(SMTP_HOST);
	}

	/**
	 * Used to retrieve set of hashtags
	 */
	private List<String> retrieveHashtags(final String hashTags) {
		final List<String> tags = Lists.newArrayList();
		final String TAG_SPLITTER = " ";
		Iterators.addAll(tags, Splitter.on(TAG_SPLITTER).split(hashTags).iterator());
		return tags;
	}

	/** {@inheritDoc} */
	@Override
	public void trigger() {
		LOGGER.debug("IFTTT Email is getting sent...");

		final List<String> tags = this.retrieveHashtags(this.m_hashTags);

		if (tags.size() == 0) {
			return;
		}

		if (tags.size() > 0) {
			for (final String tag : tags) {
				try {
					final Email email = new SimpleEmail();
					email.setHostName(this.m_smtpHost);
					email.setSmtpPort(this.m_smtpPort);
					email.setAuthenticator(new DefaultAuthenticator(this.m_smtpUsername, this.m_smtpPassword));
					email.setSSL(true);
					email.setFrom(this.m_smtpUsername);
					email.setSubject(tag);
					email.setMsg("This is a test mail ... :-)");
					email.addTo(TRIGGER_EMAIL);
					email.send();
				} catch (final EmailException e) {
					LOGGER.error(Throwables.getStackTraceAsString(e));
				}
			}
		}
		LOGGER.debug("IFTTT Email is sent...Done");
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating IFTTT Component...");

		this.m_properties = properties;
		this.extractConfiguration();
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updating IFTTT Component... Done.");
	}

}