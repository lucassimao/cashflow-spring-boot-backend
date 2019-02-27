package com.lucassimao.repositories;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

// Custom matcher in order to parse a UTC date string
// and verify if matches with the local date string
public class UTCMatcher extends TypeSafeMatcher<String> {

    private ZoneId localZoneId;
    private String expectedLocalDate;

    private UTCMatcher() {
    }

    private UTCMatcher(ZoneId localZoneId,String expectedLocalDate) {
        this.localZoneId=localZoneId;
        this.expectedLocalDate=expectedLocalDate;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(this.expectedLocalDate + " in UTC"); 
    }

    @Override
    protected boolean matchesSafely(String item) {
        DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        ZonedDateTime z= ZonedDateTime.parse(item,format).withZoneSameInstant(localZoneId);
        return format.format(z).startsWith(expectedLocalDate);
    }

    public static Matcher<String> utcMatcher(ZoneId localZoneId,String expectedLocalDate) {
        return new UTCMatcher(localZoneId,expectedLocalDate);
    }

}