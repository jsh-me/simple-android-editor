package kr.co.domain.koin.repositoryimpl

import android.util.Log
import kr.co.domain.globalconst.CookieClass
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
class AddCookiesInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val preferences = CookieClass.coockie
        for (cookie in preferences) {
            builder.addHeader("Cookie", cookie)
            Log.v(
                "OkHttp",
                "Adding Header: $cookie"
            ) // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }

        return chain.proceed(builder.build())
    }
}