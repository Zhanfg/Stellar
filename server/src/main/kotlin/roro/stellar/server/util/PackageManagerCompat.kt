package roro.stellar.server.util

import android.app.ActivityThread
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import rikka.hidden.compat.PackageManagerApis
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object PackageManagerCompat {
    private const val TAG = "PackageManagerCompat"
    private const val DEVICE_ID_DEFAULT = 0
    private const val PARCELED_LIST_SLICE = "android.content.pm.ParceledListSlice"
    private val LOGGER = Logger(TAG)

    private val systemPackageManager by lazy(LazyThreadSafetyMode.NONE) {
        ActivityThread.systemMain().systemContext.packageManager
    }

    private val packageManagerService by lazy(LazyThreadSafetyMode.NONE) {
        runCatching { obtainPackageManagerService() }.getOrNull()
    }

    // Android 17 commonly reaches these reflection fallbacks on every package query.
    // Resolve the method once instead of scanning all proxy methods per application.
    private val packageInfoMethod by lazy { findPackageManagerMethod("getPackageInfo", 3, 4) }
    private val applicationInfoMethod by lazy { findPackageManagerMethod("getApplicationInfo", 3, 4) }
    private val installedPackagesMethod by lazy {
        packageManagerService?.let { findGetInstalledPackagesMethod(it) }
    }

    fun getPackageInfo(packageName: String, flags: Long, userId: Int): PackageInfo? {
        return try {
            PackageManagerApis.getPackageInfoNoThrow(packageName, flags, userId)
        } catch (_: Throwable) {
            tryReflectPackageInfo(packageName, flags, userId)
        }
    }

    fun getApplicationInfo(packageName: String, flags: Long, userId: Int): ApplicationInfo? {
        return try {
            PackageManagerApis.getApplicationInfoNoThrow(packageName, flags, userId)
        } catch (_: Throwable) {
            tryReflectApplicationInfo(packageName, flags, userId)
        }
    }

    fun getInstalledPackagesNoThrow(flags: Long, userId: Int): List<PackageInfo?> {
        return try {
            getInstalledPackages(flags, userId) ?: emptyList()
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getInstalledPackages failed")
            emptyList()
        }
    }

    fun getInstalledPackages(flags: Long, userId: Int): List<PackageInfo?>? {
        val queryFlags = flags or PackageManager.MATCH_ALL.toLong()

        return try {
            val service = packageManagerService ?: return PackageManagerApis.getInstalledPackagesNoThrow(queryFlags, userId)
            val method = installedPackagesMethod
                ?: return PackageManagerApis.getInstalledPackagesNoThrow(queryFlags, userId)
            val result = if (Build.VERSION.SDK_INT >= 33) {
                invokeMethod(service, method, queryFlags, userId)
            } else {
                invokeMethod(service, method, queryFlags.toInt(), userId)
            }
            unwrapPackageInfoList(result)
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getInstalledPackages fallback failed")
            PackageManagerApis.getInstalledPackagesNoThrow(queryFlags, userId)
        }
    }

    private fun tryReflectPackageInfo(packageName: String, flags: Long, userId: Int): PackageInfo? {
        return try {
            val methodHolder = packageInfoMethod ?: return null
            val compatibleFlags = if (methodHolder.second.parameterTypes[1] == Int::class.javaPrimitiveType) {
                flags.toInt()
            } else {
                flags
            }
            invokeMethod(methodHolder.first, methodHolder.second, packageName, compatibleFlags, userId) as? PackageInfo
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getPackageInfo fallback failed")
            null
        }
    }

    private fun tryReflectApplicationInfo(packageName: String, flags: Long, userId: Int): ApplicationInfo? {
        return try {
            val methodHolder = applicationInfoMethod ?: return null
            val compatibleFlags = if (methodHolder.second.parameterTypes[1] == Int::class.javaPrimitiveType) {
                flags.toInt()
            } else {
                flags
            }
            invokeMethod(methodHolder.first, methodHolder.second, packageName, compatibleFlags, userId) as? ApplicationInfo
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getApplicationInfo fallback failed")
            null
        }
    }

    private fun obtainPackageManagerService(): Any {
        val binder = ServiceManager.getService("package")
        val stubClass = Class.forName($$"android.content.pm.IPackageManager$Stub")
        return checkNotNull(
            stubClass.getDeclaredMethod("asInterface", IBinder::class.java).invoke(null, binder)
        )
    }

    private fun findPackageManagerMethod(name: String, vararg parameterCounts: Int): Pair<Any, Method>? {
        fun compatible(method: Method): Boolean {
            val p = method.parameterTypes
            return method.name == name && parameterCounts.contains(p.size) &&
                p[0] == String::class.java &&
                p[1] in arrayOf(Int::class.javaPrimitiveType, Long::class.javaPrimitiveType) &&
                p.drop(2).all { it == Int::class.javaPrimitiveType }
        }

        packageManagerService?.let { receiver ->
            receiver.javaClass.methods.filter(::compatible)
                .maxByOrNull { it.parameterTypes.size }?.let { return receiver to it }
        }

        systemPackageManager.javaClass.methods.filter(::compatible)
            .maxByOrNull { it.parameterTypes.size }?.let { return systemPackageManager to it }

        return null
    }

    private fun findGetInstalledPackagesMethod(receiver: Any): Method? {
        val expectedFlagsType = if (Build.VERSION.SDK_INT >= 33) {
            Long::class.javaPrimitiveType
        } else {
            Int::class.javaPrimitiveType
        }
        return receiver.javaClass.methods.filter { method ->
            method.name == "getInstalledPackages" &&
                method.parameterTypes.size in 2..3 &&
                method.parameterTypes[0] == expectedFlagsType &&
                method.parameterTypes.drop(1).all { it == Int::class.javaPrimitiveType }
        }.maxByOrNull { it.parameterTypes.size }
    }

    @Suppress("UNCHECKED_CAST")
    private fun unwrapPackageInfoList(result: Any?): List<PackageInfo?> {
        if (result == null) {
            return emptyList()
        }
        if (result is List<*>) {
            return result as List<PackageInfo?>
        }

        val resultClassName = result.javaClass.name
        if (resultClassName.startsWith(PARCELED_LIST_SLICE) || resultClassName.contains("PackageInfoList")) {
            val list = result.javaClass.getMethod("getList").invoke(result)
            return list as? List<PackageInfo?> ?: emptyList()
        }

        throw IllegalStateException("Unsupported getInstalledPackages return type: $resultClassName")
    }

    private fun invokeMethod(receiver: Any, method: Method, vararg args: Any?): Any? {
        val actualArgs: Array<out Any?> = if (method.parameterTypes.size == args.size + 1) {
            // Android 17 inserts deviceId BEFORE userId, not after it.
            arrayOf(*args.dropLast(1).toTypedArray(), DEVICE_ID_DEFAULT, args.last())
        } else {
            args
        }
        return try {
            method.invoke(receiver, *actualArgs)
        } catch (e: InvocationTargetException) {
            when (val cause = e.cause) {
                is RuntimeException -> throw cause
                is Error -> throw cause
                is RemoteException -> throw cause
                else -> throw e
            }
        }
    }
}
