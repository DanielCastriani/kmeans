package kmeans.model;

import java.util.ArrayList;

public class Reg {
    private Object id;
    private ArrayList<Double> features;
    private Object y;
    private Object y_true;

    public Reg() {
    }

    public Reg(ArrayList<Double> features, Object y) {
        this.features = features;
        this.y = y;
    }


    public Reg(Object id, ArrayList<Double> features, Object y) {
        this.id = id;
        this.features = features;
        this.y = y;
    }
    
    public Reg(Object id, ArrayList<Double> features, Object y, Object y_true) {
        this.id = id;
        this.features = features;
        this.y = y;
        this.y_true = y_true;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public ArrayList getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<Double> features) {
        this.features = features;
    }

    public Object getY() {
        return y;
    }

    public void setY(Object y) {
        this.y = y;
    }

    public Object getY_true() {
        return y_true;
    }

    public void setY_true(Object y_true) {
        this.y_true = y_true;
    }
    
    
}
