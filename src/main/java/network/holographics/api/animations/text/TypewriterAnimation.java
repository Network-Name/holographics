package network.holographics.api.animations.text;

import network.holographics.api.animations.TextAnimation;
import network.holographics.api.utils.Common;
import network.holographics.api.utils.color.IridiumColorAPI;
import lombok.NonNull;

import java.util.Arrays;

public class TypewriterAnimation extends TextAnimation {

    public TypewriterAnimation() {
        super("typewriter", 3, 20);
    }

    @Override
    public String animate(@NonNull String string, long step, String... args) {
        StringBuilder specialColors = new StringBuilder();
        for (String color : IridiumColorAPI.SPECIAL_COLORS) {
            if (string.contains(color)) {
                specialColors.append(color);
                string = string.replace(color, "");
            }
        }
        String stripped = Common.stripColors(string);
        int currentStep = getCurrentStep(step, stripped.length());
        return specialColors + String.valueOf(Arrays.copyOfRange(stripped.toCharArray(), 0, currentStep));
    }
}
