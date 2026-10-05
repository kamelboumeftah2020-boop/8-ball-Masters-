package com.sada.podcasts;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaPlaybackPlugin.class);
        registerPlugin(EpisodeCheckerPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
