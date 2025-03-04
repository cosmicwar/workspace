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
package scripts.factions.features.lunarclient.java.mods.impl;

import com.lunarclient.apollo.option.NumberOption;
import com.lunarclient.apollo.option.SimpleOption;
import io.leangen.geantyref.TypeToken;
import java.awt.Color;

/**
 * Change how particles look and work.
 *
 * @since 1.0.0
 */
public final class ModParticleChanger {

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> ENABLED = SimpleOption.<Boolean>builder()
        .node("particle-changer", "enabled").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> ALWAYS_ENCHANT_STRIKES = SimpleOption.<Boolean>builder()
        .node("particle-changer", "always-enchant-strikes").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> AFFECT_ENCHANTED_WEAPONS = SimpleOption.<Boolean>builder()
        .node("particle-changer", "affect-enchanted-weapons").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    /**
     * Hide your players potion effect particles when you're in first person.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> HIDE_FIRST_PERSON_PARTICLES = SimpleOption.<Boolean>builder()
        .comment("Hide your players potion effect particles when you're in first person")
        .node("particle-changer", "hide-first-person-particles").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Color> COLOR = SimpleOption.<Color>builder()
        .node("particle-changer", "color").type(TypeToken.get(Color.class))
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final NumberOption<Float> SCALE = NumberOption.<Float>number()
        .node("particle-changer", "scale").type(TypeToken.get(Float.class))
        .min(0.5F).max(2.0F)
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final NumberOption<Integer> PARTICLE_MULTIPLIER = NumberOption.<Integer>number()
        .node("particle-changer", "particle-multiplier").type(TypeToken.get(Integer.class))
        .min(1).max(10)
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> HIDE_PARTICLE = SimpleOption.<Boolean>builder()
        .node("particle-changer", "hide-particle").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    /**
     * No documentation available.
     *
     * @since 1.0.0
     */
    public static final SimpleOption<Boolean> OVERLAY_COLOR = SimpleOption.<Boolean>builder()
        .node("particle-changer", "overlay-color").type(TypeToken.get(Boolean.class))
        .notifyClient()
        .build();

    private ModParticleChanger() {
    }

}
