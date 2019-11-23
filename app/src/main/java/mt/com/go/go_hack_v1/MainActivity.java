package mt.com.go.go_hack_v1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DrawingImageView view = findViewById(R.id.image);
        Button button = findViewById(R.id.clearButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.clearView();
            }
        });
    }
}
