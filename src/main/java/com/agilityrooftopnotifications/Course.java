/*
 * Course region IDs and lap-end tiles adapted from RuneLite's core Agility plugin
 * (net.runelite.client.plugins.agility.Courses), BSD 2-Clause licensed.
 */
package com.agilityrooftopnotifications;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
enum Course
{

	DRAYNOR("Draynor Village", true, 12338, new WorldPoint(3103, 3261, 0)),
	AL_KHARID("Al Kharid", true, 13105, new WorldPoint(3299, 3194, 0)),
	VARROCK("Varrock", true, 12853, new WorldPoint(3236, 3417, 0)),
	CANIFIS("Canifis", true, 13878, new WorldPoint(3510, 3485, 0)),
	FALADOR("Falador", true, 12084,
			new WorldPoint(3029, 3332, 0), new WorldPoint(3029, 3333, 0),
			new WorldPoint(3029, 3334, 0), new WorldPoint(3029, 3335, 0)),
	SEERS("Seers' Village", true, 10806, new WorldPoint(2704, 3464, 0)),
	POLLNIVNEACH("Pollnivneach", true, 13358, new WorldPoint(3363, 2998, 0)),
	RELLEKKA("Rellekka", true, 10553, new WorldPoint(2653, 3676, 0)),
	ARDOUGNE("Ardougne", true, 10547, new WorldPoint(2668, 3297, 0)),


	GNOME("Gnome Stronghold", false, 9781, new WorldPoint(2484, 3437, 0), new WorldPoint(2487, 3437, 0)),
	SHAYZIEN_BASIC("Shayzien Basic", false, 6200, new WorldPoint(1554, 3640, 0)),
	SHAYZIEN_ADVANCED("Shayzien Advanced", false, 5944, new WorldPoint(1522, 3625, 0)),
	PYRAMID("Agility Pyramid", false, 13356, new WorldPoint(3364, 2830, 0)),
	PENGUIN("Penguin", false, 10559, new WorldPoint(2652, 4039, 1)),
	BARBARIAN("Barbarian Outpost", false, 10039, new WorldPoint(2543, 3553, 0)),
	APE_ATOLL("Ape Atoll", false, 11050, new WorldPoint(2770, 2747, 0)),
	WILDERNESS("Wilderness", false, 11837,
			new WorldPoint(2993, 3933, 0), new WorldPoint(2994, 3933, 0), new WorldPoint(2995, 3933, 0)),
	WEREWOLF("Werewolf", false, 14234, new WorldPoint(3528, 9873, 0)),
	PRIFDDINAS("Prifddinas", false, 12895, new WorldPoint(3240, 6109, 0));

	private static final Map<Integer, Course> BY_REGION;

	static
	{
		ImmutableMap.Builder<Integer, Course> builder = ImmutableMap.builder();
		for (Course c : values())
		{
			builder.put(c.regionId, c);
		}
		BY_REGION = builder.build();
	}

	private final String displayName;
	private final boolean rooftop;
	private final int regionId;
	private final WorldPoint[] lapEndPoints;

	Course(String displayName, boolean rooftop, int regionId, WorldPoint... lapEndPoints)
	{
		this.displayName = displayName;
		this.rooftop = rooftop;
		this.regionId = regionId;
		this.lapEndPoints = lapEndPoints;
	}

	static Course fromRegion(int regionId)
	{
		return BY_REGION.get(regionId);
	}

	boolean isLapEnd(WorldPoint point)
	{
		return Arrays.asList(lapEndPoints).contains(point);
	}
}