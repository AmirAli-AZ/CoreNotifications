## CoreNotifications

A notification library for core applications

## Example

```java
var notification = new SimpleNotification("Title", "Message");
notification.show(primaryStage);
```

## Styling
if you have added the css style sheet into your scene of owner window and you use simple notification then you have to style it from your css file.
<br>
otherwise default style will be added.

```
.simple-notification {
    -fx-background-color: white;
    -fx-background-radius: 5px;
    -fx-background-insets: 5px;
    -fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 0);
    -fx-padding: 5px;
}

.simple-notification .title {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
}

.simple-notification .close:hover {
    -fx-fill: #DA4453;
    -fx-cursor: hand;
}
```