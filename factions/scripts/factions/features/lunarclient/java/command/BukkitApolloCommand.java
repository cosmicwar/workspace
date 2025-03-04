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
package scripts.factions.features.lunarclient.java.command;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Provides common command functions for Bukkit.
 *
 * @param <T> the sender type
 * @since 1.0.9
 */
public abstract class BukkitApolloCommand<T> extends AbstractApolloCommand<T> {

    /**
     * Returns a new instance of a Bukkit command.
     *
     * @param textConsumer the consumer for sending messages to the sender
     * @since 1.0.9
     */
    public BukkitApolloCommand(BiConsumer<T, Component> textConsumer) {
        super(textConsumer);
    }

    /**
     * Handles a player argument; if the provided player doesn't exist, a not found message
     * is sent to the sender. Otherwise, the player is passed to the provided player consumer.
     *
     * @param sender the command sender
     * @param argument the argument passed from the command execution
     * @param playerConsumer a consumer used for processing a desired action if the player is found
     * @since 1.0.9
     */
    protected void handlePlayerArgument(@NonNull T sender, @NonNull String argument, @NonNull Consumer<Player> playerConsumer) {
        Player player = Bukkit.getPlayer(argument);

        if (player == null) {
            this.textConsumer.accept(sender, Component.text("Player '", NamedTextColor.RED)
                .append(Component.text(argument, NamedTextColor.RED))
                .append(Component.text("' not found!", NamedTextColor.RED)));
            return;
        }

        playerConsumer.accept(player);
    }
}
