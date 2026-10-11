package roro.stellar.server.util

import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ServiceManager
import rikka.hidden.compat.PermissionManagerApis
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Compatibility bridge for Android 17's virtual-device-aware IPermissionManager.
 *
 * The hidden compat library still targets the pre-37 signatures. Reflection
 * is used only when those methods are absent, and the resolved methods are
 * cached. Never silently treat a failed permission mutation as success.
 */
object PermissionManagerCompat {
    private const val DEFAULT_DEVICE_ID = 0
    private const val CALLER_REASON = "stellar"
    private val INT = Int::class.javaPrimitiveType!!
    private val methodCache = ConcurrentHashMap<String, Method>()

    private val service: Any by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val binder: IBinder = checkNotNull(ServiceManager.getService("permissionmgr")) {
            "Permission Manager service is unavailable"
        }
        val stub = Class.forName("android.permission.IPermissionManager\$Stub")
        checkNotNull(stub.getDeclaredMethod("asInterface", IBinder::class.java).invoke(null, binder))
    }

    private fun method(name: String, uidVariant: Boolean = false): Method {
        val cacheKey = if (uidVariant) "${name}#uid" else "${name}#package"
        return methodCache.computeIfAbsent(cacheKey) {
            val methods = service.javaClass.methods.filter { candidate ->
                val p = candidate.parameterTypes
                candidate.name == name && if (uidVariant) {
                    p.size in 2..3 && p[0] == String::class.java &&
                        p.drop(1).all { it == INT }
                } else {
                    p.size in 3..5 && p[0] == String::class.java &&
                        p[1] == String::class.java &&
                        p[2] == INT &&
                        (p.size < 4 || p[3] == INT) &&
                        (p.size < 5 || p[4] == String::class.java)
                }
            }
            // Android 17 introduces deviceId, sometimes also an attribution reason.
            methods.maxByOrNull { it.parameterTypes.size }
                ?: throw NoSuchMethodException("No compatible IPermissionManager.$name")
        }
    }

    private fun invoke(method: Method, vararg args: Any?): Any? {
        return try {
            method.invoke(service, *args)
        } catch (e: InvocationTargetException) {
            throw (e.cause ?: e)
        }
    }

    fun grantRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        try {
            PermissionManagerApis.grantRuntimePermission(packageName, permissionName, userId)
            return
        } catch (_: NoSuchMethodError) {
            // Android 17 introduced an additional deviceId parameter.
        }
        val target = method("grantRuntimePermission")
        when (target.parameterTypes.size) {
            3 -> invoke(target, packageName, permissionName, userId)
            4 -> invoke(target, packageName, permissionName, DEFAULT_DEVICE_ID, userId)
            else -> throw NoSuchMethodException("Unsupported grantRuntimePermission signature")
        }
    }

    fun revokeRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        try {
            PermissionManagerApis.revokeRuntimePermission(packageName, permissionName, userId)
            return
        } catch (_: NoSuchMethodError) {
            // Android 17 introduced deviceId, with an optional caller reason.
        }
        val target = method("revokeRuntimePermission")
        when (target.parameterTypes.size) {
            3 -> invoke(target, packageName, permissionName, userId)
            4 -> invoke(target, packageName, permissionName, DEFAULT_DEVICE_ID, userId)
            5 -> invoke(target, packageName, permissionName, DEFAULT_DEVICE_ID, userId, CALLER_REASON)
            else -> throw NoSuchMethodException("Unsupported revokeRuntimePermission signature")
        }
    }

    fun checkUidPermission(permissionName: String, uid: Int): Int {
        try {
            return PermissionManagerApis.checkPermission(permissionName, uid)
        } catch (_: NoSuchMethodError) {
            // Fail closed if the reflected signature cannot be resolved.
        }
        return try {
            val target = method("checkPermission", uidVariant = true)
            when (target.parameterTypes.size) {
                2 -> invoke(target, permissionName, uid) as Int
                3 -> invoke(target, permissionName, DEFAULT_DEVICE_ID, uid) as Int
                else -> PackageManager.PERMISSION_DENIED
            }
        } catch (e: Exception) {
            Logger("PermissionManagerCompat").w(e, "checkPermission failed")
            PackageManager.PERMISSION_DENIED
        }
    }
}
