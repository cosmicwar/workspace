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
package scripts.factions.features.lunarclient.java.module.cooldown;

import com.lunarclient.apollo.cooldown.v1.DisplayCooldownMessage;
import com.lunarclient.apollo.cooldown.v1.RemoveCooldownMessage;
import com.lunarclient.apollo.cooldown.v1.ResetCooldownsMessage;
import com.lunarclient.apollo.network.NetworkTypes;
import com.lunarclient.apollo.player.AbstractApolloPlayer;
import com.lunarclient.apollo.recipients.Recipients;
import lombok.NonNull;
import org.starcade.starlight.helper.cooldown.Cooldown;

/**
 * Provides the cooldown module.
 *
 * @since 1.0.0
 */
public final class CooldownModuleImpl extends CooldownModule {

    @Override
    public void displayCooldown(@NonNull Recipients recipients, @NonNull Cooldown cooldown) {
        DisplayCooldownMessage message = DisplayCooldownMessage.newBuilder()
            .setName(cooldown.getName())
            .setDuration(NetworkTypes.toProtobuf(cooldown.getDuration()))
            .setIcon(NetworkTypes.toProtobuf(cooldown.getIcon()))
            .build();

        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

    @Override
    public void removeCooldown(@NonNull Recipients recipients, @NonNull String cooldownName) {
        RemoveCooldownMessage message = RemoveCooldownMessage.newBuilder()
            .setName(cooldownName)
            .build();

        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

    @Override
    public void removeCooldown(@NonNull Recipients recipients, @NonNull Cooldown cooldown) {
        this.removeCooldown(recipients, cooldown.getName());
    }

    @Override
    public void resetCooldowns(@NonNull Recipients recipients) {
        ResetCooldownsMessage message = ResetCooldownsMessage.getDefaultInstance();
        recipients.forEach(player -> ((AbstractApolloPlayer) player).sendPacket(message));
    }

}
