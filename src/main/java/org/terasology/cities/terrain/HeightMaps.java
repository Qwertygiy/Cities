/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.cities.terrain;

import java.awt.Rectangle;

/**
 * Provides access to different height maps
 * @author Martin Steiger
 */
public final class HeightMaps {
    
    private HeightMaps() {
        // avoid instantiation
    }
    
    /**
     * @param hm the height to use
     * @param area the area to cache
     * @param scale the scale level (should be a divisor of area.width and area.height)
     * @return An height map based on the given constant value 
     */
    public static HeightMap caching(HeightMap hm, Rectangle area, int scale) {
        if (scale == 1) {
            return new CachingHeightMap(area, hm);
        } else {
            return new CachingLerpHeightMap(area, hm, scale);
        }
    }
    
    /**
     * @param height the height to use
     * @return An height map based on the given constant value 
     */
    public static HeightMap constant(int height) {
        return new ConstantHeightMap(height);
    }
    
    /**
     * @param hm the backing height map
     * @param offset the height offset to use
     * @return An height map that returns all values with +offset 
     */
    public static HeightMap offset(HeightMap hm, int offset) {
        return new OffsetHeightMap(hm, offset);
    }
}
