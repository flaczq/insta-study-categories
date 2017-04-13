package abc.flaq.apps.instastudycategories.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.EveObject;
import abc.flaq.apps.instastudycategories.pojo.Response;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

import static abc.flaq.apps.instastudycategories.utils.Constants.API_CATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_CREDENTIALS;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_SUBCATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_USERS_URL;

public class Api {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Calendar calendar = Calendar.getInstance();

    private static List<Category> allCategories = new ArrayList<>();
    private static List<Subcategory> allSubcategories = new ArrayList<>();
    private static List<User> allUsers = new ArrayList<>();

    public static String getStream(InputStreamReader isr) throws IOException {
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            if (!br.ready()) {
                break;
            }
        }
        br.close();
        return sb.toString();
    }
    private static InputStreamReader handleResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream;
        int responseCode = connection.getResponseCode();

        if (responseCode >= 200 && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
            // Just in case
            if (Utils.isEmpty(inputStream)) {
                inputStream = connection.getInputStream();
            } else {
                Utils.logError("Api", responseCode + "/" + connection.getResponseMessage());
            }
        }

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return inputStreamReader;
    }
    public static InputStreamReader simplePost(String url, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
        connection.setDoInput(true);
        connection.setUseCaches(false);

        OutputStream os = connection.getOutputStream();
        os.write(data.getBytes("UTF-8"));
        os.close();

        return handleResponse(connection);
    }
    public static InputStreamReader simpleGet(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Accept", "application/json");

        return handleResponse(connection);
    }

    private static InputStreamReader getAuthorizedRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", API_CREDENTIALS);

        return handleResponse(connection);
    }
    private static InputStreamReader sendAuthorizedRequest(String method, String url, String etag, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
        if (Utils.isNotEmpty(etag)) {
            connection.setRequestProperty("If-Match", etag);
        }
        connection.setRequestProperty("Authorization", API_CREDENTIALS);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(false);

        OutputStream os = connection.getOutputStream();
        os.write(data.getBytes("UTF-8"));
        os.close();

        return handleResponse(connection);
    }
    private static InputStreamReader postRequest(String url, String data) throws IOException {
        return sendAuthorizedRequest("POST", url, null, data);
    }
    private static InputStreamReader patchRequest(String url, String etag, String data) throws IOException {
        return sendAuthorizedRequest("PATCH", url, etag, data);
    }
    private static InputStreamReader deleteRequest(String url, String etag) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("If-Match", etag);
        connection.setRequestProperty("Authorization", API_CREDENTIALS);

        return handleResponse(connection);
    }

    private static void correctDate(EveObject eveObject) {
        // is it necessary
        /*Date date = eveObject.getCreated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        eveObject.setCreated(calendar.getTime());

        date = eveObject.getUpdated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        eveObject.setUpdated(calendar.getTime());*/
    }

    private static JSONArray getItems(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray items = jsonObject.getJSONArray("_items");
        return items;
    }

    private static String getWhere(Map<String, String> conditions) throws UnsupportedEncodingException {
        String where = "?where={";
        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            where += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
        }
        if (where.endsWith(",")) {
            where = where.substring(0, where.lastIndexOf(","));
        }
        where += "}";
        return where;
    }

    // CATEGORIES
    public static List<Category> getAllCategories(boolean force) throws IOException, JSONException {
        if (!force && allCategories.size() > 0) {
            return allCategories;
        }

        InputStreamReader is = getAuthorizedRequest(API_CATEGORIES_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            Category category = mapper.readValue(items.getString(i), Category.class);
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

        InputStreamReader is = getAuthorizedRequest(API_CATEGORIES_URL + "/" + id);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        for (int i = 0; i < items.length(); i++) {
            Category category = mapper.readValue(items.getString(i), Category.class);
            if (id.equals(category.getId())) {
                correctDate(category);
                return category;
            }
        }

        return null;
    }

    // SUBCATEGORIES
    public static List<Subcategory> getAllSubcategories(boolean force) throws IOException, JSONException {
        if (!force && allSubcategories.size() > 0) {
            return allSubcategories;
        }

        InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        List<Subcategory> subcategories = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            Subcategory subcategory = mapper.readValue(items.getString(i), Subcategory.class);
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

        InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL + "/" + id);
        String stream = getStream(is);

        Subcategory subcategory = mapper.readValue(stream, Subcategory.class);
        if (id.equals(subcategory.getId())) {
            correctDate(subcategory);
            return subcategory;
        }

        return null;
    }
    public static List<Subcategory> getSubcategoriesByCategoryId(String categoryId) throws IOException, JSONException {
        List<Subcategory> subcategories = new ArrayList<>();

        if (allCategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                if (subcategory.getCategories().contains(categoryId)) {
                    subcategories.add(subcategory);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("categories", categoryId);
            InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                Subcategory subcategory = mapper.readValue(items.getString(i), Subcategory.class);
                correctDate(subcategory);
                subcategories.add(subcategory);
            }
        }

        return subcategories;
    }

    // USERS
    public static List<User> getAllUsers(boolean force) throws IOException, JSONException {
        if (!force && allUsers.size() > 0) {
            return allUsers;
        }

        InputStreamReader is = getAuthorizedRequest(API_USERS_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            User user = mapper.readValue(items.getString(i), User.class);
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

        InputStreamReader is = getAuthorizedRequest(API_USERS_URL + "/" + id);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        for (int i = 0; i < items.length(); i++) {
            User user = mapper.readValue(items.getString(i), User.class);
            if (id.equals(user.getId())) {
                correctDate(user);
                return user;
            }
        }

        return null;
    }
    public static User getUserByInstagramId(String instagramId) throws IOException, JSONException {
        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (instagramId.equals(user.getInstagramId())) {
                    return user;
                }
            }
        }

        Map<String, String> conditions = new HashMap<>();
        conditions.put("instagramId", instagramId);
        InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        for (int i = 0; i < items.length(); i++) {
            User user = mapper.readValue(items.getString(i), User.class);
            if (instagramId.equals(user.getInstagramId())) {
                correctDate(user);
                return user;
            }
        }

        return null;
    }
    public static List<User> getUsersByCategoryId(String categoryId) throws IOException, JSONException {
        List<User> users = new ArrayList<>();

        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (user.getCategories().contains(categoryId)) {
                    users.add(user);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("categories", categoryId);
            InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                User user = mapper.readValue(items.getString(i), User.class);
                correctDate(user);
                users.add(user);
            }
        }

        return users;
    }
    public static List<User> getUsersBySubcategoryId(String subcategoryId) throws IOException, JSONException {
        List<User> users = new ArrayList<>();

        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (user.getSubcategories().contains(subcategoryId)) {
                    users.add(user);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("subcategories", subcategoryId);
            InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                User user = mapper.readValue(items.getString(i), User.class);
                correctDate(user);
                users.add(user);
            }
        }

        return users;
    }
    public static Boolean addUser(User user) throws IOException, JSONException {
        InputStreamReader is = postRequest(API_USERS_URL, user.toPostJson());
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api", response.toString());
            return Boolean.FALSE;
        }
        getAllUsers(true);
        // Not needed
        //user.update(response);

        return Boolean.TRUE;
    }
    public static Boolean addUserToCategory(User user, String categoryId) throws IOException, JSONException {
        List<String> categories = user.getCategories();
        if (categories.contains(categoryId)) {
            Utils.logDebug("Api", "The user: " + user.toString() + " is already in category: " + categoryId);
            return Boolean.FALSE;
        }

        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"categories\":" + Utils.listToString(categories) + "," +
                "\"categoriesSize\":" + categories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api", response.toString());
            return Boolean.FALSE;
        }

        categories.add(categoryId);
        getAllUsers(true);
        user.update(response);

        return Boolean.TRUE;
    }
    public static Boolean addUserToSubcategory(User user, String subcategoryId) throws IOException, JSONException {
        List<String> subcategories = user.getSubcategories();
        if (subcategories.contains(subcategoryId)) {
            Utils.logDebug("Api", "User " + user.getUsername() + " is already in subcategory " + subcategoryId);
            return Boolean.FALSE;
        }

        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"subcategories\":" + Utils.listToString(subcategories) + "," +
                "\"subcategoriesSize\":" + subcategories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api", response.toString());
            return Boolean.FALSE;
        }

        subcategories.add(subcategoryId);
        getAllUsers(true);
        user.update(response);

        return Boolean.TRUE;
    }
    public static Boolean deleteUser(User user) throws IOException, JSONException {
        InputStreamReader is = patchRequest(API_USERS_URL + "/" + user.getId(), user.getEtag(), "{\"active\":false}");
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api", response.toString());
            return Boolean.FALSE;
        }

        getAllUsers(true);
        // TODO: check if needed
        //user.setActive(Boolean.FALSE);
        user.update(response);

        return Boolean.TRUE;
    }

}
