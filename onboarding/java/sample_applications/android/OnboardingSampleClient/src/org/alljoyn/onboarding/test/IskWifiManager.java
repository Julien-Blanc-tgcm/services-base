/******************************************************************************
 * Copyright (c) 2016 Open Connectivity Foundation (OCF) and AllJoyn Open
 * Source Project (AJOSP) Contributors and others.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Copyright 2016 Open Connectivity Foundation and Contributors to
 * AllSeen Alliance. All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all
 * copies.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *  WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *  AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *  DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *  PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *  TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package org.alljoyn.onboarding.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.alljoyn.onboarding.OnboardingService.AuthType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

/**
 *
 */
public class IskWifiManager {
    public static final String WIFI_AUTHENTICATION_ERROR = "org.alljoyn.onboarding.WIFI_AUTHENTICATION_ERROR";
    public static final String WIFI_TIMEOUT_ERROR = "org.alljoyn.onboarding.WIFI_TIMEOUT_ERROR";
    public static final String WIFI_CONNECTED = "org.alljoyn.onboarding.WIFI_CONNECTED";
    public static final String MARSHMALLOW_SOFTAP_CONNECT = "org.alljoyn.onboarding.MARSHMALLOW_SOFTAP_CONNECT";
    public static final String OP_FAILED_WIFI_DISABLED = "org.alljoyn.onboarding.OP_FAILED_WIFI_DISABLED";
    //WEP HEX password pattern.
    static final String WEP_HEX_PATTERN = "[\\dA-Fa-f]+";
    private static final boolean lollipop = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    private static final boolean marshmallow = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static final String TAG = "IskWifiManager";
    //======================================================================
    private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();
    final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
    WifiConfiguration mCurrentlyConnectingConfiguration = null;
    // Stores the details of the target Wi-Fi. Uses volatile to verify that the
    // broadcast receiver reads its content each time onReceive is called, thus
    // "knowing" if the an API call to connect has been made
    private volatile WifiConfiguration targetWifiConfiguration = null;
    // Timer for checking completion of Wi-Fi tasks.
    private Timer wifiTimeoutTimer = new Timer();
    private Context m_context;
    //  Android Wi-Fi manager
    private WifiManager m_wifi;
    // Listener for events generated by this class
    private WifiManagerListener m_listener;
    // Receiver for Wi-Fi intents
    private BroadcastReceiver m_scanner;

    //================================================================

