/**
 * Copyright IBM Corporation 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.ibm.watson.developer_cloud.android.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;
import com.ibm.watson.developer_cloud.alchemy.v1.util.AlchemyEndPoints;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.Language;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
  private final String TAG = "MainActivity";

  private RadioGroup targetLanguage;
  private EditText input;
  private Button translate;
  private TextView translatedText;
  private TextView sentimentText;
  private LanguageTranslation translationService;
  private AlchemyLanguage alchmemyLanguageService;
  private Language selectedTargetLanguage = Language.SPANISH;

  protected AppCompatActivity activity;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    activity = this;

    translationService = initLanguageTranslationService();
    alchmemyLanguageService = initAlchemyLanguageService();

    targetLanguage = (RadioGroup) findViewById(R.id.target_language);
    input = (EditText) findViewById(R.id.input);
    translate = (Button) findViewById(R.id.translate);
    translatedText = (TextView) findViewById(R.id.translated_text);

    sentimentText = (TextView) findViewById(R.id.sentiment_text);

    targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.spanish:
            selectedTargetLanguage = Language.SPANISH;
            break;
          case R.id.french:
            selectedTargetLanguage = Language.FRENCH;
            break;
          case R.id.italian:
            selectedTargetLanguage = Language.ITALIAN;
            break;
        }
      }
    });

    translate.setOnClickListener(new View.OnClickListener() {

      @Override public void onClick(View v) {
        String content = input.getText().toString();

        new AlchemyTask().execute(content);
        new TranslationTask().execute(content);

      }
    });
  }

  private LanguageTranslation initLanguageTranslationService() {
    LanguageTranslation service = new LanguageTranslation();
    String username = getString(R.string.language_translation_username);
    String password = getString(R.string.language_translation_password);
    service.setUsernameAndPassword(username, password);
    return service;
  }

  private AlchemyLanguage initAlchemyLanguageService() {
    AlchemyLanguage service = new AlchemyLanguage();
    String apikey = "3d22fdc17a90d800a7c5dd2a502f4d4dc480ee18";
    service.setApiKey(apikey);
    return service;
  }

  private void showSentiment(final String sentiment) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        sentimentText.setText(sentiment);
      }
    });
  }

  private void showTranslation(final String translation) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        translatedText.setText(translation);
      }
    });
  }

  private class AlchemyTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {

      Map<String, Object> params = new HashMap<>();
      params.put(AlchemyLanguage.TEXT, strings[0]);
      alchmemyLanguageService.getSentiment(params).enqueue(new ServiceCallback<DocumentSentiment>() {
        @Override
        public void onResponse(DocumentSentiment response) {
          String sentiment = response.getSentiment().toString();
          showSentiment(sentiment);
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(activity.getBaseContext(), "Failed to retreive alchemy language result", Toast.LENGTH_SHORT).show();
        }
      });

      return null;
    }
  }

  private class TranslationTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
      translationService.translate(strings[0], Language.ENGLISH, selectedTargetLanguage).enqueue(new ServiceCallback<TranslationResult>() {
        @Override
        public void onResponse(TranslationResult response) {
          showTranslation(response.getFirstTranslation());
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(activity.getBaseContext(), "Failed to retreive translation result", Toast.LENGTH_SHORT).show();
        }
      });

      return null;
    }
  }

}
