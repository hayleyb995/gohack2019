package mt.com.go.go_hack_v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import mt.com.go.go_hack_v1.apoe.model.AccessPoint;
import mt.com.go.go_hack_v1.apoe.model.plan.Material;

public class DrawingImageView extends ImageView {

    private ImageButton readyButton;
    private ImageButton undoButton;
    STATE currentState = STATE.BOUNDARY_BUILDING;
    private Paint paint = new Paint();
    private List<PointF> outline = new ArrayList<>();
    private List<List<PointF>> polygons = new ArrayList<>();

    private PointF lastPoint;
    private PointF previousLastPoint;
    private PointF targetPointLoc;

    private final DashPathEffect dashedEffect = new DashPathEffect(new float[]{5, 10, 15, 20}, 0);

//
//    protected int SCREEN_WIDTH = 1200;//this.getWidth();
//    protected int SCREEN_HEIGHT = 2000;//this.getHeight();
//    protected int CELLS_Y = 200;
//    protected int CELLS_X = 200;
//    protected int CELL_LENGTH_X = SCREEN_WIDTH / CELLS_X;
//    protected int CELL_LENGTH_Y = SCREEN_HEIGHT / CELLS_Y;
//    protected float[][] heatMap = new float[CELLS_Y][CELLS_X]; // mock


    protected Paint paintHeatMap = new Paint();

    float offsetX;
    float offsetY;

    private static final int CELL_INCREMENT = 100;
    private static final int CELL_GRANULAR_INCREMENT = 75;
    private static final int THRESHOLD = 100;

    double[][] heatMapGlobal;
    List<AccessPoint> aps = new ArrayList<>();

    public void setHeatMapGlobal(double[][] heatMapGlobal) {
        this.heatMapGlobal = heatMapGlobal;
    }

    public void setAps(List<AccessPoint> aps) {
        this.aps = aps;
    }

    private SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

    public DrawingImageView(Context context) {
        super(context);
        paint.setStrokeWidth(5);
    }

