import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String API_KEY = "fa933b60-d866-4934-b377-2ad896f87f69";
    public static void main(String[] args) {
        try {

            String lat = "55.75"; // Широта
            String lon = "37.62"; // Долгота
            int limit = 7;        // Количество дней для средней температуры
            String query = String.format("?lat=%s&lon=%s&limit=%d", lat, lon, limit);
            URL url = new URL(API_URL + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-API-Key", API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();
                System.out.println("Полный JSON-ответ:");
                System.out.println(jsonResponse);
                String tempPrefix = "\"temp\":";
                int tempIndex = jsonResponse.indexOf(tempPrefix);
                if (tempIndex != -1) {
                    int start = tempIndex + tempPrefix.length();
                    int end = jsonResponse.indexOf(",", start);
                    int currentTemp = Integer.parseInt(jsonResponse.substring(start, end));
                    System.out.println("Текущая температура: " + currentTemp);
                }

                String forecastsPrefix = "\"forecasts\":";
                int forecastsStart = jsonResponse.indexOf(forecastsPrefix);
                if (forecastsStart != -1) {
                    int forecastsEnd = jsonResponse.indexOf("]", forecastsStart);
                    String forecastsJson = jsonResponse.substring(forecastsStart + forecastsPrefix.length(), forecastsEnd + 1);
                    String[] forecastEntries = forecastsJson.split("\\},\\{");
                    int totalTemp = 0;
                    int count = Math.min(forecastEntries.length, limit);

                    for (int i = 0; i < count; i++) {
                        String entry = forecastEntries[i];
                        int dayTempIndex = entry.indexOf("\"temp_avg\":");
                        if (dayTempIndex != -1) {
                            int startTemp = dayTempIndex + "\"temp_avg\":".length();
                            int endTemp = entry.indexOf(",", startTemp);
                            if (endTemp == -1) {
                                endTemp = entry.indexOf("}", startTemp);
                            }
                            int dayTemp = Integer.parseInt(entry.substring(startTemp, endTemp));
                            totalTemp += dayTemp;
                        }
                    }

                    double averageTemp = (double) totalTemp / count;
                    System.out.println("Средняя температура за " + count + " дней: " + averageTemp);
                }
            } else {
                System.out.println("Ошибка: Код ответа сервера " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
