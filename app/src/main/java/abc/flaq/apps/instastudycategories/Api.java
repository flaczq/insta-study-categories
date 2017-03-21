package abc.flaq.apps.instastudycategories;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.API_CATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.Constants.API_CREDENTIALS;
import static abc.flaq.apps.instastudycategories.Constants.API_SUBCATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.Constants.API_USERS_URL;

public class Api {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Calendar calendar = Calendar.getInstance();

    private static String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = is.read();
        while (i != -1) {
            baos.write(i);
            i = is.read();
        }
        return baos.toString();
    }

    private static InputStream getConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Authorization", API_CREDENTIALS);
        connection.connect();
        InputStream is = new BufferedInputStream(connection.getInputStream());
        return is;
    }

    private static void correctDate(EveObject eveObject) {
        Date date = eveObject.getCreated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        eveObject.setCreated(calendar.getTime());

        date = eveObject.getUpdated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        eveObject.setUpdated(calendar.getTime());
    }

    private static JSONArray getItems(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray jsonArray = jsonObject.getJSONArray("_items");
        return jsonArray;
    }

    public static List<Category> getCategories() throws IOException, JSONException {
        InputStream is = getConnection(API_CATEGORIES_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Category category = mapper.readValue(jsonArray.getString(i), Category.class);
            correctDate(category);
            categories.add(category);
        }

        return categories;
    }
    public static List<Subcategory> getSubcategories() throws IOException, JSONException {
        InputStream is = getConnection(API_SUBCATEGORIES_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<Subcategory> subcategories = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Subcategory subcategory = mapper.readValue(jsonArray.getString(i), Subcategory.class);
            correctDate(subcategory);
            subcategories.add(subcategory);
        }

        return subcategories;
    }
    public static List<User> getUsers() throws IOException, JSONException {
        InputStream is = getConnection(API_USERS_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            User user = mapper.readValue(jsonArray.getString(i), User.class);
            correctDate(user);
            users.add(user);
        }

        return users;
    }

}
