  @DataSource:csv
  Feature: TC001
  As a QA engineer
  I want to load test data from a CSV file
  So that scenarios can be driven from CSV

  Scenario Outline: Add item to inventory
    Given admin adds item "<item_id>" with quantity "<quantity>"
    Then the inventory should reflect the new quantity

    Examples:
      | item_id | quantity | order_id | user | product | invoice_id | amount |
      | ITEM-0012 | 502 |  |  |  |  |  |
      | ITEM-002 | 102 |  |  |  |  |  |





  Scenario Outline: Remove item from inventory
    Given admin removes item "<item_id>" with quantity "<quantity>"
    Then the inventory should reflect the removal

    Examples:
      | item_id | quantity | order_id | user | product | invoice_id | amount |
      | ITEM-0012 | 502 |  |  |  |  |  |
      | ITEM-002 | 102 |  |  |  |  |  |


