# Vertx Krueger

This library creates Vertx web endpoints exposing application metrics that can be consumed by Krueger (https://github.com/zalando/krueger-cockpit)

## Usage


    val mainRouter = Router.router(vertx);
    mainRouter.mountSubRouter("/", KruegerRouter.create(vertx))

Above should expose application metrics that Krueger can consume:

    /metrics
    /metrics/statusCodes
    /metrics/vertx
    /metrics/eventBus
    /env
    /properties
    /health

### Release build

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
