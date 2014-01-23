/******************************************************************************
 * Copyright (c) 2013 - 2014, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

#ifndef CONFIGSERVICELISTENERIMPL_H_
#define CONFIGSERVICELISTENERIMPL_H_

#include <alljoyn/config/ConfigService.h>
#include <PropertyStoreImpl.h>
#include <CommonBusListener.h>

//forward declaration
class OnboardingControllerImpl;

class ConfigServiceListenerImpl : public ajn::services::ConfigService::Listener {
  public:

    ConfigServiceListenerImpl(PropertyStoreImpl& store, ajn::BusAttachment& bus, CommonBusListener& busListener, OnboardingControllerImpl* obController);

    virtual QStatus Restart();

    virtual QStatus FactoryReset();

    virtual QStatus SetPassphrase(const char* daemonRealm, size_t passcodeSize, const char* passcode, ajn::SessionId sessionId);

    virtual ~ConfigServiceListenerImpl();

  private:

    PropertyStoreImpl* m_PropertyStore;

    ajn::BusAttachment* m_Bus;

    CommonBusListener* m_BusListener;

    OnboardingControllerImpl* m_OnboardingController;

    void PersistPassword(const char* daemonRealm, const char* passcode);
};

#endif /* CONFIGSERVICELISTENERIMPL_H_ */
