package com.qonversion.android.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.QonversionProductsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.QProduct
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val productIdSubs = "main"
    private val productIdInApp = "in_app"
    private val permissionPlus = "plus"
    private val permissionStandart = "standart"
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Qonversion.products(callback = object : QonversionProductsCallback {
            override fun onSuccess(products: Map<String, QProduct>) {
                showLoading(false)
                updateContent(products)
            }

            override fun onError(error: QonversionError) {
                showLoading(false)
                showError(error)
            }
        })

        buttonSubscribe.setOnClickListener {
            purchase(productIdSubs)
        }

        buttonInApp.setOnClickListener {
            purchase(productIdInApp)
        }

        buttonRestore.setOnClickListener {
            showLoading(true)
            Qonversion.restore(object : QonversionPermissionsCallback {
                override fun onSuccess(permissions: Map<String, QPermission>) {
                    showLoading(false)
                    handleRestore(permissions)
                }

                override fun onError(error: QonversionError) {
                    showLoading(false)
                    showError(error)
                }
            })
        }

        buttonPermissions.setOnClickListener {
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateContent(products: Map<String, QProduct>) {
        buttonPermissions.text = getString(R.string.check_active_permissions)
        buttonRestore.text = getString(R.string.restore_purchases)

        val subscription = products[productIdSubs]
        if (subscription != null) {
            buttonSubscribe.text = String.format(
                "%s %s / %s", getString(R.string.subscribe_for),
                subscription.prettyPrice, subscription.duration?.name
            )
        }

        val inApp = products[productIdInApp]
        if (inApp != null) {
            buttonInApp.text = String.format(
                "%s %s", getString(R.string.buy_for),
                inApp.prettyPrice
            )
        }
    }

    private fun handleRestore(permissions: Map<String, QPermission>) {
        var isNothingToRestore = true
        val permissionPlus = permissions[permissionPlus]
        if (permissionPlus != null && permissionPlus.isActive()) {
            buttonSubscribe.text = getString(R.string.purchased)
            isNothingToRestore = false
        }
        val permissionStandart = permissions[permissionStandart]
        if (permissionStandart != null && permissionStandart.isActive()) {
            buttonInApp.text = getString(R.string.purchased)
            isNothingToRestore = false
        }

        if (isNothingToRestore) {
            buttonRestore.text = getString(R.string.nothing_to_restore)
        }
    }

    private fun purchase(productId: String) {
        Qonversion.purchase(
            this,
            productId,
            callback = object : QonversionPermissionsCallback {
                override fun onSuccess(permissions: Map<String, QPermission>) {
                    when (productId) {
                        productIdInApp -> buttonInApp.text = getString(R.string.purchased)
                        productIdSubs -> buttonSubscribe.text = getString(R.string.purchased)
                    }
                }

                override fun onError(error: QonversionError) {
                    showError(error)
                }
            })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) {
            ProgressBar.VISIBLE
        } else {
            ProgressBar.INVISIBLE
        }
    }

    private fun showError(error: QonversionError){
        Toast.makeText(applicationContext, error.description, Toast.LENGTH_LONG).show()
        Log.e(TAG, error.toString())
    }
}