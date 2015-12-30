package example ;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Hello {
  
    public static class RequestClass {
      private String name;
      private int value;
      public RequestClass() {}
      public String getName() { return name ; }
      public void setName(String n) { name = n ; }
      public int getValue() {
        return value;
      }
      public void setValue(int value) {
        this.value = value;
      }

    }


    public String myHandler(RequestClass input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : name " + input.getName() + " value: " + input.getValue());
        return  "Recieved: " + input.getName() + "=" + input.getValue() ;
    }
}
