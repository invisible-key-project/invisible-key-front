package com.example.transparentkey_aos

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class DeleteFileWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // 파일 경로 가져오기
        val filePath = inputData.getString("file_path")
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        }
        return Result.success()
    }
}