function fn(name) {

  var env = java.lang.System.getProperty("env");
  if (!env) {
    env = "dev"; // default
  }
  var RandomUtil = Java.type('com.apitesting.utilities.RandomUtil');
  var localDateTime = Java.type('com.apitesting.utilities.TimestampUtil');
  var ZonedDateTime = Java.type('java.time.ZonedDateTime');
  var DateTimeFormatter = Java.type('java.time.format.DateTimeFormatter');

  var Utils = {
    getCurrentDate: function(date_format) {
      var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
      var sdf = new SimpleDateFormat(date_format);
      var date = new java.util.Date();
      return sdf.format(date);
    },
     getCurrentDateISO: function() {
        // Format with nanoseconds and offset, e.g., 2025-10-10T11:18:33.266395600+00:00
        var now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");
        return now.format(formatter);
      }
  };
  var Faker = Java.type('com.github.javafaker.Faker');
  var faker = new Faker();

  var config = {
    env: env,
    baseUrl: "https://fakerestapi.azurewebsites.net",
    uuid : java.util.UUID.randomUUID(),
    wiremock : true,
    faker: {
          name: {
            fullName: faker.name().fullName(),
            firstName: faker.name().firstName(),
            lastName: faker.name().lastName(),
            prefix: faker.name().prefix(),
            suffix: faker.name().suffix(),
            title: faker.name().title()
          },
          address: {
            streetAddress: faker.address().streetAddress(),
            city: faker.address().cityName(),
            state: faker.address().state(),
            country: faker.address().country(),
            zipCode: faker.address().zipCode(),
            fullAddress: faker.address().fullAddress()
          },
          company: {
            name: faker.company().name(),
            industry: faker.company().industry(),
            profession: faker.company().profession(),
            url: faker.company().url()
          },
          internet: {
            username: faker.name().firstName().toLowerCase() + faker.number().digits(3),
            email: faker.internet().emailAddress(),
            domain: faker.internet().domainName(),
            password: faker.internet().password(),
            url: faker.internet().url(),
            ipv4: faker.internet().ipV4Address(),
            ipv6: faker.internet().ipV6Address(),
            uuid: faker.internet().uuid()
          },
          phone: {
            cell: faker.phoneNumber().cellPhone(),
            phone: faker.phoneNumber().phoneNumber(),
            subscriberNumber: faker.phoneNumber().subscriberNumber()
          },
          finance: {
            creditCard: faker.finance().creditCard(),
            iban: faker.finance().iban(),
            bic: faker.finance().bic(),
            amount: faker.commerce().price()
          },
          job: {
            title: faker.job().title(),
            position: faker.job().position(),
            field: faker.job().field()
          },
          commerce: {
            productName: faker.commerce().productName(),
            department: faker.commerce().department(),
            price: faker.commerce().price()
          },
          lorem: {
            word: faker.lorem().word(),
            sentence: faker.lorem().sentence(),
            paragraph: faker.lorem().paragraph()
          },
          misc: {
            color: faker.color().name(),
            beer: faker.beer().name(),
            animal: faker.animal().name(),
            food: faker.food().dish(),
            book: faker.book().title(),
            artist: faker.artist().name(),
            musicInstrument: faker.music().instrument(),
            currency: faker.currency().code(),
            country: faker.country().name()
          },
          id: {
            uuid: faker.internet().uuid()
          }
    },
    today: Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss"),
    $password : RandomUtil.generatePassword(),
    $numeric:  RandomUtil.randomAlphaNumeric(12),
    $timestamp: localDateTime.getLocalTimestamp(),
    timestamp: Utils.getCurrentDateISO(),
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
