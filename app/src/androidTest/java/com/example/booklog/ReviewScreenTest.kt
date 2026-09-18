package com.example.booklog

import android.widget.EditText
import android.widget.RatingBar
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.booklog.data.repository.ReviewRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {
    @Test fun recreationRestoresDraftAndRepeatedSaveCreatesOneReview() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val title = "회전 테스트 ${System.nanoTime()}"
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                host.navController.navigate(R.id.reviewWriteFragment,
                    bundleOf("title" to title, "author" to "저자", "isbn13" to "0123456789012", "permissionRequested" to true))
            }
            await(scenario) { it.findViewById<EditText>(R.id.etContent)?.isEnabled == true }
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.etContent).setText("화면 회전 후에도 남아야 하는 내용")
            }
            scenario.recreate()
            await(scenario) { it.findViewById<EditText>(R.id.etContent)?.text?.toString() == "화면 회전 후에도 남아야 하는 내용" }
            scenario.onActivity { activity ->
                assertEquals(title, activity.findViewById<EditText>(R.id.etTitle).text.toString())
                assertEquals("저자", activity.findViewById<EditText>(R.id.etAuthor).text.toString())
                activity.findViewById<android.view.View>(R.id.btnSave).performClick()
                activity.findViewById<android.view.View>(R.id.btnSave).performClick()
            }
            await(scenario) { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .navController.currentDestination?.id == R.id.reviewDetailFragment
            }
            val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            runBlocking {
                val repository = ReviewRepository(context)
                val records = repository.getAll().filter { it.title == title }
                assertEquals(1, records.size)
                assertEquals("0123456789012", records.single().isbn13)
                records.forEach { repository.deleteById(it.id) }
            }
        }
    }

    private fun await(scenario: ActivityScenario<MainActivity>, condition: (MainActivity) -> Boolean) {
        val deadline = android.os.SystemClock.elapsedRealtime() + 30_000
        while (android.os.SystemClock.elapsedRealtime() < deadline) {
            var complete = false
            scenario.onActivity { complete = condition(it) }
            if (complete) return
            android.os.SystemClock.sleep(50)
        }
        fail("화면 상태가 제한 시간 안에 준비되지 않았습니다")
    }
}
