package kmeans.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import javafx.stage.FileChooser;
import kmeans.model.Reg;

public class CTRKMeans {

    private int qtdC;
    private String path;
    private ArrayList<Reg> regs;
    private ArrayList<Reg> cents;
    private ArrayList<String> features_labels;
    private boolean classificou;
    

    private void init() {
        regs = null;
        features_labels = null;
        cents = new ArrayList<>();
        classificou = true;
    }

    public CTRKMeans() {
        init();
    }

    public String getPath() {
        return path;
    }

    public ArrayList<Reg> getRegs() {
        return regs;
    }

    public ArrayList<Reg> getCents() {
        return cents;
    }

    public ArrayList<String> getFeatures_labels() {
        return features_labels;
    }

    public void setQtdC(Integer value) {
        int dif = value - qtdC;
        if (dif < 0) {
            dif *= -1;
            while (dif-- > 0) {
                cents.remove(cents.size() - 1);
            }
        } else {

            while (dif-- > 0) {
                addCentroid();
            }
        }

        qtdC = value;
        classificou = true;
    }

    public void addCentroid() {
        ArrayList<Double> atribs = new ArrayList<>();
        for (int i = 0; i < regs.get(0).getFeatures().size(); i++) {
            double value = Math.random() * getMaxValue(i);
            atribs.add(value);
        }
        cents.add(new Reg(cents.size(), atribs, "Classe " + (cents.size() + 1), "Classe " + (cents.size() + 1)));
    }

    public boolean load_data() {
        regs = new ArrayList<>();
        features_labels = new ArrayList<>();

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialDirectory(new File("."));
        File f = fc.showOpenDialog(null);
        try {

            path = f.getName();
            BufferedReader br = new BufferedReader(new FileReader(f.getAbsoluteFile()));
            String line = br.readLine();
            String[] splited = line.split(",");

            if (f != null) {
                for (String s : splited) {
                    features_labels.add(s);
                }
                while ((line = br.readLine()) != null) {
                    splited = line.split(",");
                    ArrayList<Double> features = new ArrayList<>();
                    for (int i = 1; i < splited.length - 1; i++) {
                        String s = splited[i];
                        features.add(Double.parseDouble(s));
                    }
                    Reg r = new Reg(splited[0], features, "[N. C.]", splited[splited.length - 1]);
                    regs.add(r);
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR:" + ex);
            init();
            return false;
        }
        return true;
    }

    public void reinicia() {
        classificou = true;
        for (Reg r : regs) {
            r.setY("[N. C.]");
        }
    }

    /**
     * @return retorna maior valor da coluna de todas a linhas
     */
    public double getMaxValue(int index) {
        Reg r = regs.get(0);
        double max = (double) r.getFeatures().get(index);
        for (int i = 1; i < regs.size(); i++) {
            r = regs.get(i);
            if ((double) r.getFeatures().get(index) > max) {
                max = (double) r.getFeatures().get(index);
            }
        }
        return max;
    }

    /**
     * @return retorna menor valor da coluna de todas a linhas
     */
    public double getMinValue(int index) {
        Reg l = regs.get(0);
        double min = (double) l.getFeatures().get(index);
        for (int i = 1; i < regs.size(); i++) {
            l = regs.get(i);
            if ((double) l.getFeatures().get(index) < min) {
                min = (double) l.getFeatures().get(index);
            }
        }
        return min;
    }

    /**
     * Inicializa Centroides com valores com o valor maximo das colunas
     * multiplicado com um valor aleatório de 0.0 a 1
     */
    public void initCentroid() {
        cents = new ArrayList<>();
        for (int q = 0; q < qtdC; q++) {
            ArrayList<Double> atribs = new ArrayList<>();

            for (int i = 0; i < regs.get(0).getFeatures().size(); i++) {
                double value = (double) Math.random() * getMaxValue(i);
                atribs.add(value);
            }

            cents.add(new Reg(cents.size(), atribs, "Classe " + (cents.size() + 1), "Classe " + (cents.size() + 1)));
        }
    }

    /**
     * @param index_centroid index do centroide
     * @param index_reg index da linha
     * @return Distancia euclidiana entre o centroide e o registro
     */
    private double distancia(int index_centroid, int index_reg) {
        Reg l = regs.get(index_reg);
        Reg c = cents.get(index_centroid);

        double dist = 0;
        for (int i = 0; i < l.getFeatures().size(); i++) {
            double x = (double) l.getFeatures().get(i) - (double) c.getFeatures().get(i);
            dist += Math.pow(x, 2);
        }
        return (double) Math.sqrt(dist);
    }

    /**
     * Classifica uma linha
     */
    private boolean classificar_linha(int index_reg) {
        double menor = distancia(0, index_reg);
        int iMenor = 0;
        for (int iC = 1; iC < cents.size(); iC++) {
            double dist = distancia(iC, index_reg);
            if (dist < menor) {
                menor = dist;
                iMenor = iC;
            }
        }

        if (!regs.get(index_reg).getY().equals(cents.get(iMenor).getY_true())) {
            regs.get(index_reg).setY(cents.get(iMenor).getY_true());
            return true;
        }
        return false;
    }

    /**
     * Classifica todos os elementos
     */
    private boolean classifica() {
        boolean classificou = false;
        for (int i = 0; i < regs.size(); i++) {
            classificou = classificou | classificar_linha(i);
        }
        return classificou;
    }

    /**
     * Recalcula os centroides, inicializa atributos centroides com valor 0,
     * Soma ao atributo valoes dos registros da mesma classe o centroide Calcula
     * média
     */
    private void recalcula_centroid() {
        ArrayList<Object> countClass = new ArrayList();
        
        for (Reg c : cents) {
            countClass.add(0);
            for (int i = 0; i < c.getFeatures().size(); i++) {
                c.getFeatures().set(i, 0.0);
            }
        }
        
        for (Reg r : regs) {
            for (int indexC = 0; indexC < cents.size(); indexC++) {
                Reg c = cents.get(indexC);
                if (r.getY().equals(c.getY_true())) {

                    for (int i = 0; i < c.getFeatures().size(); i++) {
                        double newValues = (double) c.getFeatures().get(i) + (double) r.getFeatures().get(i);
                        c.getFeatures().set(i, newValues);
                    }
                    countClass.set(indexC, (int) countClass.get(indexC) + 1);
                    break;
                }
            }
        }
        
        for (int ic = 0; ic < cents.size(); ic++) {
            Reg c = cents.get(ic);
            for (int i = 0; i < c.getFeatures().size(); i++) {
                double newValue = (double) c.getFeatures().get(i) / ((int) countClass.get(ic)!= 0 ? (int) countClass.get(ic):1);
                c.getFeatures().set(i, newValue);
            }
        }
    }

    public void nextStep() {
        
        if (classificou) {
            classificou = classifica();
            recalcula_centroid();
        }
    }

    public boolean verificaDados() {
        return getRegs() != null && getCents() != null;
    }

}
