package com.scapes.presentation.model

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.TargetDevice

/** Platform defaults consumed by shared presentation logic. */
data class ScapesAppConfig(
    val defaultTargetDevice: TargetDevice = TargetDevice.DESKTOP,
    val defaultApplyTarget: ApplyTarget = ApplyTarget.DESKTOP,
)
