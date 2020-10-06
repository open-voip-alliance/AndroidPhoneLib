package org.openvoipalliance.phonelib.model

data class Quality(

        /**
         * The average MOS for the entire call.
         */
        val average: Float,

        /**
         * The current MOS at the time requested.
         */
        val current: Float
)