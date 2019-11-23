package mt.com.go.go_hack_v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

enum STATE {
    BOUNDARY_BUILDING,
    STABLE,
    WALLS_BUILDING
}

public class DrawingImageView extends ImageView {

    private PointF point;
    int closingPolygonPointIndex;
    STATE currentState;
    private Paint paint = new Paint();
    private List<PointF> outline = new ArrayList<>();
    private List<List<PointF>> polygons = new ArrayList<>();
    private PointF currentPoint;

    float offsetX;
    float offsetY;

    private static final int CELL_INCREMENT = 100;
    private static final int CELL_GRANULAR_INCREMENT = 50;
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
                if(intersection != null) {
                    x = intersection.x;
                    y = intersection.y;
                }
                break;
            case WALLS_BUILDING:
                // approximate x and y to be on the point collienear to the nearest line
                PointF intersection2 = getPointOnPolygonOutline(x, y);
                if(intersection2 != null) { // if (boundary wall is reached)
                    x = intersection2.x;
                    y = intersection2.y;
                } else { // still building boundary wall
                    x = offsetX + Math.round((float) Math.floor(event.getX()) / CELL_GRANULAR_INCREMENT) * CELL_GRANULAR_INCREMENT;
                    y = offsetY + Math.round((float) Math.floor(event.getY()) / CELL_GRANULAR_INCREMENT) * CELL_GRANULAR_INCREMENT;
                }
                break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (currentState) {
                    case BOUNDARY_BUILDING:
                        point = new PointF(x, y);
                        outline.add(point);
                        break;
                    case STABLE:
                        point = new PointF(x, y);
                        polygons.add(new ArrayList<PointF>());
                        break;
                    case WALLS_BUILDING:
                        point = new PointF(x, y);
                        polygons.get(polygons.size() - 1).add(point);
                        break;
                }

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean valid = false;

                switch (currentState) {
                    case BOUNDARY_BUILDING:
                        if (outline.size() > 1) { // if at least 2 points in boundary wall
                            PointF last = outline.get(outline.size() - 1);
                            PointF first = outline.get(0);
                            if (Math.abs(last.x - first.x) < THRESHOLD && Math.abs(last.y - first.y) < THRESHOLD) {
                                // snap last point to first point if within THRESHOLD
                                outline.set(outline.size() - 1, new PointF(first.x, first.y));

                                closingPolygonPointIndex = outline.size() - 1;
                                Toast toast = Toast.makeText(this.getContext(),
                                        "Outline drawn successfully",
                                        Toast.LENGTH_SHORT);
                                toast.show();

                                currentState = STATE.STABLE;
                            }
                            invalidate();
                        }

                        currentPoint = null;
                        break;
                    case STABLE:
                        if (isPointOnPolygonOutline(x, y)) {
                            // we are actually starting a new polygon
                            PointF startingPoint = new PointF(x,y);
                            polygons.get(polygons.size() - 1).add(startingPoint);
                            invalidate();

                            Toast toast = Toast.makeText(this.getContext(),
                                    "Starting room drawing",
                                    Toast.LENGTH_LONG);
                            toast.show();
                            currentState = STATE.WALLS_BUILDING;
                        } else { // not on boundary
                            List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                            if(lastPoly.size() > 0){
                                lastPoly.remove(lastPoly.size() - 1);
                            }
                            invalidate();
                        }

                        break;
                    case WALLS_BUILDING:
                         if (isPointOnPolygonOutline(x, y)) {
                            Toast toast = Toast.makeText(this.getContext(),
                                    "Room completed",
                                    Toast.LENGTH_LONG);

                            toast.show();
                            currentState = STATE.STABLE;
                        } else if (isPointInPolygon(x, y)) {
                             Toast toast = Toast.makeText(this.getContext(),
                                     "Drawing room segment",
                                     Toast.LENGTH_SHORT);

                             toast.show();
                        } else { // if point is outside polygon
                            List<PointF> lastPoly = polygons.get(polygons.size() - 1);
                            lastPoly.remove(lastPoly.size() - 1);
                            invalidate();
                        }

                        break;
                }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // paint grid only in state 0
        if(currentState == STATE.BOUNDARY_BUILDING) {
            paint.setColor(Color.GRAY);
            for (int i = 0; i < this.getWidth(); i += CELL_INCREMENT) {
                for (int j = 0; j < this.getHeight(); j += CELL_INCREMENT) {
                    canvas.drawPoint(i, j, paint);
                }
            }
        }

