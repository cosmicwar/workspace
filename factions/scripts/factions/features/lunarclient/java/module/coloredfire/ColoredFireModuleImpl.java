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
package scripts.factions.features.lunarclient.java.module.coloredfire;

import com.lunarclient.apollo.coloredfire.v1.OverrideColoredFireMessage;
import com.lunarclient.apollo.coloredfire.v1.ResetColoredFireMessage;
import com.lunarclient.apollo.coloredfire.v1.ResetColoredFiresMessage;
import com.lunarclient.apollo.network.NetworkTypes;
import com.lunarclient.apollo.player.AbstractApolloPlayer;
import com.lunarclient.apollo.recipients.Recipients;
import lombok.NonNull;

import java.awt.*;
import java.util.UUID;

/**
 * Provides the colored fire module.
 *
 * @since 1.0.0
 */
public final class ColoredFireModuleImpl extends ColoredFireModule {

    @Override
    public void overrideColoredFire(@NonNull Recipients recipients, @NonNull UUID burningPlayer, @NonNull Color color) {
        OverrideColoredFireMessage message = OverrideColoredFireMessage.newBuilder()
            .setPlayerUuid(NetworkTypes.toProtobuf(burningPlayer))
            .setColor(NetworkTypes.toProtobuf(color))
            .build();

        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

    @Override
    public void resetColoredFire(@NonNull Recipients recipients, @NonNull UUID burningPlayer) {
        ResetColoredFireMessage message = ResetColoredFireMessage.newBuilder()
            .setPlayerUuid(NetworkTypes.toProtobuf(burningPlayer))
            .build();

        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

    @Override
    public void resetColoredFires(@NonNull Recipients recipients) {
        ResetColoredFiresMessage message = ResetColoredFiresMessage.getDefaultInstance();
        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

}
