@regression @mock
Feature: Lifecycle
  Background:
    Given url <mockServerUrl>
    And header Content-Type = application/json
    And header Accept = application/json
    And header X-test = test

  Scenario: Status DocString
    And path api/job/123
    And retry GET  until condition
      """
      { "status": 200 }
      """
  Then status 200 or 404
  And match path $.state should be = DONE


  Scenario: Status json
    And path api/job/123
    And retry GET  until condition { "status": 200 }
    Then status 200 or 404
    And match path $.state should be = DONE