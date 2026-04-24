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

    private void switchSceneButton(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    /**
     * Aumenta el zoom en 0.1 unidades al pulsar el botón "+".
     *
     * @param event evento de acción del botón
     */
    @FXML
    void zoomIn(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal + 0.1);
    }

    /**
     * Reduce el zoom en 0.1 unidades al pulsar el botón "–".
     *
     * @param event evento de acción del botón
     */
    @FXML
    void zoomOut(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal - 0.1);
    }
    /*
     * @param scaleValue nuevo factor de escala (p. ej. 1.2 → 120 %)
     */
    private void zoom(double scaleValue) {
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }


    /*
     * @param event evento de ratón sobre el ListView
     */
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

    // =========================================================
    //  CONSTRUCCIÓN DEL MAPA
    // =========================================================

    /*
     * @param imgFile fichero de imagen a cargar como fondo del mapa
     */
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

    // =========================================================
    //  MENÚ CONTEXTUAL (clic derecho sobre el mapa)
    // =========================================================

    /**
     * @param x coordenada X del clic en el sistema local del mapPane
     * @param y coordenada Y del clic en el sistema local del mapPane
     */
    private void onMapRightClick(double x, double y) {
        mapContextMenu.hide();

        final double clickX = x;
        final double clickY = y;
        mapContextMenu.getItems().get(0).setOnAction(e -> addPoi(clickX, clickY));
        mapContextMenu.getItems().get(1).setOnAction(e -> addCircle(clickX, clickY));

        mapContextMenu.show(
            mapPane.getScene().getWindow(),
            mapPane.localToScreen(x, y).getX(),
            mapPane.localToScreen(x, y).getY()
        );
    }

    // =========================================================
    //  INICIALIZACIÓN DEL CONTROLADOR
    // =========================================================

    /**
     *
     * @param url  URL del documento FXML (no usado aquí)
     * @param rb   paquete de recursos de internacionalización (no usado aquí)
     */
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

    // =========================================================
    //  INDICADOR DE POSICIÓN DEL RATÓN
    // =========================================================

    /**
     * @param event evento de movimiento del ratón
     */
    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText(
            "sceneX: " + (int) event.getSceneX() +
            ", sceneY: " + (int) event.getSceneY() + "\n" +
            "         X: " + (int) event.getX() +
            ",          Y: " + (int) event.getY()
        );
    }

    // =========================================================
    //  DIÁLOGO "ACERCA DE"
    // =========================================================

    /**
     * @param event evento de acción del menú
     */

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


    /**
     * Draws the route on the mapPane using Polyline and highlights Start/End points.
     */
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

    private String formatDuration(java.time.Duration d) {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    /**
     * Error handling helper
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================
    //  AÑADIR UN POI (texto) AL MAPA
    // =========================================================

    /**
     * @param x coordenada X del clic en el sistema local del mapPane
     * @param y coordenada Y del clic en el sistema local del mapPane
     */
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

    // =========================================================
    //  CAMBIAR EL MAPA (selector de fichero)
    // =========================================================

    /**
     * @param event evento de acción del menú
     * @throws IOException si hay un problema al obtener la ruta canónica
     */
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

    // =========================================================
    //  AÑADIR UN CÍRCULO AL MAPA
    // =========================================================

    /**
     * @param x coordenada X en el sistema local del mapPane
     * @param y coordenada Y en el sistema local del mapPane
     */
    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.RED); // radio = 10 px, color = rojo
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle); // Se añade sobre el mapa como cualquier nodo
    }
}
