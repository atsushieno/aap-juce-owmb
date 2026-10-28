// The package and class name are part of JUCE's Android JNI ABI.
package com.rmsl.juce

import android.content.Context

class Java {
    companion object {
        init {
            System.loadLibrary("OpenWav_Standalone")
        }

        @JvmStatic
        external fun initialiseJUCE(applicationContext: Context)
    }
}
