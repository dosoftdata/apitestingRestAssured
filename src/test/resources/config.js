function fn(name) {

  var env = java.lang.System.getProperty("env");
  if (!env) {
    env = "dev"; // default
  }
  var RandomUtil = Java.type('com.apitesting.utilities.RandomUtil');
  var localDateTime = Java.type('com.apitesting.utilities.TimestampUtil');

  var config = {
    env: env,
    baseUrl: "https://fakerestapi.azurewebsites.net",
    uuid : java.util.UUID.randomUUID(),
    wiremock : true,
    $password : RandomUtil.generatePassword(),
    $numeric:  RandomUtil.randomAlphaNumeric(12),
    $timestamp: localDateTime.getLocalTimestamp(),
    database: {
          host: "localhost",
          ports: [5432, 5433],
          credentials: { user: "admin", pass: "secret" }
        },
        apiKeys: ["key1", "key2"],
        enabled: true
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
