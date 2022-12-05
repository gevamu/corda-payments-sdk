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

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TestLicenseLinter {
    @Test
    fun `Test Copyright Is Present`() {
        val lint = LicenseHeaderRule().lint("""
            // Copyright 2022 LOLOLOL
            //
            // Lorem ipsum

            package name.smart

            import com.test.Name

            class HelloWorld {
                /**
                 * Human function
                 */
                fun main(args: Array<String>) {
                    // print em
                    print("Hello World")
                }
            }
            """.trimIndent()
        )
        assertThat(lint).isEmpty()

        val failLint = LicenseHeaderRule().lint("""
            package name.smart

            import com.test.Name

            class HelloWorld {
                /**
                 * Human function
                 */
                fun main(args: Array<String>) {
                    // print em
                    print("Hello World")
                }
            }""".trimIndent()
        )
        assertThat(failLint).containsExactly(
            LintError(1, 1, "verify-license-header",
                "No license header found matching regexp")
        )


        val wrongYearLint = LicenseHeaderRule().lint("""
            // Copyright 1970 LOLOLOL
            //
            // Lorem ipsum

            package name.smart

            import com.test.Name

            class HelloWorld {
                /**
                 * Human function
                 */
                fun main(args: Array<String>) {
                    // print em
                    print("Hello World")
                }
            }
            """.trimIndent()
        )
        assertThat(wrongYearLint).containsExactly(
            LintError(1, 14, "verify-license-header",
                "License header year does not equal current year")
        )
    }
}
