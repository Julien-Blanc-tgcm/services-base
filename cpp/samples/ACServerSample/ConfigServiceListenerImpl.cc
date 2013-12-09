/******************************************************************************
 * Copyright (c) 2013, AllSeen Alliance. All rights reserved.
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

#include "ConfigServiceListenerImpl.h"
#include <IniParser.h>

using namespace ajn;
using namespace services;

ConfigServiceListenerImpl::ConfigServiceListenerImpl(PropertyStoreImpl& store, BusAttachment& bus,
                                                     OnboardingControllerImpl& obController) :
    ConfigService::Listener(), m_PropertyStore(&store), m_Bus(&bus), m_OnboardingController(&obController)
{
}

QStatus ConfigServiceListenerImpl::Restart()
{
    printf("Restart has been called !!!\n");
    return ER_OK;
}

QStatus ConfigServiceListenerImpl::FactoryReset()
{
    printf("FactoryReset has been called!!!\n");
    m_PropertyStore->FactoryReset();
    printf("Clearing Key Store\n");
    m_Bus->ClearKeyStore();
    printf("Calling Offboard\n");
    m_OnboardingController->Offboard();
    return ER_OK;
}

QStatus ConfigServiceListenerImpl::SetPassphrase(const char* daemonRealm, size_t passcodeSize, const char* passcode)
{
    qcc::String passCodeString(passcode, passcodeSize);
    printf("SetPassphrase has been called daemonRealm=%s passcode=%s passcodeLength=%lu\n", daemonRealm,
           passCodeString.c_str(), passcodeSize);
    PersistPassword(daemonRealm, passCodeString.c_str());

    printf("Clearing Key Store\n");
    m_Bus->ClearKeyStore();

    return ER_OK;
}

ConfigServiceListenerImpl::~ConfigServiceListenerImpl()
{
}

void ConfigServiceListenerImpl::PersistPassword(const char* daemonRealm, const char* passcode)
{
    std::map<std::string, std::string> data;
    data["daemonrealm"] = daemonRealm;
    data["passcode"] = passcode;
    IniParser::UpdateFile(m_PropertyStore->GetConfigFile().c_str(), data);
}
