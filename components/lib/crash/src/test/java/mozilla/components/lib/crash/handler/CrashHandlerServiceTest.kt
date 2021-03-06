/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.lib.crash.handler

import android.content.ComponentName
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.rule.MainCoroutineRule
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CrashHandlerServiceTest {
    private val testDispatcher = TestCoroutineDispatcher()

    @get:Rule
    val coroutinesTestRule = MainCoroutineRule(testDispatcher)

    @After
    fun tearDown() {
        CrashReporter.reset()
    }

    @Test
    fun `CrashHandlerService will forward GeckoView crash to crash reporter`() {
        val scope = TestCoroutineScope(testDispatcher)
        val reporter = spy(CrashReporter(
            shouldPrompt = CrashReporter.Prompt.NEVER,
            services = listOf(mock()),
            scope = scope
        )).install(testContext)

        val intent = Intent("org.mozilla.gecko.ACTION_CRASHED")
        intent.component = ComponentName(
            "org.mozilla.samples.browser",
            "mozilla.components.lib.crash.handler.CrashHandlerService"
        )
        intent.putExtra(
            "minidumpPath",
            "/data/data/org.mozilla.samples.browser/files/mozilla/Crash Reports/pending/3ba5f665-8422-dc8e-a88e-fc65c081d304.dmp"
        )
        intent.putExtra("fatal", false)
        intent.putExtra(
            "extrasPath",
            "/data/data/org.mozilla.samples.browser/files/mozilla/Crash Reports/pending/3ba5f665-8422-dc8e-a88e-fc65c081d304.extra"
        )
        intent.putExtra("minidumpSuccess", true)

        val service = spy(CrashHandlerService())
        doNothing().`when`(service).kill()
        doNothing().`when`(reporter).sendCrashReport(any(), any())

        service.onHandleIntent(intent)
        verify(reporter).sendCrashReport(any(), any())
    }
}
