/******************************************************************************
 *  * 
 *    Copyright (c) 2016 Open Connectivity Foundation and AllJoyn Open
 *    Source Project Contributors and others.
 *    
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0

 ******************************************************************************/

package org.alljoyn.services.android.storage;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alljoyn.bus.AboutKeys;
import org.alljoyn.bus.AboutDataListener;
import org.alljoyn.bus.ErrorReplyBusException;
import org.alljoyn.bus.Variant;
import org.alljoyn.services.common.PropertyStore;
import org.alljoyn.services.common.PropertyStoreException;
import org.alljoyn.config.ConfigDataStore;

import android.content.Context;

/**
 * A default implementation of the ConfigDataStore (which uses deprecated
 * PropertyStore under the hood.)
 * This should be replaced with the alternative API's once they become available.
 * See ASABASE-397 and ASABASE-504 for more information.
 */
public class ConfigDataStoreImpl extends AboutDataImpl implements ConfigDataStore
{
    public static final String TAG = ConfigDataStoreImpl.class.getName();

    public ConfigDataStoreImpl(Context context)
    {
        super(context);
    }

    /**
     * Read all the properties for a given language, filtered by a criteria
     *
     * @param languageTag
     *            the language in which to retrieve the properties
     * @param filter
     *            filter the results by critreria: for announcement, for
     *            configuration, etc.
     * @param dataMap
     *            a map to fill with the result (to be compatible with the C++
     *            signature)
     * @throws PropertyStoreException
     *             if an unsupported language was given
     */
    @Override
    public void readAll(String languageTag, Filter filter, Map<String, Object> dataMap) throws PropertyStoreException
    {
        if (!Property.NO_LANGUAGE.equals(languageTag) && !getLanguages().contains(languageTag))
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_LANGUAGE);
        }
        switch (filter)
        {
            case ANNOUNCE:
                getAnnouncement(languageTag, dataMap);
                break;
            case READ:
                getAbout(languageTag, dataMap);
                break;
            case WRITE:
                getConfiguration(languageTag, dataMap);
                break;
        }
    }

    /**
     * Update a property value
     *
     * @param key
     *            the property name
     * @param languageTag
     *            the language in which the value should be updated
     * @param newValue
     *            the neew value
     * @throws PropertyStoreException
     *             for the cases: UNSUPPORTED_LANGUAGE, INVALID_VALUE,
     *             UNSUPPORTED_KEY, ILLEGAL_ACCESS
     */
    @Override
    public void update(String key, String languageTag, Object newValue) throws PropertyStoreException
    {

        Property property = getAboutConfigMap().get(key);
        if (property == null)
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_KEY);
        }
        if (!property.isWritable())
        {
            throw new PropertyStoreException(PropertyStoreException.ILLEGAL_ACCESS);
        }

        if (AboutKeys.ABOUT_DEFAULT_LANGUAGE.equals(key) && !getLanguages().contains(newValue.toString()))
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_LANGUAGE);
        }

        languageTag = validateLanguageTag(languageTag, property);
        property.setValue(languageTag, newValue);

        setDefaultLanguageFromProperties();
        // save config map to persistent storage
        storeConfiguration();
    }

    /**
     * Reset the property value for a given language
     *
     * @param key
     *            the property key
     * @param languageTag
     *            the language in which to reset
     * @throws PropertyStoreException
     */
    @Override
    public void reset(String key, String languageTag) throws PropertyStoreException
    {
        if (!Property.NO_LANGUAGE.equals(languageTag) && !getLanguages().contains(languageTag))
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_LANGUAGE);
        }

        Property property = getAboutConfigMap().get(key);
        if (property == null)
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_KEY);
        }

        languageTag = validateLanguageTag(languageTag, property);
        property.remove(languageTag);

        // save config map to persistent storage
        storeConfiguration();
        loadFactoryDefaults();
        loadStoredConfiguration();

        // since the default language may be reset
        if (AboutKeys.ABOUT_DEFAULT_LANGUAGE.equals(key))
        {
            setDefaultLanguageFromProperties();
        }
    }

    /**
     * Reset all the properties in the store
     *
     * @throws PropertyStoreException
     */
    @Override
    public void resetAll() throws PropertyStoreException
    {
        // delete cache
        Property appId = getAboutConfigMap().get(AboutKeys.ABOUT_APP_ID);
        getAboutConfigMap().clear();
        // delete persistent storage
        getContext().deleteFile(CONFIG_XML);
        // load factory defaults
        loadFactoryDefaults();
        // TODO restart as a soft AP...
        getAboutConfigMap().put(AboutKeys.ABOUT_APP_ID, appId);
        loadLanguages();
    }

    /**
     * Checks if received languageTag is not {@link Property#NO_LANGUAGE} and
     * exists among supported languages, otherwise
     * {@link PropertyStoreException#UNSUPPORTED_LANGUAGE} is thrown. If the
     * received languageTag is {@link Property#NO_LANGUAGE} then it will be set
     * to the default language. If languages attribute of the received property
     * has only one language and it's set to {@link Property#NO_LANGUAGE}, then
     * returned languageTag will be set to {@link Property#NO_LANGUAGE}.
     *
     * @param languageTag
     *            The language tag to be validates.
     * @param property
     *            The {@link Property} that the language tag is validated for.
     * @return The language tag to be used.
     * @throws PropertyStoreException
     *             of {@link PropertyStoreException#UNSUPPORTED_LANGUAGE}
     */
    private String validateLanguageTag(String languageTag, Property property) throws PropertyStoreException
    {

        if (!Property.NO_LANGUAGE.equals(languageTag) && !getLanguages().contains(languageTag))
        {
            throw new PropertyStoreException(PropertyStoreException.UNSUPPORTED_LANGUAGE);
        }

        Set<String> langs = property.getLanguages();

        // If languageTag equals NO_LANGUAGE, set it to be defaultLanguage
        if (Property.NO_LANGUAGE.equals(languageTag))
        {
            languageTag = getDefaultLanguage();
        }

        // In case the field has only one language and it equals NO_LANGUAGE,
        // there
        // will be no possibility to set another value with a different language
        // but NO_LANGUAGE.
        if (langs != null && langs.size() == 1)
        {

            Iterator<String> iterator = langs.iterator();
            String lang = iterator.next();

            if (Property.NO_LANGUAGE.equals(lang))
            {

                // Override the original language tag with the NO_LANGUAGE
                languageTag = Property.NO_LANGUAGE;
            }
        }

        return languageTag;
    }
}