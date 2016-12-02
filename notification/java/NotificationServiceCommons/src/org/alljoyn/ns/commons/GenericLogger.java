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

package org.alljoyn.ns.commons;

/**
 * Implement this interface to provide logging functionality for the Notification service
 */
public interface GenericLogger {
    /**
     * Debug level message
     * @param TAG Tag to be added to the message, i.e. class that writes the message
     * @param msg
     */
    public void debug(String TAG, String msg);

    /**
     * Info level message
     * @param TAG Tag to be added to the message, i.e. class that writes the message
     * @param msg
     */
    public void info(String TAG, String msg);

    /**
     * Warn level message
     * @param TAG Tag to be added to the message, i.e. class that writes the message
     * @param msg
     */
    public void warn(String TAG, String msg);

    /**
     * Error level message
     * @param TAG Tag to be added to the message, i.e. class that writes the message
     * @param msg
     */
    public void error(String TAG, String msg);

    /**
     * Fatal level message
     * @param TAG Tag to be added to the message, i.e. class that writes the message
     * @param msg
     */
    public void fatal(String TAG, String msg);
}