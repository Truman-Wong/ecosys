/**
 * Copyright (c) 2023 TigerGraph Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tigergraph.spark.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tigergraph.spark.util.OptionDef.OptionKey;
import com.tigergraph.spark.util.OptionDef.Type;

/** Validate and transform Spark DataFrame options(configurations) */
public class Options implements Serializable {

  public static enum OptionType {
    WRITE,
    READ
  }

  private final OptionType optionType;

  public static final String GRAPH = "graph";
  public static final String URL = "url";
  public static final String VERSION = "version";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String SECRET = "secret";
  public static final String TOKEN = "token";
  // loading
  public static final String LOADING_JOB = "loading.job";
  public static final String LOADING_FILENAME = "loading.filename";
  public static final String LOADING_SEPARATOR = "loading.separator";
  public static final String LOADING_EOL = "loading.eol";
  public static final String LOADING_BATCH_SIZE_BYTES = "loading.batch.size.bytes";
  public static final String LOADING_TIMEOUT_MS = "loading.timeout.ms";
  public static final String LOADING_MAX_PERCENT_ERROR = "loading.max.percent.error";
  public static final String LOADING_MAX_NUM_ERROR = "loading.max.num.error";
  public static final String LOADING_RETRY_INTERVAL_MS = "loading.retry.interval.ms";
  public static final String LOADING_MAX_RETRY_INTERVAL_MS = "loading.max.retry.interval.ms";
  public static final String LOADING_MAX_RETRY_ATTEMPTS = "loading.max.retry.attempts";
  // loading - default
  public static final String LOADING_SEPARATOR_DEFAULT = ",";
  public static final String LOADING_EOL_DEFAULT = "\n";
  public static final int LOADING_BATCH_SIZE_BYTES_DEFAULT = 2 * 1024 * 1024; // 2mb
  public static final int LOADING_TIMEOUT_MS_DEFAULT = 0; // restpp default
  public static final int LOADING_RETRY_INTERVAL_MS_DEFAULT = 5 * 1000; // 5s
  public static final int LOADING_MAX_RETRY_INTERVAL_MS_DEFAULT = 5 * 60 * 1000; // 5min
  public static final int LOADING_MAX_RETRY_ATTEMPTS_DEFAULT = 10;
  // http transport
  public static final String IO_CONNECT_TIMEOUT_MS = "io.connect.timeout.ms";
  public static final String IO_READ_TIMEOUT_MS = "io.read.timeout.ms";
  public static final String IO_RETRY_INTERVAL_MS = "io.retry.interval.ms";
  public static final String IO_MAX_RETRY_INTERVAL_MS = "io.max.retry.interval.ms";
  public static final String IO_MAX_RETRY_ATTEMPTS = "io.max.retry.attempts";
  // http transport - default
  public static final int IO_CONNECT_TIMEOUT_MS_DEFAULT = 30 * 1000; // 30s
  public static final int IO_READ_TIMEOUT_MS_DEFAULT = 60 * 1000; // 1min
  public static final int IO_RETRY_INTERVAL_MS_DEFAULT = 5 * 1000; // 5s
  public static final int IO_MAX_RETRY_INTERVAL_MS_DEFAULT = 10 * 1000; // 10s
  public static final int IO_MAX_RETRY_ATTEMPTS_DEFAULT = 5;
  // SSL
  public static final String SSL_MODE = "ssl.mode";
  public static final String SSL_MODE_BASIC = "basic";
  public static final String SSL_MODE_VERIFY_CA = "verifyCA";
  public static final String SSL_MODE_VERIFY_HOSTNAME = "verifyHostname";
  public static final String SSL_TRUSTSTORE = "ssl.truststore";
  public static final String SSL_TRUSTSTORE_TYPE = "ssl.truststore.type";
  public static final String SSL_TRUSTSTORE_PASSWORD = "ssl.truststore.password";
  public static final String SSL_TRUSTSTORE_TYPE_DEFAULT = "JKS";

  // Options' group name
  public static final String GROUP_GENERAL = "general";
  public static final String GROUP_AUTH = "auth";
  public static final String GROUP_LOADING_JOB = "loading.job";
  public static final String GROUP_TRANSPORT_TIMEOUT = "transport.timeout";
  public static final String GROUP_SSL = "ssl";

