# =============================================================
# Feature file demonstrating DB, CSV, and Excel data sources
# =============================================================
@DataSource:csv
Feature: TC002
  As a QA engineer
  I want to load test data from the database
  So that scenarios use dynamic examples
  @datatest
  Scenario Outline: Create multiple orders
    Given user "<user>" has logged in
    When the user orders "<product>" quantity "<quantity>"
    Then the order should be created successfully

    Examples:
      | user | product | quantity |
      | alice | Widget | 1 |
      | bob | Gadget | 1 |


  Scenario: Cancel order - non outline
    Given user "user" has an existing order "order_id"
    When the user cancels the order
    Then the order status should be "Cancelled"


    @test
  Scenario Outline: Cancel order
    Given user "<user>" has an existing order "<order_id>"
    When the user cancels the order
    Then the order status should be "Cancelled"
    Examples:
      | user    | order_id |
      | default | 1001     |
#  @DataSource:excel
#  Feature: Payment Management - Excel Test Data
#  As a QA engineer
#  I want to load test data from an Excel file
#  So that scenarios can be driven from Excel
#
#  Scenario Outline: Pay invoice
#    Given user "<user>" has invoice "<invoice_id>"
#    When the user pays "<amount>"
#    Then the payment status should be "Completed"
#
#    Examples:
#      | user | invoice_id | amount |
#      | placeholder | INV-0 | 0 |
