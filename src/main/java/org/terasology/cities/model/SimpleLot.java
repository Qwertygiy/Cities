/*
 * Copyright 2013 MovingBlocks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terasology.cities.model;

import java.awt.Rectangle;

import com.google.common.base.Optional;

/**
 * A rectangular {@link Lot}
 * @author Martin Steiger
 */
public class SimpleLot extends Lot {

    private Optional<SimpleFence> fence = Optional.absent();

    /**
     * @param shape the shape of the lot
     */
    public SimpleLot(Rectangle shape) {
        super(shape);
    }

    /**
     * @param fence the fence to set (or <code>null</code> to clear)
     */
    public void setFence(SimpleFence fence) {
        this.fence = Optional.fromNullable(fence);
    }

    @Override
    public Rectangle getShape() {
        return (Rectangle) super.getShape();
    }

    /**
     * @return the fence (if available)
     */
    public Optional<SimpleFence> getFence() {
        return this.fence;
    }

    
}