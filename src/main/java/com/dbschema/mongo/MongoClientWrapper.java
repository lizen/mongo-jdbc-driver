package com.dbschema.mongo;

import static com.dbschema.mongo.DriverPropertyInfoHelper.ALLOW_INVALID_CERTIFICATES;
import static com.dbschema.mongo.DriverPropertyInfoHelper.ALLOW_INVALID_CERTIFICATES_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.ALLOW_INVALID_HOSTNAMES;
import static com.dbschema.mongo.DriverPropertyInfoHelper.ALLOW_INVALID_HOSTNAMES_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.APPLICATION_NAME;
import static com.dbschema.mongo.DriverPropertyInfoHelper.CONNECT_TIMEOUT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.CONNECT_TIMEOUT_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.ENCODE_CREDENTIALS;
import static com.dbschema.mongo.DriverPropertyInfoHelper.ENCODE_CREDENTIALS_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.MAX_POOL_SIZE;
import static com.dbschema.mongo.DriverPropertyInfoHelper.MAX_POOL_SIZE_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.SERVER_SELECTION_TIMEOUT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.SERVER_SELECTION_TIMEOUT_DEFAULT;
import static com.dbschema.mongo.DriverPropertyInfoHelper.UUID_REPRESENTATION;
import static com.dbschema.mongo.DriverPropertyInfoHelper.UUID_REPRESENTATION_DEFAULT;
import static com.dbschema.mongo.SSLUtil.getTrustEverybodySSLContext;
import static com.dbschema.mongo.Util.insertCredentials;
import static com.dbschema.mongo.Util.isNullOrEmpty;
import static com.dbschema.mongo.Util.isTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;


public class MongoClientWrapper implements AutoCloseable {
  private boolean isClosed = false;
  private final MongoClient mongoClient;
  public final String databaseNameFromUrl;

  public MongoClientWrapper(@NotNull String uri, @NotNull Properties prop, @Nullable String username, @Nullable String password) throws SQLException {
    try {
      boolean automaticEncoding = ENCODE_CREDENTIALS_DEFAULT;
      if (prop.getProperty(ENCODE_CREDENTIALS) != null) {
        automaticEncoding = Boolean.parseBoolean(prop.getProperty(ENCODE_CREDENTIALS));
      }
      uri = insertCredentials(uri, username, password, automaticEncoding);
      ConnectionString connectionString = new ConnectionString(uri);
      databaseNameFromUrl = connectionString.getDatabase();
      int maxPoolSize = getMaxPoolSize(prop);

      MongoCredential credential = null;
      MongoClientSettings.Builder builder = null;
      if (!isNullOrEmpty(username) && !isNullOrEmpty(password)) {
          String databaseName = prop.getProperty("authSource", "admin");
          credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());

          builder = MongoClientSettings.builder()
                  .applyConnectionString(connectionString)
                  .credential(credential)
                  .applyToConnectionPoolSettings(b -> b.maxSize(maxPoolSize));
      } else {
          builder = MongoClientSettings.builder()
                  .applyConnectionString(connectionString)
                  .applyToConnectionPoolSettings(b -> b.maxSize(maxPoolSize));
      }

      String application = prop.getProperty(APPLICATION_NAME);
      if (!isNullOrEmpty(application)) {
        builder.applicationName(application);
      }
      if ("true".equals(prop.getProperty("ssl"))) {
        boolean allowInvalidCertificates = uri.contains("tlsAllowInvalidCertificates=true") || uri.contains("sslAllowInvalidCertificates=true")
            || isTrue(prop.getProperty(ALLOW_INVALID_CERTIFICATES, Boolean.toString(ALLOW_INVALID_CERTIFICATES_DEFAULT)));
        builder.applyToSslSettings(s -> {
          s.enabled(true);
          boolean allowInvalidHostnames = isTrue(prop.getProperty(ALLOW_INVALID_HOSTNAMES, Boolean.toString(ALLOW_INVALID_HOSTNAMES_DEFAULT)));
          if (allowInvalidHostnames) s.invalidHostNameAllowed(true);
          if (allowInvalidCertificates) {
            String keyStoreType = System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
            String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword", "");
            String keyStoreUrl = System.getProperty("javax.net.ssl.keyStore", "");
            // check keyStoreUrl
            if (!isNullOrEmpty(keyStoreUrl)) {
              try {
                new URL(keyStoreUrl);
              } catch (MalformedURLException e) {
                keyStoreUrl = "file:" + keyStoreUrl;
              }
            }
            try {
              s.context(getTrustEverybodySSLContext(keyStoreUrl, keyStoreType, keyStorePassword));
            }
            catch (SSLUtil.SSLParamsException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
      if (connectionString.getUuidRepresentation() == null) {
        String uuidRepresentation = prop.getProperty(UUID_REPRESENTATION, UUID_REPRESENTATION_DEFAULT);
        builder.uuidRepresentation(createUuidRepresentation(uuidRepresentation));
      }
      if (connectionString.getServerSelectionTimeout() == null) {
        int timeout = Integer.parseInt(prop.getProperty(SERVER_SELECTION_TIMEOUT, SERVER_SELECTION_TIMEOUT_DEFAULT));
        builder.applyToClusterSettings(b -> b.serverSelectionTimeout(timeout, TimeUnit.MILLISECONDS));
      }
      if (connectionString.getConnectTimeout() == null) {
        int timeout = Integer.parseInt(prop.getProperty(CONNECT_TIMEOUT, CONNECT_TIMEOUT_DEFAULT));
        builder.applyToSocketSettings(b -> b.connectTimeout(timeout, TimeUnit.MILLISECONDS));
      }
      this.mongoClient = MongoClients.create(builder.build());
    }
    catch (Exception e) {
      throw new SQLException(e);
    }
  }

  private static UuidRepresentation createUuidRepresentation(String value) {
    if (value.equalsIgnoreCase("unspecified")) {
      return UuidRepresentation.UNSPECIFIED;
    }
    if (value.equalsIgnoreCase("javaLegacy")) {
      return UuidRepresentation.JAVA_LEGACY;
    }
    if (value.equalsIgnoreCase("csharpLegacy")) {
      return UuidRepresentation.C_SHARP_LEGACY;
    }
    if (value.equalsIgnoreCase("pythonLegacy")) {
      return UuidRepresentation.PYTHON_LEGACY;
    }
    if (value.equalsIgnoreCase("standard")) {
      return UuidRepresentation.STANDARD;
    }
    throw new IllegalArgumentException("Unknown uuid representation: " + value);
  }

  private int getMaxPoolSize(@NotNull Properties prop) {
    try {
      String str = prop.getProperty(MAX_POOL_SIZE);
      if (str != null) {
        int poolSize = Integer.parseInt(str);
        return poolSize > 0 ? poolSize : 1;
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return MAX_POOL_SIZE_DEFAULT;
  }

  @Override
  public void close() throws SQLAlreadyClosedException {
    checkClosed();
    isClosed = true;
    mongoClient.close();
  }

  private void checkClosed() throws SQLAlreadyClosedException {
    if (isClosed) throw new SQLAlreadyClosedException(this.getClass().getSimpleName());
  }

  public MongoIterable<String> listDatabaseNames() throws SQLAlreadyClosedException {
    checkClosed();
    return mongoClient.listDatabaseNames();
  }

  public MongoDatabase getDatabase(String databaseName) throws SQLAlreadyClosedException {
    checkClosed();
    return mongoClient.getDatabase(databaseName);
  }

  @NotNull
  public MongoClient getMongoClient() {
    return mongoClient;
  }
}
