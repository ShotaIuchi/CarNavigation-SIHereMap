package com.sihuocthai.map.here

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionsRequestor(val activity: Activity) {

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 42
    }

    interface ResultListener {
        fun permissionsGranted()
        fun permissionsDenied()
    }

    private var resultListener: ResultListener? = null

    fun request(resultListener: ResultListener) {
        this.resultListener = resultListener
        val missingPermissions: Array<String> = getPermissionsToRequest()
        if (missingPermissions.isEmpty()) {
            resultListener.permissionsGranted()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun getPermissionsToRequest(): Array<String> {
        val permissionList: ArrayList<String> = ArrayList()
        try {
            val packageInfo = activity.packageManager.getPackageInfo(
                activity.packageName, PackageManager.GET_PERMISSIONS
            )
            if (packageInfo.requestedPermissions != null) {
                for (permission in packageInfo.requestedPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            activity, permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && permission == Manifest.permission.CHANGE_NETWORK_STATE) {
                            continue
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && permission == Manifest.permission.ACTIVITY_RECOGNITION) {
                            continue
                        }
                        permissionList.add(permission)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return permissionList.toArray(arrayOfNulls<String>(0))
    }


    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (resultListener == null) {
            return
        }
        if (grantResults.isEmpty()) {
            return
        }
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var allGranted = true
            for (result in grantResults) {
                allGranted = allGranted and (result == PackageManager.PERMISSION_GRANTED)
            }
            if (allGranted) {
                resultListener!!.permissionsGranted()
            } else {
                resultListener!!.permissionsDenied()
            }
        }
    }

}