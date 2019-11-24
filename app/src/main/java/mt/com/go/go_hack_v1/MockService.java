package mt.com.go.go_hack_v1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import mt.com.go.go_hack_v1.apoe.model.plan.Wall;

public class MockService extends Service {

    private Wall[] walls;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        walls = (Wall[]) intent.getSerializableExtra("Plan");
        return null;
    }
}



