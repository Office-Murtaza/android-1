package system.tools;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import system.service.UserService;

import java.io.IOException;

@Component
public class RequestToolkit {

    @Autowired
    UserService userService;

    public void sendDeviceCommand(String deviceName, String event, String key) {
        CloseableHttpClient client = HttpClients.createDefault();
        event = event.toUpperCase();
        HttpPost post = new HttpPost("https://maker.ifttt.com/trigger/" + deviceName + "_" + event + "/with/key/" + key);

        try {
            CloseableHttpResponse response = client.execute(post);
            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
