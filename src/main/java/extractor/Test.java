package extractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;

public class Test {
	public static void main(String[] args) {
		Map<String,String> configMapper = new ConcurrentHashMap<>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("config"),
					"UTF8"));
			String currentLine = "";
			while((currentLine = br.readLine())!=null){
				String[] arr = currentLine.split("=");
				configMapper.put(arr[0],arr[1]);
			}
			System.out.println(configMapper);
			br.close();
			
			SslContextFactory sslContextFactory = new SslContextFactory();
			HttpClient httpClient = new HttpClient(sslContextFactory);
			httpClient.setFollowRedirects(false);
			httpClient.start();
			
			String key = configMapper.get("API_KEY");
			String address = "Hà Nội";
			
			ContentResponse response = httpClient.newRequest("https://maps.googleapis.com/maps/api/geocode/json").method(HttpMethod.GET).param("address", address).param("key", key).send();
			JSONObject jsonObj = new JSONObject(response.getContentAsString());
			JSONObject location = jsonObj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
			System.out.println("Lat: " +location.get("lat"));
			System.out.println("Lng: " +location.get("lng"));
			httpClient.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
