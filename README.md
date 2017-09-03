# Polyglot - a universal grpc command line client

[![Build Status](https://travis-ci.org/grpc-ecosystem/polyglot.svg?branch=master)](https://travis-ci.org/grpc-ecosystem/polyglot)

Polyglot is a grpc client which can talk to any grpc server. In order to make a call, the following are required:
* A compiled Polyglot binary, 
* the .proto files for the service,
* and a request proto instance in text format.

In particular, it is not necessary to generate grpc classes for the service or to compile the protos into the Polyglot binary.

## Features

* Supports unary, client streaming, server streaming, and bidi streaming rpcs.
* Runs on Windows, Mac and Linux.
* Parses proto files at runtime to discover services. Supports pretty-printing discovered services.
* Supports authentication via oauth.
* Accepts request protos through stdin and can output responses to stdout to allow chaining.
* Suppors plain text connections as well as TLS.

## Usage

### Requirements

All you need to run Polyglot is a Java runtime. Binaries for Mac, Linux, and Windows are available from the [releases page](https://github.com/dinowernli/polyglot/releases).

### Making a grpc request

The "Hello World" of using Polyglot is to make an rpc call. This can be done using `call` command as follows:

```
$ echo <json-request> | java -jar polyglot.jar \
    --command=call \
    --endpoint=<host>:<port> \
    --full_method=<some.package.Service/doSomething> \
    --proto_discovery_root=<path>
```

For stream requests double newlines `\n\n` are used to separate your json requests as follows:

```
$ echo '<json-request-1> \n\n <json-request-2> ... \n\n <json-request-n>' | java -jar polyglot.jar \
    --command=call \
    --endpoint=<host>:<port> \
    --full_method=<some.package.Service/doSomething> \
    --proto_discovery_root=<path>
```

Note that on Linux you should be able to just run `./polyglot.jar` as long as you have `binfmt-support` installed.

For more invocation examples, see the [examples](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example) directory.

### Configuration

Some of the features of Polyglot (such as Oauth, see below) require some configuration. Moreover, that sort of configuration tends to remain identical across multiple Polyglot runs. In order to improve usability, Polyglot supports loading a configuration set from a file at runtime. This configuration set can contain multiple named `Configuration` objects (schema defined [here](https://github.com/dinowernli/polyglot/blob/master/src/main/proto/config.proto#L14)). An example configuration could look like this:

```json
{
  "configurations": [
    {
      "name": "production",
      "call_config": {
        "use_tls": "true",
        "oauth_config": {
          "refresh_token_credentials": {
            "token_endpoint_url": "https://auth.example.com/token",
            "client": {
                "id": "example_id",
                "secret": "example_secret"
            },
            "refresh_token_path": "/path/to/refresh/token"
          }
        }
      },
      "proto_config": {
        "proto_discovery_root": "/home/dave/protos",
        "include_paths": [
          "/home/dave/lib"
        ]
      }
    },
    {
      "name": "staging",
      "call_config": {
        "oauth_config": {
          "refresh_token_credentials": {
            "token_endpoint_url": "https://auth-staging.example.com/token"
          }
        }
      },
      "proto_config": {
        "proto_discovery_root": "/home/dave/staging/protos",
        "include_paths": [
          "/home/dave/staging/lib"
        ]
      }
    }
  ]
}
```

By default, Polyglot tries to find a config file at `$HOME/.polyglot/config.pb.json`, but this can be overridden with the `--config_set_path` flag. By default, Polyglot uses the first configuration in the set, but this can be overridden with the `--config_name` flag.

The general philosophy is for the configuration to drive Polyglot's behavior and for command line flags to allow selectively overriding parts of the configuration. For a full list of what can be configured, please see [`config.proto`](https://github.com/dinowernli/polyglot/blob/master/src/main/proto/config.proto#L14).

### Using TLS

Polyglot uses statically linked [boringssl](https://boringssl.googlesource.com/boringssl/) libraries under the hood and doesn't require the host machine to have any specific libraries. Whether or not the client uses TLS to talk to the server can be controlled using the `--use_tls` flag or the corresponding configuration entry.

Polyglot can also do client certificate authentication with the `--tls_client_cert_path` and `--tls_client_key_path` flags. If the hostname on the server does not match the endpoint (e.g. connecting
to `localhost`, but the server thinks it's `foo.example.com`), `--tls_client_override_authority=foo.example.com` can be used.

### Authenticating requests using OAuth

Polyglot has built-in support for authentication of requests using OAuth tokens in two ways:
* Loading an access token from disk and attaching it to the request.
* Loading a refresh token from disk, exchanging it for an access token, and attaching the access token to the request.

In order to use this feature, Polyglot needs an `OauthConfiguration` inside its `Configuration`. For details on how to populate the `OauthConfiguration`, please see the documentation of the fields in [`config.proto`](https://github.com/dinowernli/polyglot/blob/master/src/main/proto/config.proto#L14).

### Listing services

Polyglot supports printing a list of all the discovered services using the `list_services` command. This command can be invoked as follows:

```
$ java -jar polyglot.jar \
    --command=list_services \
    --proto_discovery_root=<path> \
```

The printed services can be filtered using `--service_filter=<service_name>` or `--method_filter=<method_name>`, and the `--with_message` flag can be used to also print the exact format of the requests.

## Build requirements

In order to build Polyglot from source, you will need:

* [Java 8](https://www.oracle.com/downloads/index.html)
* [Bazel](http://bazel.io)

## Building a binary

`$ bazel build src/main/java/me/dinowernli/grpc/polyglot`

After calling this, you should have a fresh binary at:

`./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot`

## Running the examples

Example invocations can be found in the [examples](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example) directory. In order to run a simple rpc call, invoke [`run-server.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/run-server.sh) followed by (in a different terminal) [`call-command-example.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/call-command-example.sh).

## Building and running tests

`$ bazel test //src/...`

## Main contributors

* [Dino Wernli](https://github.com/dinowernli)
