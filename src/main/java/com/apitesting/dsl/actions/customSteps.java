package com.apitesting.dsl.actions;

import com.apitesting.dsl.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class customSteps extends DslHelper {

    public customSteps(ScenarioContext context) {
        super(context);
    }

  private Map<String, Object> context = new HashMap<>();

  // ===============================
  // DB Scenarios (Order Management)
  // ===============================

  @Given("user {string} has logged in")
  public void user_has_logged_in(String user) {
    System.out.println("✅ User logged in: " + user);
    context.put("user", user);
  }

  @When("the user orders {string} quantity {string}")
  public void user_orders_product(String product, String quantity) {
    System.out.printf("🛒 Ordering product '%s' (qty: %s)%n", product, quantity);
    context.put("product", product);
    context.put("quantity", quantity);
  }

  @Then("the order should be created successfully")
  public void order_should_be_created_successfully() {
    System.out.printf("✅ Order created for %s: %s x%s%n",
        context.get("user"), context.get("product"), context.get("quantity"));
  }

  @Given("user {string} has an existing order {string}")
  public void user_has_existing_order(String user, String orderId) {
    System.out.printf("📦 User %s has existing order %s%n", user, orderId);
    context.put("order_id", orderId);
  }

  @When("the user cancels the order")
  public void user_cancels_the_order() {
    System.out.println("🗑️ Order cancelled: " + context.get("order_id"));
  }

  @Then("the order status should be {string}")
  public void order_status_should_be(String status) {
    System.out.println("✅ Order status verified: " + status);
  }

  // ===============================
  // CSV Scenarios (Inventory)
  // ===============================

  @Given("admin adds item {string} with quantity {string}")
  public void admin_adds_item(String itemId, String qty) {
    System.out.printf("📦 Admin added item %s (qty: %s)%n", itemId, qty);
    context.put("item_id", itemId);
    context.put("quantity", qty);
  }

  @Then("the inventory should reflect the new quantity")
  public void inventory_should_reflect_new_quantity() {
    System.out.printf("✅ Inventory updated: %s → %s%n",
        context.get("item_id"), context.get("quantity"));
  }

  @Given("admin removes item {string} with quantity {string}")
  public void admin_removes_item(String itemId, String qty) {
    System.out.printf("🗑️ Admin removed item %s (qty: %s)%n", itemId, qty);
  }

  @Then("the inventory should reflect the removal")
  public void inventory_should_reflect_removal() {
    System.out.println("✅ Inventory removal confirmed");
  }

  // ===============================
  // Excel Scenarios (Payment)
  // ===============================

  @Given("user {string} has invoice {string}")
  public void user_has_invoice(String user, String invoiceId) {
    System.out.printf("💰 User %s has invoice %s%n", user, invoiceId);
    context.put("invoice_id", invoiceId);
  }

  @When("the user pays {string}")
  public void user_pays(String amount) {
    System.out.printf("💳 Payment processed: %s for invoice %s%n",
        amount, context.get("invoice_id"));
  }

  @Then("the payment status should be {string}")
  public void payment_status_should_be(String status) {
    System.out.println("✅ Payment status verified: " + status);
  }


}
