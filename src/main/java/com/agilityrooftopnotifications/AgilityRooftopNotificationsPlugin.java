package com.agilityrooftopnotifications;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Agility Rooftop Notifications",
		description = "Notifies you when each agility obstacle is completed, using XP drops instead of idle movement",
		tags = {"agility", "rooftop", "notification", "notifier", "idle", "xp", "marks", "grace"}
)
public class AgilityRooftopNotificationsPlugin extends Plugin
{
	private static final String TEST_COMMAND = "rooftoptest";
	private static final int TEST_DELAY_TICKS = 5;
	private static final long FAIL_COOLDOWN_MS = 3000;
	private static final long MARK_COOLDOWN_MS = 3000;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private AgilityRooftopNotificationsConfig config;

	private final List<PendingNotification> pending = new ArrayList<>();

	private int lastXp = -1;
	private Course activeCourse;
	private long lastCourseXpMs;
	private boolean stallArmed;
	private long lastObstaclePingMs;
	private long lastFailPingMs;
	private long lastMarkPingMs;

	@Provides
	AgilityRooftopNotificationsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AgilityRooftopNotificationsConfig.class);
	}

	@Override
	protected void startUp()
	{

		clientThread.invoke(() ->
		{
			reset();
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				lastXp = client.getSkillExperience(Skill.AGILITY);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(this::reset);
	}

	private void reset()
	{
		pending.clear();
		lastXp = -1;
		activeCourse = null;
		lastCourseXpMs = 0;
		stallArmed = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case CONNECTION_LOST:
				reset();
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.AGILITY)
		{
			return;
		}

		final int xp = event.getXp();
		final int previous = lastXp;
		lastXp = xp;

		if (previous < 0 || xp <= previous)
		{
		}

		final Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		final long now = System.currentTimeMillis();
		final int gained = xp - previous;
		final WorldPoint location = player.getWorldLocation();
		final Course regionCourse = Course.fromRegion(location.getRegionID());
		final CourseScope scope = config.scope();

		if (!scope.allows(regionCourse))
		{
			return;
		}

		activeCourse = regionCourse;
		lastCourseXpMs = now;
		stallArmed = regionCourse != null;

		final boolean lapComplete = regionCourse != null && scope.allows(regionCourse) && regionCourse.isLapEnd(location);
		if (lapComplete)
		{
			stallArmed = false;
			queue(config.lapNotification(), "Lap complete - " + regionCourse.getDisplayName(), config.delayTicks());
			if (config.lapReplacesObstacle())
			{
				lastObstaclePingMs = now;
				return;
			}
		}

		if (gained < config.minimumXp())
		{
			return;
		}

		if (now - lastObstaclePingMs < config.cooldownMs())
		{
			return;
		}
		lastObstaclePingMs = now;

		queue(config.obstacleNotification(), "Obstacle complete", config.delayTicks());
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (event.getActor() != client.getLocalPlayer() || event.getHitsplat().getAmount() <= 0)
		{
			return;
		}

		final long now = System.currentTimeMillis();
		if (!isOnCourse() || now - lastFailPingMs < FAIL_COOLDOWN_MS)
		{
			return;
		}

		lastFailPingMs = now;
		queue(config.failNotification(), "Obstacle failed - you took damage", 0);
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (event.getItem().getId() != ItemID.GRACE)
		{
			return;
		}

		final long now = System.currentTimeMillis();
		if (!isOnCourse() || now - lastMarkPingMs < MARK_COOLDOWN_MS)
		{
			return;
		}

		lastMarkPingMs = now;
		queue(config.markNotification(), "Mark of grace spawned", 0);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Iterator<PendingNotification> it = pending.iterator();
		while (it.hasNext())
		{
			PendingNotification p = it.next();
			if (--p.ticksLeft <= 0)
			{
				it.remove();
				send(p.notification, p.message);
			}
		}

		if (stallArmed && lastCourseXpMs > 0)
		{
			final long now = System.currentTimeMillis();
			if (now - lastCourseXpMs >= config.stallSeconds() * 1000L)
			{
				stallArmed = false;
				if (isStandingInActiveCourse())
				{
					send(config.stallNotification(), "No Agility XP for " + config.stallSeconds() + "s - still on the course?");
				}
			}
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if (!TEST_COMMAND.equalsIgnoreCase(event.getCommand()))
		{
			return;
		}

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"Agility Rooftop Notifications: test obstacle notification in ~3 seconds. Alt-tab away now to test force focus.", null);
		queue(config.obstacleNotification(), "Agility Rooftop Notifications test notification", TEST_DELAY_TICKS);
	}

	/**
	 * True if the player is currently standing in a course region covered by the selected scope.
	 */
	private boolean isOnCourse()
	{
		final Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}
		final Course here = Course.fromRegion(player.getWorldLocation().getRegionID());
		return here != null && config.scope().allows(here);
	}

	private boolean isStandingInActiveCourse()
	{
		final Player player = client.getLocalPlayer();
		if (player == null || activeCourse == null)
		{
			return false;
		}
		final Course here = Course.fromRegion(player.getWorldLocation().getRegionID());
		return here == activeCourse;
	}

	private void queue(Notification notification, String message, int delayTicks)
	{
		if (!notification.isEnabled())
		{
			return;
		}

		if (delayTicks <= 0)
		{
			send(notification, message);
		}
		else
		{
			pending.add(new PendingNotification(notification, message, delayTicks));
		}
	}

	private void send(Notification notification, String message)
	{
		if (notification.isOverride() && notification.isInitialized() && notification.getVolume() <= 0)
		{
			notification = notification.withSound(NotificationSound.OFF);
		}
		notifier.notify(notification, message);
	}

	@AllArgsConstructor
	private static class PendingNotification
	{
		private final Notification notification;
		private final String message;
		private int ticksLeft;
	}
}