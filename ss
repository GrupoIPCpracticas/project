[1mdiff --git a/src/controllerFiles/MapController.java b/src/controllerFiles/MapController.java[m
[1mindex 8f19415..48860f2 100644[m
[1m--- a/src/controllerFiles/MapController.java[m
[1m+++ b/src/controllerFiles/MapController.java[m
[36m@@ -44,8 +44,6 @@[m [mimport javafx.scene.chart.XYChart;[m
 import javafx.scene.shape.StrokeLineCap;[m
 import javafx.scene.layout.HBox;[m
 [m
[31m-import static upv.ipc.sportlib.AnnotationType.CIRCLE;[m
[31m-[m
 public class MapController implements Initializable {[m
 [m
     private Group zoomGroup;[m
[36m@@ -227,7 +225,7 @@[m [mpublic class MapController implements Initializable {[m
                     File mapImageFile = new File(currentRegion.getImagePath());[m
                     Image img = buildMap(mapImageFile);[m
                     this.projection = new MapProjection(currentRegion, img.getWidth(), img.getHeight());[m
[31m-                    drawRoute(currentActivity);[m
[32m+[m[32m                    drawRouteColoredBySpeed(currentActivity);[m[41m[m
                     loadElevationChart(currentActivity);[m
                     if (statsButton != null) statsButton.setDisable(false);[m
                 }[m
[36m@@ -257,7 +255,7 @@[m [mpublic class MapController implements Initializable {[m
 [m
         if (mapImage != null) {[m
             this.projection = new MapProjection(currentRegion, mapImage.getWidth(), mapImage.getHeight());[m
[31m-            drawRoute(activity);[m
[32m+[m[32m            drawRouteColoredBySpeed(activity);[m[41m[m
             for (Annotation ann : activity.getAnnotations()) {[m
                 displayAnnotation(ann);[m
             }[m
[36m@@ -410,228 +408,320 @@[m [mpublic class MapController implements Initializable {[m
     }[m
     // AI code[m
     private void displayAnnotation(Annotation ann) {[m
[31m-        List<GeoPoint> gps = ann.getGeoPoints();[m
[31m-        if (gps.isEmpty() || projection == null) return;[m
[31m-[m
[31m-        Color annotationColor = Color.web(ann.getColor());[m
[31m-        Point2D p1 = projection.project(gps.get(0));[m
[31m-[m
[31m-        switch (ann.getType()) {[m
[31m-            case POINT:[m
[31m-                Circle dot = new Circle(p1.getX(), p1.getY(), 5, annotationColor);[m
[31m-                dot.setStroke(Color.WHITE);[m
[31m-                mapPane.getChildren().add(dot);[m
[31m-                addLabel(p1, ann);[m
[31m-                break;[m
[31m-[m
[31m-            case TEXT:[m
[31m-                addLabel(p1, ann);[m
[31m-                break;[m
[31m-            // AI code[m
[31m-            case LINE:[m
[31m-                if (gps.size() >= 2) {[m
[31m-                    Point2D p2 = projection.project(gps.get(1));[m
[31m-                    Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());[m
[31m-                    line.setStroke(annotationColor);[m
[31m-                    line.setStrokeWidth(ann.getStrokeWidth());[m
[31m-                    mapPane.getChildren().add(line);[m
[31m-                    break;[m
[31m-                }[m
[31m-            case CIRCLE:[m
[31m-                if (gps.size() >= 2) {[m
[31m-                    Point2D edge = projection.project(gps.get(1));[m
[31m-                    double pixelRadius = p1.distance(edge);[m
[31m-[m
[31m-                    Circle circle = new Circle(p1.getX(), p1.getY(), pixelRadius);[m
[31m-                    circle.setStroke(annotationColor);[m
[31m-                    circle.setStrokeWidth(ann.getStrokeWidth());[m
[31m-                    circle.setFill(annotationColor.deriveColor(0, 1, 1, 0.3));[m
[32m+[m[32m    List<GeoPoint> gps = ann.getGeoPoints();[m[41m[m
[32m+[m[32m    if (gps.isEmpty() || projection == null) return;[m[41m[m
[32m+[m[41m[m
[32m+[m[32m    Color annotationColor = Color.web(ann.getColor());[m[41m[m
[32m+[m[32m    Point2D p1 = projection.project(gps.get(0));[m[41m[m
[32m+[m[41m[m
[32m+[m[32m    switch (ann.getType()) {[m[41m[m
[32m+[m[32m        case POINT:[m[41m[m
[32m+[m[32m            Circle dot = new Circle(p1.getX(), p1.getY(), 5, annotationColor);[m[41m[m
[32m+[m[32m            dot.setStroke(Color.WHITE);[m[41m[m
[32m+[m[32m            mapPane.getChildren().add(dot);[m[41m[m
[32m+[m[32m            break;[m[41m[m
[32m+[m[41m[m
[32m+[m[32m        case TEXT:[m[41m[m
[32m+[m[32m            break;[m[41m[m
[32m+[m[41m[m
[32m+[m[32m        case LINE:[m[41m[m
[32m+[m[32m            if (gps.size() >= 2) {[m[41m[m
[32m+[m[32m                Point2D p2 = projection.project(gps.get(1));[m[41m[m
[32m+[m[32m                Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());[m[41m[m
[32m+[m[32m                line.setStroke(annotationColor);[m[41m[m
[32m+[m[32m                line.setStrokeWidth(ann.getStrokeWidth());[m[41m[m
[32m+[m[32m                mapPane.getChildren().add(line);[m[41m[m
[32m+[m[32m            }[m[41m[m
[32m+[m[32m            break;[m[41m[m
[32m+[m[41m[m
[32m+[m[32m        case CIRCLE:[m[41m[m
[32m+[m[32m            if (gps.size() >= 2) {[m[41m[m
[32m+[m[32m                Point2D edge = projection.project(gps.get(1));[m[41m[m
[32m+[m[32m                double pixelRadius = p1.distance(edge);[m[41m[m
[32m+[m[32m                if (pixelRadius > 500) pixelRadius = 50;[m[41m[m
[32m+[m[32m                Circle circle = new Circle(p1.getX(), p1.getY(), pixelRadius);[m[41m[m
[32m+[m[32m                circle.setStroke(annotationColor);[m[41m[m
[32m+[m[32m                circle.setStrokeWidth(ann.getStrokeWidth());[m[41m[m
[32m+[m[32m                circle.setFill(annotationColor.deriveColor(0, 1, 1, 0.3));[m[41m[m
[32m+[m[32m                mapPane.getChildren().add(circle);[m[41m[m
[32m+[m[32m            }[m[41m[m
[32m+[m[32m            break;[m[41m[m
 [m
[31m-                    mapPane.getChildren().add(circle);[m
[31m-                }[m
[31m-                break;[m
[31m-        }[m
[32m+[m[32m        default:[m[41m[m
[32m+[m[32m            break;[m[41m[m
     }[m
[32m+[m[41m    [m
[32m+[m[32m    addLabel(p1, ann);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void addLabel(Point2D pos, Annotation ann) {[m[41m[m
[32m+[m[32m    if (ann.getText() == null || ann.getText().isEmpty()) return;[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    Label label = new Label(ann.getText());[m[41m[m
[32m+[m[32m    label.setTextFill(Color.web(ann.getColor()));[m[41m[m
[32m+[m[32m    label.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +[m[41m[m
[32m+[m[32m                   "-fx-font-weight: bold; " +[m[41m[m
[32m+[m[32m                   "-fx-padding: 4 8 4 8; " +[m[41m[m
[32m+[m[32m                   "-fx-background-radius: 5; " +[m[41m[m
[32m+[m[32m                   "-fx-border-color: " + ann.getColor() + "; " +[m[41m[m
[32m+[m[32m                   "-fx-border-radius: 5; " +[m[41m[m
[32m+[m[32m                   "-fx-border-width: 1;");[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    label.setLayoutX(pos.getX() + 10);[m[41m[m
[32m+[m[32m    label.setLayoutY(pos.getY() - 15);[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    mapPane.getChildren().add(label);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[32m    // end of AI code[m[41m[m
 [m
[32m+[m[32m    private void drawRoute(Activity activity) {[m[41m[m
[32m+[m[32m        List<TrackPoint> points = activity.getTrackPoints();[m[41m[m
[32m+[m[32m        if (points == null || points.isEmpty()) return;[m[41m[m
 [m
[32m+[m[32m        Polyline routeLine = new Polyline();[m[41m[m
[32m+[m[32m        routeLine.setStroke(Color.BLUE);[m[41m[m
[32m+[m[32m        routeLine.setStrokeWidth(3);[m[41m[m
[32m+[m[32m        routeLine.getStrokeDashArray().addAll(5.0, 5.0);[m[41m[m
 [m
[31m-[m
[31m-        private void addLabel (Point2D pos, Annotation ann){[m
[31m-            if (ann.getText() == null || ann.getText().isEmpty()) return;[m
[31m-            Label label = new Label(ann.getText());[m
[31m-            label.setTextFill(Color.web(ann.getColor()));[m
[31m-            label.setLayoutX(pos.getX() + 10);[m
[31m-            label.setLayoutY(pos.getY() - 10);[m
[31m-            label.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); " +[m
[31m-                    "-fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");[m
[31m-[m
[31m-            mapPane.getChildren().add(label);[m
[32m+[m[32m        for (TrackPoint tp : points) {[m[41m[m
[32m+[m[32m            Point2D pixel = projection.project(tp);[m[41m[m
[32m+[m[32m            routeLine.getPoints().addAll(pixel.getX(), pixel.getY());[m[41m[m
         }[m
[32m+[m[32m        Point2D startPx = projection.project(activity.getStartPoint());[m[41m[m
[32m+[m[32m        Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 6, Color.LIMEGREEN);[m[41m[m
[32m+[m[32m        startMarker.setStroke(Color.BLACK);[m[41m[m
 [m
[31m-        private void drawRoute (Activity activity){[m
[31m-            List<TrackPoint> points = activity.getTrackPoints();[m
[31m-            if (points == null || points.isEmpty()) return;[m
[31m-[m
[31m-            Polyline routeLine = new Polyline();[m
[31m-            routeLine.setStroke(Color.BLUE);[m
[31m-            routeLine.setStrokeWidth(3);[m
[31m-            routeLine.getStrokeDashArray().addAll(5.0, 5.0);[m
[32m+[m[32m        Point2D endPx = projection.project(activity.getEndPoint());[m[41m[m
[32m+[m[32m        Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 6, Color.RED);[m[41m[m
[32m+[m[32m        endMarker.setStroke(Color.BLACK);[m[41m[m
 [m
[31m-            for (TrackPoint tp : points) {[m
[31m-                Point2D pixel = projection.project(tp);[m
[31m-                routeLine.getPoints().addAll(pixel.getX(), pixel.getY());[m
[31m-            }[m
[31m-            Point2D startPx = projection.project(activity.getStartPoint());[m
[31m-            Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 6, Color.LIMEGREEN);[m
[31m-            startMarker.setStroke(Color.BLACK);[m
[31m-[m
[31m-            Point2D endPx = projection.project(activity.getEndPoint());[m
[31m-            Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 6, Color.RED);[m
[31m-            endMarker.setStroke(Color.BLACK);[m
[32m+[m[32m        mapPane.getChildren().addAll(routeLine, startMarker, endMarker);[m[41m[m
[32m+[m[32m    }[m[41m[m
 [m
[31m-            mapPane.getChildren().addAll(routeLine, startMarker, endMarker);[m
[31m-        }[m
[32m+[m[32m    private void showError(String message) {[m[41m[m
[32m+[m[32m        Alert alert = new Alert(Alert.AlertType.ERROR);[m[41m[m
[32m+[m[32m        alert.setTitle("Error");[m[41m[m
[32m+[m[32m        alert.setHeaderText(null);[m[41m[m
[32m+[m[32m        alert.setContentText(message);[m[41m[m
[32m+[m[32m        alert.showAndWait();[m[41m[m
[32m+[m[32m    }[m[41m[m
 [m
[31m-        private void showError (String message){[m
[31m-            Alert alert = new Alert(Alert.AlertType.ERROR);[m
[31m-            alert.setTitle("Error");[m
[31m-            alert.setHeaderText(null);[m
[31m-            alert.setContentText(message);[m
[31m-            alert.showAndWait();[m
[32m+[m[32m    private void addPoi(double x, double y) {[m[41m[m
[32m+[m[32m        if (currentActivity == null || projection == null) {[m[41m[m
[32m+[m[32m            showError("Please select or import an activity first.");[m[41m[m
[32m+[m[32m            return;[m[41m[m
         }[m
 [m
[31m-        private void addPoi ( double x, double y){[m
[31m-            if (currentActivity == null || projection == null) {[m
[31m-                showError("Please select or import an activity first.");[m
[31m-                return;[m
[32m+[m[32m        Dialog<Annotation> poiDialog = new Dialog<>();[m[41m[m
[32m+[m[32m        poiDialog.setTitle("New Point of Interest");[m[41m[m
[32m+[m[32m        poiDialog.setHeaderText("Mark a location on the map");[m[41m[m
[32m+[m[32m        ButtonType okButton = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);[m[41m[m
[32m+[m[32m        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);[m[41m[m
[32m+[m[32m        TextField nameField = new TextField();[m[41m[m
[32m+[m[32m        nameField.setPromptText("Name of POI (e.g. Refreshment Point)");[m[41m[m
[32m+[m[32m        VBox vbox = new VBox(10, new Label("Name:"), nameField);[m[41m[m
[32m+[m[32m        poiDialog.getDialogPane().setContent(vbox);[m[41m[m
[32m+[m[32m        GeoPoint geoPos = projection.unproject(x, y);[m[41m[m
[32m+[m[32m        poiDialog.setResultConverter(dialogButton -> {[m[41m[m
[32m+[m[32m            if (dialogButton == okButton) {[m[41m[m
[32m+[m[32m                return new Annotation([m[41m[m
[32m+[m[32m                        AnnotationType.POINT,[m[41m[m
[32m+[m[32m                        nameField.getText().trim(),[m[41m[m
[32m+[m[32m                        "#3498db", // Nice blue color[m[41m[m
[32m+[m[32m                        2.0,[m[41m[m
[32m+[m[32m                        List.of(geoPos)[m[41m[m
[32m+[m[32m                );[m[41m[m
             }[m
[31m-[m
[31m-            Dialog<Annotation> poiDialog = new Dialog<>();[m
[31m-            poiDialog.setTitle("New Point of Interest");[m
[31m-            poiDialog.setHeaderText("Mark a location on the map");[m
[31m-            ButtonType okButton = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);[m
[31m-            poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);[m
[31m-            TextField nameField = new TextField();[m
[31m-            nameField.setPromptText("Name of POI (e.g. Refreshment Point)");[m
[31m-            VBox vbox = new VBox(10, new Label("Name:"), nameField);[m
[31m-            poiDialog.getDialogPane().setContent(vbox);[m
[31m-            GeoPoint geoPos = projection.unproject(x, y);[m
[31m-            poiDialog.setResultConverter(dialogButton -> {[m
[31m-                if (dialogButton == okButton) {[m
[31m-                    return new Annotation([m
[31m-                            AnnotationType.POINT,[m
[31m-                            nameField.getText().trim(),[m
[31m-                            "#3498db", // Nice blue color[m
[31m-                            2.0,[m
[31m-                            List.of(geoPos)[m
[31m-                    );[m
[31m-                }[m
[31m-                return null;[m
[31m-            });[m
[31m-            Optional<Annotation> result = poiDialog.showAndWait();[m
[31m-            if (result.isPresent()) {[m
[31m-                Annotation saved = app.addAnnotation(currentActivity, result.get());[m
[31m-                if (saved != null) {[m
[31m-                    displayAnnotation(saved);[m
[31m-                }[m
[32m+[m[32m            return null;[m[41m[m
[32m+[m[32m        });[m[41m[m
[32m+[m[32m        Optional<Annotation> result = poiDialog.showAndWait();[m[41m[m
[32m+[m[32m        if (result.isPresent()) {[m[41m[m
[32m+[m[32m            Annotation saved = app.addAnnotation(currentActivity, result.get());[m[41m[m
[32m+[m[32m            if (saved != null) {[m[41m[m
[32m+[m[32m                displayAnnotation(saved);[m[41m[m
             }[m
         }[m
[32m+[m[32m    }[m[41m[m
 [m
[31m-        private void switchSceneMenu (ActionEvent event, Parent root, String title,boolean wait){[m
[31m-            if (wait) {[m
[31m-                Stage stage = new Stage();[m
[31m-                stage.setTitle(title);[m
[31m-                stage.setScene(new Scene(root));[m
[31m-                stage.initModality(Modality.APPLICATION_MODAL);[m
[31m-                Stage mainStage = (Stage) map_scrollpane.getScene().getWindow();[m
[31m-                stage.initOwner(mainStage);[m
[31m-                stage.showAndWait();[m
[31m-[m
[31m-            } else {[m
[31m-                Stage stage = (Stage) map_scrollpane.getScene().getWindow();[m
[31m-                stage.setScene(new Scene(root));[m
[31m-                stage.setTitle(title);[m
[31m-                stage.show();[m
[31m-            }[m
[31m-        }[m
[32m+[m[32m    private void switchSceneMenu(ActionEvent event, Parent root, String title, boolean wait) {[m[41m[m
[32m+[m[32m        if (wait) {[m[41m[m
[32m+[m[32m            Stage stage = new Stage();[m[41m[m
[32m+[m[32m            stage.setTitle(title);[m[41m[m
[32m+[m[32m            stage.setScene(new Scene(root));[m[41m[m
[32m+[m[32m            stage.initModality(Modality.APPLICATION_MODAL);[m[41m[m
[32m+[m[32m            Stage mainStage = (Stage) map_scrollpane.getScene().getWindow();[m[41m[m
[32m+[m[32m            stage.initOwner(mainStage);[m[41m[m
[32m+[m[32m            stage.showAndWait();[m[41m[m
 [m
[31m-        @FXML[m
[31m-        private void handleCumulative (ActionEvent event) throws IOException {[m
[31m-            Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/CumulativeMonth.fxml"));[m
[31m-            switchSceneMenu(event, root, "Monthly Stats", false);[m
[32m+[m[32m        } else {[m[41m[m
[32m+[m[32m            Stage stage = (Stage) map_scrollpane.getScene().getWindow();[m[41m[m
[32m+[m[32m            stage.setScene(new Scene(root));[m[41m[m
[32m+[m[32m            stage.setTitle(title);[m[41m[m
[32m+[m[32m            stage.show();[m[41m[m
         }[m
[32m+[m[32m    }[m[41m[m
 [m
[31m-        private void loadElevationChart (Activity activity){[m
[31m-            List<TrackPoint> points = activity.getTrackPoints();[m
[31m-            if (points == null || points.isEmpty()) return;[m
[31m-[m
[31m-            NumberAxis xAxis = new NumberAxis();[m
[31m-            NumberAxis yAxis = new NumberAxis();[m
[31m-            xAxis.setLabel("Distance (km)");[m
[31m-            yAxis.setLabel("Altitude (m)");[m
[31m-[m
[31m-            elevationChart = new LineChart<>(xAxis, yAxis);[m
[31m-            elevationChart.setTitle("Elevation graph");[m
[31m-            elevationChart.setLegendVisible(false);[m
[31m-            elevationChart.setCreateSymbols(false);[m
[31m-            elevationChart.setPrefWidth(280);[m
[31m-            elevationChart.setAnimated(false);[m
[31m-[m
[31m-            XYChart.Series<Number, Number> series = new XYChart.Series<>();[m
[31m-            double accDist = 0;[m
[31m-            for (int i = 0; i < points.size(); i++) {[m
[31m-                if (i > 0) accDist += points.get(i).distanceTo(points.get(i - 1));[m
[31m-                series.getData().add(new XYChart.Data<>(accDist / 1000.0, points.get(i).getElevation()));[m
[31m-            }[m
[31m-            elevationChart.getData().add(series);[m
[31m-[m
[31m-            // Añadir a la interfaz[m
[31m-            if (!chartVisible) {[m
[31m-                splitPane.getItems().add(elevationChart);[m
[31m-                splitPane.setDividerPositions(0.22, 0.65);[m
[31m-                chartVisible = true;[m
[31m-            } else {[m
[31m-                splitPane.getItems().set(2, elevationChart);[m
[31m-            }[m
[32m+[m[32m    @FXML[m[41m[m
[32m+[m[32m    private void handleCumulative(ActionEvent event) throws IOException{[m[41m[m
[32m+[m[32m        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/CumulativeMonth.fxml"));[m[41m[m
[32m+[m[32m        switchSceneMenu(event, root, "Monthly Stats", false);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void loadElevationChart(Activity activity) {[m[41m[m
[32m+[m[32m        List<TrackPoint> points = activity.getTrackPoints();[m[41m[m
[32m+[m[32m        if (points == null || points.isEmpty()) return;[m[41m[m
[32m+[m[41m[m
[32m+[m[32m        NumberAxis xAxis = new NumberAxis();[m[41m[m
[32m+[m[32m        NumberAxis yAxis = new NumberAxis();[m[41m[m
[32m+[m[32m        xAxis.setLabel("Distance (km)");[m[41m[m
[32m+[m[32m        yAxis.setLabel("Altitude (m)");[m[41m[m
[32m+[m[41m[m
[32m+[m[32m        elevationChart = new LineChart<>(xAxis, yAxis);[m[41m[m
[32m+[m[32m        elevationChart.setTitle("Elevation graph");[m[41m[m
[32m+[m[32m        elevationChart.setLegendVisible(false);[m[41m[m
[32m+[m[32m        elevationChart.setCreateSymbols(false);[m[41m[m
[32m+[m[32m        elevationChart.setPrefWidth(280);[m[41m[m
[32m+[m[32m        elevationChart.setAnimated(false);[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        XYChart.Series<Number, Number> series = new XYChart.Series<>();[m[41m[m
[32m+[m[32m        double accDist = 0;[m[41m[m
[32m+[m[32m        for (int i = 0; i < points.size(); i++) {[m[41m[m
[32m+[m[32m            if (i > 0) accDist += points.get(i).distanceTo(points.get(i - 1));[m[41m[m
[32m+[m[32m            series.getData().add(new XYChart.Data<>(accDist / 1000.0, points.get(i).getElevation()));[m[41m[m
[32m+[m[32m        }[m[41m[m
[32m+[m[32m        elevationChart.getData().add(series);[m[41m[m
 [m
[31m-            if (mapMarker == null) {[m
[31m-                mapMarker = new Circle(7, Color.DODGERBLUE);[m
[31m-                mapMarker.setStroke(Color.WHITE);[m
[31m-                mapMarker.setStrokeWidth(2);[m
[31m-                mapMarker.setVisible(false);[m
[31m-                mapPane.getChildren().add(mapMarker);[m
[31m-            } else {[m
[31m-                mapMarker.setVisible(false);[m
[31m-            }[m
[32m+[m[32m        // Añadir a la interfaz[m[41m[m
[32m+[m[32m        if (!chartVisible) {[m[41m[m
[32m+[m[32m            splitPane.getItems().add(elevationChart);[m[41m[m
[32m+[m[32m            splitPane.setDividerPositions(0.22, 0.65);[m[41m[m
[32m+[m[32m            chartVisible = true;[m[41m[m
[32m+[m[32m        } else {[m[41m[m
[32m+[m[32m            splitPane.getItems().set(2, elevationChart);[m[41m[m
[32m+[m[32m        }[m[41m[m
 [m
[31m-            setupChartMouseListener(points);[m
[32m+[m[32m        if (mapMarker == null) {[m[41m[m
[32m+[m[32m            mapMarker = new Circle(7, Color.DODGERBLUE);[m[41m[m
[32m+[m[32m            mapMarker.setStroke(Color.WHITE);[m[41m[m
[32m+[m[32m            mapMarker.setStrokeWidth(2);[m[41m[m
[32m+[m[32m            mapMarker.setVisible(false);[m[41m[m
[32m+[m[32m            mapPane.getChildren().add(mapMarker);[m[41m[m
[32m+[m[32m        } else {[m[41m[m
[32m+[m[32m            mapMarker.setVisible(false);[m[41m[m
         }[m
 [m
[31m-        private void setupChartMouseListener (List < TrackPoint > points) {[m
[31m-            // AI code[m
[31m-            for (XYChart.Data<Number, Number> data : elevationChart.getData().get(0).getData()) {[m
[31m-                data.nodeProperty().addListener((obs, oldNode, newNode) -> {[m
[31m-                    if (newNode != null) {[m
[31m-                        newNode.setOnMouseEntered(e -> {[m
[31m-                            double km = data.getXValue().doubleValue();[m
[31m-                            double accDist = 0;[m
[31m-                            TrackPoint closest = points.get(0);[m
[31m-                            for (int i = 1; i < points.size(); i++) {[m
[31m-                                accDist += points.get(i).distanceTo(points.get(i - 1));[m
[31m-                                if (accDist / 1000.0 >= km) {[m
[31m-                                    closest = points.get(i);[m
[31m-                                    break;[m
[31m-                                }[m
[32m+[m[32m        setupChartMouseListener(points);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void setupChartMouseListener(List<TrackPoint> points) {[m[41m[m
[32m+[m[32m        // AI code[m[41m[m
[32m+[m[32m        for (XYChart.Data<Number, Number> data : elevationChart.getData().get(0).getData()) {[m[41m[m
[32m+[m[32m            data.nodeProperty().addListener((obs, oldNode, newNode) -> {[m[41m[m
[32m+[m[32m                if (newNode != null) {[m[41m[m
[32m+[m[32m                    newNode.setOnMouseEntered(e -> {[m[41m[m
[32m+[m[32m                        double km = data.getXValue().doubleValue();[m[41m[m
[32m+[m[32m                        double accDist = 0;[m[41m[m
[32m+[m[32m                        TrackPoint closest = points.get(0);[m[41m[m
[32m+[m[32m                        for (int i = 1; i < points.size(); i++) {[m[41m[m
[32m+[m[32m                            accDist += points.get(i).distanceTo(points.get(i - 1));[m[41m[m
[32m+[m[32m                            if (accDist / 1000.0 >= km) {[m[41m[m
[32m+[m[32m                                closest = points.get(i);[m[41m[m
[32m+[m[32m                                break;[m[41m[m
                             }[m
[31m-                            Point2D p = projection.project(closest);[m
[31m-                            mapMarker.setCenterX(p.getX());[m
[31m-                            mapMarker.setCenterY(p.getY());[m
[31m-                            mapMarker.setVisible(true);[m
[31m-                        });[m
[31m-                        newNode.setOnMouseExited(e -> mapMarker.setVisible(false));[m
[31m-                    }[m
[31m-                });[m
[31m-            }[m
[31m-            // end of AI code[m
[32m+[m[32m                        }[m[41m[m
[32m+[m[32m                        Point2D p = projection.project(closest);[m[41m[m
[32m+[m[32m                        mapMarker.setCenterX(p.getX());[m[41m[m
[32m+[m[32m                        mapMarker.setCenterY(p.getY());[m[41m[m
[32m+[m[32m                        mapMarker.setVisible(true);[m[41m[m
[32m+[m[32m                    });[m[41m[m
[32m+[m[32m                    newNode.setOnMouseExited(e -> mapMarker.setVisible(false));[m[41m[m
[32m+[m[32m                }[m[41m[m
[32m+[m[32m            });[m[41m[m
         }[m
[31m-}[m
[32m+[m[32m        // end of AI code[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m[m
[32m+[m[32m    // AI code[m[41m[m
[32m+[m[32m    private void drawRouteColoredBySpeed(Activity activity) {[m[41m[m
[32m+[m[32m        List<TrackPoint> points = activity.getTrackPoints();[m[41m[m
[32m+[m[32m        if (points == null || points.size() < 2) return;[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        removeSpeedLegend();[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        for (int i = 0; i < points.size() - 1; i++) {[m[41m[m
[32m+[m[32m            TrackPoint current = points.get(i);[m[41m[m
[32m+[m[32m            TrackPoint next = points.get(i + 1);[m[41m[m
[32m+[m[41m            [m
[32m+[m[32m            double speedKmph = current.speedTo(next);[m[41m[m
[32m+[m[32m            Color segmentColor = getColorForSpeedFixed(speedKmph);[m[41m[m
[32m+[m[41m            [m
[32m+[m[32m            Point2D p1 = projection.project(current);[m[41m[m
[32m+[m[32m            Point2D p2 = projection.project(next);[m[41m[m
[32m+[m[41m            [m
[32m+[m[32m            Line segment = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());[m[41m[m
[32m+[m[32m            segment.setStroke(segmentColor);[m[41m[m
[32m+[m[32m            segment.setStrokeWidth(4);[m[41m[m
[32m+[m[32m            segment.setStrokeLineCap(StrokeLineCap.ROUND);[m[41m[m
[32m+[m[41m            [m
[32m+[m[32m            mapPane.getChildren().add(segment);[m[41m[m
[32m+[m[32m        }[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        addStartEndMarkers(activity);[m[41m[m
[32m+[m[32m        addSpeedLegend();[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private Color getColorForSpeedFixed(double speedKmph) {[m[41m[m
[32m+[m[32m        if (speedKmph > 15) return Color.RED;[m[41m[m
[32m+[m[32m        if (speedKmph > 10) return Color.ORANGE;[m[41m[m
[32m+[m[32m        if (speedKmph > 6) return Color.YELLOW;[m[41m[m
[32m+[m[32m        if (speedKmph > 0) return Color.GREEN;[m[41m[m
[32m+[m[32m        return Color.GRAY;[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void addStartEndMarkers(Activity activity) {[m[41m[m
[32m+[m[32m        Point2D startPx = projection.project(activity.getStartPoint());[m[41m[m
[32m+[m[32m        Circle startMarker = new Circle(startPx.getX(), startPx.getY(), 7, Color.LIMEGREEN);[m[41m[m
[32m+[m[32m        startMarker.setStroke(Color.BLACK);[m[41m[m
[32m+[m[32m        startMarker.setStrokeWidth(1.5);[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        Point2D endPx = projection.project(activity.getEndPoint());[m[41m[m
[32m+[m[32m        Circle endMarker = new Circle(endPx.getX(), endPx.getY(), 7, Color.RED);[m[41m[m
[32m+[m[32m        endMarker.setStroke(Color.BLACK);[m[41m[m
[32m+[m[32m        endMarker.setStrokeWidth(1.5);[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        mapPane.getChildren().addAll(startMarker, endMarker);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void addSpeedLegend() {[m[41m[m
[32m+[m[32m        VBox legend = new VBox(5);[m[41m[m
[32m+[m[32m        legend.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 5;");[m[41m[m
[32m+[m[32m        legend.setLayoutX(10);[m[41m[m
[32m+[m[32m        legend.setLayoutY(10);[m[41m[m
[32m+[m[32m        legend.setUserData("speedLegend");[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        legend.getChildren().add(new Label("🏃 Speed"));[m[41m[m
[32m+[m[32m        legend.getChildren().add(createLegendItem(Color.RED, ">15 km/h"));[m[41m[m
[32m+[m[32m        legend.getChildren().add(createLegendItem(Color.ORANGE, "10-15 km/h"));[m[41m[m
[32m+[m[32m        legend.getChildren().add(createLegendItem(Color.YELLOW, "6-10 km/h"));[m[41m[m
[32m+[m[32m        legend.getChildren().add(createLegendItem(Color.GREEN, "0-6 km/h"));[m[41m[m
[32m+[m[41m        [m
[32m+[m[32m        mapPane.getChildren().add(legend);[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private void removeSpeedLegend() {[m[41m[m
[32m+[m[32m        mapPane.getChildren().removeIf(node -> {[m[41m[m
[32m+[m[32m            Object data = node.getUserData();[m[41m[m
[32m+[m[32m            return data != null && "speedLegend".equals(data);[m[41m[m
[32m+[m[32m        });[m[41m[m
[32m+[m[32m    }[m[41m[m
[32m+[m[41m    [m
[32m+[m[32m    private HBox createLegendItem(Color color, String text) {[m[41m[m
[32m+[m[32m        Circle circle = new Circle(8, color);[m[41m[m
[32m+[m[32m        circle.setStroke(Color.BLACK);[m[41m[m
[32m+[m[32m        Label label = new Label(text);[m[41m[m
[32m+[m[32m        HBox hbox = new HBox(10, circle, label);[m[41m[m
[32m+[m[32m        ret