    public DrawingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(5);
    }

    public DrawingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setStrokeWidth(5);
    }

    private void updatePreviewState(PointF point) {
        previousLastPoint = lastPoint;
        lastPoint = point;
    }

    private void revertPreviewState() {
        lastPoint = previousLastPoint;
        previousLastPoint = null;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (currentState) {
            case BOUNDARY_BUILDING:
                x = Math.round((float) Math.floor(event.getX()) / CELL_INCREMENT) * CELL_INCREMENT;
                y = Math.round((float) Math.floor(event.getY()) / CELL_INCREMENT) * CELL_INCREMENT;
                break;
            case STABLE: // will start building room (first point on room poligon)
                // approximate x and y to be on the point collienear to the nearest line
                PointF intersection = getPointOnPolygonOutline(x, y);
                if (intersection != null) {
                    x = intersection.x;
                    y = intersection.y;

                    offsetX = x%CELL_GRANULAR_INCREMENT;
                    offsetY = y%CELL_GRANULAR_INCREMENT;
                }
                break;
            case WALLS_BUILDING:

                x = offsetX + (float)Math.floor((float) Math.floor(event.getX()) / CELL_GRANULAR_INCREMENT) * CELL_GRANULAR_INCREMENT;
                y = offsetY + (float)Math.floor((float) Math.floor(event.getY()) / CELL_GRANULAR_INCREMENT) * CELL_GRANULAR_INCREMENT;

                // approximate x and y to be on the point collienear to the nearest line
                PointF intersection2 = getPointOnPolygonOutline(x, y);
                if (intersection2 != null) { // if (boundary wall is reached)
                    x = intersection2.x;
                    y = intersection2.y;
                }
                break;
        }

        targetPointLoc = new PointF(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (currentState) {
                    case BOUNDARY_BUILDING:
                        PointF point = new PointF(x, y);
                        if (outline.isEmpty()) {
                            outline.add(point);
                            updatePreviewState(point);
                        }
                        if (outline.size() > 1) {
                            undoButton.setImageResource(R.drawable.undo);
                            undoButton.setEnabled(true);
                        }
                        break;
                    case STABLE:
                        polygons.add(new ArrayList<>());
                        if (isPointOnPolygonOutline(x, y)) {
                            if (polygons.get(polygons.size() - 1).isEmpty()) {
                                // we are actually starting a new polygon
                                PointF startingPoint = new PointF(x, y);
                                polygons.get(polygons.size() - 1).add(startingPoint);
                                updatePreviewState(startingPoint);
                            }

                            currentState = STATE.WALLS_BUILDING;
                        } else { // not on boundary
                            List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                            if (lastPoly.size() > 0) {
                                lastPoly.remove(lastPoly.size() - 1);
                                revertPreviewState();
                            }
                        }
                        break;
                    case WALLS_BUILDING:
                        point = new PointF(x, y);
                        polygons.get(polygons.size() - 1).add(point);
                        updatePreviewState(point);
                        break;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.performClick();

                targetPointLoc = null;

                switch (currentState) {
                    case BOUNDARY_BUILDING:

                        PointF point = new PointF(x, y);
                        outline.add(point);
                        updatePreviewState(point);
                        if (outline.size() > 1) {
                            undoButton.setImageResource(R.drawable.undo);
                            undoButton.setEnabled(true);
                        }

                        PointF last = null;
                        PointF first = null;
                        if (outline.size() > 1) { // if at least 2 points in boundary wall
                            last = outline.get(outline.size() - 1);
                            first = outline.get(0);
                        }

                        if (last != null && Math.abs(last.x - first.x) < THRESHOLD && Math.abs(last.y - first.y) < THRESHOLD) {
                            if (outline.size() > 2) {
                                // snap last point to first point if within THRESHOLD
                                point = new PointF(first.x, first.y);
                                outline.set(outline.size() - 1, point);
                                lastPoint = point;

                                Toast toast = Toast.makeText(this.getContext(),
                                        "Outline drawn successfully",
                                        Toast.LENGTH_SHORT);
                                toast.show();

                                currentState = STATE.STABLE;

                                readyButton.setImageResource(R.drawable.forward);
                                readyButton.setEnabled(true);
                            } else {
                                outline.remove(outline.size() - 1);
                                undoButton.setImageResource(R.drawable.undo_grey);
                                undoButton.setEnabled(false);
                            }
                        }

                        break;
                    case STABLE:
                        if (isPointOnPolygonOutline(x, y)) {
                            // we are actually starting a new polygon
                            PointF startingPoint = new PointF(x, y);
                            polygons.get(polygons.size() - 1).add(startingPoint);
                            updatePreviewState(startingPoint);

                            currentState = STATE.WALLS_BUILDING;
                        } else { // not on boundary
                            List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                            if (lastPoly.size() > 0) {
                                lastPoly.remove(lastPoly.size() - 1);
                                revertPreviewState();
                            }
                        }

                        break;
                    case WALLS_BUILDING:
                        if (isPointOnPolygonOutline(x, y)) {
                            point = new PointF(x, y);
                            polygons.get(polygons.size() - 1).add(point);
                            updatePreviewState(point);
                            currentState = STATE.STABLE;
                        } else if (isPointInPolygon(x, y)) {
                            point = new PointF(x, y);
                            polygons.get(polygons.size() - 1).add(point);
                            updatePreviewState(point);
                        } else { // if point is outside polygon
                            List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                            lastPoly.remove(lastPoly.size() - 1);
                            revertPreviewState();
                        }

                        break;
                }
        }

        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lastPoint != null && targetPointLoc != null && currentState != STATE.STABLE) {
            paint.setColor(Color.GREEN);
            canvas.drawLine(lastPoint.x, lastPoint.y, targetPointLoc.x, targetPointLoc.y, paint);
        }

        // paint grid only in state 0
        if (currentState == STATE.BOUNDARY_BUILDING) {
            paint.setColor(Color.GRAY);
            for (int i = 0; i < this.getWidth(); i += CELL_INCREMENT) {
                for (int j = 0; j < this.getHeight(); j += CELL_INCREMENT) {
                    canvas.drawPoint(i, j, paint);
                }
            }
        }

        // paint granular grid only in state 2
        float xOffset = offsetX;
        float yOffset = offsetY;

        if (currentState == STATE.WALLS_BUILDING) {
            paint.setColor(Color.GRAY);
            for (int i = 0; i < this.getWidth(); i += CELL_GRANULAR_INCREMENT) {
                for (int j = 0; j < this.getHeight(); j += CELL_GRANULAR_INCREMENT) {
                    canvas.drawPoint(i + xOffset, j + yOffset, paint);
                }
            }
        }

        // paint outline
        if (currentState == STATE.STABLE) {
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(dashedEffect);

        } else {
            paint.setColor(Color.BLUE);
        }
        for (int i = 0; i < outline.size() - 1; i++) {
            PointF p1 = outline.get(i);
            PointF p2 = outline.get(i + 1);
            if (p1 != null && p2 != null) {
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }

        // reset paint
        paint.setPathEffect(null);
        paint.setStyle(Paint.Style.FILL);

        // paint origin of outline
        if (outline.size() == 1) {
            paint.setColor(Color.RED);
            paint.setStrokeWidth(20);
            canvas.drawPoint(outline.get(0).x, outline.get(0).y, paint);
            paint.setStrokeWidth(5);
        }

        // paint rooms
        paint.setColor(Color.BLACK);
        for (int i = 0; i < polygons.size(); i++) {
            List<PointF> poly = polygons.get(i);
            for (int j = 0; j < poly.size() - 1; j++) {
                PointF p1 = poly.get(j);
                PointF p2 = poly.get(j + 1);
                if (p1 != null && p2 != null) {
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                }
            }

            if (poly.size() == 1) {

                paint.setColor(Color.RED);
                paint.setStrokeWidth(20);
                canvas.drawPoint(poly.get(0).x, poly.get(0).y, paint);
                paint.setStrokeWidth(5);
            }
        }



        if(currentState == STATE.READY) {

            double[][] heatMap = heatMapGlobal;

            for (int cellX = 0; cellX < heatMap.length; cellX++) {
                for (int cellY = 0; cellY < heatMap[0].length; cellY++) {

                    // TODO map dB to linear scale?
                    // dB → gain-multiplier: https://sound.stackexchange.com/questions/38722/convert-db-value-to-linear-scale
//                    double linearMeasure = Math.pow(10, (heatMap[cellX][cellY])/20);
//                    double minInput = Math.pow(10, (-100/20));
//                    double maxInput = Math.pow(10, (0.4/20));

                    double index = normalize(heatMap[cellX][cellY]);
                    paintHeatMap.setColor(heatmapColorMapper((float) index));
                    paintHeatMap.setAlpha(128);
                    canvas.drawRect(cellX * 20, cellY * 20, cellX * 20 + 20, cellY * 20 + 20, paintHeatMap);
                }
            }


            for (int i = 0; i < aps.size(); i++) {
                AccessPoint ap = aps.get(i);
                drawAP(canvas, ap.getCurrentGridPoint().getRow()*20 + 10, ap.getCurrentGridPoint().getColumn()*20 + 10, ""+i);
            }

            //TODO uncomment for mock data
//            double[][] mockResult = generateMockResult();
//
//            for (int cellX = 0; cellX < mockResult.length - 2; cellX++) {
//                for (int cellY = 240; cellY < mockResult[cellX].length - 2; cellY++) {
//
//                    rectangle = new Rect(
//                            cellX,
//                            cellY,
//                            cellX + 1,
//                            cellY + 1);
//
//                    paintHeatMap.setColor(heatmapColorMapper((float) mockResult[cellX][cellY]));
////                paintHeatMap.setColor(Color.GREEN);
//                    paintHeatMap.setAlpha(128);
//                    canvas.drawRect(rectangle, paintHeatMap);
//                }
//            }

            // draw APS
//            drawAP(canvas, 300, 500, "A");
//            drawAP(canvas, 700, 1350, "B");

        }


    }

    double normalize(double dbLevel) {

        if (dbLevel < -90) {
            return 0d;
        } else if (dbLevel < -80) {
            return normalize(dbLevel, -90, -80, 0d, 0.25d);
        } else if (dbLevel < -70) {
            return normalize(dbLevel, -80, -70, 0.25d, 0.5d);
        } else if (dbLevel < -60) {
            return normalize(dbLevel, -70, -60, 0.5d, 0.75d);
        } else if (dbLevel < -50) {
            return normalize(dbLevel, -60, -50, 0.75d, 1d);
        }
        return 1d;

    }


    double normalize(double x, double in_min, double in_max, double out_min, double out_max)
    {
        if(x<in_min) {
            x = in_min;
        } else if (x>in_max) {
            x = in_max;
        }
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private void drawAP(Canvas canvas, int x, int y, String label){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.wifi_point);
        canvas.drawBitmap(bitmap, x-bitmap.getWidth()/2, y-bitmap.getHeight()/2, paint);


        paint.setColor(Color.BLACK);
        paint.setTextSize(35);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(label, x+bitmap.getWidth()/2, y+bitmap.getHeight()/2, paint);
    }

    public void clearView() {
        currentState = STATE.BOUNDARY_BUILDING;
        outline.clear();
        polygons.clear();
        invalidate();
    }

    private boolean isPointInPolygon(float x, float y) {

        if (outline.size() < 3) {
            return false;
        }

        int i;
        int j;
        boolean result = false;
        for (i = 0, j = outline.size() - 1; i < outline.size(); j = i++) {
            if ((outline.get(i).y * (-1) > y * (-1)) != (outline.get(j).y * (-1) > y * (-1)) &&
                    (x < (outline.get(j).x - outline.get(i).x) * (y * (-1) - outline.get(i).y * (-1)) / (outline.get(j).y * (-1) - outline.get(i).y * (-1)) + outline.get(i).x)) {
                result = !result;
            }
        }
        return result;

    }

    private boolean isPointOnPolygonOutline(float x, float y) {

        if (outline.size() < 3) {
            return false;
        }

        // is point on plan outline
        for (int i = 0; i < outline.size() - 1; i++) {
            float x1 = outline.get(i).x;
            float y1 = outline.get(i).y;
            float x2 = outline.get(i + 1).x;
            float y2 = outline.get(i + 1).y;

            if(isPointOnLine(x, y, x1, y1, x2, y2)) {
                return true;
            }
        }

        // is point on rooms outline
        for (int i = 0; i < polygons.size() - 1; i++) { //loop through all rooms points


            for (int j = 0; j < polygons.get(i).size() - 1; j++) { //loop through all boundary points
                float x1 = polygons.get(i).get(j).x;
                float y1 = polygons.get(i).get(j).y;
                float x2 = polygons.get(i).get(j + 1).x;
                float y2 = polygons.get(i).get(j + 1).y;


                if(isPointOnLine(x, y, x1, y1, x2, y2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isPointOnLine(float x, float y, float x1, float y1, float x2, float y2) {
        float a = y1 - y2;
        float b = x2 - x1;
        float c = ((x1 - x2) * y1) + ((y2 - y1) * x1);

        if (circleIntersectsLine(a, b, c, x, y, 15)) {

            // confirm that the point is within line's limits
            float checkX1 = x-x1;
            float checkX2 = x-x2;

            float checkY1 = y-y1;
            float checkY2 = y-y2;

            return ((checkX1 >= 0 && checkX2 <= 0) || (checkX1 <= 0 && checkX2 >= 0)) &&
                    ((checkY1 >= 0 && checkY2 <= 0) || (checkY1 <= 0 && checkY2 >= 0));
        }

        return false;
    }

    private boolean circleIntersectsLine(float a, float b, float c, float x, float y, int radius) {
        double dist = (Math.abs(a * x + b * y + c)) / Math.sqrt(a * a + b * b);

        return radius >= dist;
    }

    private PointF getPointOnPolygonOutline(float x, float y) {
        if (outline.size() < 3) { // boundary is a straight line or single point
            return null;
        }

        for (int i = 0; i < outline.size() - 1; i++) { //loop through all boundary points
            float x1 = outline.get(i).x;
            float y1 = outline.get(i).y * (-1);
            float x2 = outline.get(i + 1).x;
            float y2 = outline.get(i + 1).y * (-1);

            PointF point = getPointOnLine(x, y, x1, y1, x2, y2);
            if(point != null) {
                return point;
            }
        }

        for (int i = 0; i < polygons.size() - 1; i++) { //loop through all rooms points


            for (int j = 0; j < polygons.get(i).size() - 1; j++) { //loop through all boundary points
                float x1 = polygons.get(i).get(j).x;
                float y1 = polygons.get(i).get(j).y * (-1);
                float x2 = polygons.get(i).get(j + 1).x;
                float y2 = polygons.get(i).get(j + 1).y * (-1);

                PointF point = getPointOnLine(x, y, x1, y1, x2, y2);
                if(point != null) {
                    return point;
                }
            }
        }

        return null;
    }

    private PointF getPointOnLine(float x, float y, float x1, float y1, float x2, float y2) {


        float a = y1 - y2;
        float b = x2 - x1;
        float c = ((x1 - x2) * y1) + ((y2 - y1) * x1);

        float y3 = y * (-1);

        float intersectionX;
        float intersectionY;

        if (circleIntersectsLine(a, b, c, x, y3, 50)) {
            //find coordinate of interest
            if (x1 != x2 && y1 != y2) {
                // line 1
                float m1 = ((y2 - y1) / (x2 - x1));
                float c1 = y1 - (m1 * x1);

                // line 2
                float m2 = -1 / m1;
                float c2 = y3 - (m2 * x);

                // points of intersection of line 1 and line 2
                intersectionX = ((c2 - c1) / (m1 - m2));
                intersectionY = ((m1 * intersectionX) + c1);
            } else if (y1 == y2) {
                intersectionX = x;
                intersectionY = y1;
            } else { // x1 == x2
                intersectionX = x1;
                intersectionY = y3;
            }


            // confirm that the point is within line's limits
            float checkX1 = intersectionX-x1;
            float checkX2 = intersectionX-x2;

            float checkY1 = intersectionY-y1;
            float checkY2 = intersectionY-y2;

            if(!(
                    ((checkX1>=0 && checkX2<=0) || (checkX1<=0 && checkX2>=0)) &&
                            ((checkY1>=0 && checkY2<=0) || (checkY1<=0 && checkY2>=0))
            )){
                return null;
            }

            intersectionY *= (-1); //verse y mapping;

            return new PointF(intersectionX, intersectionY);
        }

        return null;
    }


    public double calculateDistanceBetweenPoints(
            double x1,
            double y1,
            double x2,
            double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public void setUndoButton(ImageButton button){
        this.undoButton = button;
    }

    public void setReadyButton(ImageButton button){
        this.readyButton = button;
    }

    public void setCurrentState(STATE currentState) {
        this.currentState = currentState;
    }

    public List<PolyLine> getPolyLines() {
        List<PolyLine> polyLines = new ArrayList<>();
        if (outline.size() > 2) {
            for (int i = 0; i <= outline.size() - 2; i++) {
                PointF startingPoint = new PointF(outline.get(i).x, outline.get(i).y);
                PointF endingPoint = new PointF(outline.get(i + 1).x, outline.get(i + 1).y);
                PolyLine polyLine = new PolyLine(startingPoint, endingPoint, 1, Material.CONCRETE);
                polyLines.add(polyLine);
            }
        }
        for (int i = 0; i < polygons.size(); i++) {
            List<PointF> polygonOutline = polygons.get(i);
            if (polygonOutline.size() > 2) {
                for (int j = 0; j <= polygonOutline.size() - 2; j++) {
                    PointF startingPoint = new PointF(polygonOutline.get(j).x, polygonOutline.get(j).y);
                    PointF endingPoint = new PointF(polygonOutline.get(j + 1).x, polygonOutline.get(j + 1).y);
                    PolyLine polyLine = new PolyLine(startingPoint, endingPoint, 1, Material.CONCRETE);
                    polyLines.add(polyLine);
                }
            }
        }
        return polyLines;
    }

    public void undoAction(){
        if(outline.size() >=1 && polygons.isEmpty()){
            outline.remove(outline.size()-1);
            if(outline.size() ==0){
                undoButton.setImageResource(R.drawable.undo_grey);
                undoButton.setEnabled(false);
            }
            if (currentState == STATE.STABLE) {
                currentState = STATE.BOUNDARY_BUILDING;
                readyButton.setImageResource(R.drawable.forward_grey);
                readyButton.setEnabled(false);
            }
            invalidate();
        } else {
            List<PointF> polygon = polygons.get(polygons.size() - 1);
            if (!polygon.isEmpty()) {
                polygon.remove(polygon.size() - 1);
                invalidate();
                if (polygon.isEmpty()) {
                    currentState = STATE.STABLE;
                    polygons.remove(polygons.size()-1);
                } else {
                    currentState = STATE.WALLS_BUILDING;
                }

            } else if (polygons.size() != 1) {
                List<PointF> previousPolygon = polygons.get(polygons.size() - 2);
                previousPolygon.remove(previousPolygon.size() - 1);
                invalidate();
            } else {
                outline.remove(outline.size()-1);
                readyButton.setImageResource(R.drawable.forward_grey);
                if(outline.size() ==0){
                    undoButton.setImageResource(R.drawable.undo_grey);
                }
                invalidate();
                currentState = STATE.BOUNDARY_BUILDING;
            }
        }

        switch(currentState) {
            case BOUNDARY_BUILDING:
                if (!outline.isEmpty()) {
                    lastPoint = outline.get(outline.size() - 1);
                }
                break;
            case STABLE:
            case WALLS_BUILDING:
                boolean updated = false;
                for (int i = polygons.size(); i > 0; i--) {
                    List<PointF> polygon = polygons.get(i - 1);

                    if (polygon.isEmpty()) {
                        continue;
                    }

                    lastPoint = polygon.get(polygon.size() - 1);
                    updated = true;
                }

                if (!updated) {
                    if (outline.size() > 0) {
                        lastPoint = outline.get(outline.size() - 1);
                    } else {
                        lastPoint = null;
                    }
                }

                break;
        }
    }


    // 0 - RED and 1 - GREEN
    protected int heatmapColorMapper(float heatmapValue) {
        float[] hsv = {heatmapValue * 120, 1f, 1f};
        return Color.HSVToColor(hsv);
    }

    public void saveState() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CURRENT_STATE", currentState.name());
        Gson gson = new Gson();
        String outlineString = gson.toJson(outline);
        editor.putString("OUTLINE", outlineString);
        String polygonString = gson.toJson(polygons);
        editor.putString("POLYGONS", polygonString);
        editor.commit();
    }

    public void loadState() {
        Gson gson = new Gson();
        currentState = Enum.valueOf(STATE.class, preferences.getString("CURRENT_STATE", STATE.BOUNDARY_BUILDING.name()));
        Type type = new TypeToken<ArrayList<PointF>>() {
        }.getType();
        outline = gson.fromJson(preferences.getString("OUTLINE", "[]"), type);
        type = new TypeToken<ArrayList<ArrayList<PointF>>>() {
        }.getType();
        polygons = gson.fromJson(preferences.getString("POLYGONS", "[]"), type);
        invalidate();
    }

    public void loadTemplate() {
        currentState = STATE.STABLE;
        outline = new ArrayList<>();
        outline.add(new PointF(200, 300));
        outline.add(new PointF(900, 300));
        outline.add(new PointF(900, 1800));
        outline.add(new PointF(200, 1800));
        outline.add(new PointF(200, 300));
        polygons = new ArrayList<>();
        List<PointF> room1 = new ArrayList<>();
        room1.add(new PointF(750, 300));
        room1.add(new PointF(750, 1150));
        room1.add(new PointF(900, 1150));
        List<PointF> room2 = new ArrayList<>();
        room2.add(new PointF(500, 300));
        room2.add(new PointF(500, 600));
        room2.add(new PointF(200, 600));
        List<PointF> room3 = new ArrayList<>();
        room3.add(new PointF(550, 1800));
        room3.add(new PointF(550, 1450));
        room3.add(new PointF(900, 1450));
        List<PointF> room4 = new ArrayList<>();
        room4.add(new PointF(200, 1450));
        room4.add(new PointF(550, 1450));
        room4.add(new PointF(550, 1150));
        room4.add(new PointF(200, 1150));
        List<PointF> room5 = new ArrayList<>();
        room5.add(new PointF(200, 650));
        room5.add(new PointF(600, 650));
        room5.add(new PointF(600, 1050));
        room5.add(new PointF(200, 1050));
        polygons.add(room1);
        polygons.add(room2);
        polygons.add(room3);
        polygons.add(room4);
        polygons.add(room5);
        invalidate();
    }

    private double[][] generateMockResult(){
        float minX = outline.get(0).x;
        float maxX = outline.get(0).x;
        float minY = outline.get(0).y;
        float maxY = outline.get(0).y;

        for(PointF point: outline)
        {
            if (point.x < minX) {
                minX=point.x;
            }
            if (point.x > maxX) {
                maxX=point.x;
            }
            if (point.y < minY) {
                minY=point.y;
            }
            if (point.y > maxY) {
                maxY=point.y;
            }
        }


        int length = (int)Math.ceil(maxX)+(int)Math.floor(minX);
        int height = (int)Math.ceil(maxY)+(int)Math.floor(minY);

        double maxDistance = 0;
        double minDistance = 0;

        double[][] array = new double[length][height];

        for(int i=0; i<length; i++) {
            for(int j=0; j<height; j++){


                double distanceFromA = calculateDistanceBetweenPoints(i,j*(-1),300,500*(-1));
                double distanceFromB = calculateDistanceBetweenPoints(i,j*(-1),700,1350*(-1));

                double smallerDistance = Math.min(distanceFromA, distanceFromB);
                if(i == 0 && j==0){
                    maxDistance = smallerDistance;
                    minDistance = smallerDistance;

                }

                if(smallerDistance>maxDistance){
                    maxDistance=smallerDistance;
                } else if(smallerDistance<minDistance){
                    minDistance=smallerDistance;
                }


                array[i][j] = smallerDistance;

            }
        }

        for(int i=0; i<length; i++) {
            for(int j=0; j<height; j++){


                array[i][j] = (array[i][j]-minDistance)/(maxDistance-minDistance);

            }
        }


        return array;
    }
}