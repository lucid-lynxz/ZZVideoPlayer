import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import org.lynxz.zzvideoview.BuildConfig
import java.text.DecimalFormat

/**
 * Created by lynxz on 13/01/2017.
 * 扩展函数
 */
fun CharSequence.isEmpty(): Boolean {
    return TextUtils.isEmpty(this)
}

fun Context.showToast(msg: String) {
    if (msg.isNotBlank()) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

fun Context.showToast(msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
}

fun Context.getStringRes(@StringRes strId: Int): String {
    return resources.getString(strId)
}

fun Fragment.showToast(msg: String) {
    if (msg.isNotBlank()) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}

fun Fragment.showToast(msgId: Int) {
    Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show()
}

fun Fragment.getStringRes(@StringRes strResId: Int): String? {
    return activity?.resources?.getString(strResId)
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED
}

fun Activity.hideKeyboard() {
    this.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
        (it as InputMethodManager).hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
    }
}

/**
 * 保留两位小数,并返回字符串
 * */
fun Double.yuan(): String = DecimalFormat("0.##").format(this)

/**
 * double类型向上保留转换为整数,如 2.1 -> 3  2.0->2
 * */
fun Double.toIntUp(): Int {
    val remainder = if (this % 1 > 0) 1 else 0
    return this.toInt() + remainder
}

inline fun debugConf(code: () -> Unit) {
    if (BuildConfig.DEBUG) {
        code()
    }
}
