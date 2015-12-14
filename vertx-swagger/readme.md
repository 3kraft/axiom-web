# Vertx swagger

This library creates vertx web bindings using the API description in the swagger.json (spec v2) file.

## Usage

Define the bindings and retrieve the router:

    Router router = SwaggerRouter.swaggerDefinition("/swagger-minimal.json")
            .bindTo("/products")
                .get(ProductParameter.class, controller::get)
                .doBind()
            .router(vertx);

Then use the router to start an http server:

    vertx.createHttpServer().requestHandler(requestHandler -> router.accept(requestHandler)).listen(8080);

The returned router is a normal vertx router, so it can be used to bind additional controllers to paths.

To collect metrics just enable it in the configuration:

    Router router = SwaggerRouter.configure().collectMetrics()
                                 .swaggerDefinition("/swagger-minimal.json")
                                 .bindTo("/products")
                                     .get(ProductParameter.class, controller::get)
                                     .doBind()
                                 .router(vertx);

_Note:_ Metrics are collected only for the routes defined in the swagger definition.

_Note:_ The API base path is taken from the swagger definition file! The bindings should contain only the relative paths
to the base path.

# License

Copyright 2015 Zalando SE

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
