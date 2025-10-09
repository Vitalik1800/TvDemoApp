package com.vs18.tvdemoapp

import android.net.Uri
import com.google.android.exoplayer2.upstream.*
import java.io.*
import java.util.*
import kotlin.math.*

class UnstableDataSource(
    private val baseDataSOurce: DataSource,
    private val delayMs: Long = 2000,
    private val failureProbability: Float = 0.2f
) : DataSource {

    private val random = Random()

    override fun open(dataSpec: DataSpec): Long {
        Thread.sleep(delayMs)

        if (random.nextFloat() < failureProbability) {
            throw HttpDataSource.HttpDataSourceException("Simulated network failure", dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

        return baseDataSOurce.open(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        val maxReadLength = min(readLength, random.nextInt(1024) + 1)
        return baseDataSOurce.read(buffer, offset, maxReadLength)
    }

    override fun getUri(): Uri? {
        return baseDataSOurce.uri
    }

    override fun close() {
        baseDataSOurce.close()
    }

    override fun addTransferListener(transferListener: TransferListener) {
        baseDataSOurce.addTransferListener(transferListener)
    }
}

class UnstableDataSourceFactory(
    private val baseFactory: HttpDataSource.Factory
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return UnstableDataSource(baseFactory.createDataSource())
    }
}