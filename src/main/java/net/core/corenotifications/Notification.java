package net.core.corenotifications;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.core.corenotifications.model.Coordinates;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Notification extends Popup {

    private final Path notificationInfoPath = Paths.get(Environment.getAppData() + File.separator + "notification-info.json");

    private final String id = UUID.randomUUID().toString();

    private final ObjectProperty<Position> positionProperty = new SimpleObjectProperty<>(Position.BOTTOM_RIGHT);

    private final ObjectProperty<Insets> marginProperty = new SimpleObjectProperty<>(new Insets(0));

    private final ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>() {
        @Override
        public void set(Duration duration) {
            if (duration == Duration.UNKNOWN || duration == Duration.ZERO)
                throw new IllegalArgumentException("The time is invalid");
            super.set(duration);
        }
    };

    private final ReadOnlyObjectWrapper<Duration> currentTimeProperty = new ReadOnlyObjectWrapper<>(Duration.ZERO);

    private Timeline timeline;

    public Notification(@NotNull Parent parent) {
        super();

        init(parent);
    }

    public Notification() {
        super();

        init(null);
    }

    private final EventHandler<WindowEvent> onShown = windowEvent -> {
        applyPosition();
        try {
            saveInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getDuration() != null) {
            timeline = new Timeline(new KeyFrame(getDuration()));
            timeline.setOnFinished(actionEvent -> {
                if (isShowing())
                    hide();
            });
            currentTimeProperty.bind(timeline.currentTimeProperty());
            timeline.play();
        }
    };

    private final EventHandler<WindowEvent> onHidden = windowEvent -> {
        try {
            removeInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING)
            timeline.stop();
    };

    private void init(Parent parent) {
        if (parent != null) {
            parent.getStyleClass().add("notification");
            getContent().add(parent);
        }
        setAutoHide(true);
        addEventHandler(WindowEvent.WINDOW_SHOWN, onShown);
        addEventHandler(WindowEvent.WINDOW_HIDDEN, onHidden);
    }

    public void applyPosition() {
        var visualBounds = Screen.getPrimary().getVisualBounds();

        var sum = 0.0;
        try {
            sum = sumOfNotificationsHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }

        var coordinates = switch (getPosition()) {
            case BOTTOM_RIGHT -> new Coordinates(
                    visualBounds.getMinX() + (visualBounds.getWidth() - getWidth() - getMargin().getRight()),
                    visualBounds.getMinY() + (visualBounds.getHeight() - getHeight() - getMargin().getBottom() - sum)
            );

            case BOTTOM_LEFT -> new Coordinates(
                    visualBounds.getMinX() + getMargin().getLeft(),
                    visualBounds.getMinY() + (visualBounds.getHeight() - getHeight() - getMargin().getBottom() - sum)
            );

            case BOTTOM_CENTER -> new Coordinates(
                    (visualBounds.getWidth() - getWidth()) / 2,
                    visualBounds.getMinY() + (visualBounds.getHeight() - getHeight() - getMargin().getBottom() - sum)
            );

            case TOP_RIGHT -> new Coordinates(
                    visualBounds.getMinX() + (visualBounds.getWidth() - getWidth() - getMargin().getRight()),
                    visualBounds.getMinY() + getMargin().getTop() + sum
            );

            case TOP_LEFT -> new Coordinates(
                    visualBounds.getMinX() + getMargin().getLeft(),
                    visualBounds.getMinY() + getMargin().getTop() + sum
            );

            case TOP_CENTER -> new Coordinates(
                    (visualBounds.getWidth() - getWidth()) / 2,
                    visualBounds.getMinY() + getMargin().getTop() + sum
            );
        };

        setX(coordinates.x());
        setY(coordinates.y());
    }

    public void setPosition(@NotNull Position position) {
        positionProperty.set(position);
    }

    public Position getPosition() {
        return positionProperty.get();
    }

    public ObjectProperty<Position> positionProperty() {
        return positionProperty;
    }

    public void setMargin(@NotNull Insets margin) {
        marginProperty.set(margin);
    }

    public Insets getMargin() {
        return marginProperty.get();
    }

    public ObjectProperty<Insets> marginProperty() {
        return marginProperty;
    }

    public void setDuration(Duration duration) {
        durationProperty.set(duration);
    }

    public Duration getDuration() {
        return durationProperty.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return durationProperty;
    }

    public Duration getCurrentTime() {
        return currentTimeProperty.get();
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return currentTimeProperty;
    }

    private void saveInfo() throws IOException {
        var jsonArray = new JSONArray();

        if (Files.exists(notificationInfoPath))
            jsonArray = new JSONArray(Files.readString(notificationInfoPath));

        var jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("width", getWidth());
        jsonObject.put("height", getHeight());
        jsonObject.put("position", getPosition());

        jsonArray.put(jsonObject);

        var writer = new FileWriter(notificationInfoPath.toFile());
        writer.write(jsonArray.toString(4));
        writer.flush();
        writer.close();
    }

    private void removeInfo() throws IOException {
        if (Files.notExists(notificationInfoPath))
            return;
        var jsonArray = new JSONArray(Files.readString(notificationInfoPath));

        for (int i = 0; i < jsonArray.length(); i++) {
            var jsonObject = ((JSONObject) jsonArray.get(i));

            if (jsonObject.getString("id").equals(id)) {
                jsonArray.remove(i);

                var writer = new FileWriter(notificationInfoPath.toFile());
                writer.write(jsonArray.toString(4));
                writer.flush();
                writer.close();

                break;
            }
        }
    }

    private double sumOfNotificationsHeight() throws IOException {
        if (Files.notExists(notificationInfoPath))
            return 0;
        var jsonArray = new JSONArray(Files.readString(notificationInfoPath));

        var sum = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            var jsonObject = ((JSONObject) jsonArray.get(i));

            if (jsonObject.getEnum(Position.class, "position") == getPosition())
                sum += jsonObject.getDouble("height");
        }

        return sum;
    }
}
