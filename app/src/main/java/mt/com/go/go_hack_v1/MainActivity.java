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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import mt.com.go.go_hack_v1.apoe.OptimizationEngine;
import mt.com.go.go_hack_v1.apoe.model.plan.Material;
import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.plan.Wall;
import mt.com.go.go_hack_v1.apoe.model.recommendation.Recommendation;

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
        final ImageButton saveButton = findViewById(R.id.saveButton);
        final ImageButton homeButton = findViewById(R.id.backButton);
        view.setReadyButton(readyButton);
        view.setUndoButton(undoButton);
        readyButton.setEnabled(true);
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
                if (polyLines.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Issue with plan.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    Wall[] walls = new Wall[polyLines.size()];

                    for (int i = 0; i < polyLines.size(); i++) {
                        PolyLine line = polyLines.get(i);
                        UiWall wall = new UiWall(line.getCoordinates().get(0), line.getCoordinates().get(1), Material.CONCRETE, 1);
                        walls[i] = wall;
                    }

                    OptimizationEngine engine = new OptimizationEngine(walls);

                    Thread thread = new Thread(engine);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Recommendation recommendation = engine.recommendation;
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.saveState();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Saved Successfully",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();

            }
        });

        Intent intent = getIntent();
        if (intent.getBooleanExtra("LOAD_TEMPLATE", false)) {
            view.loadTemplate();
        }
    }


}