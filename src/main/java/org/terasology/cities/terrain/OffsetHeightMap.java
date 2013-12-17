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

package org.terasology.cities.terrain;

import org.terasology.math.Vector2i;

/**
 * An implementation that returns other.height + offset
 * @author Martin Steiger
 */
public class OffsetHeightMap implements HeightMap {

    private final int offset;
    private final HeightMap base;

    /**
     * @param base the base height map
     * @param offset the height offset
     */
    public OffsetHeightMap(HeightMap base, int offset) {
        this.base = base;
        this.offset = offset;
    }
    
    @Override
    public Integer apply(Vector2i input) {
        return apply(input.x, input.y);
    }

    @Override
    public int apply(int x, int z) {
        return base.apply(x, z) + offset;
    }


}
