package mt.com.go.go_hack_v1;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.Serializable;
import java.util.List;
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_main);


        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final DrawingImageView view = findViewById(R.id.image);
        ImageButton clearButton = findViewById(R.id.clearButton);
        final ImageButton readyButton = findViewById(R.id.readyButton);
        final ImageButton undoButton = findViewById(R.id.undoButton);
        view.setReadyButton(readyButton);
        view.setUndoButton(undoButton);
        readyButton.setEnabled(false);
        undoButton.setEnabled(false);
        readyButton.setImageResource(R.drawable.forward_grey);
        undoButton.setImageResource(R.drawable.undo_grey);


        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.clearView();
                undoButton.setEnabled(false);
                readyButton.setEnabled(false);
                readyButton.setImageResource(R.drawable.forward_grey);
                undoButton.setImageResource(R.drawable.undo_grey);
                view.invalidate();
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
                    view.setCurrentState(STATE.READY);
                    view.invalidate();
//                    Intent mockIntent = new Intent(getApplicationContext(), MockService.class);
//                    mockIntent.putExtra("Plan", (Serializable) polyLines);
                }
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.undoAction();
            }
        });
    }




}