/*
 *    Copyright 2018 Sage Bionetworks
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package org.sagebase.crf.step.heartrate.decline_hr;

import android.graphics.Bitmap;

import org.sagebase.crf.step.heartrate.StateDetection;
import org.sagebase.crf.step.heartrate.OutputState;

/**
 * Encompasses state detection for if the heart rate is declining.
 */
public class DeclineHRState implements StateDetection {


    public DeclineHRState() {

    }

    /**
     * Runs the declining heart rate algorithm
     * @param timestamp
     * @param bitmap
     * @return boolean representing whether this is an issue
     */
    public boolean containsIssue(Long timestamp, Bitmap bitmap) {
        return DeclineHRAlgorithm.algorithm(timestamp, bitmap) > 0.5;
    }
}
