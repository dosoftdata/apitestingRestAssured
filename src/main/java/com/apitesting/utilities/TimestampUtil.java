package com.apitesting.utilities;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampUtil {

  /**
   * Returns the current timestamp in ISO 8601 format for Athens timezone.
   * Example: 2025-10-06T13:45:30+03:00
   */
  public static String getLocalTimestamp() {
    ZoneId athensZone = ZoneId.of("Europe/Athens");
    ZonedDateTime localTime = ZonedDateTime.now(athensZone);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    return localTime.format(formatter);
  }

}