  private final Map<String, String> originals;
  private final Map<String, Serializable> transformed = new HashMap<>();
  private final OptionDef definition;

  public Options(Map<String, String> originals, OptionType ot) {
    this.optionType = ot;
    this.originals = originals != null ? originals : new HashMap<>();
    this.definition =
        new OptionDef()
            .define(GRAPH, Type.STRING, true, GROUP_GENERAL)
            .define(URL, Type.STRING, true, GROUP_GENERAL)
            .define(VERSION, Type.STRING, GROUP_GENERAL)
            .define(USERNAME, Type.STRING, GROUP_AUTH)
            .define(PASSWORD, Type.STRING, GROUP_AUTH)
            .define(SECRET, Type.STRING, GROUP_AUTH)
            .define(TOKEN, Type.STRING, GROUP_AUTH)
            .define(
                IO_READ_TIMEOUT_MS,
                Type.INT,
                IO_READ_TIMEOUT_MS_DEFAULT,
                true,
                null,
                GROUP_TRANSPORT_TIMEOUT)
            .define(
                IO_CONNECT_TIMEOUT_MS,
                Type.INT,
                IO_CONNECT_TIMEOUT_MS_DEFAULT,
                true,
                null,
                GROUP_TRANSPORT_TIMEOUT)
            .define(
                IO_RETRY_INTERVAL_MS,
                Type.INT,
                IO_RETRY_INTERVAL_MS_DEFAULT,
                true,
                null,
                GROUP_TRANSPORT_TIMEOUT)
            .define(
                IO_MAX_RETRY_INTERVAL_MS,
                Type.INT,
                IO_MAX_RETRY_INTERVAL_MS_DEFAULT,
                true,
                null,
                GROUP_TRANSPORT_TIMEOUT)
            .define(
                IO_MAX_RETRY_ATTEMPTS,
                Type.INT,
                IO_MAX_RETRY_ATTEMPTS_DEFAULT,
                true,
                null,
                GROUP_TRANSPORT_TIMEOUT)
            .define(
                SSL_MODE,
                Type.STRING,
                SSL_MODE_BASIC,
                true,
                OptionDef.ValidString.in(
                    SSL_MODE_BASIC, SSL_MODE_VERIFY_CA, SSL_MODE_VERIFY_HOSTNAME),
                GROUP_SSL)
            .define(SSL_TRUSTSTORE, Type.STRING, null, false, null, GROUP_SSL)
            .define(
                SSL_TRUSTSTORE_TYPE,
                Type.STRING,
                SSL_TRUSTSTORE_TYPE_DEFAULT,
                false,
                null,
                GROUP_SSL)
            .define(SSL_TRUSTSTORE_PASSWORD, Type.STRING, null, false, null, GROUP_SSL);

    if (OptionType.WRITE.equals(ot)) {
      this.definition
          .define(LOADING_JOB, Type.STRING, true, GROUP_LOADING_JOB)
          .define(LOADING_FILENAME, Type.STRING, true, GROUP_LOADING_JOB)
          .define(
              LOADING_SEPARATOR,
              Type.STRING,
              LOADING_SEPARATOR_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB)
          .define(LOADING_EOL, Type.STRING, LOADING_EOL_DEFAULT, true, null, GROUP_LOADING_JOB)
          .define(
              LOADING_BATCH_SIZE_BYTES,
              Type.INT,
              LOADING_BATCH_SIZE_BYTES_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB)
          .define(
              LOADING_TIMEOUT_MS,
              Type.INT,
              LOADING_TIMEOUT_MS_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB)
          .define(LOADING_MAX_PERCENT_ERROR, Type.DOUBLE, GROUP_LOADING_JOB)
          .define(LOADING_MAX_NUM_ERROR, Type.INT, GROUP_LOADING_JOB)
          .define(
              LOADING_RETRY_INTERVAL_MS,
              Type.INT,
              LOADING_RETRY_INTERVAL_MS_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB)
          .define(
              LOADING_MAX_RETRY_INTERVAL_MS,
              Type.INT,
              LOADING_MAX_RETRY_INTERVAL_MS_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB)
          .define(
              LOADING_MAX_RETRY_ATTEMPTS,
              Type.INT,
              LOADING_MAX_RETRY_ATTEMPTS_DEFAULT,
              true,
              null,
              GROUP_LOADING_JOB);
    }
  }

