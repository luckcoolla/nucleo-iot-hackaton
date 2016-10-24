package by.iot.nucleo.spectre.getyoursensors;

import android.os.Bundle;

public class BaseActivity extends OrientationBlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.incrementUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.decrementUi();
    }
}
