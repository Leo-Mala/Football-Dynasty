import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import com.leomala.footballdynasty.legacy.compatibility.DeterministicFingerprint
import com.leomala.footballdynasty.legacy.compatibility.LegacyFormatKind
import com.leomala.footballdynasty.legacy.compatibility.LegacyFormatProbe
import com.leomala.footballdynasty.legacy.compatibility.LegacySaveReader
import com.leomala.footballdynasty.legacy.compatibility.LegacySchemaCatalog
import com.leomala.footballdynasty.legacy.compatibility.LegacySerialization
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.security.MessageDigest

private const val FIXTURE_SHA = "7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7"
private const val SNAPSHOT_SHA = "9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6"

fun main(args: Array<String>) {
    require(args.size == 1) { "Usage: Phase2JvmProofKt <fixture.ban>" }
    val bytes = java.io.File(args[0]).readBytes()
    check(sha256(bytes) == FIXTURE_SHA)

    val probe = ByteArrayInputStream(bytes).use { LegacyFormatProbe.probe("fixture.ban", it) }
    check(probe.kind == LegacyFormatKind.BAN_JAVA_SERIALIZATION)

    val team = FileInputStream(args[0]).use { LegacySerialization.readBan(it) }
    check(team.name == "12 de Octubre")
    check(team.players.size == 20)
    check(team.players.first().name == "Mauro Cardozo")
    check(DeterministicFingerprint.team(team) == SNAPSHOT_SHA)

    val a = SeededRandomSource(270L)
    val b = SeededRandomSource(270L)
    check(List(8) { a.nextInt(1000) } == List(8) { b.nextInt(1000) })
    check(a.draws == 8L)
    check(LegacySchemaCatalog.core.map { it.type } == listOf("a.p", "a.ac", "a.t", "d.q", "a.b"))

    check(
        LegacyFormatProbe.probe("career.s21", ByteArrayInputStream(byteArrayOf(1, 2, 3))).kind ==
            LegacyFormatKind.CAREER_KRYO_OR_LEGACY
    )

    try {
        LegacySaveReader().readCareer(ByteArrayInputStream(byteArrayOf()))
    } catch (_: UnsupportedOperationException) {
        println("PHASE2_JVM_PROOF_OK team=${team.name} players=${team.players.size} draws=${a.draws}")
        return
    }
    error("Legacy career reader must remain blocked until a real save fixture is characterized")
}

private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
