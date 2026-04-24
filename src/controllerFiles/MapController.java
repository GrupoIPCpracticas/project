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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.TrackPoint;
import javafx.scene.shape.Polyline;

public class MapController implements Initializable {

    /** Group que se escala para aplicar el zoom. */
    private Group zoomGroup;

    @FXML
    private Pane mapPane;

    private ContextMenu mapContextMenu;

    private boolean insertionMode = false;

    @FXML
    private ListView<Poi> map_listview;

    @FXML
    private ScrollPane map_scrollpane;

    @FXML
    private Slider zoom_slider;

    private MenuButton map_pin;

    @FXML
    private Label mousePosition;
    @FXML
    private SplitPane splitPane;

    private SportActivityApp app;

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

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) map_scrollpane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
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
     *
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

    // =========================================================
    //  CONSTRUCCIÓN DEL MAPA
    // =========================================================

    /*
     * @param imgFile fichero de imagen a cargar como fondo del mapa
     */
    private void buildMap(File imgFile) {
        // Comprobación defensiva: si el fichero no existe mostramos un aviso
        if (!imgFile.exists()) {
            map_scrollpane.setContent(
                new Label("Imagen no encontrada: " + imgFile.getPath()));
            return;
        }

        // Cargamos la imagen y obtenemos sus dimensiones reales en píxeles
        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        mapPane = new Pane();
        mapPane.setPrefSize(W, H); // tamaño preferido = tamaño de la imagen
        mapPane.setMinSize(W, H);  // impedimos que el layout lo encoja
        mapPane.setMaxSize(W, H);  // impedimos que el layout lo agrande

        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        mapPane.getChildren().add(iv);

        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Clic derecho → mostrar menú contextual
                onMapRightClick(e.getX(), e.getY());

            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                // FIX 2: clic izquierdo en modo inserción → añadir POI y desactivar modo
                insertionMode = false;
                mapPane.setStyle(""); // Restauramos el cursor normal
                addPoi(e.getX(), e.getY());
            }
        });

        zoomGroup = new Group();
        Group contentGroup = new Group();
        zoomGroup.getChildren().add(mapPane);
        contentGroup.getChildren().add(zoomGroup);

        // Aplicamos el zoom actual (valor actual del slider)
        double zoom = zoom_slider.getValue();
        zoomGroup.setScaleX(zoom);
        zoomGroup.setScaleY(zoom);

        // Asignamos el contentGroup como contenido del ScrollPane
        map_scrollpane.setContent(contentGroup);

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

        zoom_slider.setMin(0.5);   // zoom mínimo: 50 %
        zoom_slider.setMax(1.5);   // zoom máximo: 150 %
        zoom_slider.setValue(1.0); // valor inicial: 100 %

        zoom_slider.valueProperty().addListener(
            (observable, oldVal, newVal) -> zoom((Double) newVal)
        );

        MenuItem miText   = new MenuItem("📝 Añadir texto");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
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
        buildMap(new File("resources/upv.jpg"));
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
    private void about(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);

        // Personalizamos el icono de la ventana del diálogo
        Stage dialogStage = (Stage) mensaje.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        mensaje.setTitle("Acerca de");
        mensaje.setHeaderText("IPC - 2026");
        mensaje.showAndWait(); // Bloquea hasta que el usuario cierra el diálogo
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        SportActivityApp app = SportActivityApp.getInstance();
        app.logout();
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/Welcome.fxml"));
        switchScene(event, root, "Demo mapas - IPC");

    }

    @FXML
    private void handleProfile(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/EditProfile.fxml"));
        switchScene(event, root, "Edit Profile");
    }

    @FXML
    private void handleSessions(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/SessionsHistory.fxml"));
        switchScene(event, root, "Sessions History");
    }

    @FXML
    private void handleImportGPX(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select GPX File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPX Files", "*.gpx"));
        File file = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                Activity activity = app.importActivity(file);

                if (activity != null) {
                    displayStatistics(activity);
                    drawRoute(activity);
                }
            } catch (Exception e) {
                showError("Error processing GPX: " + e.getMessage());
            }
        }
    }

    /**
     * Shows the statistics of the imported GPX activity in the UI.
     * @param activity The activity object returned by the library.
     */
    private void displayStatistics(Activity activity) {
        // Example assumes you have these Labels defined in FXML
        // and using the method names from section 8.3.3

        // Distance: convert meters to km for better readability
        double kms = activity.getTotalDistance() / 1000.0;

        System.out.println("Displaying stats for: " + activity.getName());

        // Update your labels (ensure these @FXML IDs match your FXML file)
        // If you don't have these labels yet, you'll need to add them to FXML
        /*
        distLabel.setText(String.format("%.2f km", kms));
        durLabel.setText(formatDuration(activity.getDuration()));
        speedLabel.setText(String.format("%.2f km/h", activity.getAverageSpeed()));
        paceLabel.setText(String.format("%.2f min/km", activity.getAveragePace()));
        posGainLabel.setText(String.format("%.0f m", activity.getElevationGain()));
        negGainLabel.setText(String.format("%.0f m", activity.getElevationLoss()));
        minAltLabel.setText(String.format("%.0f m", activity.getMinElevation()));
        maxAltLabel.setText(String.format("%.0f m", activity.getMaxElevation()));
        */
    }

    /**
     * Draws the route on the mapPane using Polyline and highlights Start/End points.
     */
    private void drawRoute(Activity activity) {

        List<TrackPoint> points = activity.getTrackPoints();
        if (points.isEmpty()) return;

        Polyline route = new Polyline();
        route.setStroke(Color.BLUE);
        route.setStrokeWidth(2);

        for (TrackPoint p : points) {
            Point2D pixel = geoToPixel(p.getLatitude(), p.getLongitude());
            route.getPoints().addAll(pixel.getX(), pixel.getY());
        }

        // 2. Highlight Start (Green)
        TrackPoint start = activity.getStartPoint();
        Point2D startPx = geoToPixel(start.getLatitude(), start.getLongitude());
        Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 5, Color.GREEN);

        // 3. Highlight End (Red)
        TrackPoint end = activity.getEndPoint();
        Point2D endPx = geoToPixel(end.getLatitude(), end.getLongitude());
        Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 5, Color.RED);

        mapPane.getChildren().addAll(route, startMarker, endMarker);
    }

    /**
     * Helper to translate GPS coordinates to Map Pixels.
     * Note: You must adjust these bounds to match your "upv.jpg" image geographic coverage.
     */
    private Point2D geoToPixel(double lat, double lon) {
        // Example bounds for UPV area (you must adjust these to your specific map)
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

        // ── Construcción del diálogo personalizado ────────────────────
        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        // Personalizamos el icono de la ventana del diálogo
        Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        // Botones del diálogo: Aceptar y Cancelar
        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Campo de texto para el nombre del POI
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        // Layout del contenido del diálogo (VBox con espaciado de 10 px)
        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        // ResultConverter: transforma la selección del botón en un objeto Poi.
        // FIX 1: ya no usamos coordenadas provisionales (0,0); pasamos (x,y)
        // directamente al constructor para que el modelo sea coherente desde el inicio.
        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
            return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        // Mostramos el diálogo y esperamos la respuesta del usuario
        Optional<Poi> result = poiDialog.showAndWait();

        if (result.isPresent()) {
            Poi poi = result.get();

            // FIX 1: confirmamos la posición como Point2D para compatibilidad
            // con getPosition(), usando las mismas coordenadas (x, y).
            poi.setPosition(new Point2D(x, y));

            // Añadimos el POI al ListView (la CellFactory mostrará nombre y código)
            map_listview.getItems().add(poi);

            // FIX 1: usamos (x, y) tanto para el modelo como para el Text,
            // garantizando que la etiqueta aparezca exactamente donde se hizo clic.
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

        // FIX 3: showOpenDialog() devuelve null si el usuario cancela la selección
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
