package jp.classmethod.aws.gradle.sample;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Configuration
@ComponentScan
@SpringBootApplication
@Controller
public class FoobarApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(FoobarApplication.class, args);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<String> index() {
		return ResponseEntity.ok("hello");
	}
}
