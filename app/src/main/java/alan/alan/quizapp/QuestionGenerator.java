package alan.alan.quizapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

// Asks the Ollama program running on the laptop to write one true/false statement.
// The model answers in JSON with a "statement" and an "isTrue" (its own answer key).
// This is the "unfamiliar code" part: it sends text to a web address and reads the reply.
// 127.0.0.1 reaches the laptop because we run: adb reverse tcp:11434 tcp:11434
public class QuestionGenerator {

    // MainActivity implements this so it gets told when the answer comes back
    public interface Listener {
        void onQuestion(String statement, boolean answer);

        void onError(String message);
    }

    static final String OLLAMA_URL = "http://127.0.0.1:11434/api/generate";
    static final String MODEL = "qwen2.5:0.5b";

    // Runs on a background thread so the screen does not freeze while the model thinks.
    // The listener is called from that background thread, so MainActivity must use runOnUiThread.
    public static void generate(final String topic, final Listener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String prompt = "Write one short true or false statement about " + topic
                            + ". Keep it under 15 words.";

                    // "format" tells Ollama the reply must be JSON shaped like:
                    // {"statement": "some text", "isTrue": true}
                    // A tiny model can't follow a text format reliably, but it can fill in this shape.
                    JSONObject statementField = new JSONObject();
                    statementField.put("type", "string");
                    JSONObject isTrueField = new JSONObject();
                    isTrueField.put("type", "boolean");
                    JSONObject properties = new JSONObject();
                    properties.put("statement", statementField);
                    properties.put("isTrue", isTrueField);
                    JSONObject format = new JSONObject();
                    format.put("type", "object");
                    format.put("properties", properties);
                    format.put("required", new JSONArray().put("statement").put("isTrue"));

                    JSONObject body = new JSONObject();
                    body.put("model", MODEL);
                    body.put("prompt", prompt);
                    body.put("stream", false);
                    body.put("format", format);

                    HttpURLConnection connection = (HttpURLConnection) new URL(OLLAMA_URL).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(120000);
                    connection.setDoOutput(true);

                    OutputStream out = connection.getOutputStream();
                    out.write(body.toString().getBytes("UTF-8"));
                    out.close();

                    InputStream in = connection.getInputStream();
                    Scanner scanner = new Scanner(in, "UTF-8").useDelimiter("\\A");
                    String reply = scanner.hasNext() ? scanner.next() : "";
                    in.close();

                    // Ollama wraps the model's text in a JSON object under "response"
                    String text = new JSONObject(reply).getString("response").trim();

                    // The text is itself JSON: {"statement": "...", "isTrue": true}
                    JSONObject result = new JSONObject(text);
                    String statement = result.getString("statement").trim();
                    boolean answer = result.getBoolean("isTrue");
                    if (statement.isEmpty()) {
                        listener.onError("Try again");
                        return;
                    }
                    listener.onQuestion(statement, answer);
                } catch (Exception e) {
                    Log.e("QuestionGenerator", "Ollama call failed", e);
                    listener.onError("Ollama error: " + e);
                }
            }
        }).start();
    }
}
