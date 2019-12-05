package mt.com.go.go_hack_v1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import mt.com.go.go_hack_v1.apoe.ApoeOptimizePlanController;
import mt.com.go.go_hack_v1.apoe.ApoeService;
import mt.com.go.go_hack_v1.apoe.model.plan.Material;
import mt.com.go.go_hack_v1.apoe.model.plan.Point;
import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.recommendation.Recommendation;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity {

    boolean isMock = false;


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
        if(isMock){

            readyButton.setEnabled(true);
            readyButton.setImageResource(R.drawable.forward);
        } else {

//            readyButton.setEnabled(false);
//            readyButton.setImageResource(R.drawable.forward_grey);

            readyButton.setEnabled(true);
            readyButton.setImageResource(R.drawable.forward);
        }
        view.setReadyButton(readyButton);
        view.setUndoButton(undoButton);
        undoButton.setEnabled(false);

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

                if(isMock) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Computing Solution",
                                Toast.LENGTH_SHORT);
                        toast.show();


                    view.setCurrentState(STATE.READY);
                    view.invalidate();

                } else {
                    final List<PolyLine> polyLines = view.getPolyLines();
                    if (polyLines.isEmpty()) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Issue with plan.",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {

                        UiWall[] walls = new UiWall[polyLines.size()];

                        for (int i = 0; i < polyLines.size(); i++) {
                            PolyLine line = polyLines.get(i);

                            PointF startingPoint = line.getCoordinates().get(0);
                            PointF endPoint = line.getCoordinates().get(1);

                            UiWall wall = new UiWall(new Point(startingPoint.x, startingPoint.y) , new Point(endPoint.x, endPoint.y), Material.CONCRETE, 20);
                            walls[i] = wall;
                        }

                        ApoeOptimizePlanController controller = new ApoeOptimizePlanController(view);
                        controller.start(walls);

//                    Intent mockIntent = new Intent(getApplicationContext(), MockService.class);
//                    mockIntent.putExtra("Plan", (Serializable) polyLines);
                    }

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
                Intent intent = new Intent(getApplicationContext(),WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent.getBooleanExtra("LOAD_TEMPLATE", false)) {
            view.loadTemplate();
        } else
        if (intent.getBooleanExtra("LOAD", false)) {
            view.loadState();
        }
    }


}