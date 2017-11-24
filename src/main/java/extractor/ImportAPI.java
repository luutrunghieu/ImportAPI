package extractor;

import static spark.Spark.port;
import static spark.Spark.post;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import model.Place;
import model.Post;

public class ImportAPI {
	public static Logger infoLogger;
	public static Logger errorlogger;
	public static void main(String[] args) throws UnknownHostException {
		// logger
		PropertyConfigurator.configure("log4j.properties");
		infoLogger = Logger.getLogger("infoLogger");
		errorlogger = Logger.getLogger("errorlogger");
		
		// load config
				Map<String, String> configMapper = new ConcurrentHashMap<>();
				try {
					BufferedReader configBr = new BufferedReader(new InputStreamReader(new FileInputStream("config"), "UTF-8"));
					String currentLine = "";
					while ((currentLine = configBr.readLine()) != null) {
						String[] arr = currentLine.split("=");
						configMapper.put(arr[0], arr[1]);
					}
					System.out.println(configMapper);
					infoLogger.info("Config: "+configMapper);
					configBr.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					errorlogger.error(e);
				}
				
		// connect mongodb
		MongoClient mongoClient = new MongoClient(new MongoClientURI(configMapper.get("CONNECT_URI")));
		DB database = mongoClient.getDB(configMapper.get("DB"));
		DBCollection collection = database.getCollection(configMapper.get("COLLECTION"));

		Map<String, Integer> dayMapper = new HashMap<>();
		dayMapper.put("một", 1);
		dayMapper.put("hai", 1);
		dayMapper.put("ba", 1);

		// load vehicle_dictionary
		Map<String, Integer> vehiclesMapper = new ConcurrentHashMap<>();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("vehicle_dictionary"), "UTF-8"));
			String currentLine = "";
			while ((currentLine = br.readLine()) != null) {
				String[] arr = currentLine.split("\t");
				vehiclesMapper.put(arr[0], Integer.parseInt(arr[1]));
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		port(Integer.parseInt(configMapper.get("SERVICE_POST")));
		post("/import", (req, res) -> {
			infoLogger.info("IP: "+req.ip());
			infoLogger.info(req.protocol()+" POST /import");
			long t1 = System.currentTimeMillis();
			res.header("Access-Control-Allow-Origin", "*");
			try {
				req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
				Part filePart = req.raw().getPart("uploaded_file");
//				String outputFile = filePart.getSubmittedFileName().split("\\.")[0] + "_result.txt";
				try (BufferedReader in = new BufferedReader(new InputStreamReader(filePart.getInputStream(), "UTF-8"));
//						Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("F://[Hieu]//BachKhoaHN//Project3//SourceCode//Python//data//1600//Result//"+ outputFile),"UTF-8"));
						) {
					String sCurrentLine = "";
					// date
					int day = 0;
					int month = 0;
					int year = 0;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					format.setTimeZone(TimeZone.getTimeZone("UTC"));

					// vehicles
					StringBuffer v1 = new StringBuffer();
					StringBuffer v2 = new StringBuffer();

					// place
					StringBuffer p1 = new StringBuffer();
					StringBuffer p2 = new StringBuffer();
					StringBuffer p3 = new StringBuffer();

					// injured_death
					int injured = 0;
					int death = 0;

					String id = "";
					while ((sCurrentLine = in.readLine()) != null) {
						if (sCurrentLine.contains("\tBEGIN")) {
							try {
								day = 0;
								month = 0;
								year = 0;

								v1.setLength(0);
								v2.setLength(0);

								p1.setLength(0);
								p2.setLength(0);
								p3.setLength(0);

								death = 0;
								injured = 0;

								id = getContent(sCurrentLine);
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("BEGIN: " + sCurrentLine);
							}

						}
						if (sCurrentLine.contains("\tDAY")) {
							try {
								String content = getContent(sCurrentLine);
								if (dayMapper.containsKey(content.toLowerCase())) {
									day -= dayMapper.get(content.toLowerCase());
								} else if (content.contains("T")) {
									day = Integer.parseInt(content.split("T")[0]);
								} else if (isInteger(content) && day != 0) {
									day -= Integer.parseInt(content);
								} else {
									day = Integer.parseInt(getContent(sCurrentLine));
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("DAY: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tMONTH")) {
							try {
								month = Integer.parseInt(getContent(sCurrentLine));
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("MONTH: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tYEAR")) {
							try {
								year = Integer.parseInt(getContent(sCurrentLine));
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("YEAR: " + sCurrentLine);
							}
						}

						if (sCurrentLine.contains("\tV1")) {
							try {
								String content = getContent(sCurrentLine);
								if (content.equalsIgnoreCase("đường sắt")) {
									v1.append("tàu hỏa ");
								} else {
									v1.append(getContent(sCurrentLine) + " ");
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("V1: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tV2")) {
							try {
								v2.append(getContent(sCurrentLine) + " ");
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("V2: " + sCurrentLine);
							}
						}

						if (sCurrentLine.contains("\tP1")) {
							try {
								String content = getContent(sCurrentLine);
								if (content.contains("HCM")) {
									p1.append("TP Hồ Chí Minh");
								} else {
									String[] arr = content.split("\\.");
									p1.append(arr[0] + " ");
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("P1: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tP2")) {
							try {
								String content = getContent(sCurrentLine);
								String[] arr = content.split("\\.");
								p2.append(arr[0] + " ");
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("P2: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tP3")) {
							try {
								p3.append(getContent(sCurrentLine) + " ");
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("P3: : " + sCurrentLine);
							}
						}

						if (sCurrentLine.contains("\tDEATH")) {
							try {
								String deathStr = getContent(sCurrentLine);
								if (isInteger(deathStr)) {
									death += Integer.parseInt(deathStr);
								} else {
									death++;
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("DEATH: " + sCurrentLine);
							}
						}

						if (sCurrentLine.contains("\tINJURED")) {
							try {
								String injuredStr = getContent(sCurrentLine);
								if (isInteger(injuredStr)) {
									injured += Integer.parseInt(injuredStr);
								} else if (injuredStr.equalsIgnoreCase("nhiều")) {
									injured = -1;
								} else {
									injured++;
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
								errorlogger.error("INJURED: " + sCurrentLine);
							}
						}
						if (sCurrentLine.contains("\tEND")) {

							Post post = new Post();
							post.setId(new ObjectId(id));
							post.setDead(death);
							post.setInjured(injured);

							Place place = new Place();
							StringBuilder builder = new StringBuilder();
							if (p3.length() != 0) {
								builder.append(p3 + ", ");
							}
							if (p2.length() != 0) {
								builder.append(p2 + ", ");
							}
							if (p1.length() != 0) {
								builder.append(p1);
							}
							place.setRaw(builder.toString());
							place.setCity(p1 + "");

							// get lat, long
							SslContextFactory sslContextFactory = new SslContextFactory();
							HttpClient httpClient = new HttpClient(sslContextFactory);
							httpClient.setFollowRedirects(false);
							try {
								httpClient.start();

								String key = configMapper.get("API_KEY");
								String address = builder.toString();

								ContentResponse response;
								JSONObject result;
								String status;
								response = httpClient.newRequest("https://maps.googleapis.com/maps/api/geocode/json")
										.method(HttpMethod.GET).param("address", address).param("key", key).send();
								result = new JSONObject(response.getContentAsString());
								status = result.getString("status");

								if (status.equalsIgnoreCase("ZERO_RESULTS")) {
									response = httpClient
											.newRequest("https://maps.googleapis.com/maps/api/geocode/json")
											.method(HttpMethod.GET).param("address", p1.toString()).param("key", key)
											.send();
									result = new JSONObject(response.getContentAsString());
									status = result.getString("status");
									if (status.equalsIgnoreCase("ZERO_RESULTS")) {
										result = null;
									}
								}
								if (result != null) {
									JSONObject location = result.getJSONArray("results").getJSONObject(0)
											.getJSONObject("geometry").getJSONObject("location");
									place.setLat(location.get("lat") + "");
									place.setLng(location.get("lng") + "");
								}
								if (place.getLat() == null || place.getLng() == null) {
									errorlogger.error("ID: " + id);
									errorlogger.error("LAT LONG NULL");
								}
							} catch (Exception ex) {
								errorlogger.error(ex);
								errorlogger.error("ID: " + id);
							} finally {
								httpClient.stop();
							}

							post.setPlace(place);
							post.setTime(format.parse(year + "-" + month + "-" + day));
							post.setLevel(2);

							List<Integer> vehiclesList = new ArrayList<>();
							String str_v1 = v1.toString().trim().toLowerCase();
							String str_v2 = v2.toString().trim().toLowerCase();
							if (vehiclesMapper.containsKey(str_v1)) {
								vehiclesList.add(vehiclesMapper.get(str_v1));
							} else if (str_v1.length() != 0) {
								errorlogger.error("ID: " + id);
								errorlogger.error("LIST V1: " + str_v1);
							}
							if (vehiclesMapper.containsKey(str_v2)) {
								vehiclesList.add(vehiclesMapper.get(str_v2));
							} else if (str_v2.length() != 0) {
								errorlogger.error("ID: " + id);
								errorlogger.error("LIST V2: " + str_v2);
							}
							post.setVehicles(vehiclesList);

							// write to files
							// out.write(post.toString());

							// import to mongo
							BasicDBObject searchQuery = new BasicDBObject();
							searchQuery.append("_id", new ObjectId(id));

							collection.update(searchQuery, new BasicDBObject().append("$set", toDBObject(post)));
						}
					}
					in.close();
//					out.close();
				}
				res.body("File uploaded and saved.");
				res.status(200);
				infoLogger.info("Time: " + (System.currentTimeMillis() - t1));
				return res;
			} catch (Exception ex) {
				errorlogger.error(ex);
			}
			res.status(500);
			res.body("Failed");
			infoLogger.info("Time: " + (System.currentTimeMillis() - t1));
			return res;
		});
	}

	public static String getContent(String str) {
		String[] strArr = str.split("\t");
		return strArr[strArr.length - 2];
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	public static final DBObject toDBObject(Post post) {
		return new BasicDBObject("dead", post.getDead()).append("injured", post.getInjured())
				.append("time", post.getTime())
				.append("place",
						new BasicDBObject("raw", post.getPlace().getRaw()).append("city", post.getPlace().getCity())
								.append("latLng", new BasicDBObject("lat", post.getPlace().getLat()).append("lng",
										post.getPlace().getLng())))
				.append("vehicles", post.getVehicles())
				.append("level", post.getLevel());
	}
}
