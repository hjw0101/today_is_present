package com.example.today_;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.net.ssl.HttpsURLConnection;
import com.example.today_.MainActivity;

public class searching extends AppCompatActivity {

    private ImageButton imageView3;
    private ImageButton unbook;
    private TextView word;
    private TextView pos;
    private TextView definition;
    private TextView exam_;
    private TextView sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        imageView3 = findViewById(R.id.imageView3);
        unbook = findViewById(R.id.unbook);
        word = findViewById(R.id.word);
        pos = findViewById(R.id.pos);
        definition = findViewById(R.id.definition);
        exam_ = findViewById(R.id.exam_);
        sd = findViewById(R.id.sd);

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    try {
                        EditText searchEditText = findViewById(R.id.search_search);
                        String searchText = searchEditText.getText().toString();

                        String key = "C10D7ED3FF3C002BD826D59347A832F1";
                        URL url = new URL("https://stdict.korean.go.kr/api/search.do?key=" + key
                                + "&type_search=search&q=" + searchText + "&part=word&part=exam&sort=popular");

                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(url.openStream());
                        NodeList nl = doc.getElementsByTagName("item");

                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                        br.close();

                        for (int i = 0; i < nl.getLength(); i++) {
                            Node node = nl.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                String wordValue = getValue("word", element);
                                String posValue = getValue("pos", element);
                                String definitionValue = getValue("definition", element);
                                String examValue = getValue("exam",element);

                                word.setText(wordValue);
                                pos.setText(posValue);
                                definition.setText(definitionValue);
                                exam_.setText(examValue);


                                unbook.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Firebase Realtime Database에 접근하기 위한 DatabaseReference 생성
                                        DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("bookmarks");

                                        // 새로운 북마크 키 생성
                                        String bookmarkKey = bookmarksRef.push().getKey();

                                        // 북마크 객체 생성 및 값 설정
                                        Bookmark bookmark = new Bookmark(searchText, wordValue, posValue, definitionValue);

                                        // Firebase Realtime Database에 북마크 저장
                                        bookmarksRef.child(bookmarkKey).setValue(bookmark);

                                        // 이미지 변경
                                        unbook.setImageResource(R.drawable.baseline_bookmark);
                                    }
                                });
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    EditText searchEditText = findViewById(R.id.search_search);
                    String searchText = searchEditText.getText().toString();

                    String apiUrl = "https://stdict.korean.go.kr/api/search.do";
                    String apiKey = "C10D7ED3FF3C002BD826D59347A832F1";

                    Uri.Builder builder = Uri.parse(apiUrl).buildUpon();
                    builder.appendQueryParameter("key", apiKey);
                    builder.appendQueryParameter("q", searchText);

                    String apiUrlWithParams = builder.build().toString();

                    new FetchDataAsyncTask().execute(apiUrlWithParams);
                }).start(); // Move the start() method here
            } // Remove the extra closing parenthesis here
        });
    }

    public static String getValue(String tag, Element element) {
        NodeList nl = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node value = nl.item(0);
        return value.getNodeValue();
    }

    public static class Bookmark {
        private String searchText;
        private String word;
        private String pos;
        private String definition;

        public Bookmark(String searchText, String word, String pos, String definition) {
            this.searchText = searchText;
            this.word = word;
            this.pos = pos;
            this.definition = definition;
        }

        // Getters and setters

        public String getSearchText() {
            return searchText;
        }

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }

    private static class FetchDataAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String apiUrl = urls[0];

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Handle the fetched data
            }
        }
    }
}