        // paint granular grid only in state 2
        //TODO define offset x and offset y
        float xOffset = (float)offsetX;
        float yOffset = (float)offsetY;

        if(currentState == STATE.WALLS_BUILDING) {
            paint.setColor(Color.GRAY);
            for (int i = 0; i < this.getWidth(); i += CELL_GRANULAR_INCREMENT) {
                for (int j = 0; j < this.getHeight(); j += CELL_GRANULAR_INCREMENT) {
                    canvas.drawPoint(i+xOffset, j+yOffset, paint);
                }
            }
        }

        // paint outline
        paint.setColor(Color.BLUE);
        for (int i = 0; i < outline.size() - 1; i++) {
            PointF p1 = outline.get(i);
            PointF p2 = outline.get(i + 1);
            if (p1 != null && p2 != null) {
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
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

//        if (currentPoint != null) {
//            PointF p1 = points.get(points.size() - 1);
//            PointF p2 = currentPoint;
//            paint.setColor(Color.RED);
//            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
//            paint.setColor(Color.BLACK);
//        }
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
        for (i = 0, j = outline.size() - 1; i < closingPolygonPointIndex + 1; j = i++) {
            if ((outline.get(i).y*(-1) > y*(-1)) != (outline.get(j).y*(-1) > y*(-1)) &&
                    (x < (outline.get(j).x - outline.get(i).x) * (y*(-1) - outline.get(i).y*(-1)) / (outline.get(j).y*(-1) - outline.get(i).y*(-1)) + outline.get(i).x)) {
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

            if (circleIntersectsLine(a, b, c, x, y, 30)) {
                return true;
            }
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
            float y1 = outline.get(i).y;
            float x2 = outline.get(i + 1).x;
            float y2 = outline.get(i + 1).y;

            float a = y1 - y2;
            float b = x2 - x1;
            float c = ((x1 - x2) * y1) + ((y2 - y1) * x1);

            if (circleIntersectsLine(a, b, c, x, y, 15)) {
                //find coordinate of interest
                // line 1
                float m1 = ((y2-y1) / (x2-x1));
                float c1 = y1 - (m1*x1);

                // line 2
                float x3 = x;
                float y3 = y * (-1);
                float m2 = -1 / m1;
                float c2 = y3 - (m2 * x3);

                // points of intersection of line 1 and line 2
                float intersectionX = ((c2 - c1) / (m1-m2));
                float intersectionY = ((m1 * intersectionX) + c1)*(-1);

                return new PointF(intersectionX, intersectionY);
            }
        }

        return null;
    }

    private PointF getPointOnPolygonOutlineOld(float x, float y) {

        if (outline.size() < 3) {
            return null;
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

                //find coordinate of interest
                float gradient1 = ((y2-y1)/(x2-x1));
                float intercept1 = y1-(gradient1*x1);



                List<PointF> currentPolygon = polygons.get(polygons.size() - 1);
                x1 =  currentPolygon.get(currentPolygon.size()-1).x;
                y1 = (currentPolygon.get(currentPolygon.size()-1).y)*(-1);
                x2 = x;
                y2 = y*(-1);
                float gradient2 = ((y2-y1)/(x2-x1));
                float intercept2 = y1-(gradient1*x1);

                float intersectionX = ((intercept2-intercept1)/(gradient1-gradient2));
                float intersectionY = ((gradient1*intersectionX)+intercept1)*(-1);


                return new PointF(intersectionX, intersectionY);
            }
        }

        return null;
    }

}