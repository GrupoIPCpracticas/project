package controllerFiles;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Poi;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    @FXML private ListView<Poi> map_listview;
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


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statsButton.setDisable(true);
        zoom_slider.setMin(0.5);   // zoom mínimo: 50 %
        zoom_slider.setMax(1.5);   // zoom máximo: 150 %
        zoom_slider.setValue(1.0); // valor inicial: 100 %

        zoom_slider.valueProperty().addListener(
                (observable, oldVal, newVal) -> zoom((Double) newVal)
        );

        MenuItem miText   = new MenuItem("📝 Add Text");
        MenuItem miCircle = new MenuItem("⭕ Add Point");
        mapContextMenu = new ContextMenu(miText, miCircle);

        map_listview.setCellFactory(listView -> new ListCell<Poi>() {
            @Override
            protected void updateItem(Poi poi, boolean empty) {
                super.updateItem(poi, empty);

                if (empty || poi == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(poi.getCode() + " – " + poi.getPosition());
                }
            }
        });
        buildMap(new File("maps/upv.jpg"));
        app = SportActivityApp.getInstance();
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
        Poi itemSelected = map_listview.getSelectionModel().getSelectedItem();
        if (itemSelected == null) return;

        double mapWidth  = mapPane.getWidth()  * zoomGroup.getScaleX();
        double mapHeight = mapPane.getHeight() * zoomGroup.getScaleY();

        double poiX = itemSelected.getPosition().getX() * zoomGroup.getScaleX();
        double poiY = itemSelected.getPosition().getY() * zoomGroup.getScaleY();

        double viewW = map_scrollpane.getViewportBounds().getWidth();
        double viewH = map_scrollpane.getViewportBounds().getHeight();

        double scrollH = (poiX - viewW / 2) / (mapWidth  - viewW);
        double scrollV = (poiY - viewH / 2) / (mapHeight - viewH);

        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        final Timeline timeline = new Timeline();
        final KeyValue kv1 = new KeyValue(map_scrollpane.hvalueProperty(), scrollH);
        final KeyValue kv2 = new KeyValue(map_scrollpane.vvalueProperty(), scrollV);
        final KeyFrame kf  = new KeyFrame(Duration.millis(500), kv1, kv2);
        timeline.getKeyFrames().add(kf);
        timeline.play();
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
                    statsButton.setDisable(false);
                    currentRegion = currentActivity.getSuggestedMap();
                    File mapImageFile = new File(currentRegion.getImagePath());
                    buildMap(mapImageFile);
                    this.projection = new MapProjection(currentRegion, mapPane.getWidth(), mapPane.getHeight());
                    drawRoute(currentActivity);
                }
            } catch (Exception e) {
                showError("Error processing GPX: " + e.getMessage());
            }
        }
    }

    // Auxiliary methods

    private void buildMap(File imgFile) {
        if (!imgFile.exists()) {
            map_scrollpane.setContent(new Label("Image not found: " + imgFile.getAbsolutePath()));
            return;
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
            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                insertionMode = false;
                mapPane.setStyle("");
                addPoi(e.getX(), e.getY());
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
    }

    private void onMapRightClick(double x, double y) {
        if (currentActivity == null || projection == null) return;

        GeoPoint geoPoint = projection.unproject(x, y);

        Dialog<Annotation> dialog = new Dialog<>();
        dialog.setTitle("Add Annotation");
        dialog.setHeaderText("Create a new point annotation");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textDescription = new TextField();
        textDescription.setPromptText("e.g., Dangerous crossing");

        ColorPicker picker = new ColorPicker(Color.RED);

        grid.add(new Label("Description:"), 0, 0);
        grid.add(textDescription, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(picker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String hexColor = toHex(picker.getValue());
                return new Annotation(
                        AnnotationType.POINT,
                        textDescription.getText(),
                        hexColor,
                        2.0,
                        List.of(geoPoint)
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(ann -> {
            Annotation saved = app.addAnnotation(currentActivity, ann);
            if (saved != null) {
                displayAnnotation(saved);
            }
        });
    }

    private void displayAnnotation(Annotation ann) {
        GeoPoint gp = ann.getGeoPoints().get(0);
        Point2D pix = projection.project(gp);

        if (ann.getType() == AnnotationType.POINT) {
            Circle marker = new Circle(pix.getX(), pix.getY(), 6);
            marker.setFill(Color.web(ann.getColor()));
            marker.setStroke(Color.WHITE);
            marker.setStrokeWidth(ann.getStrokeWidth());

            Label label = new Label(ann.getText());
            label.setLayoutX(pix.getX() + 10);
            label.setLayoutY(pix.getY() - 10);
            label.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-padding: 2;");

            mapPane.getChildren().addAll(marker, label);
        }
        // Add logic here for LINE (Polyline) or CIRCLE (Circle with radius)
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
    }


    private void drawRoute(Activity activity) {
        List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.isEmpty()) return;

        Polyline route = new Polyline();
        route.setStroke(Color.BLUE);
        route.setStrokeWidth(3);
        route.setMouseTransparent(true);

        List<Point2D> pixelPoints = projection.projectActivity(activity);

        for (Point2D p : pixelPoints) {
            route.getPoints().addAll(p.getX(), p.getY());
        }

        TrackPoint start = activity.getStartPoint();
        Point2D startPx = projection.project(start);
        Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 7, Color.GREEN);
        startMarker.setStroke(Color.WHITE);
        TrackPoint end = activity.getEndPoint();
        Point2D endPx = projection.project(end);
        Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 7, Color.RED);
        endMarker.setStroke(Color.WHITE);

        mapPane.getChildren().addAll(route, startMarker, endMarker);
    }

    private Point2D geoToPixel(double lat, double lon) {
        double topLat = 39.485;
        double bottomLat = 39.475;
        double leftLon = -0.345;
        double rightLon = -0.330;

        double x = mapPane.getWidth() * (lon - leftLon) / (rightLon - leftLon);
        double y = mapPane.getHeight() * (topLat - lat) / (topLat - bottomLat);

        return new Point2D(x, y);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addPoi(double x, double y) {

        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
            return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        Optional<Poi> result = poiDialog.showAndWait();

        if (result.isPresent()) {
            Poi poi = result.get();
            poi.setPosition(new Point2D(x, y));
            map_listview.getItems().add(poi);
            Text text = new Text(poi.getCode());
            text.setX(x);
            text.setY(y);
            mapPane.getChildren().add(text);
        }
    }

    @FXML
    private void changeMap(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(".")); // Empezamos en el directorio del proyecto

        File imgFile = fc.showOpenDialog(zoom_slider.getScene().getWindow());

        if (imgFile != null) {
            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile); // Reconstruimos la vista con la nueva imagen
            map_listview.getItems().clear(); // Borramos los datos del mapa anterior
        }
    }

    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.RED); // radio = 10 px, color = rojo
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle); // Se añade sobre el mapa como cualquier nodo
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
