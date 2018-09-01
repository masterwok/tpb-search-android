package com.masterwok.tpbsearchandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.masterwok.tpbsearchandroid.common.AndroidJob
import com.masterwok.tpbsearchandroid.constants.QueryFactories
import com.masterwok.tpbsearchandroid.services.QueryService
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val Tag = "Demo"
    }

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val queryService = QueryService(QueryFactories)

        launch(parent = rootJob) {
            val result = queryService.query("Hobbit 1977")

            Log.d(Tag, result.toString())
        }
    }
}
