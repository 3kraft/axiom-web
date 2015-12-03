# Vertx swagger

This library creates vertx web bindings using the API description in the swagger.json file.

## Usage

1. define the API with the swagger standard
2. set the operationId as the full class name + the method name. For example:

    foo.bar.MyController.getFoo

3. load the swagger file

    jsonStream = this.getClass().getResourceAsStream("/swagger-minimal.json")

4. add the controllers to the router

    SwaggerRouter router = SwaggerRouter.router(vertx);
    router.controller(new MyController())
          .controller(new MyControllerNG());
    router.setupRoutes(jsonStream);

5. start vertx with the router

    vertx.createHttpServer().requestHandler(requestHandler -> router.accept(requestHandler)).listen(8080);

# License

Copyright 20015 Zalando SE

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