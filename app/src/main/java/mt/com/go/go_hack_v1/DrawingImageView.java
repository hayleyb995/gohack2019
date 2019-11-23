package mt.com.go.go_hack_v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DrawingImageView extends ImageView {

    private PointF point;
    int closingPolygonPointIndex;
    int currentState;
    private Paint paint = new Paint();
    private List<PointF> outline = new ArrayList<>();
    private List<List<PointF>> polygons = new ArrayList<>();
    private final float tickness = (float) 0.5;
    private final int material = 1;
    private PointF currentPoint;
    private Button readyButton;


    private static final int THRESHOLD = 100;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        float x = (float) Math.floor(event.getX());
        float y = (float) Math.floor(event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                point = new PointF(x, y);
                if (currentState == 0) { //Drawing outline
                    outline.add(point);
                } else {
                    if (polygons.size() == 0) {
                        polygons.add(new ArrayList<PointF>());
                    }

                    polygons.get(polygons.size() - 1).add(point);
                }

                invalidate();

//                if(isPointInPolygon(point)) {
//                    Toast toast = Toast.makeText(this.getContext(),
//                            "Point is in polygon",
//                            Toast.LENGTH_SHORT);
//
//                    toast.show();
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentPoint = new PointF(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean valid = false;

                if (currentState >= 1) {
                    if (isPointInPolygon(x, y) && currentState == 2) {
                        Toast toast = Toast.makeText(this.getContext(),
                                "Drawing room segment",
                                Toast.LENGTH_SHORT);

                        toast.show();
                        valid = true;
                    }


                    if (isPointOnPolygonOutline(x, y)) {
                        if (currentState == 2) {
                            Toast toast = Toast.makeText(this.getContext(),
                                    "Room completed",
                                    Toast.LENGTH_LONG);

                            toast.show();
                            valid = true;
                            currentState = 1;
                        } else {
                            Toast toast = Toast.makeText(this.getContext(),
                                    "Starting room drawing",
                                    Toast.LENGTH_LONG);
                            List<PointF> lastPolyPoints = polygons.get(polygons.size() - 1);
                            PointF lastPoint = lastPolyPoints.get(lastPolyPoints.size() - 1);
                            lastPolyPoints.remove(lastPolyPoints.size() - 1);
                            polygons.add(new ArrayList<PointF>());
                            polygons.get(polygons.size() - 1).add(lastPoint);
                            invalidate();
                            toast.show();
                            valid = true;
                            currentState = 2;
                        }
                    }

                    if (!valid) {
                        List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                        lastPoly.remove(lastPoly.size() - 1);
                        invalidate();
                    }

                    return true;
                } else {
                    if (outline.size() > 1) {
                        PointF last = outline.get(outline.size() - 1);
                        PointF first = outline.get(0);
                        if (Math.abs(last.x - first.x) < THRESHOLD && Math.abs(last.y - first.y) < THRESHOLD) {
                            outline.set(outline.size() - 1, new PointF(first.x, first.y));

                            closingPolygonPointIndex = outline.size() - 1;
                            Toast toast = Toast.makeText(this.getContext(),
                                    "Outline drawn successfully",
                                    Toast.LENGTH_SHORT);

                            toast.show();

                            //go to state 1
                            currentState = 1;
                            readyButton.setEnabled(true);
                        }
                        invalidate();
                    }
                }

                currentPoint = null;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.BLUE);
        for (int i = 0; i < outline.size() - 1; i++) {
            PointF p1 = outline.get(i);
            PointF p2 = outline.get(i + 1);
            if (p1 != null && p2 != null) {
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
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
        }

//        if (currentPoint != null) {
//            PointF p1 = points.get(points.size() - 1);
//            PointF p2 = currentPoint;
//            paint.setColor(Color.RED);
//            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
//            paint.setColor(Color.BLACK);
//        }
    }

    public void clearView() {
        currentState = 0;
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
        for (i = 0, j = outline.size() - 1; i < closingPolygonPointIndex + 1; j = i++) {
            if ((outline.get(i).y > y) != (outline.get(j).y > y) &&
                    (x < (outline.get(j).x - outline.get(i).x) * (y - outline.get(i).y) / (outline.get(j).y - outline.get(i).y) + outline.get(i).x)) {
                result = !result;
            }
        }
        return result;

    }

//    private boolean isPointOnPolygonOutline(float x, float y) {
//
//        if (points.size() < 3) {
//            return false;
//        }
//
//        int i;
//        int j;
//        boolean result = false;
//        for (i = 0; i < closingPolygonPointIndex - 1; i++) {
//
//            float gradient = (points.get(i + 1).y - points.get(i).y) / (points.get(i + 1).x - points.get(i).x);
//            float intercept = points.get(i).y - (gradient * points.get(i).x);
//
//            if (y == (gradient * x) + intercept) {
//                return true;
//            }
//        }
//        return false;
//
//    }

    private boolean isPointOnPolygonOutline(float x, float y) {

        if (outline.size() < 3) {
            return false;
        }

        for (int i = 0; i < outline.size() - 1; i++) {
            float x1 = outline.get(i).x;
            float y1 = outline.get(i).y;
            float x2 = outline.get(i + 1).x;
            float y2 = outline.get(i + 1).y;

            float a = y1 - y2;
            float b = x2 - x1;
            float c = ((x1 - x2) * y1) + ((y2 - y1) * x1);

            if (circleIntersectsLine(a, b, c, x, y, 15)) {
                return true;
            }
        }

        return false;
    }

    private boolean circleIntersectsLine(float a, float b, float c, float x, float y, int radius) {
        double dist = (Math.abs(a * x + b * y + c)) / Math.sqrt(a * a + b * b);

        return radius >= dist;
    }

    public List<PolyLine> getPolyLines() {

        List<PolyLine> polyLines = new ArrayList<>();
        if (outline.size() > 2) {
            for (int i = 0; i <= outline.size() - 2; i++) {

                Coordinate startingPoint = new Coordinate(outline.get(i).x, outline.get(i).y);
                Coordinate endingPoint = new Coordinate(outline.get(i + 1).x, outline.get(i + 1).y);
                PolyLine polyLine = new PolyLine(startingPoint, endingPoint, 1, PolyLine.Material.CONCRETE);
                polyLines.add(polyLine);
            }
        }

        for(int i = 0; i < polygons.size(); i++){
            List<PointF> polygonOutline = polygons.get(0);
            if (polygonOutline.size() > 2) {
                for (int j = 0; j <= polygonOutline.size() - 2; j++) {

                    Coordinate startingPoint = new Coordinate(polygonOutline.get(j).x, polygonOutline.get(i).y);
                    Coordinate endingPoint = new Coordinate(polygonOutline.get(i + 1).x, polygonOutline.get(i + 1).y);
                    PolyLine polyLine = new PolyLine(startingPoint, endingPoint, 1, PolyLine.Material.CONCRETE);
                    polyLines.add(polyLine);
                }
            }
        }
        return polyLines;
    }

    public void setReadyButton(Button button){
        this.readyButton = button;

    }
}