package mt.com.go.go_hack_v1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
        setContentView(R.layout.activity_main);
        final DrawingImageView view = findViewById(R.id.image);
        Button clearButton = findViewById(R.id.clearButton);
        final Button readyButton = findViewById(R.id.readyButton);
        final Button undoButton = findViewById(R.id.undoButton);
        view.setReadyButton(readyButton);
        view.setUndoButton(undoButton);
        clearButton.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);
        readyButton.setEnabled(true);
        undoButton.setEnabled(false);

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.clearView();
                undoButton.setEnabled(false);
                readyButton.setEnabled(false);
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
                    System.out.println("DONE ------- " + recommendation);

                    //ExecutorService executor = Executors.newSingleThreadExecutor();
                    //executor.execute(engine);
//                    try {
//                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                            executor.shutdownNow();
//                        } else {
//                            Recommendation recommendation = engine.recommendation;
//                            System.out.println("DONE ------- " + recommendation);
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
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