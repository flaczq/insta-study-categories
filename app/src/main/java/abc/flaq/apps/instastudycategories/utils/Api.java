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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.EveObject;
import abc.flaq.apps.instastudycategories.pojo.Response;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

import static abc.flaq.apps.instastudycategories.utils.Constants.API_ALL_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_CATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_CREDENTIALS;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_SUBCATEGORIES_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.API_USERS_URL;

public class Api {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Calendar calendar = Calendar.getInstance();

    private static ArrayList<Category> allCategories = new ArrayList<>();
    private static ArrayList<Subcategory> allSubcategories = new ArrayList<>();
    private static ArrayList<User> allUsers = new ArrayList<>();

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
                Utils.logError("Api.handleResponse()", responseCode + "/" + connection.getResponseMessage());
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

    // Not used, correcting date during format and show
    private static void correctDate(EveObject eveObject) {
        Date date = eveObject.getCreated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        eveObject.setCreated(calendar.getTime());

        date = eveObject.getUpdated();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        eveObject.setUpdated(calendar.getTime());
    }
    private static void correctForeignId(String url, EveObject eveObject) throws IOException {
        String id = eveObject.getId();
        String foreignId = Utils.doForeignId(id);
        InputStreamReader is = patchRequest(url, eveObject.getEtag(), "{\"foreignId\":\"" + foreignId + "\"}");
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.correctForeignId()", stream);
        } else {
            eveObject.updateFromResponse(response);
        }
    }
    private static void correctSize(String url, EveObject eveObject, String field, Integer size) throws IOException {
        InputStreamReader is = patchRequest(url, eveObject.getEtag(), "{\"" + field + "\":\"" + size + "\"}");
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.correctSize()", stream);
        } else {
            eveObject.updateFromResponse(response);
        }
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
    public static ArrayList<Category> getAllCategories(boolean force) throws IOException, JSONException {
        if (!force && allCategories.size() > 0) {
            return allCategories;
        }

        InputStreamReader is = getAuthorizedRequest(API_CATEGORIES_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        Category firstCategory = null;
        ArrayList<Category> categories = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            Category category = mapper.readValue(items.getString(i), Category.class);
            //correctDate(category);
            if (API_ALL_CATEGORY_NAME.equals(category.getName())) {
                firstCategory = category;
            } else {
                categories.add(category);
            }
        }
        Collections.sort(categories);
        // Set "all" category always first
        if (Utils.isNotEmpty(firstCategory)) {
            categories.add(0, firstCategory);
        }
        allCategories = categories;
        Session.getInstance().setCategories(categories);

        return categories;
    }
    public static Category getCategoryById(String id) throws IOException, JSONException {
        InputStreamReader is = getAuthorizedRequest(API_CATEGORIES_URL + "/" + id);
        String stream = getStream(is);
        Category category = mapper.readValue(stream, Category.class);
        return category;
    }
    public static Category getCategoryByName(String name) throws IOException, JSONException {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("name", name);
        InputStreamReader is = getAuthorizedRequest(API_CATEGORIES_URL + getWhere(conditions));
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        for (int i = 0; i < items.length(); i++) {
            Category category = mapper.readValue(items.getString(i), Category.class);
            if (name.equals(category.getName())) {
                //correctDate(category);
                return category;
            }
        }

        return null;
    }
    public static Category addCategory(String categoryName) throws IOException, JSONException {
        Category category = Factory.categoryFromName(categoryName);
        InputStreamReader is = postRequest(API_CATEGORIES_URL, category.toPostJson());
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.addCategory()", stream);
            return null;
        }

        // Set etag
        category.updateFromResponse(response);
        correctForeignId(API_CATEGORIES_URL + "/" + category.getId(), category);
        ///getAllCategories(true);

        return category;
    }
    public static Boolean removeCategory(Category category) throws IOException, JSONException {
        InputStreamReader is = deleteRequest(API_SUBCATEGORIES_URL + "/" + category.getId(), category.getEtag());
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.removeCategory()", stream);
            return Boolean.FALSE;
        }

        getAllCategories(true);

        return Boolean.TRUE;
    }

    // SUBCATEGORIES
    public static ArrayList<Subcategory> getAllSubcategories(boolean force) throws IOException, JSONException {
        if (!force && allSubcategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                for (String categoryForeignId : subcategory.getCategories()) {
                    getSubcategoriesByCategoryForeignId(force, categoryForeignId);
                }
            }
            return allSubcategories;
        }

        InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        ArrayList<Subcategory> subcategories = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            Subcategory subcategory = mapper.readValue(items.getString(i), Subcategory.class);
            //correctDate(subcategory);
            subcategories.add(subcategory);

            for (String categoryForeignId : subcategory.getCategories()) {
                getSubcategoriesByCategoryForeignId(force, categoryForeignId);
            }
        }
        Collections.sort(subcategories);
        allSubcategories = subcategories;

        return subcategories;
    }
    public static Subcategory getSubcategoryById(String id) throws IOException, JSONException {
        InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL + "/" + id);
        String stream = getStream(is);
        Subcategory subcategory = mapper.readValue(stream, Subcategory.class);
        return subcategory;
    }
    public static ArrayList<Subcategory> getSubcategoriesByCategoryForeignId(boolean force, String categoryForeignId) throws IOException, JSONException {
        ArrayList<Subcategory> subcategories = new ArrayList<>();

        if (!force && allSubcategories.size() > 0) {
            for (Subcategory subcategory : allSubcategories) {
                if (subcategory.getCategories().contains(categoryForeignId)) {
                    subcategories.add(subcategory);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("categories", categoryForeignId);
            InputStreamReader is = getAuthorizedRequest(API_SUBCATEGORIES_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                Subcategory subcategory = mapper.readValue(items.getString(i), Subcategory.class);
                //correctDate(subcategory);
                subcategories.add(subcategory);
            }
        }
        Collections.sort(subcategories);
        Session.getInstance().setSubcategories(categoryForeignId, subcategories);

        return subcategories;
    }
    public static Subcategory addSubcategory(String subcategoryName, String parentCategoryForeignId) throws IOException, JSONException {
        Subcategory subcategory = Factory.subcategoryFromName(subcategoryName, parentCategoryForeignId);
        InputStreamReader is = postRequest(API_SUBCATEGORIES_URL, subcategory.toPostJson());
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.addSubcategory()", stream);
            return null;
        }

        // Set etag
        subcategory.updateFromResponse(response);
        correctForeignId(API_SUBCATEGORIES_URL + "/" + subcategory.getId(), subcategory);

        String categoryId = Utils.undoForeignId(parentCategoryForeignId);
        Category category = getCategoryById(categoryId);
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.addSubcategory()", "Zwiększenie liczby podkategorii w kategorii: " + categoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + categoryId,
                    category,
                    "subcategoriesSize",
                    category.getSubcategoriesSize() + 1
            );
        }
        // Update sizes of category
        ///getAllCategories(true);
        ///getAllSubcategories(true);

        return subcategory;
    }
    public static Boolean removeSubcategory(Subcategory subcategory) throws IOException, JSONException {
        InputStreamReader is = deleteRequest(API_SUBCATEGORIES_URL + "/" + subcategory.getId(), subcategory.getEtag());
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.removeSubcategory()", stream);
            return Boolean.FALSE;
        }

        getAllSubcategories(true);

        return Boolean.TRUE;
    }

    // USERS
    public static ArrayList<User> getAllUsers(boolean force) throws IOException, JSONException {
        if (!force && allUsers.size() > 0) {
            return allUsers;
        }

        InputStreamReader is = getAuthorizedRequest(API_USERS_URL);
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            User user = mapper.readValue(items.getString(i), User.class);
            //correctDate(user);
            users.add(user);
            if (Utils.isNotEmpty(Session.getInstance().getUser()) &&
                    Utils.isNotEmpty(Session.getInstance().getUser().getId()) &&
                    Session.getInstance().getUser().getId().equals(user.getId())) {
                Session.getInstance().setUser(user);
            }
        }
        Collections.sort(users);
        allUsers = users;

        return users;
    }
    public static User getUserById(String id) throws IOException, JSONException {
        InputStreamReader is = getAuthorizedRequest(API_USERS_URL + "/" + id);
        String stream = getStream(is);
        User user = mapper.readValue(stream, User.class);
        return user;
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
                //correctDate(user);
                return user;
            }
        }

        return null;
    }
    public static ArrayList<User> getUsersByCategoryForeignId(String categoryForeignId) throws IOException, JSONException {
        ArrayList<User> users = new ArrayList<>();

        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (user.getCategories().contains(categoryForeignId)) {
                    users.add(user);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("categories", categoryForeignId);
            InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                User user = mapper.readValue(items.getString(i), User.class);
                //correctDate(user);
                users.add(user);
            }
        }

        return users;
    }
    public static ArrayList<User> getUsersBySubcategoryForeignId(String subcategoryForeignId) throws IOException, JSONException {
        ArrayList<User> users = new ArrayList<>();

        if (allUsers.size() > 0) {
            for (User user : allUsers) {
                if (user.getSubcategories().contains(subcategoryForeignId)) {
                    users.add(user);
                }
            }
        } else {
            Map<String, String> conditions = new HashMap<>();
            conditions.put("subcategories", subcategoryForeignId);
            InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
            String stream = getStream(is);
            JSONArray items = getItems(stream);

            for (int i = 0; i < items.length(); i++) {
                User user = mapper.readValue(items.getString(i), User.class);
                //correctDate(user);
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
            Utils.logError("Api.addUser()", stream);
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        correctForeignId(API_USERS_URL + "/" + user.getId(), user);
        getAllUsers(true);

        Category category = getCategoryByName(API_ALL_CATEGORY_NAME);
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.addUser()", "Zwiększenie liczby użytkowników w kategorii: " + API_ALL_CATEGORY_NAME + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + category.getId(),
                    category,
                    "usersSize",
                    category.getUsersSize() + 1
            );
        }
        // Update sizes of category
        getAllCategories(true);

        return Boolean.TRUE;
    }
    public static Boolean addUserToCategory(User user, Category category) throws IOException, JSONException {
        ArrayList<String> categories = user.getCategories();
        if (categories.contains(category.getForeignId())) {
            Utils.logDebug("Api.addUserToCategory()", "Użytkownik: " + user.toString() + " znajduje się już w kategorii: " + category.getForeignId());
            return Boolean.FALSE;
        }

        categories.add(category.getForeignId());
        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"categories\":" + Utils.listToString(categories) + "," +
                        "\"categoriesSize\":" + categories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.addUserToCategory()", stream);
            categories.remove(category.getForeignId());
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        String categoryId = category.getId();
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.addUserToCategory()", "Zwiększenie liczby użytkowników w kategorii: " + categoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + categoryId,
                    category,
                    "usersSize",
                    category.getUsersSize() + 1
            );
        }
        // Update sizes of category
        ///getAllCategories(true);

        return Boolean.TRUE;
    }
    public static Boolean addUserToSubcategory(User user, Subcategory subcategory) throws IOException, JSONException {
        ArrayList<String> subcategories = user.getSubcategories();
        if (subcategories.contains(subcategory.getForeignId())) {
            Utils.logDebug("Api.addUserToSubcategory()", "Użytkownik " + user.getUsername() + " znajduje się już w podkategorii: " + subcategory.getForeignId());
            return Boolean.FALSE;
        }

        subcategories.add(subcategory.getForeignId());
        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"subcategories\":" + Utils.listToString(subcategories) + "," +
                        "\"subcategoriesSize\":" + subcategories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.addUserToSubcategory()", stream);
            subcategories.remove(subcategory.getForeignId());
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        // FIXME: jeśli jestem >= 10 userem - przenieść podkategorię do aktywnych
        String subcategoryId = subcategory.getId();
        if (Utils.isEmpty(subcategory)) {
            Utils.logDebug("Api.addUserToSubcategory()", "Zwiększenie liczby użytkowników w podkategorii: " + subcategoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_SUBCATEGORIES_URL + "/" + subcategoryId,
                    subcategory,
                    "usersSize",
                    subcategory.getUsersSize() + 1
            );
        }
        // Update sizes of subcategory
        ///getAllSubcategories(true);

        // FIXME: jeśli jestem >= 10 userem - przenieść kategorię do aktywnych
        // Assuming one subcategory is only in one category
        String categoryId = Utils.undoForeignId(subcategory.getCategories().get(0));
        Category category = getCategoryById(categoryId);
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.addUserToSubcategory()", "Zwiększenie liczby użytkowników w kategorii: " + categoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + categoryId,
                    category,
                    "usersSize",
                    category.getUsersSize() + 1
            );
        }
        // Update sizes of category
        ///getAllCategories(true);

        return Boolean.TRUE;
    }
    public static Boolean updateUser(User user) throws IOException, JSONException {
        if (!isUserActive(user.getUsername())) {
            activateUser(user);
        }
        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"instagramId\":\"" + user.getInstagramId() + "\"," +
                "\"fullname\":\"" + user.getFullname() + "\"," +
                "\"username\":\"" + user.getUsername() + "\"," +
                "\"bio\":\"" + user.getBio() + "\"," +
                "\"profilePicUrl\":\"" + user.getProfilePicUrl() + "\"," +
                "\"followers\":" + user.getFollowers() + "," +
                "\"media\":" + user.getMedia() + "," +
                "\"active\":" + "true}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.updateUser()", stream);
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        return Boolean.TRUE;
    }
    public static Boolean isUserActive(String username) throws IOException, JSONException {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("username", username);
        InputStreamReader is = getAuthorizedRequest(API_USERS_URL + getWhere(conditions));
        String stream = getStream(is);
        JSONArray items = getItems(stream);

        if (items.length() == 0) {
            return Boolean.FALSE;
        }

        User user = mapper.readValue(items.getString(0), User.class);
        return user.isActive();
    }
    public static Boolean activateUser(User user) throws IOException, JSONException {
        InputStreamReader is = patchRequest(API_USERS_URL + "/" + user.getId(), user.getEtag(), "{\"active\":true}");
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.activateUser()", stream);
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        return Boolean.TRUE;
    }

    public static Boolean removeUser(User user) throws IOException, JSONException {
        InputStreamReader is = patchRequest(API_USERS_URL + "/" + user.getId(), user.getEtag(), "{\"active\":false}");
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.removeUser()", stream);
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        // TODO: remove from all categories and subcategories?

        return Boolean.TRUE;
    }
    public static Boolean removeUserFromCategory(User user, Category category) throws IOException, JSONException {
        ArrayList<String> categories = user.getCategories();
        if (!categories.contains(category.getForeignId())) {
            Utils.logDebug("Api.removeUserFromCategory()", "Użytkownik: " + user.toString() + " nie znajdował się w kategorii: " + category.getForeignId());
            return Boolean.TRUE;
        }

        categories.remove(category.getForeignId());
        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"categories\":" + Utils.listToString(categories) + "," +
                        "\"categoriesSize\":" + categories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.removeUserFromCategory()", stream);
            categories.add(category.getForeignId());
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        String categoryId = category.getId();
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.removeUserFromCategory()", "Zmniejszenie liczby użytkowników w kategorii: " + categoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + categoryId,
                    category,
                    "usersSize",
                    category.getUsersSize() - 1
            );
        }
        // Update sizes of category
        ///getAllCategories(true);

        return Boolean.TRUE;
    }
    public static Boolean removeUserFromSubcategory(User user, Subcategory subcategory) throws IOException, JSONException {
        ArrayList<String> subcategories = user.getSubcategories();
        if (!subcategories.contains(subcategory.getForeignId())) {
            Utils.logDebug("Api.removeUserFromSubcategory()", "Użytkownik: " + user.toString() + " nie znajdował się w podkategorii: " + subcategory.getForeignId());
            return Boolean.TRUE;
        }

        subcategories.remove(subcategory.getForeignId());
        InputStreamReader is = patchRequest(
                API_USERS_URL + "/" + user.getId(),
                user.getEtag(),
                "{\"subcategories\":" + Utils.listToString(subcategories) + "," +
                        "\"subcategoriesSize\":" + subcategories.size() + "}"
        );
        String stream = getStream(is);
        Response response = mapper.readValue(stream, Response.class);
        if (response.isError()) {
            Utils.logError("Api.removeUserFromSubcategory()", stream);
            subcategories.add(subcategory.getForeignId());
            return Boolean.FALSE;
        }

        user.updateFromResponse(response);
        getAllUsers(true);

        String subcategoryId = subcategory.getId();
        if (Utils.isEmpty(subcategory)) {
            Utils.logDebug("Api.removeUserFromSubcategory()", "Zmniejszenie liczby użytkowników w podkategorii: " + subcategoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_SUBCATEGORIES_URL + "/" + subcategoryId,
                    subcategory,
                    "usersSize",
                    subcategory.getUsersSize() - 1
            );
        }
        // Update sizes of subcategory
        ///getAllSubcategories(true);

        // Assuming one subcategory is only in one category
        String categoryId = Utils.undoForeignId(subcategory.getCategories().get(0));
        Category category = getCategoryById(categoryId);
        if (Utils.isEmpty(category)) {
            Utils.logDebug("Api.removeUserFromCategory()", "Zmniejszenie liczby użytkowników w kategorii: " + categoryId + " zakończone niepowodzeniem");
        } else {
            correctSize(
                    API_CATEGORIES_URL + "/" + categoryId,
                    category,
                    "usersSize",
                    category.getUsersSize() - 1
            );
        }
        // Update sizes of category
        ///getAllCategories(true);

        return Boolean.TRUE;
    }

}
