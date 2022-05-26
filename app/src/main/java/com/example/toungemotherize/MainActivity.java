package com.example.toungemotherize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText sourceText;
    private TextView translatedTV;
    String[] fromLanguage={"From", "English","Arabic", "Russian"
            ,"Turkish",	"Vietnamese","Greek","Spanish",
            "French","Afrikaans","Bengali","Catalan","Czech"
        ,"Hindi","Urdu"};
    String[] toLanguage={"To", "English","Arabic", "Russian"
            ,"Turkish",	"Vietnamese","Greek","Spanish"
            , "French","Afrikaans","Bengali", "Catalan","Czech"
           ,"Hindi","Urdu"};

    private final static int REQUEST_PERMISSION_CODE = 1;
    int languageCode , fromLanguageCode, toLanguageCode =0;




    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner fromSpinner = findViewById(R.id.idFromSpinner);
        Spinner toSpinner = findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        MaterialButton translateButton = findViewById(R.id.idBtnTranslation);
        translatedTV = findViewById(R.id.idTrasnlatedTV);
        ImageView micIV = findViewById(R.id.idVoiceRecorder);


        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguage[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);


        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguage[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate");
                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }


            }
        });

            translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setVisibility(View.VISIBLE);
                translatedTV.setText("");
                if(sourceText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please input your text", Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                }
                else if(toLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select target language", Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode==toLanguageCode){
                    Toast.makeText(MainActivity.this, "I'm feeling like the most useful translate", Toast.LENGTH_SHORT).show();
                }
                else{
                    translateText(fromLanguageCode,toLanguageCode,sourceText.getText().toString());
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_PERMISSION_CODE){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceText.setText(result.get(0));
        }
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        translatedTV.setText("Downloading model, please wait ...");
        FirebaseTranslatorOptions options =new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();
        FirebaseTranslator translator =FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translation ...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to translate the text !"
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Couldn't download translation package", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private int getLanguageCode(String language) {
        int languageCode=0;
        switch (language){
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;

            case "Russian":
                languageCode= FirebaseTranslateLanguage.RU;
                break;

            case "Turkish":
                languageCode= FirebaseTranslateLanguage.TR;
                break;

            case "Vietnamese":
                languageCode= FirebaseTranslateLanguage.VI;
                break;

            case "Greek":
                languageCode= FirebaseTranslateLanguage.EL;
                break;
            case "Spanish":
                languageCode= FirebaseTranslateLanguage.ES;
                break;

            case "French":
                languageCode= FirebaseTranslateLanguage.FR;
                break;

            case "Afrikaans":
                languageCode= FirebaseTranslateLanguage.AF;
                break;

            case "Bangali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;

            case "Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;

            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;

            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;

            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
        }
        return languageCode;


    }
}