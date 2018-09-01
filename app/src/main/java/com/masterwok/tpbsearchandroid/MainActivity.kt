package com.masterwok.tpbsearchandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.masterwok.tpbsearchandroid.common.AndroidJob
import com.masterwok.tpbsearchandroid.constants.Config
import com.masterwok.tpbsearchandroid.services.QueryService
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val queryService = QueryService(Config.Hosts)

        launch(parent = rootJob) {
            val result = queryService.query("Hackers 1995")

            Log.d("DERP", result.size.toString())


        }
    }
}
