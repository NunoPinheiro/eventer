import authHandler.AuthHandler
import authHandler.AuthProviders
import authHandler.OAuthHandler
import com.mitchellbosecke.pebble.PebbleEngine
import spark.Spark.get
import java.io.StringWriter
import spark.Request
import spark.Response

fun main(args: Array<String>) {
	get("/", ::home)
	get("/auth/:authProvider", ::startAuth)
	AuthProviders.authProviders.forEach { authHandler -> setupAuthHandler(authHandler.value)}
}

fun home(req : Request, res : Response) : String{
	val args = mutableMapOf("authProviders" to AuthProviders.authProviders)
	return render("home", args)	
}

fun startAuth(req : Request, res : Response){
	val authProviderId = req.params("authProvider").toInt()
	val authProvider = AuthProviders.authProviders.get(authProviderId)
	if(authProvider is OAuthHandler){
		//Redirect to external system
		//TODO enable calculation of host from configurations
		res.redirect(authProvider.initUrl("http://localhost:4567"))
	}
	
}

fun handleOauthAccept(authHandler : OAuthHandler, req : Request, res : Response) : String{
	return authHandler.confirmCode(req.queryParams("code"), req.queryParams("state"))
}

fun setupAuthHandler(authHandler : AuthHandler){
	//Setup redirect urls for all OAuth Handlers
	if(authHandler is OAuthHandler){
		get(authHandler.redirectUrl(), { req, res -> handleOauthAccept(authHandler, req, res)});
	}
}

val pebbleEngine : PebbleEngine = PebbleEngine.Builder().build()
fun render(templateName : String, args : Map<String, Any> = emptyMap()) : String{
	val writer = StringWriter()
	val template = pebbleEngine.getTemplate(templateName + ".html")
	template.evaluate(writer, args)
	return writer.toString()
}