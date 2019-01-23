package com.gerdaszabo.szage.bookpilot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.gerdaszabo.szage.bookpilot.model.Book;
import com.gerdaszabo.szage.bookpilot.model.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for Http Url connection and Json response extraction.
 * Creates a List of Book objects.
 */

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();


    private static final String ITEMS = "items";
    private static final String VOLUME_INFO = "volumeInfo";
    private static final String TITLE = "title";
    private static final String AUTHORS = "authors";
    private static final String DESCRIPTION = "description";
    private static final String IMAGE_LINKS = "imageLinks";
    private static final String THUMBNAIL = "smallThumbnail";
    private static final String INDUSTRY_IDENTIFIERS = "industryIdentifiers";
    private static final String TYPE = "type";
    private static final String IDENTIFIER = "identifier";
    private static final String ISBN_13 = "ISBN_13";
    private static final String NO_ISBN = "ISBN nr is not available";

    private static final String STORE_ID = "id";
    private static final String NAME = "name";
    private static final String PLACE_ID = "place_id";
    private static final String VICINITY = "vicinity";
    private static final String REFERENCE = "reference";
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";
    private static final String ICON = "icon";

    private static final String RESULTS = "results";
    private static final String STATUS = "status";
    private static final String OK = "OK";
    private static final String ZERO_RESULTS = "ZERO_RESULTS";



    public QueryUtils() {
        super();
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        // If the url is null, return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        // Create connection
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            /* If the request was successful (response code 200),
              then read the input stream and parse the response.
              */
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else Log.e(LOG_TAG, String.valueOf(R.string.error_response_code) + urlConnection.getResponseCode());

        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.json_error), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                /* makeHttpRequest(URL url) method signature specifies than an IOException
                 could be thrown. */
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a string which contains
     * the whole JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    /**
     * Returns the URL object from the given string URL.
     *
     * @param stringUrl is in string format of the url
     * @return url is the the url for fetching book data
     */
    private static URL createUrl( String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.url_building_error), e);
        }
        return url;
    }

    /**
     * Request desired book information and return a list of Book objects
     *
     * @param requestUrl is the url that contains Book information
     * @return books is a list of Book objects
     */
    public static ArrayList<Book> fetchBookData (String requestUrl) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);

        Log.i(LOG_TAG, String.valueOf(R.string.request_url) + requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.http_request_error), e);
        }

        // Extract relevant fields from the JSON Response and create a list {@link book}s
        ArrayList<Book> books = extractDataFromJson(jsonResponse);

        // Return the list of Books
        return books;
    }

    /**
     * Extract Book information of Json format
     *
     * @param bookJSON contains all requested books with details
     * @return a list of Book objects
     */
    private static ArrayList<Book> extractDataFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        ArrayList<Book> books = new ArrayList<>();

        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of features (or books).
            JSONArray bookArray = baseJsonResponse.getJSONArray(ITEMS);

            // For each book in the bookArray, create an {@link Book} object
            for (int i = 0; i < bookArray.length(); i++) {

                // Get the single book at position i with the list of books
                JSONObject currentBook = bookArray.getJSONObject(i);

                // Get the volume info node from the book
                JSONObject volumeInfo = currentBook.getJSONObject(VOLUME_INFO);

                // Extract the value for the key called "title"
                String title = volumeInfo.getString(TITLE);

                // Extract the value for the key called "authors"
                String authors = volumeInfo.getString(AUTHORS);

                String description = volumeInfo.getString(DESCRIPTION);

                // Extract the value for the key called "smallThumbnail"
                String imageUrl = volumeInfo.getJSONObject(IMAGE_LINKS).getString(THUMBNAIL);

                Book book;
                String isbn;

                try {
                    // Extract the value for the key called "industryIdentifiers"
                    JSONArray industryIdentifiers = volumeInfo.getJSONArray(INDUSTRY_IDENTIFIERS);

                /* Get the type of the identifier and the isbn number */
                    isbn = "";
                    for (int j = 0; j < industryIdentifiers.length(); j++) {

                        // Extract the identifier object of the array
                        JSONObject identifierObject = industryIdentifiers.getJSONObject(j);

                        // Get the identifier type ISBN_13 or ISBN_10
                        String type = identifierObject.getString(TYPE);

                        // Get only one identifier (isbn number)
                        if (type.equals(ISBN_13)) {
                            isbn = identifierObject.getString(IDENTIFIER);
                        } else {
                            if (isbn.isEmpty()) {
                                isbn = identifierObject.getString(IDENTIFIER);
                            }
                        }
                    }
                    // Create new Book object
                    book  = new Book(title, authors, isbn, imageUrl, description);
                    // And add it to the list
                    books.add(book);

                } catch (JSONException ex) {
                    // In case there's no value for identifier information
                    // assign this information to the isbn variable
                    isbn = NO_ISBN;
                    // Create new Book object with that
                    book  = new Book(title, authors, isbn, imageUrl, description);
                    // Add it to list
                    books.add(book);
                    Log.e(LOG_TAG, String.valueOf(R.string.identifier_error), ex);
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with with the message from the exception.
            Log.e(LOG_TAG, String.valueOf(R.string.parsing_error), e);
        }
        // Return the list of books
        return books;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr =(ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo;
    }

    /**
     * Creates the URL for places query
     *
     * @param longitude is the longitude of device location
     * @param latitude is the latitude of device location
     * @return
     */
    public static String getURL(double longitude, double latitude) {

        String radius = String.valueOf(5000);

        // TODO: Place your own API key here
        String myApiKey = "AIzaSyDMKPmKpihAouypyqx8AEDBeBMV7VZZgQM";

        // Build the google places url
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(radius);
        googlePlacesUrl.append("&types=").append("book_store");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + myApiKey);

        Log.i(LOG_TAG, "url is " + googlePlacesUrl.toString());

        return googlePlacesUrl.toString();
    }

    /**
     * Fetches nearby stores
     *
     * @param requestUrl is the query url
     * @return stores is a list of Store objects
     */
    public static List<Store> fetchStoreLocations(String requestUrl) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.http_request_error), e);
        }

        // Return the list of Stores after extracted relevant fields
        return parseLocationData(jsonResponse);
    }

    /**
     * Extract data from the Json response
     *
     * @param response is the Json response
     * @return stores is a list of Store objects
     */
    private static List<Store> parseLocationData(String response) {

        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stores to
        List<Store> stores = new ArrayList<>();

        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(response);
            // Extract the JSONArray represents a list of stores/ places
            JSONArray jsonArray = baseJsonResponse.getJSONArray(RESULTS);

            if (baseJsonResponse.getString(STATUS).equalsIgnoreCase(OK)) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    id = place.getString(STORE_ID);
                    place_id = place.getString(PLACE_ID);

                    if (!place.isNull(NAME)) {
                        placeName = place.getString(NAME);
                    }
                    if (!place.isNull(VICINITY)) {
                        vicinity = place.getString(VICINITY);
                    }
                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)

                            .getDouble(LATITUDE);

                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LONGITUDE);

                    reference = place.getString(REFERENCE);
                    icon = place.getString(ICON);

                    // Create a new {@link Store} object
                    Store store = new Store(latitude, longitude, placeName, vicinity);
                    // add it to the list
                    stores.add(store);
                }
            } else if (baseJsonResponse.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                Log.i(LOG_TAG, String.valueOf(R.string.store_finder_error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(LOG_TAG, String.valueOf(R.string.store_log) + stores);
        return stores;
    }
}
