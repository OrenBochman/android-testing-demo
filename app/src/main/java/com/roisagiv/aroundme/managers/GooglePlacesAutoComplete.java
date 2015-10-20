package com.roisagiv.aroundme.managers;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The type Google places auto complete.
 */
public class GooglePlacesAutoComplete implements PlacesAutoComplete {

  private final String baseUrl;
  private final String apiKey;

  public GooglePlacesAutoComplete(String baseUrl, String apiKey) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  /**
   * Auto complete list.
   *
   * @param text the text
   * @return the list
   */
  @Override public Response<List<AutoCompletePrediction>> autoComplete(String text) {
    OkHttpClient client = new OkHttpClient();

    HttpUrl.Builder httpBuilder = HttpUrl.get(URI.create(baseUrl)).newBuilder();
    httpBuilder.encodedPath("/maps/api/place/autocomplete/json")
        .addQueryParameter("input", text)
        .addQueryParameter("key", apiKey);

    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(httpBuilder.build());

    Response<List<AutoCompletePrediction>> results = new Response<>();
    Call call = client.newCall(requestBuilder.build());
    try {

      com.squareup.okhttp.Response response = call.execute();
      results.setHttpCode(response.code());

      JSONObject json = new JSONObject(response.body().string());
      JSONArray predictionsAsJson = json.getJSONArray("predictions");
      List<AutoCompletePrediction> predictions = new ArrayList<>(predictionsAsJson.length());

      for (int i = 0; i < predictionsAsJson.length(); i++) {
        JSONObject predictionAsJson = predictionsAsJson.getJSONObject(i);
        AutoCompletePrediction prediction = new AutoCompletePrediction();
        prediction.setDescription(predictionAsJson.getString("description"));
        prediction.setId(predictionAsJson.getString("id"));

        predictions.add(prediction);
      }

      results.setResults(predictions);
    } catch (IOException | JSONException e) {
      results.setError(e);
    }

    return results;
  }
}
