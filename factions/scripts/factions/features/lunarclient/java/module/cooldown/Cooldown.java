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

import com.lunarclient.apollo.common.icon.Icon;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a cooldown which can be shown on the client.
 *
 * @since 1.0.0
 */
@Getter
@Builder
public final class Cooldown {

    /**
     * Returns the cooldown {@link String} name.
     *
     * @return the cooldown name
     * @since 1.0.0
     */
    String name;

    /**
     * Returns the cooldown {@link Duration}.
     *
     * @return the cooldown duration
     * @since 1.0.0
     */
    Duration duration;

    /**
     * Returns the cooldown {@link Icon}.
     *
     * <p>Can be any of the icons found in {@link com.lunarclient.apollo.common.icon} package,
     * for the most common use case, use {@link com.lunarclient.apollo.common.icon.ItemStackIcon}.</p>
     *
     * @return the cooldown icon
     * @since 1.0.0
     */
    Icon icon;

}
