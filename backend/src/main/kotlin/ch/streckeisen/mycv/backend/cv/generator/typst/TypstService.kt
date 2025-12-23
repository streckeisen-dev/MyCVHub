package ch.streckeisen.mycv.backend.cv.generator.typst

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger { }

@Service
class TypstService {

    suspend fun compile(
        workingDir: Path,
        sourceFile: String,
        outputFile: String
    ): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            val sourcePath = workingDir.resolve(sourceFile).pathString
            val outputPath = workingDir.resolve(outputFile).pathString
            val process = ProcessBuilder(
                "typst",
                "compile",
                sourcePath,
                outputPath
            ).start()

            withTimeout(30_000) {
                process.onExit().await()
            }

            val resultCode = process.exitValue()
            if (resultCode != 0) {
                val error = process.errorStream.bufferedReader().use { it.readText() }
                logger.error { "Failed to compile typst document: $error" }
                return@withContext Result.failure(TypstException("Compilation of document $sourceFile failed."))
            }
            val bytes = workingDir.resolve(outputFile).toFile().readBytes()
            return@withContext Result.success(bytes)
        }
}