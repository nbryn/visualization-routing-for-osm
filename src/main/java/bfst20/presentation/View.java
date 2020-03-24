package bfst20.presentation;

import bfst20.logic.kdtree.Rect;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bfst20.data.Model;
import bfst20.logic.DrawableGenerator;
import bfst20.logic.Type;
import bfst20.logic.entities.Relation;
import bfst20.logic.entities.Way;
import bfst20.logic.interfaces.Drawable;
import bfst20.logic.kdtree.KdTree;

import java.awt.*;

public class View {

    Affine trans = new Affine();
    List<Way> data;
    List<Relation> islandRelations;
    Canvas canvas;
    GraphicsContext gc;
    Map<Type, List<LinePath>> drawables;
    Map<Type, KdTree> kdtrees;
    boolean kd;

    public View(Canvas canvas) {
        kd = false;
        this.canvas = canvas;
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void update() {

    }

    public void initializeData() {
        Model model = Model.getInstance();
        float minlon = model.getMinLon();
        float maxlon = model.getMaxLon();
        float minlat = model.getMinLat();
        float maxlat = model.getMaxLat();

        DrawableGenerator drawableGenerator = DrawableGenerator.getInstance();

        drawables = drawableGenerator.createDrawables();
        //Burde flyttes.
        kdtrees = new HashMap<>();
        Rect rect = new Rect(minlat, maxlat, minlon, maxlon);
        kdtrees.put(Type.BUILDING, new KdTree(drawables.get(Type.BUILDING), rect));

        pan(-minlon, -minlat);
        zoom(canvas.getHeight() / (maxlon- minlon), (minlat- maxlat)/2, 0);


        repaint();
    }

    public void repaint() {

        gc.setTransform(new Affine());

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        
        double pixelwidth = 1 / Math.sqrt(Math.abs(trans.determinant()));
        gc.setLineWidth(pixelwidth);

        for (Drawable element : drawables.get(Type.COASTLINE)) {
            element.draw(gc);
            gc.fill();
        }



        /*for (Drawable element : drawables.get(Type.HIGHWAY)){
            element.draw(gc);
            gc.fill();
        }
        for (Drawable element : drawables.get(Type.BUILDING)) {
            element.draw(gc);
            gc.fill();
        }
        for (Drawable element : drawables.get(Type.NATURAL)) {
            element.draw(gc);
            gc.fill();
        }  */

        /*for (Drawable element : drawables.get(Type.BUILDING)) {
            element.draw(gc);
            gc.fill();
        }*/

        Rect rect = new Rect(-55, -56, 0, (float) 5.93);

        for (Drawable element : kdtrees.get(Type.BUILDING).query(rect)) {
            element.draw(gc);
            gc.fill();
        }

    }

    public void drawWay(){

    }

    public void zoom(double factor, double x, double y) {
        if(trans.determinant() >= 1.7365306045084698E9){
            kd = true;
        }else{
            kd = false;
        }
        trans.prependScale(factor, factor, x, y);
        repaint();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
	}
}