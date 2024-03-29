package mt.com.go.go_hack_v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mt.com.go.go_hack_v1.apoe.model.plan.Material;

enum STATE {
    BOUNDARY_BUILDING,
    STABLE,
    WALLS_BUILDING
}

public class DrawingImageView extends ImageView {

    private Button readyButton;
    private Button undoButton;
    private PointF point;
    int closingPolygonPointIndex;
    STATE currentState = STATE.BOUNDARY_BUILDING;
    private Paint paint = new Paint();
    private List<PointF> outline = new ArrayList<>();
    private List<List<PointF>> polygons = new ArrayList<>();
    private PointF currentPoint;

    protected int SCREEN_WIDTH = 1200;//this.getWidth();
    protected int SCREEN_HEIGHT = 2000;//this.getHeight();
    protected int CELLS_Y = 200;
    protected int CELLS_X = 200;
    protected int CELL_LENGTH_X = SCREEN_WIDTH/CELLS_X;
    protected int CELL_LENGTH_Y = SCREEN_HEIGHT/CELLS_Y;
    protected float[][] heatMap = new float[CELLS_Y][CELLS_X]; // mock
    protected Paint paintHeatMap = new Paint();
    protected Rect rectangle;

    float offsetX;
    float offsetY;

    private static final int CELL_INCREMENT = 100;
    private static final int CELL_GRANULAR_INCREMENT = 50;
    private static final int THRESHOLD = 100;



    public DrawingImageView(Context context) {
        super(context);
        paint.setStrokeWidth(5);
        generateHeatmap();
    }

    public DrawingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(5);
        generateHeatmap();
    }

    public DrawingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setStrokeWidth(5);
        generateHeatmap();
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
                        if(outline.size() >1){
                            undoButton.setEnabled(true);
                        }
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
                                readyButton.setEnabled(true);
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

                            invalidate();
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
        if(currentState == STATE.STABLE) {
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));

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

        for (int cellY = 0; cellY < CELLS_Y-1; cellY++){
            for( int cellX = 0; cellX < CELLS_X-1; cellX++){

                int left = (cellX*CELL_LENGTH_X);
                int top=(cellY*CELL_LENGTH_Y);
                int right=(cellX+1)*CELL_LENGTH_X;
                int bottom=(cellY+1)*CELL_LENGTH_Y;

                BoundingBox boundingBox = getBoundingBox();
                rectangle = new Rect(
                        (int)Math.floor(boundingBox.getMinX()),
                        (int)Math.floor(boundingBox.getMinY()),
                        (int)Math.floor(boundingBox.getMaxX()),
                        (int)Math.floor(boundingBox.getMaxY()));

                rectangle = new Rect(left,top,right,bottom);

//                rectangle = new Rect(100,100,200,200);


                paintHeatMap.setColor(heatmapColorMapper(heatMap[cellY][cellX]));

//                paintHeatMap.setColor(Color.GREEN);

                paintHeatMap.setAlpha(128);
                canvas.drawRect(rectangle, paintHeatMap);
            }
        }
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
            float y1 = outline.get(i).y * (-1);
            float x2 = outline.get(i + 1).x;
            float y2 = outline.get(i + 1).y * (-1);

            float a = y1 - y2;
            float b = x2 - x1;
            float c = ((x1 - x2) * y1) + ((y2 - y1) * x1);

            float x3 = x;
            float y3 = y * (-1);

            float intersectionX;
            float intersectionY;

            if (circleIntersectsLine(a, b, c, x3, y3, 50)) {
                //find coordinate of interest
                if (x1 != x2 && y1 != y2) {
                    // line 1
                    float m1 = ((y2 - y1) / (x2 - x1));
                    float c1 = y1 - (m1 * x1);

                    // line 2
                    float m2 = -1 / m1;
                    float c2 = y3 - (m2 * x3);

                    // points of intersection of line 1 and line 2
                    intersectionX = ((c2 - c1) / (m1-m2));
                    intersectionY = ((m1 * intersectionX) + c1);
                } else if (y1 == y2) {
                    intersectionX = x3;
                    intersectionY = y1;
                } else { // x1 == x2
                    intersectionX = x1;
                    intersectionY = y3;
                }
                intersectionY *= (-1); //verse y mapping;

                return new PointF(intersectionX, intersectionY);
            }
        }

        return null;
    }

    public void setUndoButton(Button button){
        this.undoButton = button;
    }

    public void setReadyButton(Button button){
        this.readyButton = button;
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
        for(int i = 0; i < polygons.size(); i++){
            List<PointF> polygonOutline = polygons.get(0);
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
        if(outline.size() >1 && polygons.isEmpty()){
            outline.remove(outline.size()-1);
            if(outline.size() ==0){
                undoButton.setEnabled(false);
            }
            if(currentState == STATE.STABLE){
                currentState = STATE.BOUNDARY_BUILDING;
            }
            invalidate();
        } else {
            List<PointF> polygon = polygons.get(polygons.size() - 1);
            if(!polygon.isEmpty()){
                polygon.remove(polygon.size()-1);
                invalidate();
                if(polygon.isEmpty()){
                    currentState = STATE.STABLE;
                } else {
                    currentState = STATE.WALLS_BUILDING;
                }

            } else if (polygons.size() != 1) {
                List<PointF> previousPolygon = polygons.get(polygons.size() - 2);
                previousPolygon.remove(previousPolygon.size()-1);
                invalidate();
            } else {
                outline.remove(outline.size()-1);
                if(outline.size() ==0){
                    undoButton.setEnabled(false);
                }
                invalidate();
                currentState = STATE.BOUNDARY_BUILDING;
            }
        }
    }


    protected void generateHeatmap() {
        for(int grid_y = 0; grid_y < CELLS_Y; grid_y++){
            for(int grid_x = 0; grid_x < CELLS_X; grid_x++){
                heatMap[grid_y][grid_x] = (float)grid_x/(float)CELLS_X;
            }
        }
    }

    protected int heatmapColorMapper(float heatmapValue) {
        float colourValue = 1 * heatmapValue;
        return Color.rgb(1f, colourValue, 0f);


    }
    
    private BoundingBox getBoundingBox(){
        float minX = 0;
        float maxX = 0;
        float minY = 0;
        float maxY = 0;
        
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

        return new BoundingBox(minX, maxX, minY, maxY);
    }
}