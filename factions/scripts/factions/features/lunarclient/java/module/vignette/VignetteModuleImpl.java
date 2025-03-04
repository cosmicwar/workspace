/*
 * This file is part of Apollo, licensed under the MIT License.
 *
 * Copyright (c) 2023 Moonsworth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package scripts.factions.features.lunarclient.java.module.vignette;

import com.lunarclient.apollo.player.AbstractApolloPlayer;
import com.lunarclient.apollo.recipients.Recipients;
import com.lunarclient.apollo.vignette.v1.DisplayVignetteMessage;
import com.lunarclient.apollo.vignette.v1.ResetVignetteMessage;
import lombok.NonNull;

import static com.lunarclient.apollo.util.Ranges.checkRange;

/**
 * Provides the vignette module.
 *
 * @since 1.0.0
 */
public final class VignetteModuleImpl extends VignetteModule {

    @Override
    public void displayVignette(@NonNull Recipients recipients, @NonNull Vignette vignette) {
        DisplayVignetteMessage message = DisplayVignetteMessage.newBuilder()
            .setResourceLocation(vignette.getResourceLocation())
            .setOpacity(checkRange(vignette.getOpacity(), 0, 1, "Vignette#opacity"))
            .build();

        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

    @Override
    public void resetVignette(@NonNull Recipients recipients) {
        ResetVignetteMessage message = ResetVignetteMessage.getDefaultInstance();
        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

}
