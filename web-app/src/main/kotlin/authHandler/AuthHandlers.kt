package authHandler

import java.security.AuthProvider
import com.github.kittinunf.fuel.httpPost;

abstract class AuthHandler{
	abstract fun presentationName() : String
}

/**
 Default API for OAuth Flows
 **/
abstract class OAuthHandler : AuthHandler(){
	// We need the host to be able to calculate the redirect url sent in the requests
	abstract fun initUrl(host : String) : String
	abstract fun redirectUrl() : String
	abstract fun confirmCode(code : String, state : String) : String
}

/**
 GitHub Oauth
 **/
object GitHubAuthHandler : OAuthHandler(){
	val clientId = ""
	val clientSecret = ""
	//val scope = ""
	val state = "102391290"
	
	override fun initUrl(host : String): String {
		return "https://github.com/login/oauth/authorize?client_id=$clientId&state=$state&redirect_uri=$host${redirectUrl()}"
	}

	override fun redirectUrl(): String {
		return "/github";
	}
	
	override fun presentationName() = "GitHub"
	
	override fun confirmCode(code : String, state : String) : String{
		//TODO We should be confirming the state!
		val params = listOf("client_id" to clientId, "client_secret" to clientSecret, "code" to code )
		val responseString = "https://github.com/login/oauth/access_token".httpPost(params).responseString().component3().component1()
		if(responseString != null){
			return responseString
		}else{
			throw OAuthFailedException(this);
		}
	}
}

class OAuthFailedException(val handler : OAuthHandler): Exception()

object AuthProviders{
	/**
	 Map with Available AuthProviders and their identifier
	 **/
	 val authProviders : MutableMap<Int, AuthHandler> = mutableMapOf(0 to GitHubAuthHandler)
}

