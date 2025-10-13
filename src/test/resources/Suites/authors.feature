@regression @Authors

Feature: Author API Test

  Background:
    Given url <baseUrl>
    * def basepath = /api/v1/Authors
    And header Content-Type = application/json


  @happy
  Scenario: Retrieve a list of all authors
    When path <basepath>
    And method GET
    Then status 200
    * def id in response.[0].id
    And match each $.[*] should match
    """
    {
      "id": "#number",
      "idBook": "#number",
      "firstName": "#string"
     }
    """
  @happy
  Scenario: Retrieve details of a specific author by their ID
    When path <basepath>/{id}
    And pathParam id = <id>
    And method GET
    Then status 200
    And match
    """
    {
      "id": "#number",
      "idBook": "#number",
      "firstName": "#string"
     }
    """

  @happy
  Scenario: Add a new author to the system
    When path <basepath>
    And request
      """
      {
          "id": 0,
          "idBook": 0,
          "firstName": "First Name",
          "lastName": "Last Name"
      }
      """
    And method POST
    Then status 200
    And match
    """
    {
      "id": "#number",
      "idBook": "#number",
      "firstName": "#string"
     }
    """
  @happy
  Scenario: Update an existing author’s details
    When path <basepath>/{id}
    And pathParam id = <id>
    And request
      """
      {
          "id": #(id),
          "idBook": 1,
          "firstName": "Test First Name",
          "lastName": "Test Last Name"
      }
      """
    And method PUT
    Then status 200
    And match
     """
      {
        "id": "#number",
        "idBook": "#number",
        "firstName": "#string"
       }
      """
  @happy
  Scenario: Delete an author by their ID
    When path <basepath>/{id}
    And pathParam id = <id>
    And method DELETE
    Then status 200
    And match
    """
    """
  @edge
  Scenario: Create Author with missing fields
    When path <basepath>
    And request
      """
      {
          "firstName": "Test First Name",
          "lastName": "Test Last Name"
      }
      """
    And method POST
    Then status 200
    And match
    """
    {
      "id": 0,
      "idBook": 0,
      "firstName": "Test First Name",
      "lastName": "Test Last Name"
     }
    """
  @edge
  Scenario:  Get Author with invalid ID
    When path <basepath>/{id}
    And pathParam id = 999999
    And method GET
    Then status 404
    And match path $.title should be = Not Found
  @edge
  Scenario: Get Author with invalid ID
    When path <basepath>/{id}
    And pathParam id = abc
    And method DELETE
    Then status 400
    And match path $.errors.id[*] should be #contains The value 'abc' is not valid.



