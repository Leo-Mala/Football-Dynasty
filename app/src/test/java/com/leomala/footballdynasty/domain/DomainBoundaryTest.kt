package com.leomala.footballdynasty.domain

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainBoundaryTest {
    @Test
    fun `domain source has no Android Room or legacy implementation imports`() {
        val root = File("src/main/java/com/leomala/footballdynasty/domain")
        assertTrue("Domain source directory must exist", root.isDirectory)
        val forbiddenPrefixes = listOf(
            "import android.",
            "import androidx.room.",
            "import androidx.compose.",
            "import java.io.ObjectInputStream",
            "import java.io.ObjectOutputStream",
            "import com.leomala.footballdynasty.legacy.",
        )
        val violations = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().asSequence()
                    .filter { line -> forbiddenPrefixes.any(line::startsWith) }
                    .map { line -> "${file.relativeTo(root).path}: $line" }
            }
            .toList()
        assertTrue("Forbidden domain imports: $violations", violations.isEmpty())
    }
}
