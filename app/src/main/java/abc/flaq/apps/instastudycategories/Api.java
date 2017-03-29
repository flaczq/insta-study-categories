package abc.flaq.apps.instastudycategories;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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

    private static String getStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = is.read();
        while (i != -1) {
            baos.write(i);
            i = is.read();
        }
        return baos.toString();
    }

    // fixme: use library
    private static InputStream getRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Authorization", API_CREDENTIALS);
        connection.connect();
        InputStream is = new BufferedInputStream(connection.getInputStream());
        return is;
    }
    private static InputStream postRequest(String url, String data) throws IOException {
        byte[] byteData = data.getBytes();
        int byteDataLength = byteData.length;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(byteDataLength));
        connection.setRequestProperty("Authorization", API_CREDENTIALS);
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.write(byteData);
        connection.connect();
        InputStream is = new BufferedInputStream(connection.getInputStream());
        dos.flush();
        dos.close();
        return is;
    }
    private static InputStream putRequest(String url, String etag, String data) throws IOException {
        byte[] byteData = data.getBytes();
        int byteDataLength = byteData.length;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("If-Match", etag);
        connection.setRequestProperty("Content-Length", Integer.toString(byteDataLength));
        connection.setRequestProperty("Authorization", API_CREDENTIALS);
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.write(byteData);
        connection.connect();
        InputStream is = new BufferedInputStream(connection.getInputStream());
        dos.flush();
        dos.close();
        return is;
    }
    private static InputStream deleteRequest(String url, String etag) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("If-Match", etag);
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

    private static String getWhere(String field, String value) {
        return "?where='" + field + "': '" + value + "'}";
    }

    // CATEGORIES
    public static List<Category> getAllCategories(boolean force) throws IOException, JSONException {
        if (false && !force && allCategories.size() > 0) {
            return allCategories;
        }

        InputStream is = getRequest(API_CATEGORIES_URL);
        String data = getStream(is);
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
        if (false && allCategories.size() > 0) {
            for (Category category : allCategories) {
                if (id.equals(category.getId())) {
                    return category;
                }
            }
        }

        InputStream is = getRequest(API_CATEGORIES_URL + "/" + id);
        String data = getStream(is);
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
    public static List<Subcategory> getAllSubcategories(boolean force) throws IOException, JSONException {
        if (false && !force && allSubcategories.size() > 0) {
            return allSubcategories;
        }

        InputStream is = getRequest(API_SUBCATEGORIES_URL);
        String data = getStream(is);
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
        if (false && allSubcategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                if (id.equals(subcategory.getId())) {
                    return subcategory;
                }
            }
        }

        InputStream is = getRequest(API_SUBCATEGORIES_URL + "/" + id);
        String data = getStream(is);

        Subcategory subcategory = mapper.readValue(data, Subcategory.class);
        if (id.equals(subcategory.getId())) {
            correctDate(subcategory);
            return subcategory;
        }

        return null;
    }
    public static List<Subcategory> getSubcategoriesByCategoryId(String categoryId) throws IOException, JSONException {
        List<Subcategory> subcategories = new ArrayList<>();

        if (false && allCategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                if (subcategory.getCategories().contains(categoryId)) {
                    subcategories.add(subcategory);
                }
            }
        } else {
            InputStream is = getRequest(API_SUBCATEGORIES_URL + getWhere("categories", categoryId));
            String data = getStream(is);
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
    public static List<User> getAllUsers(boolean force) throws IOException, JSONException {
        if (false && !force && allUsers.size() > 0) {
            return allUsers;
        }

        InputStream is = getRequest(API_USERS_URL);
        String data = getStream(is);
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
        if (false && allUsers.size() > 0) {
            for (User user : allUsers) {
                if (id.equals(user.getId())) {
                    return user;
                }
            }
        }

        InputStream is = getRequest(API_USERS_URL + "/" + id);
        String data = getStream(is);

        User user = mapper.readValue(data, User.class);
        if (id.equals(user.getId())) {
            correctDate(user);
            return user;
        }

        return null;
    }
    public static List<User> getUsersByCategorySubcategoryId(String categorySubcategoryId, Boolean asSubcategory) throws IOException, JSONException {
        List<User> users = new ArrayList<>();

        if (false && allUsers.size() > 0) {
            for (User user : allUsers) {
                List<String> list = (asSubcategory ? user.getSubcategories() : user.getCategories());
                if (list.contains(categorySubcategoryId)) {
                    users.add(user);
                }
            }
        } else {
            String field = (asSubcategory ? "subcategories" : "categories");
            InputStream is = getRequest(API_USERS_URL + getWhere(field, categorySubcategoryId));
            String data = getStream(is);
            JSONArray jsonArray = getItems(data);

            for (int i = 0; i < jsonArray.length(); i++) {
                User user = mapper.readValue(jsonArray.getString(i), User.class);
                correctDate(user);
                users.add(user);
            }
        }

        return users;
    }
    public static Boolean deleteUserById(User user) throws IOException, JSONException {
        InputStream is = deleteRequest(API_USERS_URL + "/" + user.getId(), user.getEtag());
        String data = getStream(is);
        // todo: try to change local data first, if not - call api
        //getAllUsers(Boolean.TRUE);

        return Boolean.FALSE;
    }

}
