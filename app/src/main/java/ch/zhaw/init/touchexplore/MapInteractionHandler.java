package ch.zhaw.init.touchexplore;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class MapInteractionHandler {

    TextToSpeech textToSpeech;

    private void tospeak(final String speech) {
        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

}