  /**
   * validate all the Options on their type and validator, then put the parsed value into map
   * 'transformed'.
   *
   * <p>Visible for testing
   *
   * @return the errors of validation, If the returned List's size is 0, there is no validation
   *     error.
   */
  protected List<OptionError> validateOpts() {
    List<OptionError> errors = new ArrayList<>();
    this.definition
        .optionKeys()
        .forEach(
            (k, v) -> {
              OptionError err = null;
              String key = v.name;
              Serializable value = null;
              try {
                value = this.parse(v.name);
                transformed.put(key, value);
              } catch (Exception e) {
                err = new OptionError(key, value, e.getMessage());
              }
              try {
                if (v.validator != null && this.containsOption(key)) {
                  v.validator.ensureValid(key, value);
                }
              } catch (Exception e) {
                if (err == null) {
                  err = new OptionError(key, value, e.getMessage());
                } else {
                  err.getErrorMsgs().add(e.getMessage());
                }
              }
              if (err != null) {
                errors.add(err);
              }
            });
    return errors;
  }

  public void validate() {
    List<OptionError> errors = validateOpts();
    if (errors != null && errors.size() > 0) {
      throw new IllegalArgumentException(
          "Invalid input options: "
              + errors.stream().map(e -> e.toString()).reduce(". ", String::concat));
    }
  }

  /**
   * Determine if the Option is contained. The required option will be considered to always exist.
   */
  public boolean containsOption(String key) {
    if (!this.originals.containsKey(key)) {
      if (this.definition.optionKeys().containsKey(key)) {
        OptionKey optionKey = this.definition.optionKeys().get(key);
        if (optionKey.required || optionKey.hasDefault()) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  /**
   * Gets the value of the specified Option, converts the original value to the corresponding type,
   * but does not attempt to convert the default value.
   *
   * @param key the name of Option
   * @return the value of Option
   */
  private Serializable parse(String key) {
    if (this.definition.optionKeys().containsKey(key)) {
      OptionKey optionKey = this.definition.optionKeys().get(key);
      if (this.originals.containsKey(key)) {
        String value = this.originals.get(key);
        String trimmed = null;
        if (value != null) {
          trimmed = value.trim();
        }
        Type type = optionKey.type;
        try {
          switch (type) {
            case BOOLEAN:
              if (trimmed != null && trimmed.equalsIgnoreCase("true")) return true;
              else if (trimmed != null && trimmed.equalsIgnoreCase("false")) return false;
              else throw new IllegalArgumentException("Expected value to be either true or false");
            case STRING:
              return value;
            case INT:
              return Integer.parseInt(trimmed);
            case LONG:
              return Long.parseLong(trimmed);
            case DOUBLE:
              return Double.parseDouble(trimmed);
            default:
              throw new IllegalStateException("Unknown type.");
          }
        } catch (Exception e) {
          throw new IllegalArgumentException(
              "Option("
                  + key
                  + ") failed to convert the value to type "
                  + type
                  + ", error is "
                  + e.toString());
        }
      } else {
        if (optionKey.hasDefault()) {
          return optionKey.defaultValue;
        } else if (optionKey.required) {
          throw new IllegalArgumentException(
              "Option(" + key + ") has no default value and has not been set to a value");
        } else {
          return null;
        }
      }
    } else {
      throw new IllegalArgumentException("Option(" + key + ") is not defined");
    }
  }

  /**
   * Retrive the value from transformed option map. Retrive it from the original options if not in
   * transformed map.
   *
   * @param key
   */
  public Object get(String key) {
    if (transformed.containsKey(key)) {
      return transformed.get(key);
    } else if (originals.containsKey(key)) {
      return originals.get(key);
    } else {
      return null;
    }
  }

  public String getString(String key) {
    return (String) get(key);
  }

  public Integer getInt(String key) {
    return (Integer) get(key);
  }

  public Long getLong(String key) {
    return (Long) get(key);
  }

  public Double getDouble(String key) {
    return (Double) get(key);
  }

  public Map<String, String> getOriginals() {
    return originals;
  }

  public OptionType getOptionType() {
    return this.optionType;
  }
}
