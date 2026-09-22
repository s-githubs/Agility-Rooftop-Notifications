package com.agilityrooftopnotifications;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AgilityRooftopNotificationsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AgilityRooftopNotificationsPlugin.class);
		RuneLite.main(args);
	}
}