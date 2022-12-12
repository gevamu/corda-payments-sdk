/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.gevamu.kotlin-common-conventions")
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

dependencies {
    cordapp(project(":contracts"))
    cordapp(project(":workflows"))
    cordapp(project(":payments-app:app-contracts"))
    api("javax.xml.bind:jaxb-api:2.3.1")
}

cordapp {
    contract {
        name("Payments Application Workflows")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}