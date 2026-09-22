package com.agilityrooftopnotifications;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseScope
{
	ROOFTOPS("Rooftop courses only"),
	ALL_COURSES("All agility courses"),
	ANY_AGILITY_XP("Any Agility XP");

	private final String name;

	boolean allows(Course course)
	{
		switch (this)
		{
			case ANY_AGILITY_XP:
				return true;
			case ALL_COURSES:
				return course != null;
			case ROOFTOPS:
			default:
				return course != null && course.isRooftop();
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}