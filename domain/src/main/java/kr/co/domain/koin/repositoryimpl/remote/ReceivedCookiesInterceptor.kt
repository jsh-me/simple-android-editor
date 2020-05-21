package kr.co.domain.koin.repositoryimpl.remote
import kr.co.domain.globalconst.CookieClass
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.HashSet

class ReceivedCookiesInterceptor: Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val cookies = HashSet<String>()

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            for (header in originalResponse.headers("Set-Cookie")) {
               cookies.add(header)
            }
            //singleton
            CookieClass.coockie = cookies
        }
        return originalResponse
    }
}