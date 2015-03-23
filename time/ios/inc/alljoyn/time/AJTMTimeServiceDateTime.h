/******************************************************************************
 * Copyright (c) 2016 Open Connectivity Foundation (OCF) and AllJoyn Open
 *    Source Project (AJOSP) Contributors and others.
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Copyright 2016 Open Connectivity Foundation and Contributors to
 *    AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for
 *    any purpose with or without fee is hereby granted, provided that the
 *    above copyright notice and this permission notice appear in all
 *    copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *     DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *     PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *     TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *     PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

/**
 * This class is experimental, and as such has not been tested.
 * Please help make it more robust by contributing fixes if you find issues
 **/

#import <Foundation/Foundation.h>
#import "AJTMTimeServiceDate.h"
#import "AJTMTimeServiceTime.h"
#import "alljoyn/time/TimeServiceServerClock.h"


@interface AJTMTimeServiceDateTime : NSObject

/**
 * Initialize the object with data.
 *
 * @param handle
 * @return ER_OK if the object was initialized successfully and valid arguments have been passed to this method,
 * otherwise ER_BAD_ARGUMENT of the appropriate argument is returned
 */
-(id)initWithHandle:(ajn::services::TimeServiceDateTime const*) handle;

/**
 * Initialize the object with data.
 *
 * @param date
 * @param time
 * @param offsetMinutes
 * @return ER_OK if the object was initialized successfully and valid arguments have been passed to this method,
 * otherwise ER_BAD_ARGUMENT of the appropriate argument is returned
 */
-(QStatus)populateWithDate:(AJTMTimeServiceDate*) date time:(AJTMTimeServiceTime*) time offsetMinutes:(int16_t) offsetMinutes;

/**
 * Checks whether data of the object is valid, the object variables have a correct values, date and time are valid
 */
-(bool)isValid;

/**
 * Returns Date
 *
 * @return Date
 */
-(AJTMTimeServiceDate*)date;

/**
 * Returns Time
 *
 * @return Time
 */
-(AJTMTimeServiceTime*)time;

/**
 * Returns Offset minutes
 *
 * @return Offset minutes
 */
-(int16_t)offsetMinutes;

-(ajn::services::TimeServiceDateTime*)getHandle;


@end