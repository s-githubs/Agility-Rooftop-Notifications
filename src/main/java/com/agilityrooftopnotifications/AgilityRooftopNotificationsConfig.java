package com.agilityrooftopnotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(AgilityRooftopNotificationsConfig.GROUP)
public interface AgilityRooftopNotificationsConfig extends Config
{
	String GROUP = "agilityrooftopnotifications";

	/*
	 * Every item below of type Notification gets RuneLite's built-in cog menu, exactly like the
	 * Idle Notifier: tray notification, request focus (Off / Request / Taskbar / Force),
	 * sound (Native / Custom / Off, plus any .wav in .runelite/notifications), volume,
	 * game message, screen flash + colour, and "send when focused".
	 */

	@ConfigSection(
			name = "Notifications",
			description = "Click the cog next to each notification to customise sound, focus, flash, etc.",
			position = 0
	)
	String notificationsSection = "notifications";

	@ConfigSection(
			name = "Triggers",
			description = "When notifications should fire",
			position = 1
	)
	String triggersSection = "triggers";

	@ConfigSection(
			name = "Timing",
			description = "Delays and cooldowns",
			position = 2
	)
	String timingSection = "timing";

	@ConfigItem(
			keyName = "obstacleNotification",
			name = "Obstacle complete",
			description = "Fires on each Agility XP drop",
			position = 0,
			section = notificationsSection
	)
	default Notification obstacleNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "lapNotification",
			name = "Lap complete",
			description = "Fires when you land on the final tile of a course",
			position = 1,
			section = notificationsSection
	)
	default Notification lapNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "failNotification",
			name = "Obstacle failed",
			description = "Fires when you take damage while running a course (e.g. falling off a rooftop)",
			position = 2,
			section = notificationsSection
	)
	default Notification failNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "markNotification",
			name = "Mark of grace spawned",
			description = "Fires when a Mark of grace appears while you're on a course",
			position = 3,
			section = notificationsSection
	)
	default Notification markNotification()
	{
		return Notification.OFF;
	}

	@ConfigItem(
			keyName = "stallNotification",
			name = "Stalled on course",
			description = "Fires if no Agility XP has been gained for the stall time below while on a course (catches misclicks)",
			position = 4,
			section = notificationsSection
	)
	default Notification stallNotification()
	{
		return Notification.OFF;
	}

	@ConfigItem(
			keyName = "scope",
			name = "Where to notify",
			description = "Limit obstacle notifications to rooftops, all courses, or any Agility XP (shortcuts, Sepulchre, etc.)",
			position = 0,
			section = triggersSection
	)
	default CourseScope scope()
	{
		return CourseScope.ROOFTOPS;
	}

	@ConfigItem(
			keyName = "lapReplacesObstacle",
			name = "Lap ping replaces last obstacle",
			description = "On the final obstacle, send only the lap notification instead of both",
			position = 1,
			section = triggersSection
	)
	default boolean lapReplacesObstacle()
	{
		return true;
	}

	@ConfigItem(
			keyName = "minimumXp",
			name = "Minimum XP",
			description = "Ignore XP drops smaller than this",
			position = 2,
			section = triggersSection
	)
	@Range(min = 1, max = 5000)
	default int minimumXp()
	{
		return 1;
	}


	@ConfigItem(
			keyName = "delayTicks",
			name = "Obstacle delay",
			description = "Wait this many game ticks (0.6s each) after the XP drop before notifying",
			position = 0,
			section = timingSection
	)
	@Units(Units.TICKS)
	@Range(max = 10)
	default int delayTicks()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "cooldownMs",
			name = "Obstacle cooldown",
			description = "Ignore further obstacle XP drops within this window (prevents double pings)",
			position = 1,
			section = timingSection
	)
	@Units(Units.MILLISECONDS)
	@Range(max = 10000)
	default int cooldownMs()
	{
		return 600;
	}

	@ConfigItem(
			keyName = "stallSeconds",
			name = "Stall time",
			description = "Seconds without Agility XP before the 'Stalled on course' notification fires",
			position = 2,
			section = timingSection
	)
	@Units(Units.SECONDS)
	@Range(min = 3, max = 120)
	default int stallSeconds()
	{
		return 12;
	}
}