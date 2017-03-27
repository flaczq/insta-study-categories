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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.API_CATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.Constants.API_CREDENTIALS;
import static abc.flaq.apps.instastudycategories.Constants.API_SUBCATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.Constants.API_USERS_URL;

public class Api {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Calendar calendar = Calendar.getInstance();

    private static List<Category> allCategories = new ArrayList<>();
    private static List<Subcategory> allSubcategories = new ArrayList<>();
    private static List<User> allUsers = new ArrayList<>();

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

    // CATEGORIES
    public static List<Category> getAllCategories() throws IOException, JSONException {
        if (allCategories.size() > 0) {
            return allCategories;
        }

        InputStream is = getConnection(API_CATEGORIES_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Category category = mapper.readValue(jsonArray.getString(i), Category.class);
            correctDate(category);
            categories.add(category);
        }
        Collections.sort(categories);
        allCategories = categories;

        return categories;
    }
    public static Category getCategoryById(String id) throws IOException, JSONException {
        if (allCategories.size() > 0) {
            for (Category category : allCategories) {
                if (id.equals(category.getId())) {
                    return category;
                }
            }
        }

        InputStream is = getConnection(API_CATEGORIES_URL + "/" + id);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        for (int i = 0; i < jsonArray.length(); i++) {
            Category category = mapper.readValue(jsonArray.getString(i), Category.class);
            if (id.equals(category.getId())) {
                correctDate(category);
                return category;
            }
        }

        return null;
    }

    // SUBCATEGORIES
    public static List<Subcategory> getAllSubcategories() throws IOException, JSONException {
        if (allSubcategories.size() > 0) {
            return allSubcategories;
        }

        InputStream is = getConnection(API_SUBCATEGORIES_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<Subcategory> subcategories = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Subcategory subcategory = mapper.readValue(jsonArray.getString(i), Subcategory.class);
            correctDate(subcategory);
            subcategories.add(subcategory);
        }
        Collections.sort(subcategories);
        allSubcategories = subcategories;

        return subcategories;
    }
    public static Subcategory getSubcategoryById(String id) throws IOException, JSONException {
        if (allSubcategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                if (id.equals(subcategory.getId())) {
                    return subcategory;
                }
            }
        }

        InputStream is = getConnection(API_SUBCATEGORIES_URL + "/" + id);
        String data = readStream(is);

        Subcategory subcategory = mapper.readValue(data, Subcategory.class);
        if (id.equals(subcategory.getId())) {
            correctDate(subcategory);
            return subcategory;
        }

        return null;
    }
    public static List<Subcategory> getSubcategoriesByCategoryId(String categoryId) throws IOException, JSONException {
        List<Subcategory> subcategories = new ArrayList<>();

        if (allCategories.size() > 0) {
            Category category = getCategoryById(categoryId);
            if (Utils.isNotEmpty(category)) {
                for (String subcategoryId : category.getSubcategories()) {
                    subcategories.add(getSubcategoryById(subcategoryId));
                }
            }
        } else {
            // TODO: fix me
            InputStream is = getConnection(API_SUBCATEGORIES_URL + "/" + categoryId);
            String data = readStream(is);
            JSONArray jsonArray = getItems(data);

            for (int i = 0; i < jsonArray.length(); i++) {
                Subcategory subcategory = mapper.readValue(jsonArray.getString(i), Subcategory.class);
                correctDate(subcategory);
                subcategories.add(subcategory);
            }
        }

        return subcategories;
    }

    // USERS
    public static List<User> getAllUsers() throws IOException, JSONException {
        if (allUsers.size() > 0) {
            return allUsers;
        }

        InputStream is = getConnection(API_USERS_URL);
        String data = readStream(is);
        JSONArray jsonArray = getItems(data);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            User user = mapper.readValue(jsonArray.getString(i), User.class);
            correctDate(user);
            users.add(user);
        }
        Collections.sort(users);
        allUsers = users;

        return users;
    }
    public static User getUserById(String id) throws IOException, JSONException {
        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (id.equals(user.getId())) {
                    return user;
                }
            }
        }

        InputStream is = getConnection(API_USERS_URL + "/" + id);
        String data = readStream(is);

        User user = mapper.readValue(data, User.class);
        if (id.equals(user.getId())) {
            correctDate(user);
            return user;
        }

        return null;
    }
    public static List<User> getUsersBySubcategoryId(String subcategoryId) throws IOException, JSONException {
        List<User> users = new ArrayList<>();

        if (allUsers.size() > 0) {
            Subcategory subcategory = getSubcategoryById(subcategoryId);
            if (Utils.isNotEmpty(subcategory)) {
                for (String userId : subcategory.getUsers()) {
                    users.add(getUserById(userId));
                }
            }
        } else {
            // fixme
            InputStream is = getConnection(API_USERS_URL + "/" + subcategoryId);
            String data = readStream(is);
            JSONArray jsonArray = getItems(data);

            for (int i = 0; i < jsonArray.length(); i++) {
                User subcategory = mapper.readValue(jsonArray.getString(i), User.class);
                correctDate(subcategory);
                users.add(subcategory);
            }
        }

        return users;
    }

}
