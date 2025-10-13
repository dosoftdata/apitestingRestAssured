@regression @mock
Feature: Mock API Test
  Background:
    Given url <mockServerUrl>
    * def basepath = /api/payments
    And header Content-Type = application/json

  @happy
  Scenario: Get payment
    When path <basepath>/1234567890000
    And method GET
    Then status 200

  @happy
  Scenario: Hello
    When path /api/greet
    And method GET
    Then status 200
 #   * def id in response.[1].id
#    And match each $.[*] should match
#    """
#    { "id": "#number", "title": "#string" }
#    """

