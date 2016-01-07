# Vertx swagger

This library creates vertx web bindings using the API description in the swagger.json (spec v2) file.

## Usage

Define the bindings and retrieve the router:

    Router router = SwaggerRouter.swaggerDefinition("/swagger-definition.json")
            .bindTo("/products")
                .get(ProductParameter.class, controller::get)
                .doBind()
            .router(vertx);

For some operations there is a short hand version as well. This only works if the path has only one operation.

    Router router = SwaggerRouter.swaggerDefinition("/swagger-definition.json")
            .get("/products", ProductParameter.class, controller::get)
            .router(vertx);

Then use the router to start an http server:

    vertx.createHttpServer().requestHandler(requestHandler -> router.accept(requestHandler)).listen(8080);

The returned router is a normal vertx router, so it can be used to bind additional controllers to paths.

_Note:_ The API base path is taken from the swagger definition file! The bindings should contain only the relative paths
to the base path.

## Metrics

To collect metrics just enable it in the configuration:

    Router router = SwaggerRouter.configure().collectMetrics()
                                 .swaggerDefinition("/swagger-minimal.json")
                                 .bindTo("/products")
                                     .get(ProductParameter.class, controller::get)
                                     .doBind()
                                 .router(vertx);

_Note:_ Metrics are collected only for the routes defined in the swagger definition.

## Operations

### Get

There are 3 types of get operations implemented:

* with zero parameters - get all functionality
* with one parameter (int or String) - get by Id functionality
* with n parameters - for query parameters and such.

For the n parameter case a parameter object is required. The field names of the parameters object must match the ones in
the swagger definitions.

If the get operation returns `null`, then `404` is returned as status.

If a query parameter is required, but not present, then `400` is returned as a status.

### Post

Currently only `application/json` is supported as body. The object is converted to the type given in the API.

    SwaggerRouter.swaggerDefinition("/swagger-post.json")
                        .bindTo("/products")
                            .post(Product.class, controller::create)
                            .doBind()
                        .router(vertx));

If the `Location` header is specified in the swagger definitions then:

* an id is taken from the return object, which is either a String (id) or an object with a method `String getId()`.
* the `Location` header is set to: `Location: /{BINDING_PATH}/{ID}`

### Delete

A single parameter is required, and interpreted as id. If no exceptions occur `204` is returned as status.

### Building

Gradle expects to have couple of build properties:

    #~/.gradle/gradle.properties

    org.gradle.daemon=true
    signing.keyId=
    # signing.password=
    signing.secretKeyRingFile=/home/USER/.gnupg/secring.gpg

    sonatypeUsername=
    sonatypePassword=


# License

Copyright 2016 Zalando SE

http://opensource.org/licenses/MIT

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
