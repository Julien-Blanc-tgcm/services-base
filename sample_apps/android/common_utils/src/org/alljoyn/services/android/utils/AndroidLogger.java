/******************************************************************************
 *    Copyright (c) Open Connectivity Foundation (OCF) and AllJoyn Open
 *    Source Project (AJOSP) Contributors and others.
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Copyright (c) Open Connectivity Foundation and Contributors to AllSeen
 *    Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for
 *    any purpose with or without fee is hereby granted, provided that the
 *    above copyright notice and this permission notice appear in all
 *    copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *    WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *    AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *    PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *    TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *    PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package org.alljoyn.services.android.utils;


import org.alljoyn.common.GenericLogger;

import android.util.Log;

/**
 * A default GenericLogger implementation for Android
 */
public class AndroidLogger implements GenericLogger {

	/**
	 * @see GenericLogger#debug(java.lang.String, java.lang.String)
	 */
	@Override
	public void debug(String TAG, String msg) {
		Log.d(TAG, msg);
	}

	/**
	 * @see GenericLogger#info(java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String TAG, String msg) {
		Log.i(TAG, msg);
	}

	/**
	 * @see GenericLogger#warn(java.lang.String, java.lang.String)
	 */
	@Override
	public void warn(String TAG, String msg) {
		Log.w(TAG, msg);
	}

	/**
	 * @see GenericLogger#error(java.lang.String, java.lang.String)
	 */
	@Override
	public void error(String TAG, String msg) {
		Log.e(TAG, msg);
	}

	/**
	 * @see GenericLogger#fatal(java.lang.String, java.lang.String)
	 */
	@Override
	public void fatal(String TAG, String msg) {
		Log.wtf(TAG, msg);
	}
}