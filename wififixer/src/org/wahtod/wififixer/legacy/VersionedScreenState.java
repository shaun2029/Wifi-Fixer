/*Copyright [2010-2011] [David Van de Ven]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.wahtod.wififixer.legacy;

import android.content.Context;
import android.os.Build;

public abstract class VersionedScreenState {
    private static VersionedScreenState selector;

    public abstract boolean vgetScreenState(Context context);

    public static boolean getScreenState(Context context) {
	if (selector == null) {
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1) {
		selector = new LegacyScreenState();
	    } else
		selector = new EclairScreenState();
	}

	return selector.vgetScreenState(context);
    }
}