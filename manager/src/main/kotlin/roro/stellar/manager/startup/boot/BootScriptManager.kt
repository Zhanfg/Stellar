package roro.stellar.manager.startup.boot

import com.topjohnwu.superuser.Shell

/** Opt-in Magisk/KernelSU/APatch service.d integration (no polling daemon). */
object BootScriptManager {
    const val SCRIPT_PATH = "/data/adb/service.d/stellar.sh"
    private const val TEMP_PATH = "/data/adb/service.d/.stellar.sh.tmp"
    private val scriptContent = """
        #!/system/bin/sh
        # Resolve current APK and native library on every boot, including after updates.
        PATH=/system/bin:/system/xbin:/sbin:/data/adb/magisk:/data/adb/ksu/bin:${'PATH
        export PATH
        attempt=0
        while [ "$(getprop sys.boot_completed)" != "1" ] && [ "$attempt" -lt 60 ]; do
          sleep 3
          attempt=$((attempt + 1))
        done
        [ "$(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "$(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}PATH
        export PATH
        attempt=0
        while [ "${'(getprop sys.boot_completed)" != "1" ] && [ "$attempt" -lt 60 ]; do
          sleep 3
          attempt=$((attempt + 1))
        done
        [ "$(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "$(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}(getprop sys.boot_completed)" != "1" ] && [ "${'attempt" -lt 60 ]; do
          sleep 3
          attempt=$((attempt + 1))
        done
        [ "$(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "$(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}attempt" -lt 60 ]; do
          sleep 3
          attempt=${'((attempt + 1))
        done
        [ "$(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "$(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}((attempt + 1))
        done
        [ "${'(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "$(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}(getprop sys.boot_completed)" = "1" ] || exit 0
        [ "${'(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "$attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}(id -u)" = "0" ] || exit 1
        pidof stellar_server >/dev/null 2>&1 && exit 0
        attempt=0
        apk=
        while [ "${'attempt" -lt 10 ]; do
          apk="$(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}attempt" -lt 10 ]; do
          apk="${'(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}(pm path roro.stellar.manager 2>/dev/null | sed -n 's/^package://p' | grep '/base.apk${'' | head -n 1)"
          [ -r "$apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}' | head -n 1)"
          [ -r "${'apk" ] && break
          apk=
          attempt=$((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}apk" ] && break
          apk=
          attempt=${'((attempt + 1))
          sleep 3
        done
        [ -n "$apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}((attempt + 1))
          sleep 3
        done
        [ -n "${'apk" ] && [ -r "$apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}apk" ] && [ -r "${'apk" ] || exit 1
        appdir="${apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}apk" ] || exit 1
        appdir="${'{apk%/base.apk}"
        for native in "$appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}{apk%/base.apk}"
        for native in "${'appdir"/lib/*/libstellar.so; do
          [ -f "$native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}appdir"/lib/*/libstellar.so; do
          [ -f "${'native" ] && [ -x "$native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}native" ] && [ -x "${'native" ] || continue
          "$native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}native" ] || continue
          "${'native" "--apk=$apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}native" "--apk=${'apk" >/dev/null 2>&1
          exit $?
        done
        exit 1}apk" >/dev/null 2>&1
          exit ${'?
        done
        exit 1}?
        done
        exit 1
    """.trimIndent().trimEnd() + "\n"

    data class Result(val success: Boolean, val message: String)

    fun hasRootPermission(): Boolean =
        runCatching { Shell.getShell().isRoot }.getOrDefault(false)

    fun isScriptInstalled(): Boolean {
        if (!hasRootPermission()) return false
        return runCatching {
            Shell.cmd("[ -x '$SCRIPT_PATH' ]").exec().code == 0
        }.getOrDefault(false)
    }

    fun installScript(): Result {
        if (!hasRootPermission()) return Result(false, "No root permission")
        // Fixed delimiter; content is application-owned, never user-controlled.
        val command = """
            umask 077
            mkdir -p /data/adb/service.d || exit 1
            cat > '$TEMP_PATH' <<'STELLAR_SERVICE_SCRIPT_END'
${scriptContent.trimEnd()}
STELLAR_SERVICE_SCRIPT_END
            chmod 0700 '$TEMP_PATH' &&
            mv -f '$TEMP_PATH' '$SCRIPT_PATH'
        """.trimIndent()
        return exec(command)
    }

    fun removeScript(): Result =
        if (hasRootPermission()) exec("rm -f '$SCRIPT_PATH' '$TEMP_PATH'")
        else Result(false, "No root permission")

    private fun exec(command: String): Result = try {
        val result = Shell.cmd(command).exec()
        if (result.code == 0) Result(true, "")
        else Result(false, result.err.joinToString("\n").ifBlank { "exit code: ${result.code}" })
    } catch (e: Exception) {
        Result(false, e.message ?: "Unknown error")
    }
}
