// Copyright 2022 Exactpro Systems Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gevamu.ktlintcustomrules

import com.pinterest.ktlint.core.Rule
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

/**
 * Regex match against {//   Copyright (c) YYYY ...}/.* Copyright YYYY-YYYY ...};
 * Get year and warn if it isn't current year;
 * And match only first line of the file (no blank spaces before the license, it has to be first)
 */
class LicenseHeaderRule : Rule("verify-license-header") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == KtStubElementTypes.FILE) {
            val fileFirstLine = node.firstChildNode
            val regexMatch = Regex("/[/*] *Copyright [()cC ]?([0-9]{4}-?)?([0-9]{4})[a-zA-Z0-9 .,]*")
                .matchEntire(fileFirstLine.text)
            if (regexMatch == null) {
                emit(fileFirstLine.startOffset, "No license header found matching regexp",
                    false)
            } else {
                val utcCalendarNow = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("UTC")))
                if (regexMatch.groups[2]!!.value.toInt() != utcCalendarNow.get(Calendar.YEAR)) {
                    emit(regexMatch.groups[2]!!.range.first, "License header year does not equal current year",
                        true)
                }
            }
        }
    }
}
