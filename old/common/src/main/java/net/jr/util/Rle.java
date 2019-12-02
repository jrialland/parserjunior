/*
 * Copyright 2018 jrialland.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jr.util;

import net.jr.collection.CollectionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jrialland
 */
public class Rle {

    public static Collection<Integer> encode(Collection<Integer> coll) {
        int[] encoded = encode(coll.stream().mapToInt(i -> i).toArray());
        return CollectionsUtil.fromArray(encoded);
    }

    public static int[] encode(int[] array) {

        if (array.length == 0) {
            return new int[]{};
        }

        if (array.length == 1) {
            return new int[]{array[0], 1};
        }

        Integer current = array[0];
        int count = 1;
        List<Integer> out = new ArrayList<>();

        for (int i = 1; i < array.length; i++) {
            if (array[i] != current) {
                out.add(current);
                out.add(count);
                current = array[i];
                count = 1;
            } else {
                count++;
            }
        }
        out.add(current);
        out.add(count);
        return out.stream().mapToInt(i -> i).toArray();
    }

    public static int[] decode(int[] array) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < array.length; i += 2) {
            int value = array[i];
            int times = array[i + 1];
            for (int j = 0; j < times; j++) {
                out.add(value);
            }
        }
        return out.stream().mapToInt(i -> i).toArray();
    }

}
