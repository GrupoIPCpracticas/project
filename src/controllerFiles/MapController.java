package controllerFiles;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Poi;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import upv.ipc.sportlib.*;
import javafx.scene.shape.Polyline;

public class MapController implements Initializable {

    private Group zoomGroup;

    @FXML private Pane mapPane;
    @FXML private ListView<Activity> map_listview;
    @FXML private ScrollPane map_scrollpane;
    @FXML private Slider zoom_slider;
    @FXML private Label mousePosition;
    @FXML private Button statsButton;


    private ContextMenu mapContextMenu;
    private boolean insertionMode = false;
    private SportActivityApp app;
    private MapProjection projection;
    private MapRegion currentRegion;
    private Activity currentActivity = null;
    private GeoPoint firstPoint = null; // Stores the start/center
    private AnnotationType pendingType = null; // Stores what we are drawing
    private String pendingText = "";
    private String pendingColor = "#FF0000";


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        statsButton.setDisable(true);
        zoom_slider.setMin(0.5);   // zoom mínimo: 50 %
        zoom_slider.setMax(1.5);   // zoom máximo: 150 %
        zoom_slider.setValue(1.0); // valor inicial: 100 %

        refreshActivityList();
        zoom_slider.valueProperty().addListener(
                (observable, oldVal, newVal) -> zoom((Double) newVal)
        );

        MenuItem miText   = new MenuItem("📝 Add Text");
        MenuItem miCircle = new MenuItem("⭕ Add Point");
        mapContextMenu = new ContextMenu(miText, miCircle);

