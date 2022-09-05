package net.core.corenotifications;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SimpleNotification extends NotificationBase {

    private final StringProperty
            titleProperty = new SimpleStringProperty(),
            messageProperty = new SimpleStringProperty();

    public SimpleNotification(@NotNull String title, @NotNull String message) {
        super();

        titleProperty.set(title);
        messageProperty.set(message);

        init();
    }

    public SimpleNotification() {
        super();

        init();
    }

    private void init() {
        var content = createContent();
        content.getStyleClass().add("simple-notification");
        getContent().add(content);

        addEventHandler(WindowEvent.WINDOW_SHOWN, windowEvent -> {
            if (getScene().getStylesheets().isEmpty())
                getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("default-simple-notification-style.css")).toExternalForm());
        });
    }

    private Parent createContent() {
        var root = new BorderPane();
        var title = new Label();
        var closeBtnSVGPath = new SVGPath();
        var top = new HBox(3, title, closeBtnSVGPath);
        var message = new Label();

        root.getStyleClass().add("root");
        root.setPrefSize(250, 130);
        title.getStyleClass().add("title");
        title.setPadding(new Insets(5));
        title.textProperty().bind(titleProperty);
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);
        message.getStyleClass().add("message");
        message.setWrapText(true);
        message.setPadding(new Insets(5));
        message.setAlignment(Pos.TOP_LEFT);
        message.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        message.textProperty().bind(messageProperty);
        closeBtnSVGPath.getStyleClass().add("close");
        closeBtnSVGPath.setContent(Icons.CLOSE);
        closeBtnSVGPath.setPickOnBounds(true);
        HBox.setMargin(closeBtnSVGPath, new Insets(5));
        closeBtnSVGPath.setOnMouseClicked(mouseEvent -> hide());

        root.setTop(top);
        root.setCenter(message);

        return root;
    }

    public void setTitle(@NotNull String title) {
        titleProperty.set(title);
    }

    public String getTitle() {
        return titleProperty.get();
    }

    public StringProperty titleProperty() {
        return titleProperty;
    }

    public void setMessage(@NotNull String message) {
        messageProperty.set(message);
    }

    public String getMessage() {
        return messageProperty.get();
    }

    public StringProperty messageProperty() {
        return messageProperty;
    }
}
