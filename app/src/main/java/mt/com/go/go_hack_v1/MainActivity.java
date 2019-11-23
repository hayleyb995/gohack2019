package mt.com.go.go_hack_v1;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.Serializable;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DrawingImageView view = findViewById(R.id.image);
        Button clearButton = findViewById(R.id.clearButton);
        Button readyButton = findViewById(R.id.readyButton);
        final Button undoButton = findViewById(R.id.undoButton);
        view.setReadyButton(readyButton);
        clearButton.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);
        readyButton.setEnabled(false);
        undoButton.setEnabled(false);

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.clearView();
                undoButton.setEnabled(false);
            }
        });

        readyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final List<PolyLine> polyLines = view.getPolyLines();
                if(polyLines.isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Issue with plan.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent mockIntent = new Intent(getApplicationContext(), MockService.class);
                    mockIntent.putExtra("Plan", (Serializable) polyLines);
                }
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final List<PolyLine> polyLines = view.getPolyLines();
                if(polyLines.isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Issue with plan.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent mockIntent = new Intent(getApplicationContext(), MockService.class);
                    mockIntent.putExtra("Plan", (Serializable) polyLines);
                }
            }
        });
    }


}