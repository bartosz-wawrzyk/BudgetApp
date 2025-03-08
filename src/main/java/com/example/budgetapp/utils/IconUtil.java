package com.example.budgetapp.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

public class IconUtil {

    private static final String ICON_PATH = "/com/example/budgetapp/icons/house.png";

    public static void setAppIcon(Stage stage) {
        stage.getIcons().add(new Image(Objects.requireNonNull(IconUtil.class.getResourceAsStream(ICON_PATH))));
    }
}