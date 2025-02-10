package com.seattleweb.emprouter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QpidConfigurer {

	public static Logger logger = LoggerFactory.getLogger(QpidConfigurer.class);

	public static void configure(List<AppConfig> apps) throws IOException {

		List<String> toAppQueueNames = new ArrayList<String>();

		for (AppConfig appConfig : apps) {
			if (appConfig.getQueueName() != null && appConfig.getQueueName().length() > 0) {
				createQueue(appConfig.getQueueName());
				toAppQueueNames.add(appConfig.getQueueName());
			}
		}
		createQueue("FromAppQueue");
		createExchange("FromApp.Ex", List.of("FromAppQueue"));
		createExchange("ToApp.Ex", toAppQueueNames);
	}

	// docker run -d -p 5672:5672 -p 8080:8080 --name qpid apache/qpid-broker-j
	// curl -v --user admin:admin -X PUT  -d '{"durable":true}' http://localhost:8080/api/latest/queue/default/default/dkqueue4   
	// curl -v --user admin:admin -X PUT  -d '{"durable":true,"type":"direct","durableBindings":[{"bindingKey" : "bro3","destination":"dkqueue3" } ]}' http://localhost:8080/api/latest/exchange/default/default/FromApp2e

	private static void createQueue(String queueName) throws IOException{

		logger.info("creating queue "+ queueName);
		String postBody = "{\"durable\":true}";

		OkHttpClient client = new OkHttpClient();
    
		Request request = new Request.Builder()
		  .url("http://localhost:8080/api/latest/queue/default/default/" + queueName)
		  .addHeader("Authorization", Credentials.basic("admin", "admin"))
		  .put(RequestBody.create(
			MediaType.parse("application/json"), postBody))
		  .build();
	
		Call call = client.newCall(request);
		Response response = call.execute();
		ResponseBody body = response.body();
		body.close();

	}

	private static void createExchange(String exchangeName, List<String> queues) throws IOException{

		logger.info("creating queue "+ exchangeName);
		//String postBody = "{\"durable\":true, \"type\":\"direct\",\"durableBindings\":[{\"bindingKey\" : \"bro3\",\"destination\":\"FromAppQueue\" } ]}";
		String postBody = "{\"durable\":true, \"type\":\"direct\"}";

		OkHttpClient client = new OkHttpClient();
    
		Request request = new Request.Builder()
		  .url("http://localhost:8080/api/latest/exchange/default/default/" + exchangeName)
		  .addHeader("Authorization", Credentials.basic("admin", "admin"))
		  .put(RequestBody.create(
			MediaType.parse("application/json"), postBody))
		  .build();
	
		Call call = client.newCall(request);
		Response response = call.execute();
		ResponseBody body = response.body();
		body.close();
	}
}