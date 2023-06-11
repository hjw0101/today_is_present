package com.example.today_;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordWidget extends AppWidgetProvider {
    private List<String[]> wordList;
    private Random random;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            // CSV 파일에서 단어와 뜻을 읽어옴
            readWordList(context);

            // 단어 업데이트
            updateWord(context, appWidgetManager, appWidgetIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("WordWidget", "onUpdate");
    }

    private int[] getAppWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, WordWidget.class);
        return appWidgetManager.getAppWidgetIds(componentName);
    }

    private void readWordList(Context context) {
        wordList = new ArrayList<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("50000.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] wordData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // 정규 표현식을 사용하여 쉼표를 구분자로 사용 (따옴표 내부의 쉼표는 무시)
                for (int i = 0; i < wordData.length; i++) {
                    wordData[i] = wordData[i].replace("\"", ""); // 따옴표 제거
                }
                wordList.add(wordData);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateWord(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String[] randomWord = getRandomWord();
        if (randomWord != null) {
            String word = randomWord[0];
            String definition = randomWord[1];
            String pos = randomWord[2]; // 품사는 세 번째 요소로 수정

            // 위젯 UI 업데이트
            updateWidgetUI(context, appWidgetManager, appWidgetIds, word, pos, definition);
        }
    }


    private String[] getRandomWord() {
        if (wordList != null && wordList.size() > 0) {
            if (random == null) {
                random = new Random();
            }
            int index = random.nextInt(wordList.size());
            return wordList.remove(index);  // 기존 단어를 리스트에서 제거하고 반환
        }
        return null;
    }

    private void updateWidgetUI(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, String word, String pos, String definition) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.wordTextView, word);
            views.setTextViewText(R.id.posTextView, pos);
            views.setTextViewText(R.id.meaningTextView, definition);

            // 버튼에 클릭 이벤트 등록
            Intent intent = new Intent(context, WordWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.nextButton, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}

