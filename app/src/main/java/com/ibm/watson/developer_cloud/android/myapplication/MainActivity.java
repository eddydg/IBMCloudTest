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

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyVision;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;
import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeywords;
import com.ibm.watson.developer_cloud.alchemy.v1.util.AlchemyEndPoints;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.Language;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
  private final String TAG = "MainActivity";

  private RadioGroup targetLanguage;
  private EditText input;
  private Button translate;
  private TextView translatedText;
  private TextView sentimentText;

  private ImageView visionImage1;
  private ImageView visionImage2;
  private TextView visionText1;
  private TextView visionText2;


  private LanguageTranslation translationService;
  private AlchemyLanguage alchmemyLanguageService;
  private AlchemyVision alchmemyVisionService;
  private Language selectedTargetLanguage = Language.SPANISH;

  protected AppCompatActivity activity;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    activity = this;

    translationService = initLanguageTranslationService();
    alchmemyLanguageService = initAlchemyLanguageService();
    alchmemyVisionService = initAlchemyVisionService();

    targetLanguage = (RadioGroup) findViewById(R.id.target_language);
    input = (EditText) findViewById(R.id.input);
    translate = (Button) findViewById(R.id.translate);
    translatedText = (TextView) findViewById(R.id.translated_text);

    sentimentText = (TextView) findViewById(R.id.sentiment_text);

    visionImage1 = (ImageView) findViewById(R.id.visionImage1);
    visionImage2 = (ImageView) findViewById(R.id.visionImage2);
    visionText1 = (TextView) findViewById(R.id.visionText1);
    visionText2 = (TextView) findViewById(R.id.visionText2);

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

        try {
          File img1 = new File(getCacheDir(), "img1.jpg");
          File img2 = new File(getCacheDir(), "img2.jpg");

          InputStream ims = getAssets().open("party.jpg");
          Drawable img = Drawable.createFromStream(ims, null);
          ims.reset();
          visionImage1.setImageDrawable(img);
          copyInputStreamToFile(ims, img1);

          ims = getAssets().open("party2.jpg");
          img = Drawable.createFromStream(ims, null);
          ims.reset();
          visionImage2.setImageDrawable(img);
          copyInputStreamToFile(ims, img2);

          new VisionTask().execute(img1, img2);

        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    });
  }

  private void copyInputStreamToFile(InputStream in, File file) {
    try {
      OutputStream out = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while((len=in.read(buf))>0){
        out.write(buf,0,len);
      }
      out.close();
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    String apikey = getString(R.string.alchemy_api);
    service.setApiKey(apikey);
    return service;
  }

  private AlchemyVision initAlchemyVisionService() {
    AlchemyVision service = new AlchemyVision();
    String apikey = getString(R.string.alchemy_api);
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

  private void showVision1(final String keywords) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        visionText1.setText(keywords);
      }
    });
  }

  private void showVision2(final String keywords) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        visionText2.setText(keywords);
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

  private class VisionTask extends AsyncTask<File, Void, Void> {
    @Override
    protected Void doInBackground(File... files) {
      alchmemyVisionService.getImageKeywords(files[0], true, true).enqueue(new ServiceCallback<ImageKeywords>() {
        @Override
        public void onResponse(ImageKeywords response) {
          showVision1(response.toString());
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(activity.getBaseContext(), "Failed to retreive vision1 result", Toast.LENGTH_SHORT).show();
        }
      });

      alchmemyVisionService.getImageKeywords(files[1], true, true).enqueue(new ServiceCallback<ImageKeywords>() {
        @Override
        public void onResponse(ImageKeywords response) {
          showVision2(response.toString());
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(activity.getBaseContext(), "Failed to retreive vision2 result", Toast.LENGTH_SHORT).show();
        }
      });

      return null;
    }
  }

}
