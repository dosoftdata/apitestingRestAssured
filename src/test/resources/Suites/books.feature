@regression @Books
Feature: Book API Test

  Background:
    Given url <baseUrl>
    * def basepath = /api/v1/Books
    And header Content-Type = application/json

  @happy
  Scenario: Retrieve a list of all books
    When path <basepath>
    And method GET
    Then status 200
    * def id in response.[1].id
    And match each $.[*] should match
    """
    { "id": "#number", "title": "#string" }
    """

  @happy
  Scenario: Retrieve details of a specific book by its ID
    When path <basepath>/{id}
    And pathParam id = <id>
    And method GET
    Then status 200
    And match
    """
    {
      "id": "#number",
      "title": "#string",
      "description": "#string",
      "pageCount": "#number",
      "excerpt": "#string",
      "publishDate": "#string"
    }
    """

  @happy
  Scenario: Add a new book to the system
    When path <basepath>
    And request
      """
      {
        "title": "Lorem lorem",
        "description": "Lorem lorem lorem.",
        "excerpt": "Lorem lorem lorem.",
        "publishDate": "2025-10-10T11:18:33.2663956+00:00"
      }
      """
    And method POST
    Then status 200
    And match
    """
    {
      "id": "#number",
      "title": "#string",
      "description": "#string",
      "pageCount": "#number",
      "excerpt": "#string",
      "publishDate": "#string"
    }
    """
    And match path $.id should be = 0
    And match path $.id should be != 1
    And match path $.title should be != NonExistingTitle

  @happy
  Scenario: Update an existing book by its ID
    When path <basepath>/{id}
    And pathParam id = <id>
    And request
      """
      {
        "title": "Lorem lorem",
        "description": "Lorem lorem lorem.",
        "excerpt": "Lorem lorem lorem.",
        "publishDate": "2025-10-10T11:18:33.2663956+00:00"
      }
      """
    And method PUT
    Then status 200
    And match path $.title should be = Lorem lorem
    And match path $.description should be = Lorem lorem lorem.

  @happy
  Scenario: Delete a book by its ID
    When path <basepath>/{id}
    And pathParam id = <id>
    And method DELETE
    Then status 200
    And match
      """
      """
  @edge
  Scenario: Get book with invalid ID
    When path <basepath>/{id}
    And pathParam id = 999999
    And method GET
    Then status 404
    And match path $.title should be = Not Found

  @edge
  Scenario: Delete book with invalid ID
    When path <basepath>/{id}
    And pathParam id = abc
    And method DELETE
    Then status 400
    And match path $.errors.id[*] should be #contains The value 'abc' is not valid.

  @edge
  Scenario: Create book with missing fields
    When path <basepath>
    And request
      """
      {
        "description": "Lorem lorem lorem."
      }
      """
    And method POST
    Then status 200
    And match path $.publishDate should be = 0001-01-01T00:00:00

  @edge
  Scenario: Add a new book to the system with invalid body
    When path <basepath>
    And request
      """
      {
        "title": "Lorem lorem",
        "description": "Lorem lorem lorem."
        "excerpt": "Lorem lorem lorem."
        "publishDate": "2025-10-10T11:18:33.2663956+00:00"
      }
      """
    And method POST
    Then status 400
    And match path $.errors.$[*] should be #contains is invalid after a value. Expected either ','
