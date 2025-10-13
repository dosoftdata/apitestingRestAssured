function fn(name) {

  var env = java.lang.System.getProperty("env");
  if (!env) {
    env = "dev"; // default
  }

  var config = {
    env: env,
    baseUrl: "https://fakerestapi.azurewebsites.net",
    uuid : java.util.UUID.randomUUID(),
    wiremock : true
  };
  if (env === "uat") {
     config.baseUrl = "https://fakerestapi.azurewebsites.net";
     config.wiremock = false
  } else if (env === "preprod") {
    config.baseUrl = "https://fakerestapi.azurewebsites.net";
    config.wiremock = false
  }
  return config;
}