    //================================================================
    public IskWifiManager(Context context) {
        m_context = context;
        m_wifi = (WifiManager)m_context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * @return compare result of two SSID strings.
     */
    public static boolean isSsidEquals(String ssid1, String ssid2) {
        if (ssid1 == null || ssid1.length() == 0 || ssid2 == null || ssid2.length() == 0) {
            return false;
        }
        if (ssid1.startsWith("\"")) {
            ssid1 = ssid1.replace("\"", "");
        }
        if (ssid2.startsWith("\"")) {
            ssid2 = ssid2.replace("\"", "");
        }
        return ssid1.equals(ssid2);
    }
    //================================================================

    /**
     * A utility method for removing wrapping quotes from SSID name. Some
     * Android devices return an SSID surrounded with quotes. For the sake of
     * comparison and readability, remove those.
     *
     * @param ssid could be AJ_QA but also "AJ_QA" (with quotes).
     * @return normalized SSID: AJ_QA
     */
    static String normalizeSSID(String ssid) {
        if (ssid != null && ssid.length() > 0 && ssid.startsWith("\"")) {
            ssid = ssid.replace("\"", "");
        }
        return ssid;
    }

    //================================================================

    /**
     * A wrapper function to send broadcast intent messages.
     *
     * @param action the name of the action
     * @param extras contains extra information bundled with the intent
     */
    private void sendBroadcast(String action, Bundle extras) {
        Intent intent = new Intent(action);
        if (extras != null && !extras.isEmpty()) {
            intent.putExtras(extras);
        }
        m_context.sendBroadcast(intent);
    }

    //================================================================

    /**
     * Kick off a Wi-Fi scan, after registering for Wi-Fi intents
     *
     * @param ctx            context
     * @param listener       listener to events generated by this class
     * @param AJlookupPrefix filter for discovered devices
     */
    public void scanForWifi(Context ctx, WifiManagerListener listener, final String AJLookupPrefix, final String AJLookupPostfix) {
        m_listener = listener;
        m_context = ctx;
        m_wifi = (WifiManager)m_context.getSystemService(Context.WIFI_SERVICE);

        // listen to Wi-Fi intents
        m_scanner = new BroadcastReceiver() {

            // will get here after scan
            @Override
            public void onReceive(Context context, Intent intent) {
                // Only update the list with the WifiManager results if wifi is enabled.
                if (m_wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                    m_listener.OnScanResultComplete(new ArrayList<ScanResult>());
                    return;
                }

                // === Current scans ===
                List<ScanResult> scans = m_wifi.getScanResults();

                // remove duplicate SSID with different BSSID ,
                if (scans != null) {

                    // keep one item per SSID, the one with the strongest signal
                    HashMap<String, ScanResult> alljoynSoftAPs = new HashMap<String, ScanResult>();
                    for (ScanResult currentScan : scans) {
                        if (currentScan.SSID.startsWith(AJLookupPrefix) || currentScan.SSID.endsWith(AJLookupPostfix)) {
                            ScanResult l = alljoynSoftAPs.get(currentScan.SSID);
                            if (l == null) {
                                alljoynSoftAPs.put(currentScan.SSID, currentScan);
                            } else {
                                if (l.level < currentScan.level) {
                                    alljoynSoftAPs.put(currentScan.SSID, currentScan);
                                }
                            }
                        }
                    }

                    // sort list by level of Wi-Fi signal
                    List<ScanResult> list = new ArrayList<ScanResult>(alljoynSoftAPs.values());
                    Collections.sort(list, new Comparator<ScanResult>() {
                        public int compare(ScanResult o1, ScanResult o2) {
                            if (o1.level > o2.level) {
                                return -1;
                            } else if (o1.level < o2.level) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    // listener callback
                    m_listener.OnScanResultComplete(list);
                }
            }
        };

        // register for Wi-Fi intents that will be generated by the scanning process
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        m_context.registerReceiver(m_scanner, filter);

        int currentWifiState = m_wifi.getWifiState();
        if (currentWifiState == WifiManager.WIFI_STATE_ENABLED || currentWifiState == WifiManager.WIFI_STATE_ENABLING) {
            // start a scan
            m_wifi.startScan();
        } else {
            m_listener.OnScanResultComplete(new ArrayList<ScanResult>());
            sendBroadcast(OP_FAILED_WIFI_DISABLED, null);
        }
    }

    //================================================================

    public void unregisterWifiManager() {
        if (m_scanner != null) {
            try {
                if (m_context != null) {
                    m_context.unregisterReceiver(m_scanner);
                }
            } catch (Exception ignored) {
            }
        }
    }

    //================================================================

    /**
     * Connect to a a Wi-Fi Access Point
     *
     * @param networkName  SSID of the network
     * @param passkey      Wi-Fi password
     * @param capabilities the Wi-Fi capabilities string: supported authentication
     */
    public void connectToAP(String networkName, String passkey, String capabilities) {
        Log.i(TAG, "* connectToAP with capabilities");
        AuthType securityMode = getScanResultSecurity(capabilities);
        connectToAP(networkName, passkey, securityMode);
    }

    /**
     * Map a capabilities string from Android's scan to AllJoyn Onboarding service AuthType enum
     *
     * @param capabilities the AP's capabilities screen. Authentication
     * @return AuthType
     * @see AuthType
     */
    public AuthType getScanResultSecurity(String capabilities) {
        Log.i(TAG, "* getScanResultSecurity");

        if (capabilities.contains(AuthType.WEP.name())) {
            return AuthType.WEP;
        } else if (capabilities.contains("WPA")) {
            if (capabilities.contains("WPA2")) {
                return AuthType.WPA2_AUTO;
            } else {
                return AuthType.WPA_AUTO;
            }
        }
        return AuthType.OPEN;

    }

    /**
     * @return the Wi-Fi AP that we're logged into
     */
    public String getCurrentNetworkSSID() {
        return m_wifi.getConnectionInfo().getSSID();
    }

    //======================================================================

    /**
     * Convert a given password in decimal format to hex format.
     *
     * @param pass The given password in decimal format.
     * @return The password in hex format.
     */
    public String toHexadecimalString(String pass) {

        Log.d(TAG, "toHexadecimalString(" + pass + ")");
        byte[] data;
        try {
            data = pass.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed getting bytes of passcode by UTF-8", e);
            data = pass.getBytes();
        }
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(HEX_CODE[(b >> 4) & 0xF]);
            r.append(HEX_CODE[(b & 0xF)]);
        }
        String hexadecimalString = r.toString();
        Log.d(TAG, "toHexadecimalString -->" + hexadecimalString);
        return hexadecimalString;
    }
    //====================================================================

    /**
     * A utility method that checks if a password complies with WEP password
     * rules, and if it's in HEX format.
     *
     * @param password
     * @return {@link Pair} of two {@link Boolean} is it a valid WEP password
     * and is it a HEX password.
     */
    public Pair<Boolean, Boolean> checkWEPPassword(String password) {
        Log.d(TAG, "checkWEPPassword");

        if (password == null || password.isEmpty()) {
            Log.w(TAG, "checkWEPPassword empty password");
            return new Pair<Boolean, Boolean>(false, false);
        }

        int length = password.length();
        switch (length) {
        // valid ASCII keys length
        case 5:
        case 13:
        case 16:
        case 29:
            Log.d(TAG, "checkWEPPassword valid WEP ASCII password");
            return new Pair<Boolean, Boolean>(true, false);

        // valid hex keys length
        case 10:
        case 26:
        case 32:
        case 58:
            if (password.matches(WEP_HEX_PATTERN)) {
                Log.d(TAG, "checkWEPPassword valid WEP password length, and HEX pattern match");
                return new Pair<Boolean, Boolean>(true, true);
            }
            Log.w(TAG, "checkWEPPassword valid WEP password length, but HEX pattern matching failed: " + WEP_HEX_PATTERN);
            return new Pair<Boolean, Boolean>(false, false);

        default:
            Log.w(TAG, "checkWEPPassword invalid WEP password length: " + length);
            return new Pair<Boolean, Boolean>(false, false);
        }
    }
    //======================================================================

    /**
     * Extracts AuthType from a SSID by retrieving its capabilities via WifiManager.
     *
     * @param ssid  SSID of the network
     * @return AuthType of SSID or null if not found
     */
    private AuthType getSSIDAuthType(String ssid) {
        Log.d(TAG, "getSSIDAuthType SSID = " + ssid);
        if (ssid == null || ssid.isEmpty()) {
            Log.w(TAG, "getSSIDAuthType given string was null");
            return null;
        }

        AuthType authType = null;
        List<ScanResult> networks = m_wifi.getScanResults();
        for (ScanResult scan : networks) {
            if (ssid.equalsIgnoreCase(scan.SSID)) {
                authType = getScanResultSecurity(scan.capabilities);
                break;
            }
        }
        return authType;
    }
    //======================================================================

    /**
     * Look through the supplicant and find a configuration that matches
     * the supplied ssid if one exists.
     *
     * @param ssid  SSID of the network
     * @return wifiConfiguration for supplied ssid if found, else null
     */
    private WifiConfiguration findConfiguration(String ssid) {
        // the configured Wi-Fi networks
        List<WifiConfiguration> wifiConfigs = new ArrayList<WifiConfiguration>();

        for (WifiConfiguration w : m_wifi.getConfiguredNetworks()) {
            if (w.SSID != null) {
                wifiConfigs.add(w);
            }
        }

        // for debugging purposes only log the list
        StringBuilder buff = new StringBuilder();
        for (WifiConfiguration w : wifiConfigs) {
            w.SSID = normalizeSSID(w.SSID);
            if (!w.SSID.isEmpty()) {
                buff.append(w.SSID).append(",");
            }
        }
        Log.i(TAG, "connectTom_wifiAP ConfiguredNetworks " + (buff.length() > 0 ? buff.toString().substring(0, buff.length() - 1) : " empty"));

        WifiConfiguration config = null;
        // find any existing m_wifiConfiguration that has the same SSID as the
        // supplied one and return it if found
        for (WifiConfiguration w : wifiConfigs) {
            if (isSsidEquals(w.SSID, ssid)) {
                Log.i(TAG, "connectTom_wifiAP found " + ssid + " in ConfiguredNetworks. networkId = " + w.networkId);
                config = w;
                break;
            }
        }

        return config;
    }

    WifiConfiguration setupWifiConfiguration(String ssid, AuthType authType, String password, boolean isHidden) {
        WifiConfiguration wifiConfiguration = findConfiguration(ssid);

        // cant change the wifiConfiguration because we may not have created it
        if (marshmallow && wifiConfiguration != null) {
            return wifiConfiguration;
        }

        boolean shouldUpdate = false;
        boolean newNetworkConfiguration = false;

        // On lollipop we update the wifiConfiguration
        if (wifiConfiguration == null) {
            wifiConfiguration = new WifiConfiguration();
            newNetworkConfiguration = true;
        } else {
            if (lollipop) {
                // Use wifi.updateNetwork on lollipop
                shouldUpdate = true;
            } else {
                // Delete network on pre-lollipop
                boolean res = m_wifi.removeNetwork(wifiConfiguration.networkId);
                Log.i(TAG, "connectToWifiAP delete " + wifiConfiguration.networkId + " ? " + res);
                res = m_wifi.saveConfiguration();
                Log.i(TAG, "connectToWifiAP saveConfiguration  res = " + res);
            }
        }

        /*
        // check the AuthType of the SSID against the WifiManager
        // if null use the one given by the API
        // else use the result from getSSIDAuthType
        AuthType verrifiedWifiAuthType = getSSIDAuthType(ssid);
        if (verrifiedWifiAuthType != null) {
            authType = verrifiedWifiAuthType;
        }
        */

        if (isHidden) {
            wifiConfiguration.hiddenSSID = true;
        }

        Log.i(TAG, "connectToWifiAP selectedAuthType = " + authType);

        if (lollipop) {
            wifiConfiguration.priority = 140;
        }

        int networkId = -1;
        // set the WifiConfiguration parameters
        switch (authType) {
        case OPEN:
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            networkId = shouldUpdate ? m_wifi.updateNetwork(wifiConfiguration) : m_wifi.addNetwork(wifiConfiguration);
            Log.d(TAG, "connectToWifiAP [OPEN] add Network returned " + networkId);
            break;

        case WEP:
            wifiConfiguration.SSID = "\"" + ssid + "\"";

            // check the validity of a WEP password
            Pair<Boolean, Boolean> wepCheckResult = checkWEPPassword(password);
            if (!wepCheckResult.first) {
                Log.w(TAG, "connectToWifiAP  auth type = WEP: password '" + password + "' invalid length or characters");
                return null;
            }
            Log.i(TAG, "connectToWifiAP [WEP] using " + (!wepCheckResult.second ? "ASCII" : "HEX"));
            if (!wepCheckResult.second) {
                wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            } else {
                wifiConfiguration.wepKeys[0] = password;
            }
            if (!lollipop) {
                // jamesl: What is this doing here?
                wifiConfiguration.priority = 40;
            }
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfiguration.wepTxKeyIndex = 0;
            networkId = shouldUpdate ? m_wifi.updateNetwork(wifiConfiguration) : m_wifi.addNetwork(wifiConfiguration);
            Log.d(TAG, "connectToWifiAP [WEP] add Network returned " + networkId);
            break;

        case WPA_AUTO:
        case WPA_CCMP:
        case WPA_TKIP:
        case WPA2_AUTO:
        case WPA2_CCMP:
        case WPA2_TKIP:
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            // handle special case when WPA/WPA2 and 64 length password that can
            // be HEX
            if (password.length() == 64 && password.matches(WEP_HEX_PATTERN)) {
                wifiConfiguration.preSharedKey = password;
            } else {
                wifiConfiguration.preSharedKey = "\"" + password + "\"";
            }
            if (!lollipop) {
                // jamesl: What is this doing here?
                // WPA(2) networks are always "hidden"?
                wifiConfiguration.hiddenSSID = true;
            }
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            networkId = shouldUpdate ? m_wifi.updateNetwork(wifiConfiguration) : m_wifi.addNetwork(wifiConfiguration);
            Log.d(TAG, "connectToWifiAP  [WPA..WPA2] add Network returned " + networkId);
            break;

        default:
            networkId = -1;
            break;
        }

        if (networkId < 0) {
            return null;
        }

        wifiConfiguration.networkId = networkId;

        return wifiConfiguration;
    }

    //======================================================================

    private void stopWifiTimeoutTimer() {
        Log.d(TAG, "stopWifiTimeoutTimer");
        wifiTimeoutTimer.cancel();
        wifiTimeoutTimer.purge();
        wifiTimeoutTimer = new Timer();
    }

    /**
     * Connect to an AJ Wi-Fi Access Point
     *
     * @param ssid     SSID of the network
     * @param password Wi-Fi password
     * @param authType AuthType
     */
    public void connectToAP(String ssid, String password, AuthType authType) {
        Log.d(TAG, "connectToAP SSID = " + ssid + " authtype = " + authType.toString());

        int currentWifiState = m_wifi.getWifiState();
        if (currentWifiState != WifiManager.WIFI_STATE_ENABLED) {
            Log.d(TAG, "Can't connect to AP, wifi is not enabled. Current state is: " + currentWifiState);
            sendBroadcast(OP_FAILED_WIFI_DISABLED, null);
            return;
        }

        // if networkPass is null set it to ""
        if (password == null) {
            password = "";
        }

        mCurrentlyConnectingConfiguration = setupWifiConfiguration(ssid, authType, password, true);

        if (mCurrentlyConnectingConfiguration == null) {
            Log.d(TAG, "setupWifiConfiguration failed, raising an WIFI_AUTHENTICATION_ERROR");
            Bundle extras = new Bundle();
            sendBroadcast(WIFI_AUTHENTICATION_ERROR, extras);
            return;
        }

        final BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "WiFi BroadcastReceiver onReceive: " + intent.getAction());
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo == null) {
                        Log.i(TAG, "WiFi BroadcastReceiver onReceive with no networkInfo");
                        return;
                    }
                    if (networkInfo.getState() == null || !networkInfo.isConnected()) {
                        return;
                    }
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if (wifiInfo == null) {
                        wifiInfo = m_wifi.getConnectionInfo();
                    }
                    if (wifiInfo == null) {
                        Log.i(TAG, "WiFi BroadcastReceiver onReceive with no wifiInfo");
                        return;
                    }
                    synchronized (this) {
                        if (mCurrentlyConnectingConfiguration == null) {
                            Log.i(TAG, "WiFi BroadcastReceiver onReceive with no mCurrentlyConnectingConfiguration");
                            return;
                        }
                        if (isSsidEquals(mCurrentlyConnectingConfiguration.SSID, wifiInfo.getSSID())) {
                            // TODO: do we need to acquire a multicast lock here?
                            stopWifiTimeoutTimer();
                            context.unregisterReceiver(this);
                            Bundle extras = new Bundle();
                            sendBroadcast(WIFI_CONNECTED, extras);
                            mCurrentlyConnectingConfiguration = null;
                            Log.i(TAG, "WiFi BroadcastReceiver onReceive WIFI_CONNECTED network: " + wifiInfo.getSSID());
                        } else {
                            Log.i(TAG, "WiFi BroadcastReceiver onReceive connected to other network! " + wifiInfo.getSSID());
                        }
                    }
                } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction()) && intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR)
                           && intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0) == WifiManager.ERROR_AUTHENTICATING) {
                    // Wi-Fi authentication error
                    synchronized (this) {
                        if (mCurrentlyConnectingConfiguration == null) {
                            return;
                        }
                        Log.e(TAG, "Network Listener ERROR_AUTHENTICATING when trying to connect");

                        // it was the SDK that initiated the Wi-Fi change,
                        // hence the timer should be cancelled
                        stopWifiTimeoutTimer();
                        context.unregisterReceiver(this);
                        Bundle extras = new Bundle();
                        sendBroadcast(WIFI_AUTHENTICATION_ERROR, extras);
                        mCurrentlyConnectingConfiguration = null;
                    }
                }

            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        m_context.registerReceiver(wifiBroadcastReceiver, filter);

        if (lollipop) {
            lollipop_connect(mCurrentlyConnectingConfiguration);

            if (marshmallow) {
                sendBroadcast(MARSHMALLOW_SOFTAP_CONNECT, null);
                Log.d(TAG, "send broadcast message that the marshmallow softAP connect flow has executed");
            }
        } else {
            connect(mCurrentlyConnectingConfiguration);
        }

        // this is the application's Wi-Fi connection timeout
        long connectionTimeout = 30 * 1000;
        wifiTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (mCurrentlyConnectingConfiguration != null) {
                        Log.e(TAG, "Connect Network WIFI_TIMEOUT  when trying to connect to " + normalizeSSID(mCurrentlyConnectingConfiguration.SSID));
                        m_context.unregisterReceiver(wifiBroadcastReceiver);
                        Bundle extras = new Bundle();
                        sendBroadcast(WIFI_TIMEOUT_ERROR, extras);
                        mCurrentlyConnectingConfiguration = null;
                    }
                }
            }
        }, connectionTimeout);

    }

    /**
     * Make the actual connection to the requested Wi-Fi target.
     *
     * @param wifiConfig details of the Wi-Fi access point used by the WifiManger
     */
    private void connect(final WifiConfiguration wifiConfig) {
        Log.i(TAG, "connect  SSID=" + wifiConfig.SSID + " networkId " + wifiConfig.networkId);
        boolean res;

        if (m_wifi.getConnectionInfo().getSupplicantState() == SupplicantState.DISCONNECTED) {
            m_wifi.disconnect();
        }

        m_wifi.saveConfiguration();

        res = m_wifi.enableNetwork(wifiConfig.networkId, false);
        Log.d(TAG, "connect enableNetwork [false] status=" + res);
        res = m_wifi.disconnect();
        Log.d(TAG, "connect disconnect  status=" + res);

        // enabling a network doesn't guarantee that it's the one that Android
        // will connect to.
        // Selecting a particular network is achieved by passing 'true' here to
        // disable all other networks.
        // the side effect is that all other user's Wi-Fi networks become
        // disabled.
        // The recovery for that is enableAllWifiNetworks method.
        res = m_wifi.enableNetwork(wifiConfig.networkId, true);
        Log.d(TAG, "connect enableNetwork [true] status=" + res);
        res = m_wifi.reconnect();
        m_wifi.setWifiEnabled(true);
    }

    /**
     * Make the actual connection to the requested Wi-Fi target.
     *
     * @param wifiConfig details of the Wi-Fi access point used by the m_wifiManger
     */
    private void lollipop_connect(final WifiConfiguration wifiConfig) {
        Log.i(TAG, "lollipop_connect  SSID=" + wifiConfig.SSID + " networkId " + wifiConfig.networkId);
        boolean res;

        res = m_wifi.disconnect();
        Log.d(TAG, "lollipop_connect disconnect  status=" + res);

        if (!m_wifi.isWifiEnabled()) {
            m_wifi.setWifiEnabled(true);
        }

        // enabling a network doesn't guarantee that it's the one that Android
        // will connect to.
        // Selecting a particular network is achieved by passing 'true' here to
        // disable all other networks.
        // the side effect is that all other user's Wi-Fi networks become
        // disabled.
        // The recovery for that is enableAllWifiNetworks method.
        res = m_wifi.enableNetwork(wifiConfig.networkId, true);
        Log.d(TAG, "lollipop_connect enableNetwork [true] status=" + res);
        // Wait a few for the WiFi to do something and try again just in case
        // Android has decided that the network we configured is not "good enough"
        try {
            Thread.sleep(500);
        } catch (Exception ignored) {
        }
        res = m_wifi.enableNetwork(wifiConfig.networkId, true);
        Log.d(TAG, "lollipop_connect reconnect [true] status=" + res);
    }
}