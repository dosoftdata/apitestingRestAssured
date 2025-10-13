package com.apitesting.dsl.actions;

import com.apitesting.dsl.ScenarioContext;

public abstract class DslHelper {
  final ScenarioContext context;
  public DslHelper(ScenarioContext context) {
    this.context = context;
  }
}
