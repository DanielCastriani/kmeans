package kmeans;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import kmeans.controller.CTRKMeans;
import kmeans.model.Reg;

public class FXMLMainController implements Initializable {

    private CTRKMeans ctr;
    private ToggleGroup tx, ty;
    private ArrayList<RadioButton> rbX, rbY;
    private XYChart.Series seriesCentroid;

    @FXML
    private ScatterChart<?, ?> sc;
    @FXML
    private Spinner<Integer> spnC;
    @FXML
    private TableView<Object> tvData;
    @FXML
    private TableColumn<?, ?> tvData_ID;
    @FXML
    private TableColumn<?, ?> tvData_FEATURES;
    @FXML
    private TableColumn<?, ?> tvData_CLASSTRUE;
    @FXML
    private TableColumn<?, ?> tvData_CLASS;
    @FXML
    private HBox hbX;
    @FXML
    private HBox hbY;
    @FXML
    private NumberAxis naY;
    @FXML
    private NumberAxis naX;
    @FXML
    private TableView<Reg> tvCentroides;
    @FXML
    private TableColumn<?, ?> tvCentroidesClass;
    @FXML
    private TableColumn<?, ?> tvCentroidesFeatures;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sc.setAnimated(false);
        ctr = new CTRKMeans();

        tvData_ID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tvData_FEATURES.setCellValueFactory(new PropertyValueFactory<>("features"));
        tvData_CLASS.setCellValueFactory(new PropertyValueFactory<>("y"));
        tvData_CLASSTRUE.setCellValueFactory(new PropertyValueFactory<>("y_true"));
        tvCentroidesClass.setCellValueFactory(new PropertyValueFactory<>("y_true"));
        tvCentroidesFeatures.setCellValueFactory(new PropertyValueFactory<>("features"));

        spnC.setValueFactory(new SpinnerValueFactory<Integer>() {
            @Override
            public void decrement(int steps) {
                if (getValue() > 1) {
                    setValue(getValue() - steps);
                    ctr.setQtdC(getValue());
                }
            }

            @Override
            public void increment(int steps) {
                setValue(getValue() + steps);
                ctr.setQtdC(getValue());
            }
        });
        spnC.getValueFactory().setValue(new Integer(2));

    }

    private void addRadioButton(int Index, HBox hBox, ToggleGroup tg, ArrayList<RadioButton> rb) {
        RadioButton r = new RadioButton(ctr.getFeatures_labels().get(Index));
        r.setToggleGroup(tg);
        r.setOnAction((ActionEvent event) -> {
            OnAction_RadioButtonChange(event);
        });
        hBox.getChildren().add(r);
        rb.add(r);
    }

    public void init() {
        tx = new ToggleGroup();
        ty = new ToggleGroup();

        hbX.getChildren().clear();
        hbY.getChildren().clear();

        hbX.getChildren().add(new Label("X:"));
        hbY.getChildren().add(new Label("Y:"));
        rbX = new ArrayList<>();
        rbY = new ArrayList<>();
        for (int i = 1; i < ctr.getFeatures_labels().size() - 1; i++) {
            addRadioButton(i, hbX, tx, rbX);
            addRadioButton(i, hbY, ty, rbY);
        }

        rbX.get(0).setSelected(true);
        rbY.get(1).setSelected(true);

        ctr.setQtdC(spnC.getValue());
        ctr.initCentroid();
        ctr.reinicia();
        sc.setTitle(ctr.getPath());
    }

    @FXML
    private void OnAction_Abrir(ActionEvent event) {
        if (ctr.load_data()) {
            if (ctr.verificaDados()) {
                init();
                update();
            }
        }
    }

    @FXML
    private void OnAction_Reset(ActionEvent event) {
        if (ctr.verificaDados()) {
            init();
            update();
        }
    }

    @FXML
    private void OnAction_PassoAPasso(ActionEvent event) {
        ctr.nextStep();
        update();
    }
    Task auto;

    @FXML
    private void OnAction_Resolve(ActionEvent event) {

        while (ctr.nextStep()) {
            update();
        }

    }

    private int getRadioIndex(ArrayList<RadioButton> rb) {
        int i = 0;
        for (RadioButton r : rb) {
            if (r.isSelected()) {
                break;
            }
            i++;
        }
        return i;
    }

    private XYChart.Series addOnSeries(ArrayList<Reg> regs, int ix, int iy) {
        XYChart.Series s = new XYChart.Series();
        for (Reg r : regs) {
            double x = (double) r.getFeatures().get(ix);
            double y = (double) r.getFeatures().get(iy);
            s.getData().add(new XYChart.Data(x, y));
        }
        return s;
    }

    private void updateChart() {
        sc.getData().clear();
        int irbX = getRadioIndex(rbX);
        int irbY = getRadioIndex(rbY);

        naX.setLabel(rbX.get(irbX).getText());
        naY.setLabel(rbY.get(irbY).getText());

        seriesCentroid = addOnSeries(ctr.getCents(), irbX, irbY);
        seriesCentroid.setName("Centroides");
        sc.getData().addAll(seriesCentroid);

        ArrayList<XYChart.Series> classes = new ArrayList<>();

        for (Reg c : ctr.getCents()) {
            classes.add(new XYChart.Series());
            classes.get(classes.size() - 1).setName((String) c.getY());
        }

        for (Reg r : ctr.getRegs()) {
            for (XYChart.Series s : classes) {
                if (r.getY().equals(s.getName())) {
                    s.getData().add(new XYChart.Data<>(r.getFeatures().get(irbX), r.getFeatures().get(irbY)));
                    break;
                }
            }
        }

        for (XYChart.Series s : classes) {
            sc.getData().addAll(s);
        }
    }

    private void updateChartNC() {
        sc.getData().clear();
        int irbX = getRadioIndex(rbX);
        int irbY = getRadioIndex(rbY);

        naX.setLabel(rbX.get(irbX).getText());
        naY.setLabel(rbY.get(irbY).getText());

        seriesCentroid = addOnSeries(ctr.getCents(), irbX, irbY);
        seriesCentroid.setName("Centroides");

        XYChart.Series ncSeries = addOnSeries(ctr.getRegs(), irbX, irbY);
        ncSeries.setName("N/C");

        sc.getData().addAll(seriesCentroid);
        sc.getData().add(ncSeries);

    }

    private void updateTab() {
        tvCentroides.getItems().clear();
        tvCentroides.setItems(FXCollections.observableArrayList(ctr.getCents()));
        tvData.getItems().clear();
        tvData.setItems(FXCollections.observableArrayList(ctr.getRegs()));
    }

    private void update() {
        if (ctr.verificaDados()) {
            if (ctr.getRegs().get(0).getY().equals("[N. C.]")) {
                updateChartNC();
            } else {
                updateChart();
            }
            updateTab();
        }
    }

    @FXML
    private void OnMouseClicked_spn(MouseEvent event) {
        update();
    }

    private void OnAction_RadioButtonChange(ActionEvent event) {
        update();
    }
}
