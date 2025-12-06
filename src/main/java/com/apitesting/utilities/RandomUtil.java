package com.apitesting.utilities;

import java.security.SecureRandom;

public class RandomUtil {

  private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom random = new SecureRandom();

  /**
   * Generates a random alphanumeric string of the given length.
   */
  public static String randomAlphaNumeric(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
    }
    return sb.toString();
  }

  /**
   * Generates a random password similar to Postman's {{$randomPassword}} + 5 alphanumerics.
   */
  public static String generatePassword() {
    // Simulate {{$randomPassword}} with 8 characters
    String basePassword = randomAlphaNumeric(8);
    // Add 5 more random alphanumerics
    String extra = randomAlphaNumeric(5);
    return basePassword + extra;
  }

}
