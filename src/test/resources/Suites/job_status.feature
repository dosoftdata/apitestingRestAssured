@regression @mock
Feature: Lifecycle
  Background:
    Given url <mockServerUrl>
    And header Content-Type = application/json

  Scenario: Status
    And retry GET /api/job/123 until condition
      """
      {
        "status": 200
      }
      """
  Then status 200
  And match path $.state should be = DONE