        map_listview.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                statsButton.setDisable(false);
                loadActivityData(newVal);
            }
        });

        map_listview.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);
                if (empty || activity == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String date = activity.getStartTime().toLocalDate().toString();
                    setText(activity.getName() + " — " + date);
                    setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
                }
            }
        });
        buildMap(new File("maps/upv.jpg"));
    }

    @FXML
    private void changeMap(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));

        File imgFile = fc.showOpenDialog(zoom_slider.getScene().getWindow());

        if (imgFile != null) {
            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile);
            map_listview.getItems().clear();
        }
    }

    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText(
                "sceneX: " + (int) event.getSceneX() +
                        ", sceneY: " + (int) event.getSceneY() + "\n" +
                        "         X: " + (int) event.getX() +
                        ",          Y: " + (int) event.getY()
        );
    }

    @FXML
    void zoomIn(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal + 0.1);
    }

    @FXML
    void zoomOut(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal - 0.1);
    }

    private void zoom(double scaleValue) {
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    @FXML
    void listClicked(MouseEvent event) {

    }

    @FXML
    private void handleStats(ActionEvent event) throws IOException{
        if (this.currentActivity == null) {
            showError("No activity loaded. Please import a GPX file first.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/Statistics.fxml"));
        Parent root = loader.load();

        StatisticsController controller = loader.getController();
        controller.setActivityData(currentActivity);
        switchSceneMenu(event, root, "Activity Statistics", true);

    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        SportActivityApp app = SportActivityApp.getInstance();
        app.logout();
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/Welcome.fxml"));
        switchSceneMenu(event, root, "Demo mapas - IPC", false);

    }

    @FXML
    private void handleProfile(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/EditProfile.fxml"));
        switchSceneMenu(event, root, "Edit Profile", false);
    }

    @FXML
    private void handleSessions(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/SessionsHistory.fxml"));
        switchSceneMenu(event, root, "Sessions History", false);
    }

    @FXML
    private void handleImportGPX(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select GPX File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPX Files", "*.gpx"));
        File file = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                currentActivity = app.importActivity(file);

                if (currentActivity != null) {
                    refreshActivityList();
                    map_listview.getSelectionModel().select(currentActivity);
                    currentRegion = currentActivity.getSuggestedMap();
                    File mapImageFile = new File(currentRegion.getImagePath());
                    Image img = buildMap(mapImageFile);
                    this.projection = new MapProjection(currentRegion, img.getWidth(), img.getHeight());
                    drawRoute(currentActivity);
                    if (statsButton != null) statsButton.setDisable(false);
                }
            } catch (Exception e) {
                showError("Error processing GPX: " + e.getMessage());
            }
        }
    }

    // Auxiliary methods

    private void refreshActivityList() {
        List<Activity> activities = app.getUserActivities();
        map_listview.getItems().setAll(activities);
    }

    private void loadActivityData(Activity activity) {
        currentActivity = activity;
        currentRegion = activity.getSuggestedMap();
        if (currentRegion == null) {
            showError("No map region found for this activity.");
            return;
        }
        File mapFile = new File(currentRegion.getImagePath());
        Image mapImage = buildMap(mapFile);

        if (mapImage != null) {
            this.projection = new MapProjection(currentRegion, mapImage.getWidth(), mapImage.getHeight());
            drawRoute(activity);
            for (Annotation ann : activity.getAnnotations()) {
                displayAnnotation(ann);
            }

        }
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Next Step");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private Image buildMap(File imgFile) {
        if (!imgFile.exists()) {
            map_scrollpane.setContent(new Label("Image not found: " + imgFile.getAbsolutePath()));
            return null;
        }

        Image img = new Image(imgFile.toURI().toString());
        double w = img.getWidth();
        double h = img.getHeight();

        mapPane.getChildren().clear();

        ImageView background = new ImageView(img);
        background.setFitWidth(w);
        background.setFitHeight(h);
        background.setPreserveRatio(true);
        mapPane.getChildren().add(background);
        mapPane.setPrefSize(w, h);
        mapPane.setMinSize(w, h);
        mapPane.setMaxSize(w, h);
        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                onMapRightClick(e.getX(), e.getY());
            }
            else if (e.getButton() == MouseButton.PRIMARY) {

                if (firstPoint != null) {
                    complexAnnotation(e.getX(), e.getY());
                }
                else if (insertionMode) {
                    insertionMode = false;
                    mapPane.setStyle("");
                    addPoi(e.getX(), e.getY());
                }
            }
        });

        if (zoomGroup == null) {
            zoomGroup = new Group(mapPane);
        } else {
            zoomGroup.getChildren().setAll(mapPane);
        }

        Group contentGroup = new Group(zoomGroup);
        map_scrollpane.setContent(contentGroup);

        double zoomLevel = zoom_slider.getValue();
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);

        return img;
    }

    private void onMapRightClick(double x, double y) {
        if (currentActivity == null || projection == null) return;

        GeoPoint geoPoint = projection.unproject(x, y);

        Dialog<Annotation> dialog = new Dialog<>();
        dialog.setTitle("Add Annotation");
        dialog.setHeaderText("Create a new map annotation");

        TextField textDescription = new TextField();
        ColorPicker picker = new ColorPicker(Color.RED);
        ComboBox<AnnotationType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(AnnotationType.values()));
        typeCombo.setValue(AnnotationType.POINT);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(textDescription, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(picker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                AnnotationType type = typeCombo.getValue();

                if (type == AnnotationType.POINT || type == AnnotationType.TEXT) {
                    return new Annotation(type, textDescription.getText(), toHex(picker.getValue()), 2.0, List.of(geoPoint));
                } else {
                    this.firstPoint = geoPoint;
                    this.pendingType = type;
                    this.pendingText = textDescription.getText();
                    this.pendingColor = toHex(picker.getValue());

                    mapPane.setCursor(Cursor.CROSSHAIR);
                    showInformation("Click anywhere on the map to set the " + (type == AnnotationType.LINE ? "end point" : "radius"));
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(ann -> {
            Annotation saved = app.addAnnotation(currentActivity, ann);
            if (saved != null) displayAnnotation(saved);
        });
    }

    private void complexAnnotation(double x, double y) {
        GeoPoint secondPoint = projection.unproject(x, y);
        Annotation complexAnn = new Annotation(
                pendingType,
                pendingText,
                pendingColor,
                3.0,
                List.of(firstPoint, secondPoint)
        );
        Annotation saved = app.addAnnotation(currentActivity, complexAnn);
        if (saved != null) {
            displayAnnotation(saved);
        }
        firstPoint = null;
        pendingType = null;
        mapPane.setCursor(Cursor.DEFAULT);
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
    }

    private void displayAnnotation(Annotation ann) {
        List<GeoPoint> gps = ann.getGeoPoints();
        if (gps.isEmpty() || projection == null) return;

        Color annotationColor = Color.web(ann.getColor());
        Point2D p1 = projection.project(gps.get(0));

        switch (ann.getType()) {
            case POINT:
                Circle dot = new Circle(p1.getX(), p1.getY(), 5, annotationColor);
                dot.setStroke(Color.WHITE);
                mapPane.getChildren().add(dot);
                addLabel(p1, ann);
                break;

            case TEXT:
                addLabel(p1, ann);
                break;

            case LINE:
                if (gps.size() >= 2) {
                    Point2D p2 = projection.project(gps.get(1));
                    Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                    line.setStroke(annotationColor);
                    line.setStrokeWidth(ann.getStrokeWidth());
                    mapPane.getChildren().add(line);
                }
                break;

            case CIRCLE:
                if (gps.size() >= 2) {
                    Point2D edge = projection.project(gps.get(1));
                    double pixelRadius = p1.distance(edge);

                    Circle circle = new Circle(p1.getX(), p1.getY(), pixelRadius);
                    circle.setStroke(annotationColor);
                    circle.setStrokeWidth(ann.getStrokeWidth());
                    circle.setFill(annotationColor.deriveColor(0, 1, 1, 0.3));

                    mapPane.getChildren().add(circle);
                }
                break;
        }
    }

    private void addLabel(Point2D pos, Annotation ann) {
        if (ann.getText() == null || ann.getText().isEmpty()) return;
        Label label = new Label(ann.getText());
        label.setTextFill(Color.web(ann.getColor()));
        label.setLayoutX(pos.getX() + 10);
        label.setLayoutY(pos.getY() - 10);
        label.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); " +
                "-fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");

        mapPane.getChildren().add(label);
    }

    private void drawRoute(Activity activity) {
        List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.isEmpty()) return;

        Polyline routeLine = new Polyline();
        routeLine.setStroke(Color.BLUE);
        routeLine.setStrokeWidth(3);
        routeLine.getStrokeDashArray().addAll(5.0, 5.0);

        for (TrackPoint tp : points) {
            Point2D pixel = projection.project(tp);
            routeLine.getPoints().addAll(pixel.getX(), pixel.getY());
        }
        Point2D startPx = projection.project(activity.getStartPoint());
        Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 6, Color.LIMEGREEN);
        startMarker.setStroke(Color.BLACK);

        Point2D endPx = projection.project(activity.getEndPoint());
        Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 6, Color.RED);
        endMarker.setStroke(Color.BLACK);

        mapPane.getChildren().addAll(routeLine, startMarker, endMarker);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addPoi(double x, double y) {
        if (currentActivity == null || projection == null) {
            showError("Please select or import an activity first.");
            return;
        }

        Dialog<Annotation> poiDialog = new Dialog<>();
        poiDialog.setTitle("New Point of Interest");
        poiDialog.setHeaderText("Mark a location on the map");
        ButtonType okButton = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        TextField nameField = new TextField();
        nameField.setPromptText("Name of POI (e.g. Refreshment Point)");
        VBox vbox = new VBox(10, new Label("Name:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);
        GeoPoint geoPos = projection.unproject(x, y);
        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Annotation(
                        AnnotationType.POINT,
                        nameField.getText().trim(),
                        "#3498db", // Nice blue color
                        2.0,
                        List.of(geoPos)
                );
            }
            return null;
        });
        Optional<Annotation> result = poiDialog.showAndWait();
        if (result.isPresent()) {
            Annotation saved = app.addAnnotation(currentActivity, result.get());
            if (saved != null) {
                displayAnnotation(saved);
            }
        }
    }

    private void switchSceneMenu(ActionEvent event, Parent root, String title, boolean wait) {
        if (wait) {
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage mainStage = (Stage) map_scrollpane.getScene().getWindow();
            stage.initOwner(mainStage);
            stage.showAndWait();

        } else {
            Stage stage = (Stage) map_scrollpane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        }
    }
}
