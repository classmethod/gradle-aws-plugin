//
// Borrowed from https://medium.com/@benorama/how-to-build-a-microservice-with-aws-lambda-in-groovy-4f7384c3b804#.9z2rs5hud
//

package example

import com.amazonaws.services.lambda.runtime.Context

class Hello {
  Map myHandler(data, Context context) {
    context.logger.log "received in groovy: $data"
    [greeting: "Hello, ${data?.firstName} ${data?.lastName}".toString()]
  }

}
