/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2011 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.ep.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class Extension implements FREExtension
{
	@Override
	public FREContext createContext(String extId)
	{
		if (extId.equals(PushContext.extensionId))
			return new PushContext();
		if (extId.equals(LocalNotificationsContext.extensionId))
			return new LocalNotificationsContext();
		return null;
	}

	@Override
	public void initialize()
	{

	}

	@Override
	public void dispose()
	{
	}
}
